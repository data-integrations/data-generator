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

import com.github.javafaker.Lorem;
import io.cdap.plugin.datagen.generator.GeneratorType;
import io.cdap.plugin.datagen.generator.LoremGenerator;
import io.cdap.plugin.datagen.generator.NameGenerator;
import io.cdap.plugin.datagen.generator.RandomBytesGenerator;
import io.cdap.plugin.datagen.generator.RandomIntGenerator;
import io.cdap.plugin.datagen.generator.RandomLongSkewedGenerator;
import io.cdap.plugin.datagen.generator.SemiRandomStringGenerator;
import io.cdap.plugin.datagen.generator.SequentialLongSkewedGenerator;
import io.cdap.plugin.datagen.generator.TimestampGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Specifies what data should be generated.
 */
class DataGeneratorSpec extends FieldsSpecification {
  private final int numSplits;
  private final long recordsPerSplit;
  private final Long seed;

  DataGeneratorSpec(int numSplits, long recordsPerSplit, List<FieldSpec> fields,
                    @Nullable String schemaName, @Nullable Long seed) {
    super(fields, schemaName);
    this.numSplits = numSplits;
    this.recordsPerSplit = recordsPerSplit;
    this.seed = seed;
  }

  int getNumSplits() {
    return numSplits;
  }

  long getRecordsPerSplit() {
    return recordsPerSplit;
  }

  @Nullable
  Long getSeed() {
    return seed;
  }

  /**
   * Pre-canned users dataset
   */
  static class Users {

    static DataGeneratorSpec create(int numSplits, long recordsPerSplit, int numSkewedIds,
                                    int skewedIdChance, String payloadType, int recordSizeKB, int nullChance,
                                    @Nullable Long seed) {
      List<FieldSpec> fields = new ArrayList<>(10);

      fields.add(new FieldSpec("id", GeneratorType.SEQUENTIAL_LONG_SKEWED, 0,
                               new SequentialLongSkewedGenerator.Config(1L, 1L, 1L, numSkewedIds,
                                                                        numSkewedIds < 1 ? 0 : skewedIdChance)));
      fields.add(new FieldSpec("first_name", GeneratorType.NAME, nullChance,
                               new NameGenerator.Config(NameGenerator.FIRST_NAME)));
      fields.add(new FieldSpec("last_name", GeneratorType.NAME, nullChance,
                               new NameGenerator.Config(NameGenerator.LAST_NAME)));
      fields.add(new FieldSpec("email", GeneratorType.EMAIL, nullChance, null));
      fields.add(new FieldSpec("phone", GeneratorType.PHONE_NUMBER, nullChance, null));
      fields.add(new FieldSpec("profession", GeneratorType.PROFESSION, nullChance, null));
      fields.add(new FieldSpec("age", GeneratorType.RANDOM_INT, nullChance, new RandomIntGenerator.Config(18, 100)));
      fields.add(new FieldSpec("address", GeneratorType.ADDRESS, nullChance, null));
      fields.add(new FieldSpec("score", GeneratorType.GAUSSIAN, nullChance, null));
      fields.add(new FieldSpec("payload", GeneratorType.valueOf(payloadType), 0,
                               Collections.singletonMap("size", recordSizeKB * 1024)));

      return new DataGeneratorSpec(numSplits, recordsPerSplit, fields, "user", seed);
    }
  }

  /**
   * Pre-canned purchases dataset
   */
  static class Purchases {

    static DataGeneratorSpec create(int numSplits, long recordsPerSplit, int numSkewedUserIds,
                                    int skewedUserIdChance, long maxUserId, String payloadType, int recordSizeKB,
                                    int nullChance, @Nullable Long seed) {
      List<FieldSpec> fields = new ArrayList<>(6);

      fields.add(new FieldSpec("id", GeneratorType.UUID, 0, null));
      fields.add(new FieldSpec("user_id", GeneratorType.RANDOM_LONG_SKEWED, 0,
                               new RandomLongSkewedGenerator.Config(1L, maxUserId, 1L, numSkewedUserIds,
                                                                    numSkewedUserIds < 1 ? 0 : skewedUserIdChance)));
      // 2020-01-01 to 2020-04-01
      fields.add(new FieldSpec("ts", GeneratorType.TIMESTAMP, nullChance,
                               new TimestampGenerator.Config(1577836800, 1585699200)));
      fields.add(new FieldSpec("price", GeneratorType.RANDOM_INT, nullChance,
                               new RandomIntGenerator.Config(99, 10 * 1000)));
      fields.add(new FieldSpec("credit_card", GeneratorType.CREDIT_CARD, nullChance, null));
      fields.add(new FieldSpec("payload", GeneratorType.valueOf(payloadType), 0,
                               Collections.singletonMap("size", recordSizeKB * 1024)));

      return new DataGeneratorSpec(numSplits, recordsPerSplit, fields, "purchase", seed);
    }
  }
}
