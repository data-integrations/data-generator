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
 * Random longs.
 */
public class RandomLongGenerator extends RandomGenerator<Long> {
  private final Config config;

  public RandomLongGenerator(Config config) {
    this.config = config;
  }

  @Override
  public Long generate() {
    return config.min + randomService.nextLong(config.max - config.min + 1);
  }

  /**
   * Config.
   */
  public static class Config {
    private long min;
    private long max;

    public Config() {
      this(0, Long.MAX_VALUE);
    }

    public Config(long min, long max) {
      this.min = min;
      this.max = max;
    }
  }
}
