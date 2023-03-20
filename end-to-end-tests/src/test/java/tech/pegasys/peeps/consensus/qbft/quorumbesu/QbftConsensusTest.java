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
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.verification.ValueReceived;
import tech.pegasys.peeps.node.verification.ValueSent;
import tech.pegasys.peeps.signer.SignerConfiguration;

import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.Test;

public class QbftConsensusTest extends NetworkTest {

  private Web3Provider quorumNode;
  private final SignerConfiguration signer = FixedSignerConfigs.ALPHA;

  @Override
  protected void setUpNetwork(final Network network) {
    final Web3Provider besuNode = network.addNode("besu", KeyPair.random());
    quorumNode = network.addNode("quorum", KeyPair.random(), Web3ProviderType.GOQUORUM, signer);
    var gammaNode = network.addNode("quorum", KeyPair.random(), Web3ProviderType.GOQUORUM, signer);
    network.set(ConsensusMechanism.QBFT, besuNode, quorumNode, gammaNode);
  }

  @Test
  public void consensusAfterMiningMustHappen() {
    verify().consensusOnBlockNumberIsAtLeast(1);

    final Address sender = signer.address();
    final Address receiver = Account.BETA.address();
    final Wei transferAmount = Wei.valueOf(5000L);

    verify().consensusOnValueAt(sender, receiver);

    final Wei senderStartBalance = execute(quorumNode).getBalance(sender);
    final Wei receiverStartBalance = execute(quorumNode).getBalance(receiver);

    final Hash receipt = execute(quorumNode).transfer(signer.address(), receiver, transferAmount);

    await().consensusOnTransactionReceipt(receipt);

    verifyOn(quorumNode)
        .transition(
            new ValueSent(sender, senderStartBalance, receipt),
            new ValueReceived(receiver, receiverStartBalance, transferAmount));

    verify().consensusOnValueAt(sender, receiver);
  }
}
