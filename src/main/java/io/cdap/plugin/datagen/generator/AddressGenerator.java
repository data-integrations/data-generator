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

import com.github.javafaker.Address;
import io.cdap.plugin.datagen.GeneratorContext;

import java.util.function.Supplier;

/**
 * Generates random addresses.
 */
public class AddressGenerator extends RandomGenerator<String> {
  private final Config config;
  private Supplier<String> supplier;
  private Address address;

  public AddressGenerator(Config config) {
    this.config = config;
  }

  @Override
  public void initialize(GeneratorContext context) {
    super.initialize(context);
    address = context.getFaker().address();
    supplier = getSupplier(config.type.toLowerCase());
  }

  private Supplier<String> getSupplier(String type) {
    switch (type) {
      case "street":
        return address::streetAddress;
      case "city":
        return address::city;
      case "zip":
        return address::zipCode;
      case "full":
        return address::fullAddress;
      case "country":
        return address::country;
      case "state":
        return address::state;
      case "latitude":
        return address::latitude;
      case "longitude":
        return address::longitude;
    }
    throw new IllegalArgumentException("Unsupported address type: " + type);
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
      this("full");
    }

    public Config(String type) {
      this.type = type;
    }
  }
}
