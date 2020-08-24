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

import com.github.javafaker.Name;
import io.cdap.plugin.datagen.GeneratorContext;

import java.util.function.Supplier;

/**
 * Generates random names.
 */
public class NameGenerator extends RandomGenerator<String> {
  public static final String FIRST_NAME = "first";
  public static final String LAST_NAME = "last";
  public static final String USERNAME = "username";
  public static final String FULL_NAME = "full";
  private final Config config;
  private Supplier<String> supplier;
  private Name name;

  public NameGenerator(Config config) {
    this.config = config;
  }

  @Override
  public void initialize(GeneratorContext context) {
    super.initialize(context);
    name = context.getFaker().name();
    supplier = getSupplier(config.type);
  }

  private Supplier<String> getSupplier(String type) {
    switch (type) {
      case FIRST_NAME:
        return name::firstName;
      case LAST_NAME:
        return name::lastName;
      case USERNAME:
        return name::username;
      case FULL_NAME:
        return name::fullName;
    }
    throw new IllegalArgumentException("Unsupported name type: " + type);
  }

  @Override
  public String generate() {
    return supplier.get();
  }

  /**
   * Config.
   */
  public static class Config {
    private String type;

    public Config() {
      this(FULL_NAME);
    }

    public Config(String type) {
      this.type = type;
    }
  }
}
