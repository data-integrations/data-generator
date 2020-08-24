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

import com.github.javafaker.Faker;
import io.cdap.plugin.datagen.GeneratorContext;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Generates semi random text. Each word will be chosen from a pre-defined set of possible words.
 * Use this instead of {@link LoremGenerator} if the data generated should be compressible.
 */
public class SemiRandomStringGenerator extends RandomGenerator<String> {
  private final Config config;
  private List<Supplier<String>> suppliers;

  public SemiRandomStringGenerator(Config config) {
    this.config = config;
  }

  @Override
  public void initialize(GeneratorContext context) {
    super.initialize(context);
    Faker faker = context.getFaker();
    suppliers = Arrays.asList(
      () -> faker.backToTheFuture().quote(),
      () -> faker.dune().quote(),
      () -> faker.elderScrolls().quote(),
      () -> faker.gameOfThrones().quote(),
      () -> faker.hitchhikersGuideToTheGalaxy().quote(),
      () -> faker.hobbit().quote(),
      () -> faker.lebowski().quote(),
      () -> faker.princessBride().quote(),
      () -> faker.rickAndMorty().quote(),
      () -> faker.yoda().quote()
    );
  }

  @Override
  public String generate() {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (sb.length() < config.size) {
      sb.append(suppliers.get(i).get()).append(" ");
      i = (i + 1) % 10;
    }
    return sb.substring(0, config.size);
  }

  /**
   * Config.
   */
  public static class Config {
    private int size;

    public Config() {
      this(100);
    }

    public Config(int size) {
      this.size = size;
    }
  }
}
