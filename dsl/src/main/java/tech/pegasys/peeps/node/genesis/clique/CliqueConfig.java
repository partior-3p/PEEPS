/*
 * Copyright 2020 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.peeps.node.genesis.clique;

import com.fasterxml.jackson.annotation.JsonGetter;

public class CliqueConfig {

  private final int blockPeriodSeconds;
  private final int epochLength;

  public CliqueConfig() {
    this(2, 30000);
  }

  public CliqueConfig(final int blockPeriodSeconds, final int epochLength) {
    this.blockPeriodSeconds = blockPeriodSeconds;
    this.epochLength = epochLength;
  }

  @JsonGetter("blockperiodseconds")
  public int getBlockPeriodSeconds() {
    return blockPeriodSeconds;
  }

  @JsonGetter("period")
  public int getPeriod() {
    return blockPeriodSeconds;
  }

  @JsonGetter("epochlength")
  public int getEpochLength() {
    return epochLength;
  }

  @JsonGetter("epoch")
  public int getEpoch() {
    return epochLength;
  }
}
