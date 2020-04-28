/*
 * Copyright © 2020 Cask Data, Inc.
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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.batch.Input;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.StageConfigurer;
import io.cdap.cdap.etl.api.batch.BatchSource;
import io.cdap.cdap.etl.api.batch.BatchSourceContext;
import io.cdap.plugin.common.LineageRecorder;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Generates test data for a pipeline.
 */
@SuppressWarnings("unused")
@Plugin(type = BatchSource.PLUGIN_TYPE)
@Name(DataGeneratorBatchSource.NAME)
@Description("Generates test data.")
public class DataGeneratorBatchSource extends BatchSource<Void, StructuredRecord, StructuredRecord> {
  public static final String NAME = "DataGenerator";
  private final DataGeneratorConfig conf;

  public DataGeneratorBatchSource(DataGeneratorConfig conf) {
    this.conf = conf;
  }

  @Override
  public void configurePipeline(PipelineConfigurer pipelineConfigurer) {
    StageConfigurer stageConfigurer = pipelineConfigurer.getStageConfigurer();
    DataGeneratorSpec spec = conf.asSpec();
    if (spec != null) {
      stageConfigurer.setOutputSchema(spec.getSchema());
    }
  }

  @Override
  public void prepareRun(BatchSourceContext context) {
    DataGeneratorSpec spec = conf.asSpec();
    if (spec == null) {
      throw new IllegalStateException("Unsupported dataset: " + conf.getDataset());
    }
    context.setInput(Input.of(conf.getReferenceName(), new FakeDataInputFormat(spec))
                       .alias(UUID.randomUUID().toString()));

    LineageRecorder lineageRecorder = new LineageRecorder(context, conf.getReferenceName());
    Schema schema = spec.getSchema();
    lineageRecorder.createExternalDataset(schema);

    if (schema.getFields() != null) {
      lineageRecorder.recordRead("Generate", "Generated fake data.",
                                 schema.getFields().stream().map(Schema.Field::getName).collect(Collectors.toList()));
    }
  }

}

