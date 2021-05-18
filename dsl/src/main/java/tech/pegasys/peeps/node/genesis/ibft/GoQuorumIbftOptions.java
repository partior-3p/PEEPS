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
package tech.pegasys.peeps.node.genesis.ibft;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonGetter;

public class GoQuorumIbftOptions {

  private final int blockPeriodSeconds;
  private final int epochLength;
  private final int requestTimeoutSeconds;
  private final Optional<Integer> qbftBlock;
  private final Optional<Integer> ceil2nBy3Block;

  public GoQuorumIbftOptions() {
    this(2, 30000, 10, Optional.empty(), Optional.empty());
  }

  public static GoQuorumIbftOptions createQbft() {
    return new GoQuorumIbftOptions(2, 30000, 10, Optional.of(0), Optional.empty());
  }

  public GoQuorumIbftOptions(
      final int blockPeriodSeconds,
      final int epochLength,
      final int requestTimeoutSeconds,
      final Optional<Integer> qbftBlock,
      final Optional<Integer> ceil2nBy3Block) {
    this.blockPeriodSeconds = blockPeriodSeconds;
    this.epochLength = epochLength;
    this.requestTimeoutSeconds = requestTimeoutSeconds;
    this.qbftBlock = qbftBlock;
    this.ceil2nBy3Block = ceil2nBy3Block;
  }

  @JsonGetter("blockperiod")
  public int getBlockPeriodSeconds() {
    return blockPeriodSeconds;
  }

  @JsonGetter("epoch")
  public int getEpochLength() {
    return epochLength;
  }

  @JsonGetter("requesttimeout")
  public int getRequestTimeoutSeconds() {
    return requestTimeoutSeconds;
  }

  @JsonGetter("qibftblock")
  public Optional<Integer> getQbftBlock() {
    return qbftBlock;
  }

  @JsonGetter("ceil2nby3block")
  public Optional<Integer> getCeil2nBy3Block() {
    return ceil2nBy3Block;
  }
}
