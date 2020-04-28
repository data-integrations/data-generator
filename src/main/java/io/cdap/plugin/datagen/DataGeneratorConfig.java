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
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.plugin.PluginConfig;

import javax.annotation.Nullable;

/**
 * Config for data generator.
 */
public class DataGeneratorConfig extends PluginConfig {
  private static final Gson GSON = new Gson();
  static final String REFERENCE_NAME = "referenceName";
  static final String DATASET = "dataset";
  static final String NUM_SPLITS = "numSplits";
  static final String RECORDS_PER_SPLIT = "recordsPerSplit";
  static final String SEED = "seed";
  static final String CUSTOM_CONFIG = "customConfig";

  // general properties

  @SuppressWarnings("unused")
  @Name(REFERENCE_NAME)
  @Description("Reference name for lineage")
  private String referenceName;

  @Macro
  @Nullable
  @Name(NUM_SPLITS)
  @Description("Number of splits")
  private Integer numSplits;

  @Macro
  @Nullable
  @Name(RECORDS_PER_SPLIT)
  @Description("Number of records to output per split")
  private Integer recordsPerSplit;

  @Macro
  @Nullable
  @Name(SEED)
  @Description("Seed to use for random data generation")
  private Long seed;

  @SuppressWarnings("unused")
  @Macro
  @Name(DATASET)
  @Description("The dataset to generate. A pre-canned dataset can be chosen, " +
    "or a fully customizable one can be configured.")
  private String dataset;

  // properties for 'users' dataset

  @Macro
  @Nullable
  @Description("Number of skewed user ids to generate.")
  private Integer usersNumSkewedIds;

  @Macro
  @Nullable
  @Description("Percentage of data that should use a skewed id (0 - 100)")
  private Integer usersSkewedIdChance;

  @Macro
  @Nullable
  @Description("Size of payload in kb.")
  private Integer usersPayloadSizeKB;

  @Macro
  @Nullable
  @Description("Type of payload.")
  private String usersPayloadType;

  @Macro
  @Nullable
  @Description("Chance for a nullable value to be null (0 - 100).")
  private Integer usersNullChance;

  // properties for 'purchases' dataset

  @Macro
  @Nullable
  @Description("Number of skewed user ids to generate.")
  private Integer purchasesNumSkewedUserIds;

  @Macro
  @Nullable
  @Description("Percentage of data that should use a skewed user id (0 - 100)")
  private Integer purchasesSkewedUserIdChance;

  @Macro
  @Nullable
  @Description("Maximum possible user id to generate")
  private Integer purchasesMaxUserId;

  @Macro
  @Nullable
  @Description("Payload size in kb.")
  private Integer purchasesPayloadSizeKB;

  @Macro
  @Nullable
  @Description("Payload type.")
  private String purchasesPayloadType;

  @Macro
  @Nullable
  @Description("Chance for a nullable value to be null (0 - 100).")
  private Integer purchasesNullChance;

  // properties for 'custom' dataset

  @SuppressWarnings("unused")
  @Macro
  @Nullable
  @Name(CUSTOM_CONFIG)
  @Description("Custom configuration for each output field")
  private String customConfig;

  public DataGeneratorConfig() {
    this.numSplits = 10;
    this.recordsPerSplit = 1000;
    this.seed = null;

    this.usersNumSkewedIds = 0;
    this.usersSkewedIdChance = 0;
    this.usersPayloadSizeKB = 1;
    this.usersPayloadType = "RANDOM_BYTES";
    this.usersNullChance = 10;

    this.purchasesNumSkewedUserIds = 0;
    this.purchasesSkewedUserIdChance = 0;
    this.purchasesMaxUserId = 1000 * 1000;
    this.purchasesPayloadSizeKB = 1;
    this.purchasesPayloadType = "RANDOM_BYTES";
    this.purchasesNullChance = 10;
  }

  String getReferenceName() {
    return referenceName;
  }

  String getDataset() {
    return dataset;
  }

  @SuppressWarnings("ConstantConditions")
  @Nullable
  DataGeneratorSpec asSpec() {
    switch (dataset) {
      case "users":
        return DataGeneratorSpec.Users.create(numSplits, recordsPerSplit, usersNumSkewedIds, usersSkewedIdChance,
                                              usersPayloadType, usersPayloadSizeKB, usersNullChance, seed);
      case "purchases":
        return DataGeneratorSpec.Purchases.create(numSplits, recordsPerSplit, purchasesNumSkewedUserIds,
                                                  purchasesSkewedUserIdChance, purchasesMaxUserId,
                                                  purchasesPayloadType, purchasesPayloadSizeKB, purchasesNullChance,
                                                  seed);
      case "custom":
        FieldsSpecification fieldsSpecification = GSON.fromJson(customConfig, FieldsSpecification.class);
        return new DataGeneratorSpec(numSplits, recordsPerSplit, fieldsSpecification.getFields(),
                                     fieldsSpecification.getSchemaName(), seed);
    }
    return null;
  }
}
