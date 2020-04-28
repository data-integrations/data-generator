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
 * Generates doubles in a gaussian distribution.
 */
public class GaussianGenerator extends RandomGenerator<Double> {
  private final Config config;

  public GaussianGenerator(Config config) {
    this.config = config;
  }

  @Override
  public Double generate() {
    return config.mean + random.nextGaussian() * config.stddev;
  }

  /**
   * Config.
   */
  public static class Config {
    private double mean;
    private double stddev;

    public Config() {
      this(0, 100);
    }

    public Config(double mean, double stddev) {
      this.mean = mean;
      this.stddev = stddev;
    }
  }
}
