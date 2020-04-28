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

import io.cdap.plugin.datagen.GeneratorContext;

/**
 * Generates ints in a regular sequence.
 */
public class SequentialIntGenerator extends RandomGenerator<Integer> {
  private final Config config;
  private int current;

  public SequentialIntGenerator(Config config) {
    this.config = config;
  }

  @Override
  public void initialize(GeneratorContext context) {
    super.initialize(context);
    current = config.start + (int) context.getOffset() * config.step;
  }

  @Override
  public Integer generate() {
    int val = current;
    current += config.step;
    return val;
  }

  /**
   * Config.
   */
  public static class Config {
    private int start;
    private int step;

    public Config() {
      this(0, 1);
    }

    public Config(int start, int step) {
      this.start = start;
      this.step = step;
    }
  }
}
