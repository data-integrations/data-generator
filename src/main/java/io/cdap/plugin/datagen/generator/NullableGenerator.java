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

import io.cdap.plugin.datagen.DataGenerator;
import io.cdap.plugin.datagen.GeneratorContext;

/**
 * Sometimes generates a null value, otherwise delegates to another generator.
 *
 * @param <T> type of data to generate
 */
public class NullableGenerator<T> extends RandomGenerator<T> {
  private final DataGenerator<T> delegate;
  private final int nullChance;

  public NullableGenerator(DataGenerator<T> delegate, int nullChance) {
    this.delegate = delegate;
    this.nullChance = nullChance;
  }

  @Override
  public void initialize(GeneratorContext context) {
    super.initialize(context);
    delegate.initialize(context);
  }

  @Override
  public T generate() {
    if (random.nextInt(100) < nullChance) {
      return null;
    }
    return delegate.generate();
  }

}
