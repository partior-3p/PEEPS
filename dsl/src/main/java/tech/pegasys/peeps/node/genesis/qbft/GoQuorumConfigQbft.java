/*
 * Copyright 2022 ConsenSys AG.
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
package tech.pegasys.peeps.node.genesis.qbft;

import tech.pegasys.peeps.node.genesis.GenesisConfig;
import tech.pegasys.peeps.node.genesis.bft.BftConfig;
import tech.pegasys.peeps.node.genesis.transitions.Transition;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class GoQuorumConfigQbft extends GenesisConfig {
  private final BftConfig consensusConfig;
  private final List<Transition> transitions = new ArrayList<>();

  public GoQuorumConfigQbft(
      final long chainId,
      final BftConfig consensusConfig,
      final Wei minGasPrice,
      final long blockNumber,
      final Wei blockReward,
      final long miningBeneficiaryBlock,
      final Address miningBeneficiary) {
    super(chainId);
    this.consensusConfig = consensusConfig;
    if (minGasPrice.toLong() > 0) {
      transitions.add(new GasPriceEnabledTransition(0));
    }
    if (blockReward.toLong() > 0) {
      transitions.add(new BlockRewardTransition(blockNumber, blockReward));
    }
    if (miningBeneficiary != null) {
      transitions.add(new MiningBeneficiaryTransition(miningBeneficiaryBlock, miningBeneficiary));
    }
  }

  @JsonGetter("qbft")
  public BftConfig getConsensusConfig() {
    return consensusConfig;
  }

  @JsonGetter("transitions")
  public List<Transition> getTransitions() {
    return transitions;
  }
}
