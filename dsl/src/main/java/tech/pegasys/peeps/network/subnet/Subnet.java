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
package tech.pegasys.peeps.network.subnet;

import static com.google.common.base.Preconditions.checkState;

import java.io.Closeable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Network;

public class Subnet implements Closeable {

  private static final Logger LOG = LogManager.getLogger();

  private static final int MAXIMUM_ATTEMPTS = 25;
  private static final int OCTET_MAXIMUM = 255;
  private static final String SUBNET_FORMAT = "172.20.%d.0/24";
  private static final AtomicInteger THIRD_OCTET = new AtomicInteger(0);

  private final SubnetAddresses addresses;
  private final Network network;

  public Subnet() {

    int attempt = 0;
    String subnet = null;
    Network possibleNetwork = null;

    while (attempt < MAXIMUM_ATTEMPTS && possibleNetwork == null) {
      subnet = getNextSubnetAndIncrement();

      try {
        possibleNetwork = createDockerNetwork(attempt, subnet);
      } catch (final UndeclaredThrowableException e) {
        logSubnetUnavailable(attempt, subnet);
      }

      attempt++;
    }

    checkState(
        possibleNetwork != null,
        "Failed to create a Docker network within %s attempts",
        MAXIMUM_ATTEMPTS);

    logNetworkAndSubnet(possibleNetwork, subnet);

    this.network = possibleNetwork;
    this.addresses = new SubnetAddresses(subnetAddressFormat(subnet));
  }

  public SubnetAddress getAddressAndIncrement() {
    return addresses.getAddressAndIncrement();
  }

  public Network network() {
    return network;
  }

  @Override
  public void close() {
    network.close();
  }

  private String getNextSubnetAndIncrement() {
    return String.format(SUBNET_FORMAT, consumeNextThirdOctet());
  }

  private void logSubnetUnavailable(final int attempt, final String subnet) {
    LOG.warn("Attempt: {}, failed to create Network with subnet: {}", attempt, subnet);
  }

  private void logNetworkAndSubnet(final Network network, final String subnet) {
    LOG.info("Created Network: {}, with subnet: {}", network.getId(), subnet);
  }

  private synchronized int consumeNextThirdOctet() {

    if (THIRD_OCTET.get() > OCTET_MAXIMUM) {
      THIRD_OCTET.set(0);
    }

    return THIRD_OCTET.getAndIncrement();
  }

  private String subnetAddressFormat(final String subnet) {
    return subnet.substring(0, subnet.lastIndexOf('.')) + ".%d";
  }

  /**
   * TestContainers uses lazy initialization of Docker networks, creation with the Docker client
   * being triggered by getId().
   */
  private Network createDockerNetwork(final int attempt, final String subnet) {
    final Network network =
        Network.builder()
            .createNetworkCmdModifier(
                modifier ->
                    modifier.withIpam(new Ipam().withConfig(new Config().withSubnet(subnet))))
            .build();

    LOG.info("Attempt: {}, creating Network with subnet: {}", attempt, subnet);

    checkState(network.getId() != null, "Creation of Network failed, no Id returned");
    checkState(!network.getId().isBlank(), "Network created with an empty Id returned");

    return network;
  }
}
