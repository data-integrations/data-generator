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

package io.cdap.plugin.datagen.generator;

import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.datagen.DataGenerator;

import javax.annotation.Nullable;

/**
 * Type of generator
 */
public enum GeneratorType {
  ADDRESS(AddressGenerator.class, AddressGenerator.Config.class, Schemas.STRING),
  CREDIT_CARD(CreditCardGenerator.class, null, Schemas.STRING),
  EMAIL(EmailGenerator.class, null, Schemas.STRING),
  GAUSSIAN(GaussianGenerator.class, GaussianGenerator.Config.class, Schemas.DOUBLE),
  LOREM(LoremGenerator.class, LoremGenerator.Config.class, Schemas.STRING),
  NAME(NameGenerator.class, NameGenerator.Config.class, Schemas.STRING),
  PHONE_NUMBER(PhoneNumberGenerator.class, null, Schemas.STRING),
  PROFESSION(ProfessionGenerator.class, null, Schemas.STRING),
  RANDOM_BYTES(RandomBytesGenerator.class, RandomBytesGenerator.Config.class, Schemas.BYTES),
  RANDOM_INT(RandomIntGenerator.class, RandomIntGenerator.Config.class, Schemas.INT),
  RANDOM_INT_SKEWED(RandomIntSkewedGenerator.class, RandomIntSkewedGenerator.Config.class, Schemas.INT),
  RANDOM_LONG(RandomLongGenerator.class, RandomLongGenerator.Config.class, Schemas.LONG),
  RANDOM_LONG_SKEWED(RandomLongSkewedGenerator.class, RandomLongSkewedGenerator.Config.class, Schemas.LONG),
  SEMI_RANDOM_STRING(SemiRandomStringGenerator.class, SemiRandomStringGenerator.Config.class, Schemas.STRING),
  SEQUENTIAL_INT(SequentialIntGenerator.class, SequentialIntGenerator.Config.class, Schemas.INT),
  SEQUENTIAL_INT_SKEWED(SequentialIntSkewedGenerator.class, SequentialIntSkewedGenerator.Config.class, Schemas.INT),
  SEQUENTIAL_LONG(SequentialLongGenerator.class, SequentialLongGenerator.Config.class, Schemas.LONG),
  SEQUENTIAL_LONG_SKEWED(SequentialLongSkewedGenerator.class, SequentialLongSkewedGenerator.Config.class, Schemas.LONG),
  TIMESTAMP(TimestampGenerator.class, TimestampGenerator.Config.class, Schemas.TIMESTAMP),
  UUID(UUIDGenerator.class, null, Schemas.STRING);

  private final Class<? extends DataGenerator> generatorClass;
  private final Class<?> configType;
  private final Schema schema;

  GeneratorType(Class<? extends DataGenerator> generatorClass, Class<?> configType, Schema schema) {
    this.generatorClass = generatorClass;
    this.configType = configType;
    this.schema = schema;
  }

  public Class<? extends DataGenerator> getGeneratorClass() {
    return generatorClass;
  }

  @Nullable
  public Class<?> getConfigClass() {
    return configType;
  }

  public Schema getSchema() {
    return schema;
  }
}
