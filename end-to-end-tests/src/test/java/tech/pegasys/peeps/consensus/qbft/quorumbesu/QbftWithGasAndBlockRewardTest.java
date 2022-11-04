/*
 * Copyright 2021 ConsenSys AG.
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
package tech.pegasys.peeps.consensus.qbft.quorumbesu;

import tech.pegasys.peeps.FixedSignerConfigs;
import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.network.ConsensusMechanism;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Account;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.Web3ProviderType;
import tech.pegasys.peeps.node.verification.ValueReceived;
import tech.pegasys.peeps.node.verification.ValueSent;
import tech.pegasys.peeps.signer.SignerConfiguration;

import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.Test;

public class QbftWithGasAndBlockRewardTest extends NetworkTest {
  int blockRewardTransitionBlock = 20;
  int miningBeneficiaryTransitionBlock = 30;

  int someBlocks = 10;

  private final Wei gasPrice = Wei.valueOf(1);

  private final Wei blockReward = Wei.valueOf(1);

  private Web3Provider quorumNode1;
  private Web3Provider quorumNode2;
  private Web3Provider besuNode1;
  private Web3Provider besuNode2;

  private final SignerConfiguration signer = FixedSignerConfigs.ALPHA;

  private final Address miningBeneficiary =
      Address.fromHexString("0xab5801a7d398351b8be11c439e05c5b3259aec9b");

  @Override
  protected void setUpNetwork(final Network network) {
    besuNode1 = network.addNode("besu1", KeyPair.random(), Web3ProviderType.BESU, gasPrice);
    besuNode2 = network.addNode("besu2", KeyPair.random(), Web3ProviderType.BESU, gasPrice);
    quorumNode1 = network.addNode("quorum1", KeyPair.random(), Web3ProviderType.GOQUORUM, gasPrice);
    quorumNode2 = network.addNode("quorum2", KeyPair.random(), Web3ProviderType.GOQUORUM, gasPrice);

    network.addBlockRewardTransition(blockRewardTransitionBlock, blockReward);
    network.addMiningBeneficiaryTransition(miningBeneficiaryTransitionBlock, miningBeneficiary);

    network.set(ConsensusMechanism.QBFT, besuNode1, besuNode2, quorumNode1, quorumNode2);

    network.addSigner(signer.name(), signer.resources(), besuNode1, gasPrice);
  }

  @Test
  public void validatorReceivesGasRewards() {
    verify().consensusOnBlockNumberIsAtLeast(1);

    final Address sender = signer.address();
    final Address receiver = Account.BETA.address();
    final Wei transferAmount = Wei.valueOf(5000L);

    verify().consensusOnValueAt(sender, receiver);

    final Wei senderStartBalance = execute(besuNode1).getBalance(sender);
    final Wei receiverStartBalance = execute(besuNode2).getBalance(receiver);

    var receipt = execute(signer).transferTo(receiver, transferAmount);

    await().consensusOnTransactionReceipt(receipt);

    verifyOn(besuNode2)
        .transition(
            new ValueSent(sender, senderStartBalance, receipt),
            new ValueReceived(receiver, receiverStartBalance, transferAmount));
    verify().consensusOnValueAt(sender, receiver);
    verify().gasRewardsAreTransferredToValidator(receipt);

    verify().consensusOnBlockNumberIsAtLeast(blockRewardTransitionBlock + someBlocks);
    verifyOn(besuNode2)
        .blockRewardsAreTransferredToValidators(
            blockRewardTransitionBlock, blockRewardTransitionBlock + someBlocks, blockReward);

    verify().consensusOnBlockNumberIsAtLeast(miningBeneficiaryTransitionBlock + someBlocks);
    verifyOn(besuNode1)
        .blockRewardsAreTransferredToMiningBeneficiary(
            miningBeneficiaryTransitionBlock,
            miningBeneficiaryTransitionBlock + someBlocks,
            blockReward,
            miningBeneficiary);

    receipt = execute(signer).transferTo(receiver, transferAmount);
    await().consensusOnTransactionReceipt(receipt);
    verifyOn(besuNode1)
        .gasRewardsAreTransferredToMiningBeneficiary(receipt, miningBeneficiary, blockReward);
  }
}
