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
import io.cdap.cdap.api.artifact.ArtifactSummary;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.dataset.table.Table;
import io.cdap.cdap.datapipeline.DataPipelineApp;
import io.cdap.cdap.datapipeline.SmartWorkflow;
import io.cdap.cdap.etl.api.batch.BatchSource;
import io.cdap.cdap.etl.mock.batch.MockSink;
import io.cdap.cdap.etl.mock.test.HydratorTestBase;
import io.cdap.cdap.etl.proto.v2.ETLBatchConfig;
import io.cdap.cdap.etl.proto.v2.ETLPlugin;
import io.cdap.cdap.etl.proto.v2.ETLStage;
import io.cdap.cdap.proto.ProgramRunStatus;
import io.cdap.cdap.proto.artifact.AppRequest;
import io.cdap.cdap.proto.id.ArtifactId;
import io.cdap.cdap.proto.id.NamespaceId;
import io.cdap.cdap.test.ApplicationManager;
import io.cdap.cdap.test.DataSetManager;
import io.cdap.cdap.test.WorkflowManager;
import io.cdap.plugin.datagen.generator.GeneratorType;
import io.cdap.plugin.datagen.generator.Schemas;
import io.cdap.plugin.datagen.generator.SequentialIntGenerator;
import io.cdap.plugin.datagen.generator.SequentialLongGenerator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Tests for Data Generator.
 */
public class DataGeneratorTest extends HydratorTestBase {
  private static final Gson GSON = new Gson();
  private static final ArtifactId APP_ARTIFACT_ID = NamespaceId.DEFAULT.artifact("app", "1.0.0");
  private static final ArtifactSummary APP_ARTIFACT = new ArtifactSummary("app", "1.0.0");

  @BeforeClass
  public static void setupTest() throws Exception {
    setupBatchArtifacts(APP_ARTIFACT_ID, DataPipelineApp.class);

    // add plugins
    addPluginArtifact(NamespaceId.DEFAULT.artifact("datagen-plugins", "1.0.0"), APP_ARTIFACT_ID,
                      DataGeneratorBatchSource.class);
  }

