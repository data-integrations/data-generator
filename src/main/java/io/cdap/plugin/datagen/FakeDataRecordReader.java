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
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.datagen.generator.NullableGenerator;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates fake data in a record reader.
 */
public class FakeDataRecordReader extends RecordReader<Void, StructuredRecord> {
  private static final Gson GSON = new Gson();
  private DataGeneratorSpec spec;
  private long recordCount = 0;
  // this is ordered so that the same seed will always generate the same data
  private List<FieldDataGenerator<?>> generators;
  private Schema schema;

  @Override
  public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException {
    this.recordCount = 0;
    FakeDataInputSplit split = (FakeDataInputSplit) inputSplit;
    this.spec = split.getSpec();
    this.schema = spec.getSchema();
    this.generators = new ArrayList<>();
    long offset = split.getSplitNum() * spec.getRecordsPerSplit();
    Random random = spec.getSeed() == null ? new Random() : new Random(spec.getSeed() + offset);
    for (FieldSpec fieldSpec : spec.getFields()) {
      String fieldName = fieldSpec.getName();

      Class<? extends DataGenerator> genClass = fieldSpec.getType().getGeneratorClass();
      Class<?> confClass = fieldSpec.getType().getConfigClass();
      try {
        DataGenerator<?> generator;
        if (confClass == null) {
          generator = genClass.newInstance();
        } else {
          Object conf = GSON.fromJson(fieldSpec.getArgs(), confClass);
          if (conf == null) {
            conf = confClass.newInstance();
          }
          generator = genClass.getConstructor(confClass).newInstance(conf);
        }
        if (fieldSpec.getNullChance() > 0) {
          generator = new NullableGenerator<>(generator, fieldSpec.getNullChance());
        }
        generator.initialize(new GeneratorContext(random, offset));
        generators.add(new FieldDataGenerator<>(fieldName, generator));
      } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
        // should never happen
        throw new IOException(String.format("Unable to create '%s' generator for field '%s'",
                                            fieldSpec.getType(), fieldName), e);
      }
    }
  }

  @Override
  public boolean nextKeyValue() {
    boolean hasNext = recordCount < spec.getRecordsPerSplit();
    recordCount++;
    return hasNext;
  }

  @Override
  public Void getCurrentKey() {
    return null;
  }

  @Override
  public StructuredRecord getCurrentValue() {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    for (FieldDataGenerator<?> generator : generators) {
      builder.set(generator.fieldName, generator.generator.generate());
    }
    return builder.build();
  }

  @Override
  public float getProgress() {
    return (float) recordCount / spec.getRecordsPerSplit();
  }

  @Override
  public void close() {
    // no-op
  }

  /**
   * Field and data generator pair.
   *
   * @param <T> type of data to generate.
   */
  private static class FieldDataGenerator<T> {
    private final String fieldName;
    private final DataGenerator<T> generator;

    private FieldDataGenerator(String fieldName, DataGenerator<T> generator) {
      this.fieldName = fieldName;
      this.generator = generator;
    }
  }
}
