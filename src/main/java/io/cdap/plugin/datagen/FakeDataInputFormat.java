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

import com.google.gson.Gson;
import io.cdap.cdap.api.data.batch.InputFormatProvider;
import io.cdap.cdap.api.data.format.StructuredRecord;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * InputFormat that generates fake data.
 */
public class FakeDataInputFormat extends InputFormat<Void, StructuredRecord> implements InputFormatProvider {
  private static final Gson GSON = new Gson();
  private static final String CONFIG = "io.cdap.plugin.datagen.conf";
  private DataGeneratorSpec spec;

  @SuppressWarnings("unused")
  public FakeDataInputFormat() {
    // required for mapreduce deserialization
  }

  FakeDataInputFormat(DataGeneratorSpec spec) {
    this.spec = spec;
  }

  @Override
  public List<InputSplit> getSplits(JobContext jobContext) {
    spec = GSON.fromJson(jobContext.getConfiguration().get(CONFIG), DataGeneratorSpec.class);
    List<InputSplit> splits = new ArrayList<>(spec.getNumSplits());
    for (int i = 0; i < spec.getNumSplits(); i++) {
      splits.add(new FakeDataInputSplit(spec, i));
    }
    return splits;
  }

  @Override
  public RecordReader<Void, StructuredRecord> createRecordReader(InputSplit inputSplit,
                                                                 TaskAttemptContext taskAttemptContext) {
    return new FakeDataRecordReader();
  }

  @Override
  public String getInputFormatClassName() {
    return this.getClass().getName();
  }

  @Override
  public Map<String, String> getInputFormatConfiguration() {
    return Collections.singletonMap(CONFIG, GSON.toJson(spec));
  }
}
