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
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * An input split for fake data.
 */
public class FakeDataInputSplit extends InputSplit implements Writable {
  private static final Gson GSON = new Gson();
  private int splitNum;
  private DataGeneratorSpec spec;

  @SuppressWarnings("unused")
  public FakeDataInputSplit() {
    // required for mapreduce deserialization
  }

  FakeDataInputSplit(DataGeneratorSpec spec, int splitNum) {
    this.spec = spec;
    this.splitNum = splitNum;
  }

  int getSplitNum() {
    return splitNum;
  }

  DataGeneratorSpec getSpec() {
    return spec;
  }

  @Override
  public void write(DataOutput dataOutput) throws IOException {
    dataOutput.writeInt(splitNum);
    dataOutput.writeUTF(GSON.toJson(spec));
  }

  @Override
  public void readFields(DataInput dataInput) throws IOException {
    splitNum = dataInput.readInt();
    String specStr = dataInput.readUTF();
    spec = GSON.fromJson(specStr, DataGeneratorSpec.class);
  }

  @Override
  public long getLength() {
    return spec.getRecordsPerSplit();
  }

  @Override
  public String[] getLocations() {
    return new String[0];
  }
}
