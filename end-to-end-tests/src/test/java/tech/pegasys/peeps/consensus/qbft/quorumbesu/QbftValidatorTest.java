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

import static tech.pegasys.peeps.network.ConsensusMechanism.QBFT;

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.Web3ProviderType;
import tech.pegasys.peeps.node.rpc.QbftRpc.VoteType;

import java.util.List;

import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.junit.jupiter.api.Test;

public class QbftValidatorTest extends NetworkTest {
  private Web3Provider quorumNode1;
  private Web3Provider besuNode1;
  private Web3Provider besuNode2;

  @Override
  protected void setUpNetwork(final Network network) {
    besuNode1 = network.addNode("besu1", KeyPair.random());
    besuNode2 = network.addNode("besu2", KeyPair.random());
    quorumNode1 = network.addNode("quorum1", KeyPair.random(), Web3ProviderType.GOQUORUM);
    network.set(QBFT, besuNode1, quorumNode1); // exclude besuNode2 from genesis extraData
  }

  @Test
  public void validatorCanBeAdded() {
    verify().consensusOnBlockNumberIsAtLeast(1);

    besuNode1.rpc().qbftProposeValidatorVote(besuNode2.address(), VoteType.ADD);
    quorumNode1.rpc().qbftProposeValidatorVote(besuNode2.address(), VoteType.ADD);

    verify()
        .consensusOnValidators(
            List.of(quorumNode1.address(), besuNode1.address(), besuNode2.address()));
  }

  @Test
  void validatorCanBeRemoved() {
    verify().consensusOnBlockNumberIsAtLeast(1);

    besuNode1.rpc().qbftProposeValidatorVote(besuNode1.address(), VoteType.REMOVE);
    quorumNode1.rpc().qbftProposeValidatorVote(besuNode1.address(), VoteType.REMOVE);

    verify().consensusOnValidators(List.of(quorumNode1.address()));
  }
}
