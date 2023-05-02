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

import com.github.javafaker.DateAndTime;
import io.cdap.plugin.datagen.GeneratorContext;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Generates random timestamps.
 */
public class TimestampGenerator extends RandomGenerator<Long> {

  private final Date from;
  private final Date to;
  private DateAndTime dateAndTime;

  public TimestampGenerator(Config config) {
    Date now = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
    this.from = config.isAlwaysNow ? now : Date.from(Instant.ofEpochMilli(config.from));
    this.to = config.isAlwaysNow ? now : Date.from(Instant.ofEpochMilli(config.to));
  }

  @Override
  public void initialize(GeneratorContext context) {
    super.initialize(context);
    dateAndTime = context.getFaker().date();
  }

  @Override
  public Long generate() {
    Instant instant = dateAndTime.between(from, to).toInstant();
    long micros = TimeUnit.SECONDS.toMicros(instant.getEpochSecond());
    return Math.addExact(micros, TimeUnit.NANOSECONDS.toMicros(instant.getNano()));
  }

  /**
   * Config.
   */
  public static class Config {

    private long from;
    private long to;
    private boolean isAlwaysNow;

    public Config() {
      this(0, System.currentTimeMillis());
    }

    public Config(boolean isAlwaysNow) {
      this.isAlwaysNow = isAlwaysNow;
    }

    public Config(long from, long to) {
      this.from = from;
      this.to = to;
    }

  }
}
