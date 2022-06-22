/*
 * Copyright © 2022 Cask Data, Inc.
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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Configuration object for the {@link DataGeneratorStreamingSource}.
 */
public class DataGeneratorStreamingConfig extends DataGeneratorConfig {

  static final String PAUSE_MILLIS_PER_BATCH = "pauseMillisPerBatch";

  @Macro
  @Nullable
  @Name(PAUSE_MILLIS_PER_BATCH)
  @Description("Number of milliseconds to pause between each data batch generated")
  private Long pauseMillisPerBatch;

  public DataGeneratorStreamingConfig() {
    super();
    this.pauseMillisPerBatch = TimeUnit.SECONDS.toMillis(1);
  }

  public long getPauseMillisPerBatch() {
    return pauseMillisPerBatch == null ? TimeUnit.SECONDS.toMillis(1) : pauseMillisPerBatch;
  }
}