  @Test
  public void testUsersDataset() throws Exception {
    Map<String, String> props = new HashMap<>();
    props.put(DataGeneratorConfig.REFERENCE_NAME, "users-fake");
    props.put(DataGeneratorConfig.DATASET, "users");
    props.put(DataGeneratorConfig.NUM_SPLITS, "2");
    props.put(DataGeneratorConfig.RECORDS_PER_SPLIT, "2");
    props.put(DataGeneratorConfig.SEED, "0");

    String outputName = UUID.randomUUID().toString();
    ETLBatchConfig config = ETLBatchConfig.builder()
      .addStage(new ETLStage("src", new ETLPlugin(DataGeneratorBatchSource.NAME, BatchSource.PLUGIN_TYPE, props)))
      .addStage(new ETLStage("sink", MockSink.getPlugin(outputName)))
      .addConnection("src", "sink")
      .build();

    AppRequest<ETLBatchConfig> appRequest = new AppRequest<>(APP_ARTIFACT, config);
    ApplicationManager appManager = deployApplication(NamespaceId.DEFAULT.app("usersTest"), appRequest);

    WorkflowManager workflowManager = appManager.getWorkflowManager(SmartWorkflow.NAME);
    workflowManager.startAndWaitForRun(ProgramRunStatus.COMPLETED, 2, TimeUnit.MINUTES);

    // this is a subset of the full schema, but we can't easily compare double and byte[] values.
    Schema schema = Schema.recordOf("user",
                                    Schema.Field.of("id", Schemas.LONG),
                                    Schema.Field.of("first_name", Schema.nullableOf(Schemas.STRING)),
                                    Schema.Field.of("last_name", Schema.nullableOf(Schemas.STRING)),
                                    Schema.Field.of("email", Schema.nullableOf(Schemas.STRING)),
                                    Schema.Field.of("phone", Schema.nullableOf(Schemas.STRING)),
                                    Schema.Field.of("profession", Schema.nullableOf(Schemas.STRING)),
                                    Schema.Field.of("age", Schema.nullableOf(Schemas.INT)),
                                    Schema.Field.of("address", Schema.nullableOf(Schemas.STRING)));
    StructuredRecord record1 = StructuredRecord.builder(schema)
      .set("id", 1L)
      .set("first_name", "Lavina")
      .set("last_name", "Dicki")
      .set("email", "terisa.johnston@hotmail.com")
      .set("phone", "(254) 451-0384 x7203")
      .set("profession", "scientist")
      .set("age", 81)
      .set("address", "253 Monahan Meadows, Perrystad, TN 62297-6676")
      .build();
    StructuredRecord record2 = StructuredRecord.builder(schema)
      .set("id", 2L)
      .set("first_name", "Fairy")
      .set("last_name", "Watsica")
      .set("email", "dusty.nicolas@yahoo.com")
      .set("phone", "150-104-7037")
      .set("profession", "accountant")
      .set("age", 97)
      .set("address", "110 Sung Ferry, South Britney, OH 22738")
      .build();
    StructuredRecord record3 = StructuredRecord.builder(schema)
      .set("id", 3L)
      .set("first_name", "Luther")
      .set("last_name", "Kulas")
      .set("phone", "846.444.6790")
      .set("profession", "postman")
      .set("age", 85)
      .set("address", "Apt. 253 6700 Astrid Point, North Novamouth, TX 43679")
      .build();
    StructuredRecord record4 = StructuredRecord.builder(schema)
      .set("id", 4L)
      .set("first_name", "Jamel")
      .set("last_name", "Hoeger")
      .set("email", "elvina.gibson@hotmail.com")
      .set("phone", "(021) 285-7324 x2869")
      .set("profession", "programmer")
      .set("age", 79)
      .build();

    DataSetManager<Table> outputTable = getDataset(outputName);
    Set<StructuredRecord> actual = MockSink.readOutput(outputTable).stream()
      .map(record -> subset(record, schema))
      .collect(Collectors.toSet());
    Set<StructuredRecord> expected = new HashSet<>(Arrays.asList(record1, record2, record3, record4));
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testPurchasesDataset() throws Exception {
    Map<String, String> props = new HashMap<>();
    props.put(DataGeneratorConfig.REFERENCE_NAME, "purchases-fake");
    props.put(DataGeneratorConfig.DATASET, "purchases");
    props.put(DataGeneratorConfig.NUM_SPLITS, "2");
    props.put(DataGeneratorConfig.RECORDS_PER_SPLIT, "2");
    props.put(DataGeneratorConfig.SEED, "0");

    String outputName = UUID.randomUUID().toString();
    ETLBatchConfig config = ETLBatchConfig.builder()
      .addStage(new ETLStage("src", new ETLPlugin(DataGeneratorBatchSource.NAME, BatchSource.PLUGIN_TYPE, props)))
      .addStage(new ETLStage("sink", MockSink.getPlugin(outputName)))
      .addConnection("src", "sink")
      .build();

    AppRequest<ETLBatchConfig> appRequest = new AppRequest<>(APP_ARTIFACT, config);
    ApplicationManager appManager = deployApplication(NamespaceId.DEFAULT.app("purchases"), appRequest);

    WorkflowManager workflowManager = appManager.getWorkflowManager(SmartWorkflow.NAME);
    workflowManager.startAndWaitForRun(ProgramRunStatus.COMPLETED, 2, TimeUnit.MINUTES);

    // this is a subset of the full schema, but we can't easily compare byte[] values and can't predict UUIDs.
    Schema schema = Schema.recordOf("purchase",
                                    Schema.Field.of("user_id", Schemas.LONG),
                                    Schema.Field.of("ts", Schema.nullableOf(Schemas.TIMESTAMP)),
                                    Schema.Field.of("price", Schema.nullableOf(Schemas.INT)),
                                    Schema.Field.of("credit_card", Schema.nullableOf(Schemas.STRING)));
    StructuredRecord record1 = StructuredRecord.builder(schema)
      .set("user_id", 821009L)
      .set("credit_card", "1228-1221-1221-1431")
      .set("ts", 1579503693000L)
      .build();
    StructuredRecord record2 = StructuredRecord.builder(schema)
      .set("user_id", 944438L)
      .set("credit_card", "1212-1221-1121-1234")
      .set("ts", 1579197049000L)
      .set("price", 4659)
      .build();
    StructuredRecord record3 = StructuredRecord.builder(schema)
      .set("user_id", 273467L)
      .set("credit_card", "1228-1221-1221-1431")
      .set("ts", 1580971578000L)
      .set("price", 6610)
      .build();
    StructuredRecord record4 = StructuredRecord.builder(schema)
      .set("user_id", 673464L)
      .set("credit_card", "1228-1221-1221-1431")
      .set("ts", 1584361414000L)
      .build();

    DataSetManager<Table> outputTable = getDataset(outputName);
    Set<StructuredRecord> actual = MockSink.readOutput(outputTable).stream()
      .map(record -> subset(record, schema))
      .collect(Collectors.toSet());
    Set<StructuredRecord> expected = new HashSet<>(Arrays.asList(record1, record2, record3, record4));
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testCustomDataset() throws Exception {
    List<FieldSpec> fields = new ArrayList<>(2);
    // 0, 10, 20, etc
    fields.add(new FieldSpec("int_id", GeneratorType.SEQUENTIAL_INT, 0, new SequentialIntGenerator.Config(0, 10)));
    // 0, 100, 200, etc
    fields.add(new FieldSpec("long_id", GeneratorType.SEQUENTIAL_LONG, 0, new SequentialLongGenerator.Config(0, 100)));
    FieldsSpecification fieldsSpec = new FieldsSpecification(fields, "sequence");

    Map<String, String> props = new HashMap<>();
    props.put(DataGeneratorConfig.REFERENCE_NAME, "purchases-fake");
    props.put(DataGeneratorConfig.DATASET, "custom");
    props.put(DataGeneratorConfig.NUM_SPLITS, "10");
    props.put(DataGeneratorConfig.RECORDS_PER_SPLIT, "100");
    props.put(DataGeneratorConfig.CUSTOM_CONFIG, GSON.toJson(fieldsSpec));

    String outputName = UUID.randomUUID().toString();
    ETLBatchConfig config = ETLBatchConfig.builder()
      .addStage(new ETLStage("src", new ETLPlugin(DataGeneratorBatchSource.NAME, BatchSource.PLUGIN_TYPE, props)))
      .addStage(new ETLStage("sink", MockSink.getPlugin(outputName)))
      .addConnection("src", "sink")
      .build();

    AppRequest<ETLBatchConfig> appRequest = new AppRequest<>(APP_ARTIFACT, config);
    ApplicationManager appManager = deployApplication(NamespaceId.DEFAULT.app("custom"), appRequest);

    WorkflowManager workflowManager = appManager.getWorkflowManager(SmartWorkflow.NAME);
    workflowManager.startAndWaitForRun(ProgramRunStatus.COMPLETED, 2, TimeUnit.MINUTES);

    DataSetManager<Table> outputTable = getDataset(outputName);
    Map<Integer, Long> actual = MockSink.readOutput(outputTable).stream()
      .collect(Collectors.toMap(r -> r.get("int_id"), r -> r.get("long_id")));
    for (int i = 0; i < 1000; i++) {
      Assert.assertEquals(i * 100L, (long) actual.get(i * 10));
    }
  }

  private static StructuredRecord subset(StructuredRecord record, Schema schema) {
    StructuredRecord.Builder output = StructuredRecord.builder(schema);
    for (Schema.Field field : schema.getFields()) {
      output.set(field.getName(), record.get(field.getName()));
    }
    return output.build();
  }
}
