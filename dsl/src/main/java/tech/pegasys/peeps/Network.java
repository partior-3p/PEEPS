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
package tech.pegasys.peeps;

import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.NodeConfigurationBuilder;
import tech.pegasys.peeps.node.NodeKeys;
import tech.pegasys.peeps.privacy.Orion;
import tech.pegasys.peeps.privacy.OrionConfigurationBuilder;
import tech.pegasys.peeps.privacy.OrionKeys;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.EthSignerConfigurationBuilder;
import tech.pegasys.peeps.util.PeepsTemporaryDirectory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;
import io.vertx.core.Vertx;

public class Network implements Closeable {

  private final PeepsTemporaryDirectory workingDirectory;

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

  // TODO IP management

  // TODO choosing the topology should be elsewhere
  public Network() {
    this.workingDirectory = new PeepsTemporaryDirectory();
    this.vertx = Vertx.vertx();

    // TODO subnet with substitution for static IPs
    this.network =
        org.testcontainers.containers.Network.builder()
            .createNetworkCmdModifier(
                modifier ->
                    modifier.withIpam(
                        new Ipam().withConfig(new Config().withSubnet("172.20.0.0/24"))))
            .build();

    // TODO 0.1 seems to be used, maybe assigned by the network container?

    // TODO no magic strings!?!?
    // TODO better typing then String
    final String ipAddressOrionA = "172.20.0.5";
    final String ipAddressBesuA = "172.20.0.6";
    final String ipAddressSignerA = "172.20.0.7";
    final String ipAddressOrionB = "172.20.0.8";
    final String ipAddressBesuB = "172.20.0.9";
    final String ipAddressSignerB = "172.20.0.10";

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
                .withFileSystemConfigurationFile(workingDirectory.getUniqueFile())
                .build());

    this.besuA =
        new Besu(
            new NodeConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(ipAddressBesuA)
                .withNodePrivateKeyFile(NodeKeys.BOOTNODE.getPrivateKeyFile())
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
    orionBootnodes.add(orionA.getNetworkAddress());

    this.orionB =
        new Orion(
            new OrionConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(ipAddressOrionB)
                .withPrivateKeys(Collections.singletonList(OrionKeys.TWO.getPrivateKey()))
                .withPublicKeys(Collections.singletonList(OrionKeys.TWO.getPublicKey()))
                .withBootnodeUrls(orionBootnodes)
                .withFileSystemConfigurationFile(workingDirectory.getUniqueFile())
                .build());

    // TODO better typing then String
    final String bootnodeEnodeAddress = NodeKeys.BOOTNODE.getEnodeAddress(ipAddressBesuA, "30303");

    this.besuB =
        new Besu(
            new NodeConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(ipAddressBesuB)
                .withBootnodeEnodeAddress(bootnodeEnodeAddress)
                .build());

    this.signerB =
        new EthSigner(
            new EthSignerConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(ipAddressSignerB)
                .withChainId(chainId)
                .withDownstreamHost(ipAddressBesuB)
                .withDownstreamPort(portBesuB)
                .withKeyFile(keyFileSignerB)
                .withPasswordFile(passwordFileSignerB)
                .build());
  }

  public void start() {
    // TODO multi-thread the blocking start ops, using await connectivity as the sync point
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
    workingDirectory.close();
  }

  private void awaitConnectivity() {
    besuA.awaitConnectivity(besuB);
    besuB.awaitConnectivity(besuA);
    orionA.awaitConnectivity(orionB);
    orionB.awaitConnectivity(orionA);
  }

  // TODO interfaces for the signer used by the test?
  // TODO figure out a nicer way for the UT to get a handle on the signers
  public EthSigner getSignerA() {
    return signerA;
  }

  // TODO figure out a nicer way for the UT to get a handle on the signers
  public EthSigner getSignerB() {
    return signerB;
  }

  // TODO provide a handle for Besus too? (web3j?)
}
