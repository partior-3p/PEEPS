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

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.network.ConsensusMechanism;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.Web3ProviderType;
import tech.pegasys.peeps.util.Await;

import java.util.List;

import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.junit.jupiter.api.Test;

public class QbftRoundChangeTest extends NetworkTest {

  private Web3Provider quorumNode1;
  private Web3Provider quorumNode2;
  private Web3Provider besuNode1;
  private Web3Provider besuNode2;

  @Override
  protected void setUpNetwork(final Network network) {
    besuNode1 = network.addNode("besu1", KeyPair.random());
    besuNode2 = network.addNode("besu2", KeyPair.random());
    quorumNode1 = network.addNode("quorum1", KeyPair.random(), Web3ProviderType.GOQUORUM);
    quorumNode2 = network.addNode("quorum2", KeyPair.random(), Web3ProviderType.GOQUORUM);
    network.set(ConsensusMechanism.QBFT, besuNode1, besuNode2, quorumNode1, quorumNode2);
  }

  @Test
  public void roundChangesWhenNodesLessThanQuorum() {
    verify().consensusOnBlockNumberIsAtLeast(1);

    besuNode1.stop();
    quorumNode1.stop();

    // network should now be stalled and creating round changes
    final List<Web3Provider> runningNodes = List.of(besuNode2, quorumNode2);
    besuNode2.awaitConnectivity(runningNodes);
    quorumNode2.awaitConnectivity(runningNodes);

    final long stalledBlockNumber = quorumNode2.rpc().getBlockNumber();

    Await.await(
        () ->
            assertThat(
                    findRoundChangeLogs(
                        quorumNode2, "Start new qibft round", "old_seq=", stalledBlockNumber))
                .isEqualTo(1),
        "Quorum node not in roundchange");
    Await.await(
        () ->
            assertThat(
                    findRoundChangeLogs(
                        besuNode2, "Round has expired", "Sequence=", stalledBlockNumber + 1))
                .isEqualTo(1),
        "Besu node not in roundchange");

    // network should function and start producing blocks after starting the two stopped nodes
    besuNode1.start();
    quorumNode1.start();
    verify().consensusOnBlockNumberIsAtLeast(stalledBlockNumber + 1);
  }

  private long findRoundChangeLogs(
      final Web3Provider web3Provider,
      final String roundChangeMsg,
      final String sequenceMsg,
      final long blockNumber) {
    return web3Provider
        .getLogs()
        .lines()
        .filter(l -> l.contains(roundChangeMsg) && l.contains(sequenceMsg + blockNumber))
        .count();
  }
}
