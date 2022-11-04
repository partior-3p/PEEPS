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
package tech.pegasys.peeps.node;

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.verification.NodeValueTransition;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class NodeVerify {
  private final Web3Provider node;

  public NodeVerify(final Web3Provider node) {
    this.node = node;
  }

  public void transition(final NodeValueTransition... changes) {
    node.verifyTransition(changes);
  }

  public void successfulTransactionReceipt(final Hash transaction) {
    node.verifySuccessfulTransactionReceipt(transaction);
  }

  public void blockRewardsAreTransferredToValidators(
      final int startBlockNumber, final int endBlockNumber, final Wei blockReward) {
    for (long blockNumber = startBlockNumber; blockNumber < endBlockNumber; blockNumber++) {
      node.verifyBlockRewardsAreTransferredToValidatorAtBlock(blockNumber, blockReward);
    }
  }

  public void blockRewardsAreTransferredToMiningBeneficiary(
      final int startBlockNumber,
      final int endBlockNumber,
      final Wei blockReward,
      final Address miningBeneficiary) {
    for (long blockNumber = startBlockNumber; blockNumber < endBlockNumber; blockNumber++) {
      node.verifyBlockRewardsAreTransferredToMiningBeneficiary(
          blockNumber, blockReward, miningBeneficiary);
    }
  }

  public void gasRewardsAreTransferredToMiningBeneficiary(
      final Hash receipt, final Address miningBeneficiary, final Wei blockReward) {
    node.verifyGasRewardsAreTransferredToMiningBeneficiary(receipt, miningBeneficiary, blockReward);
  }
}
