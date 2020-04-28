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

import io.cdap.cdap.api.data.schema.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Specifies the fields that should be generated and how they should be generated.
 */
class FieldsSpecification {
  private final List<FieldSpec> fields;
  private final String schemaName;
  private transient Schema schema;

  protected FieldsSpecification(List<FieldSpec> fields, @Nullable String schemaName) {
    this.fields = fields;
    this.schemaName = schemaName;
  }

  List<FieldSpec> getFields() {
    return Collections.unmodifiableList(fields);
  }

  public String getSchemaName() {
    return schemaName == null ? "custom" : schemaName;
  }

  Schema getSchema() {
    if (schema != null) {
      return schema;
    }

    List<Schema.Field> schemaFields = new ArrayList<>(fields.size());
    for (FieldSpec fieldSpec : fields) {
      Schema fieldSchema = fieldSpec.getType().getSchema();
      if (fieldSpec.getNullChance() > 0) {
        fieldSchema = fieldSchema.isNullable() ? fieldSchema : Schema.nullableOf(fieldSchema);
      }
      schemaFields.add(Schema.Field.of(fieldSpec.getName(), fieldSchema));
    }
    schema = Schema.recordOf(getSchemaName(), schemaFields);
    return schema;
  }
}
