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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;
import io.vertx.core.Vertx;

public class Network {

  // TODO do not be hard coded as two nodes - flexibility in nodes & stack
  // TODO cater for one-many & many-one for Besu/Orion
  // TODO cater for one-many for Besu/EthSigner

  private final Besu besuA;
  private final Orion orionA;
  private final EthSigner signerA = new EthSigner();

  private final Besu besuB;
  private final EthSigner signerB = new EthSigner();
  private final Orion orionB;

  private final org.testcontainers.containers.Network network;

  private final Vertx vertx;

  // TODO IP management

  public Network(final Path workingDirectory) {
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

    this.orionA =
        new Orion(
            new OrionConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress("172.20.0.5")
                .withPrivateKeys(Collections.singletonList(OrionKeys.ONE.getPrivateKey()))
                .withPublicKeys(Collections.singletonList(OrionKeys.ONE.getPublicKey()))
                .withFileSystemConfigurationFile(
                    new File(workingDirectory.toFile(), "orionA.conf").toPath())
                .build());

    this.besuA =
        new Besu(
            new NodeConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress("172.20.0.6")
                .withNodePrivateKeyFile(NodeKeys.BOOTNODE.getPrivateKeyFile())
                .build());

    // TODO File or Path
    final List<String> orionBootnodes = new ArrayList<>();
    orionBootnodes.add(orionA.getNetworkAddress());

    this.orionB =
        new Orion(
            new OrionConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress("172.20.0.7")
                .withPrivateKeys(Collections.singletonList(OrionKeys.TWO.getPrivateKey()))
                .withPublicKeys(Collections.singletonList(OrionKeys.TWO.getPublicKey()))
                .withBootnodeUrls(orionBootnodes)
                .withFileSystemConfigurationFile(
                    new File(workingDirectory.toFile(), "orionB.conf").toPath())
                .build());

    // TODO can fail otherwise - runtime exception
    final String bootnodeEnodeAddress = NodeKeys.BOOTNODE.getEnodeAddress("172.20.0.6", "30303");

    this.besuB =
        new Besu(
            new NodeConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress("172.20.0.8")
                .withBootnodeEnodeAddress(bootnodeEnodeAddress)
                .build());
  }

  public void start() {
    // TODO multi-thread the blocking start ops, using await connectivity as the sync point
    orionA.start();
    besuA.start();
    orionB.start();
    besuB.start();
    awaitConnectivity();
  }

  public void stop() {
    orionA.stop();
    besuA.stop();
    orionB.stop();
    besuB.stop();
  }

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
  }

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
