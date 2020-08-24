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

import io.cdap.cdap.api.data.schema.Schema;

/**
 * Schema constants
 */
public class Schemas {
  public static final Schema BYTES = Schema.of(Schema.Type.BYTES);
  public static final Schema DOUBLE = Schema.of(Schema.Type.DOUBLE);
  public static final Schema INT = Schema.of(Schema.Type.INT);
  public static final Schema LONG = Schema.of(Schema.Type.LONG);
  public static final Schema STRING = Schema.of(Schema.Type.STRING);
  public static final Schema TIMESTAMP = Schema.of(Schema.LogicalType.TIMESTAMP_MICROS);
}
