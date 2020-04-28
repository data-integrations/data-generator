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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.cdap.plugin.datagen.generator.GeneratorType;

/**
 * Information about how a field should generate data.
 */
public class FieldSpec {
  private static final Gson GSON = new Gson();
  private final String name;
  private final GeneratorType type;
  private final int nullChance;
  private final JsonObject args;

  FieldSpec(String name, GeneratorType type, int nullChance, Object args) {
    this(name, type, nullChance, GSON.toJsonTree(args).getAsJsonObject());
  }

  FieldSpec(String name, GeneratorType type, int nullChance, JsonObject args) {
    this.name = name;
    this.type = type;
    this.nullChance = nullChance;
    this.args = args;
  }

  public String getName() {
    return name;
  }

  public GeneratorType getType() {
    return type;
  }

  public int getNullChance() {
    return nullChance;
  }

  public JsonObject getArgs() {
    return args;
  }
}
