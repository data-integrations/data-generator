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

import com.github.javafaker.Faker;

import java.util.Random;

/**
 * Information used to initialize a data generator.
 */
public class GeneratorContext {
  private final Random random;
  private final Faker faker;
  private final long offset;

  GeneratorContext(Random random, long offset) {
    this.random = random;
    this.faker = new Faker(random);
    this.offset = offset;
  }

  /**
   * @return the offset, which represents the record number in the entire collection.
   *   For example, if there are 10 splits with 1000 records per split, the generator for the first split will get
   *   initialized with offset 0. The generator for the second split will get initialized with offset 1000, etc.
   */
  public long getOffset() {
    return offset;
  }

  /**
   * @return random number generator to use for generating random data. It will be initialized to a specific seed
   *   by the framework.
   */
  public Random getRandom() {
    return random;
  }

  /**
   * @return faker for generating different types of fake data. The faker will generate the same pseudo-random data
   *   based on the seed set by the framework and thus should be used instead of manually creating separate instances
   *   of Faker.
   */
  public Faker getFaker() {
    return faker;
  }
}
