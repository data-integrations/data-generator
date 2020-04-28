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

/**
 * Generates integers within a range, with a subset of numbers more likely than the rest.
 *
 * Skew percentage of the time, the number will be randomly chosen between the skew min and max. The rest of the time,
 * the number will be randomly chosen between the min and max.
 */
public class RandomIntSkewedGenerator extends RandomGenerator<Integer> {
  private final Config config;

  public RandomIntSkewedGenerator(Config config) {
    this.config = config;
  }

  @Override
  public Integer generate() {
    if (random.nextInt(100) < config.skewChance) {
      return randomService.nextInt(config.skewMin, config.skewMax);
    }
    return randomService.nextInt(config.min, config.max);
  }

  /**
   * Config.
   */
  public static class Config {
    private int min;
    private int max;
    private int skewMin;
    private int skewMax;
    private int skewChance;

    public Config() {
      this(0, Integer.MAX_VALUE, 0, 10, 10);
    }

    public Config(int min, int max, int skewMin, int skewMax, int skewChance) {
      this.min = min;
      this.max = max;
      this.skewMin = skewMin;
      this.skewMax = skewMax;
      this.skewChance = skewChance;
    }
  }
}
