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
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.etl.api.batch.BatchSource;
import io.cdap.cdap.etl.api.batch.BatchSourceContext;

/**
 * Generates test data for a pipeline.
 */
@Plugin(type = BatchSource.PLUGIN_TYPE)
@Name(DataGeneratorBatchSource.NAME)
@Description("Generates test data.")
public class DataGeneratorBatchSource extends BatchSource<Void, StructuredRecord, StructuredRecord> {
  public static final String NAME = "DataGenerator";

  @Override
  public void prepareRun(BatchSourceContext batchSourceContext) {

  }


}

