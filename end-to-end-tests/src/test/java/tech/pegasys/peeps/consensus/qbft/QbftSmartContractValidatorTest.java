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
package tech.pegasys.peeps.consensus.qbft;

import static java.math.BigInteger.ZERO;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.web3j.tx.gas.DefaultGasProvider.GAS_LIMIT;

import tech.pegasys.peeps.FixedSignerConfigs;
import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.contract.ValidatorSmartContractAllowList;
import tech.pegasys.peeps.network.ConsensusMechanism;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.genesis.bft.BftConfig;
import tech.pegasys.peeps.signer.SignerConfiguration;
import tech.pegasys.peeps.util.AddressConverter;
import tech.pegasys.peeps.util.Await;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.web3j.tx.gas.StaticGasProvider;

public class QbftSmartContractValidatorTest extends NetworkTest {

  private Web3Provider alphaNode;
  private Web3Provider bravoNode;
  private Web3Provider charlieNode;
  private Web3Provider deltaNode;
  private final SignerConfiguration signer = FixedSignerConfigs.ALPHA;

  @Override
  protected void setUpNetwork(final Network network) {
    alphaNode = network.addNode("alpha", KeyPair.random());
    bravoNode = network.addNode("bravo", KeyPair.random());
    charlieNode = network.addNode("charlie", KeyPair.random());
    deltaNode = network.addNode("delta", KeyPair.random());

    network.set(ConsensusMechanism.QBFT_TRANSITIONS, alphaNode, bravoNode, charlieNode, deltaNode);
    network.addSigner(signer.name(), signer.resources(), alphaNode);
  }

  @Test
  public void consensusAfterSmartContractTransitionMustHappen() throws Exception {

    final List<Web3Provider> validatorNodes = List.of(alphaNode, bravoNode, charlieNode, deltaNode);

    assertThat(alphaNode.rpc().getBlockNumber()).isLessThan(18);

    ValidatorSmartContractAllowList allowListDeploy =
        ValidatorSmartContractAllowList.deploy(
                alphaNode.getWeb3j(),
                signer.getCredentials(),
                new StaticGasProvider(ZERO, GAS_LIMIT),
                validatorNodes.stream()
                    .map(x -> x.address().toHexString())
                    .collect(Collectors.toList()))
            .send();

    BigInteger currentBlock = allowListDeploy.getTransactionReceipt().get().getBlockNumber();
    BigInteger transitionBlock = currentBlock.add(BigInteger.TEN);

    network.setValidatorContractValidatorTransaction(
        transitionBlock, allowListDeploy.getContractAddress());

    validatorNodes.forEach(
        node -> {
          node.stop();
          node.start();
          verify().consensusOnBlockNumberIsAtLeast(currentBlock.longValue());
        });

    verify().consensusOnBlockNumberIsAtLeast(transitionBlock.longValue());

    alphaNode.rpc().qbftGetValidatorsByBlockBlockNumber(transitionBlock.toString());

    ValidatorSmartContractAllowList allowListUpdate =
        ValidatorSmartContractAllowList.load(
            allowListDeploy.getContractAddress(),
            alphaNode.getWeb3j(),
            signer.getCredentials(),
            new StaticGasProvider(ZERO, GAS_LIMIT));

    assertThat(allowListUpdate.getValidators().send().size()).isEqualTo(4);

    allowListUpdate
        .activate(
            AddressConverter.fromPublicKey(KeyPair.random().publicKey().toHexString())
                .toHexString())
        .send();
    allowListUpdate
        .activate(
            AddressConverter.fromPublicKey(KeyPair.random().publicKey().toHexString())
                .toHexString())
        .send();

    assertThat(allowListUpdate.getValidators().send().size()).isEqualTo(6);

    alphaNode.stop();
    bravoNode.stop();

    final List<Web3Provider> runningNodes = List.of(charlieNode, deltaNode);

    // 2 nodes are up 2 nodes are down and 2 nodes don't exist
    runningNodes.forEach(this::verifyChainStalled);

    final long stalledBlockNumber = charlieNode.rpc().getBlockNumber();

    alphaNode.start();
    bravoNode.start();

    final List<Web3Provider> allNodes = List.of(alphaNode, bravoNode, charlieNode, deltaNode);
    allNodes.forEach(node -> node.awaitConnectivity(allNodes));

    verify().consensusOnBlockNumberIsAtLeast(stalledBlockNumber + 1);
  }

  private void verifyChainStalled(final Web3Provider web3Provider) {
    Await.await(
        () -> {
          final long startBlockNumber = web3Provider.rpc().getBlockNumber();
          Thread.sleep(Duration.of(BftConfig.DEFAULT_BLOCK_PERIOD_SECONDS * 2, SECONDS).toMillis());
          final long currentBlockNumber = web3Provider.rpc().getBlockNumber();
          Assertions.assertThat(currentBlockNumber).isEqualTo(startBlockNumber);
        },
        "Node %s has not stalled",
        web3Provider.getNodeId());
  }
}
