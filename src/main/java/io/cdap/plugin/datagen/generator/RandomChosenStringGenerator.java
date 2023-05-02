/*
 * Copyright © 2023 Cask Data, Inc.
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

import io.cdap.plugin.datagen.GeneratorContext;

import java.util.List;
import java.util.Random;

/**
 * Generates string randomly chosen from a selection.
 */
public class RandomChosenStringGenerator extends RandomGenerator<String> {

  private final Config config;

  private Random random;

  public RandomChosenStringGenerator(Config config) {
    this.config = config;
  }

  @Override
  public void initialize(GeneratorContext context) {
    super.initialize(context);
    random = context.getRandom();
  }

  @Override
  public String generate() {
    int i = random.nextInt(config.choices.size());
    return config.choices.get(i);
  }

  /**
   * Config.
   */
  public static class Config {

    private final List<String> choices;

    public Config(List<String> choices) {
      this.choices = choices;
    }
  }
}
