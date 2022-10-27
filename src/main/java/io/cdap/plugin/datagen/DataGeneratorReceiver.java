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

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.gson.Gson;
import io.cdap.cdap.api.data.format.StructuredRecord;
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

/**
 * DStream Receiver for DataGeneration.
 */
public class DataGeneratorReceiver extends Receiver<StructuredRecord> {
  private static final Logger LOG = LoggerFactory.getLogger(DataGeneratorReceiver.class);
  private final String jsonSpec;
  private final int partition;
  private final long pauseMillis;
  private transient Thread receiverThread;

  public DataGeneratorReceiver(String jsonSpec, int partition, long pauseMillis) {
    super(StorageLevel.MEMORY_AND_DISK());
    this.jsonSpec = jsonSpec;
    this.partition = partition;
    this.pauseMillis = pauseMillis;
  }

  @Override
  public void onStart() {
    receiverThread = new Thread(() -> {
      LOG.info("Receiver thread started for partition {}", partition);
      DataGeneratorSpec generatorSpec = new Gson().fromJson(jsonSpec, DataGeneratorSpec.class);
      FakeDataInputSplit inputSplit = new FakeDataInputSplit(generatorSpec, partition);

      // Generate a fake TaskAttemptContext. It is not used by the FakeDataInputFormat
      TaskID taskId = new TaskID(new JobID("generator", 0), TaskType.MAP, partition);
      TaskAttemptContext taskAttemptContext = new TaskAttemptContextImpl(new Configuration(),
                                                                         new TaskAttemptID(taskId, 0));
      while (!isStopped()) {
        FakeDataInputFormat inputFormat = new FakeDataInputFormat(generatorSpec);
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

      LOG.info("Receiver thread stopped for partition {}", partition);
    });
    receiverThread.start();
  }

  @Override
  public void onStop() {
    LOG.info("Stopping receiver thread for partition {}", partition);
    if (receiverThread != null) {
      receiverThread.interrupt();
      Uninterruptibles.joinUninterruptibly(receiverThread);
    }
  }

  static JavaDStream<StructuredRecord> createPartitionedDStream(JavaStreamingContext jsc,
                                                                 int partition,
                                                                 DataGeneratorSpec spec,
                                                                 long pauseMillis) {
    // Turn it to String so that it can be serialized by Spark to transport to the receiver node
    String jsonSpec = new Gson().toJson(spec);
    return jsc.<StructuredRecord>receiverStream(new DataGeneratorReceiver(jsonSpec, partition, pauseMillis));
  }
}
