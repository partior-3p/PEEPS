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
package tech.pegasys.peeps.consensus;

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.NodeConfiguration;
import tech.pegasys.peeps.SignerConfiguration;
import tech.pegasys.peeps.network.ConsensusMechanism;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Account;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.verification.ValueReceived;
import tech.pegasys.peeps.node.verification.ValueSent;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.Test;

public class Ibft2ConsensusTest extends NetworkTest {

  private final NodeConfiguration node = NodeConfiguration.ALPHA;
  private final SignerConfiguration signer = SignerConfiguration.ALPHA;

  @Override
  protected void setUpNetwork(final Network network) {
    network.addNode(node.id(), node.keys());
    network.addNode(NodeConfiguration.BETA.id(), NodeConfiguration.BETA.keys());
    network.set(ConsensusMechanism.CLIQUE, node.id());
    network.addSigner(signer.id(), signer.resources(), node.id());
  }

  @Test
  public void consensusAfterMiningMustHappen() {
    final Address sender = signer.address();
    final Address receiver = Account.BETA.address();
    final Wei transderAmount = Wei.valueOf(5000L);

    verify().consensusOnValue(sender, receiver);

    final Wei senderStartBalance = execute(node).getBalance(sender);
    final Wei receiverStartBalance = execute(node).getBalance(receiver);

    final Hash receipt = execute(signer).transfer(sender, receiver, transderAmount);

    await().consensusOnTransactionReciept(receipt);

    verify(node)
        .transistion(
            new ValueSent(sender, senderStartBalance, receipt),
            new ValueReceived(receiver, receiverStartBalance, transderAmount));

    verify().consensusOnValue(sender, receiver);
  }
}
