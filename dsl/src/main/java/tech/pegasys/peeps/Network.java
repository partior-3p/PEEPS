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

import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;

public class Network {

  // TODO do not be hard coded as two nodes - flexibility in nodes & stack
  // TODO cater for one-many & many-one for Besu/Orion
  // TODO cater for one-many for Besu/EthSigner

  private final Besu besuA;
  private final Orion orionA = new Orion();
  private final EthSigner signerA = new EthSigner();

  private final Besu besuB;
  private final EthSigner signerB = new EthSigner();
  private final Orion orionB = new Orion();

  // TODO IP management

  public Network() {
    final org.testcontainers.containers.Network network =
        org.testcontainers.containers.Network.builder()
            .createNetworkCmdModifier(
                modifier ->
                    modifier.withIpam(
                        new Ipam().withConfig(new Config().withSubnet("172.20.0.0/24"))))
            .build();

    // TODO 0.1 seems to be used, maybe assigned by the network container?

    // TODO no magic string!?!?

    besuA =
        new Besu(
            new NodeConfigurationBuilder()
                .withContainerNetwork(network)
                .withIpAddress("172.20.0.5")
                .withNodePrivateKeyFile(NodeKeys.BOOTNODE.getPrivateKeyFile())
                .build());

    // TODO move this into besu; can figure out if these parts are defined in construction or is
    // after starting
    // TODO can fail otherwise - runtime exception
    final String bootnodeEnodeAddress = NodeKeys.BOOTNODE.getEnodeAddress("172.20.0.5", "30303");

    besuB =
        new Besu(
            new NodeConfigurationBuilder()
                .withContainerNetwork(network)
                .withIpAddress("172.20.0.6")
                .withBootnodeEnodeAddress(bootnodeEnodeAddress)
                .build());
  }

  public void start() {
    besuA.start();
    besuB.start();
    awaitConnectivity();
  }

  public void stop() {
    besuA.stop();
    besuB.stop();
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
