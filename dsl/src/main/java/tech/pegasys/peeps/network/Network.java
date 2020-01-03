/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.peeps.network;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.BesuConfigurationBuilder;
import tech.pegasys.peeps.node.NodeKeys;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.privacy.Orion;
import tech.pegasys.peeps.privacy.OrionConfigurationBuilder;
import tech.pegasys.peeps.privacy.OrionKeys;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.EthSignerConfigurationBuilder;
import tech.pegasys.peeps.util.Await;
import tech.pegasys.peeps.util.PathGenerator;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertx.core.Vertx;

public class Network implements Closeable {

  // TODO do not be hard coded as two nodes - flexibility in nodes & stack
  // TODO cater for one-many & many-one for Besu/Orion
  // TODO cater for one-many for Besu/EthSigner

  private final Besu besuA;
  private final Orion orionA;
  private final EthSigner signerA;

  private final Besu besuB;
  private final EthSigner signerB;
  private final Orion orionB;

  private final org.testcontainers.containers.Network network;

  private final Vertx vertx;

  // TODO choosing the topology should be elsewhere
  public Network(final Path configurationDirectory) {
    checkNotNull(configurationDirectory);

    final PathGenerator pathGenerator = new PathGenerator(configurationDirectory);
    this.vertx = Vertx.vertx();

    final Subnet subnet = new Subnet();

    this.network = subnet.createContainerNetwork();

    // TODO 0.1 seems to be used, maybe assigned by the network container?

    // TODO better typing then String
    final String ipAddressOrionA = subnet.getAddressAndIncrement();
    final String ipAddressBesuA = subnet.getAddressAndIncrement();
    final String ipAddressSignerA = subnet.getAddressAndIncrement();
    final String ipAddressOrionB = subnet.getAddressAndIncrement();
    final String ipAddressBesuB = subnet.getAddressAndIncrement();
    final String ipAddressSignerB = subnet.getAddressAndIncrement();

    // TODO these should come from the Besu, or config aggregation
    final long chainId = 4004;
    final int portBesuA = 8545;
    final int portBesuB = 8545;

    // TODO name files according the account pubkey

    // TODO these should come from somewhere, programmatically generated?
    final String keyFileSignerA = "signer/account/funded/wallet_a.v3";
    final String passwordFileSignerA = "signer/account/funded/wallet_a.pass";
    final String keyFileSignerB = "signer/account/funded/wallet_b.v3";
    final String passwordFileSignerB = "signer/account/funded/wallet_b.pass";

    this.orionA =
        new Orion(
            new OrionConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(ipAddressOrionA)
                .withPrivateKeys(Collections.singletonList(OrionKeys.ONE.getPrivateKey()))
                .withPublicKeys(Collections.singletonList(OrionKeys.ONE.getPublicKey()))
                .withFileSystemConfigurationFile(pathGenerator.uniqueFile())
                .build());

    this.besuA =
        new Besu(
            new BesuConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withPrivacyUrl(orionA.getNetworkRpcAddress())
                .withIpAddress(ipAddressBesuA)
                .withNodePrivateKeyFile(NodeKeys.BOOTNODE.getPrivateKeyFile())
                .withPrivacyManagerPublicKey(OrionKeys.ONE.getPublicKey())
                .build());

    this.signerA =
        new EthSigner(
            new EthSignerConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(ipAddressSignerA)
                .withChainId(chainId)
                .withDownstreamHost(ipAddressBesuA)
                .withDownstreamPort(portBesuA)
                .withKeyFile(keyFileSignerA)
                .withPasswordFile(passwordFileSignerA)
                .build());

    // TODO More typing then a String - URI, URL, File or Path
    final List<String> orionBootnodes = new ArrayList<>();
    orionBootnodes.add(orionA.getPeerNetworkAddress());

    this.orionB =
        new Orion(
            new OrionConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(ipAddressOrionB)
                .withPrivateKeys(Collections.singletonList(OrionKeys.TWO.getPrivateKey()))
                .withPublicKeys(Collections.singletonList(OrionKeys.TWO.getPublicKey()))
                .withBootnodeUrls(orionBootnodes)
                .withFileSystemConfigurationFile(pathGenerator.uniqueFile())
                .build());

    // TODO better typing then String
    final String bootnodeEnodeAddress = NodeKeys.BOOTNODE.getEnodeAddress(ipAddressBesuA, "30303");

    this.besuB =
        new Besu(
            new BesuConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withPrivacyUrl(orionB.getNetworkRpcAddress())
                .withIpAddress(ipAddressBesuB)
                .withBootnodeEnodeAddress(bootnodeEnodeAddress)
                .withPrivacyManagerPublicKey(OrionKeys.TWO.getPublicKey())
                .build());

    this.signerB =
        new EthSigner(
            new EthSignerConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withChainId(chainId)
                .withIpAddress(ipAddressSignerB)
                .withDownstreamHost(ipAddressBesuB)
                .withDownstreamPort(portBesuB)
                .withKeyFile(keyFileSignerB)
                .withPasswordFile(passwordFileSignerB)
                .build());
  }

  public void start() {
    // TODO multi-thread the blocking start ops, using await connectivity as the
    // sync point
    orionA.start();
    besuA.start();
    signerA.start();
    orionB.start();
    besuB.start();
    signerB.start();
    awaitConnectivity();
  }

  public void stop() {
    orionA.stop();
    besuA.stop();
    signerA.stop();
    orionB.stop();
    besuB.stop();
    signerB.stop();
  }

  @Override
  public void close() {
    stop();
    vertx.close();
    network.close();
  }

  private void awaitConnectivity() {
    besuA.awaitConnectivity(besuB);
    besuB.awaitConnectivity(besuA);
    orionA.awaitConnectivity(orionB);
    orionB.awaitConnectivity(orionA);

    signerA.awaitConnectivity(besuA);
    signerB.awaitConnectivity(besuB);
  }

  // TODO restructure, maybe Supplier related or a utility on network?
  // TODO stricter typing than String
  public void awaitConsensusOn(final String receiptHash) {

    Await.await(
        () -> {
          final TransactionReceipt pmtReceiptNodeA = besuA.getTransactionReceipt(receiptHash);
          final TransactionReceipt pmtReceiptNodeB = besuB.getTransactionReceipt(receiptHash);

          assertThat(pmtReceiptNodeA).isNotNull();
          assertThat(pmtReceiptNodeA.isSuccess()).isTrue();
          assertThat(pmtReceiptNodeA).usingRecursiveComparison().isEqualTo(pmtReceiptNodeB);
        },
        "Consensus was not reached in time for receipt hash: " + receiptHash);
  }

  // TODO interfaces for the signer used by the test?
  // TODO figure out a nicer way for the UT to get a handle on the signers
  public EthSigner getSignerA() {
    return signerA;
  }

  public EthSigner getSignerB() {
    return signerB;
  }

  // TODO figure out a nicer way for the UT to get a handle on the node or send
  // requests
  public Besu getNodeA() {
    return besuA;
  }

  // TODO figure out a nicer way for the UT to get a handle on the node or send
  // requests
  public Besu getNodeB() {
    return besuB;
  }

  // TODO figure out a nicer way for the UT to get a handle on the Orion or send
  // requests
  public Orion getOrionA() {
    return orionA;
  }

  // TODO figure out a nicer way for the UT to get a handle on the Orion or send
  // requests
  public Orion getOrionB() {
    return orionB;
  }

  // TODO provide a handle for Besus too? (web3j?)
}
