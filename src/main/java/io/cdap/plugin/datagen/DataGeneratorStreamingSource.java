/*
 * Copyright © 2022 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.datagen;

import com.google.gson.Gson;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.StageConfigurer;
import io.cdap.cdap.etl.api.streaming.StreamingContext;
import io.cdap.cdap.etl.api.streaming.StreamingSource;
import io.cdap.cdap.etl.api.streaming.StreamingSourceContext;
import io.cdap.plugin.common.LineageRecorder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.receiver.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class provides a streaming source from generated data.
 */
@SuppressWarnings("unused")
@Plugin(type = StreamingSource.PLUGIN_TYPE)
@Name(DataGeneratorStreamingSource.NAME)
@Description("Generates test data stream.")
public class DataGeneratorStreamingSource extends StreamingSource<StructuredRecord> {

  public static final String NAME = "DataStreamGenerator";
  private static final Logger LOG = LoggerFactory.getLogger(DataGeneratorStreamingSource.class);

  private final DataGeneratorStreamingConfig conf;

  public DataGeneratorStreamingSource(DataGeneratorStreamingConfig conf) {
    this.conf = conf;
  }

  @Override
  public JavaDStream<StructuredRecord> getStream(StreamingContext ssc) {
    DataGeneratorSpec spec = conf.asSpec();
    if (spec == null) {
      throw new IllegalStateException("Missing data generator specification");
    }

    JavaStreamingContext jsc = ssc.getSparkStreamingContext();

    return IntStream.range(0, spec.getNumSplits())
      .mapToObj(partition -> createPartitionedDStream(jsc, partition, spec, conf.getPauseMillisPerBatch()))
      .reduce(JavaDStream::union)
      .orElseThrow(() -> new IllegalStateException("Empty split"));
  }

  @Override
  public void configurePipeline(PipelineConfigurer pipelineConfigurer) {
    LOG.debug("Configure pipeline called");
    StageConfigurer stageConfigurer = pipelineConfigurer.getStageConfigurer();
    DataGeneratorSpec spec = conf.asSpec();
    if (spec != null) {
      LOG.debug("Setting output schema");
      stageConfigurer.setOutputSchema(spec.getSchema());
    }
  }

  @Override
  public void prepareRun(StreamingSourceContext context) {
    DataGeneratorSpec spec = conf.asSpec();
    if (spec == null) {
      throw new IllegalStateException("Unsupported dataset: " + conf.getDataset());
    }

    LineageRecorder lineageRecorder = new LineageRecorder(context, conf.getReferenceName());
    Schema schema = spec.getSchema();
    lineageRecorder.createExternalDataset(schema);

    if (schema.getFields() != null) {
      lineageRecorder.recordRead("Generate", "Generated fake data.",
                                 schema.getFields().stream().map(Schema.Field::getName).collect(Collectors.toList()));
    }
  }

  private JavaDStream<StructuredRecord> createPartitionedDStream(JavaStreamingContext jsc,
                                                                 int partition,
                                                                 DataGeneratorSpec spec,
                                                                 long pauseMillis) {

    // Turn it to String so that it can be serialized by Spark to transport to the receiver node
    String jsonSpec = new Gson().toJson(spec);

    return jsc.<StructuredRecord>receiverStream(new Receiver<StructuredRecord>(StorageLevel.MEMORY_AND_DISK()) {

      private transient Thread receiverThread;

      @Override
      public void onStart() {
        receiverThread = new Thread(() -> {
          DataGeneratorSpec generatorSpec = new Gson().fromJson(jsonSpec, DataGeneratorSpec.class);
          FakeDataInputSplit inputSplit = new FakeDataInputSplit(generatorSpec, partition);

          // Generate a fake TaskAttemptContext. It is not used by the FakeDataInputFormat
          TaskID taskId = new TaskID(new JobID("generator", 0), TaskType.MAP, partition);
          TaskAttemptContext taskAttemptContext = new TaskAttemptContextImpl(new Configuration(),
                                                                             new TaskAttemptID(taskId, 0));
          while (!isStopped()) {
            FakeDataInputFormat inputFormat = new FakeDataInputFormat(conf.asSpec());
            try (RecordReader<Void, StructuredRecord> reader = inputFormat.createRecordReader(inputSplit,
                                                                                              taskAttemptContext)) {
              reader.initialize(inputSplit, taskAttemptContext);
              while (!isStopped() && reader.nextKeyValue()) {
                store(reader.getCurrentValue());
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            } catch (InterruptedException e) {
              // End the current stream
            }

            if (!isStopped()) {
              try {
                TimeUnit.MILLISECONDS.sleep(pauseMillis);
              } catch (InterruptedException e) {
                // Signaled from onStop(), just continue and the while loop will exit
              }
            }
          }
        });
        receiverThread.start();
      }

      @Override
      public void onStop() {
        if (receiverThread != null) {
          receiverThread.interrupt();
        }
      }
    });
  }
}
