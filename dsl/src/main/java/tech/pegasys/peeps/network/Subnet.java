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
package tech.pegasys.peeps.network;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;
import com.google.common.annotations.VisibleForTesting;
import org.testcontainers.containers.Network;

public class Subnet {

  private static final int MAXIMUM_ATTEMPTS = 25;
  private static final int OCTET_MAXIMUM = 255;
  private static final String SUBNET_FORMAT = "172.20.%d.0/24";
  private static final AtomicInteger THIRD_OCTET = new AtomicInteger(0);

  private SubnetAddresses addresses;

  public Network createContainerNetwork() {

    for (int attempt = 0; attempt < MAXIMUM_ATTEMPTS; attempt++) {

      final String subnet = getNextSubnetAndIncrement();

      try {
        final Network network = createDockerNetwork(subnet);
        addresses = new SubnetAddresses(subnetAddressFormat(subnet));
        return network;
      } catch (final UndeclaredThrowableException e) {
        // Try creating with the next subnet
      }
    }

    throw new IllegalStateException(
        String.format("Failed to create a Docker network within %s attempts", MAXIMUM_ATTEMPTS));
  }

  public String getAddressAndIncrement() {
    return addresses.getAddressAndIncrement();
  }

  @VisibleForTesting
  String nextSubnet() {
    return String.format(SUBNET_FORMAT, THIRD_OCTET.get());
  }

  private String getNextSubnetAndIncrement() {
    return String.format(SUBNET_FORMAT, consumeNextThirdOctet());
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
  private Network createDockerNetwork(final String subnet) {
    final Network network =
        Network.builder()
            .createNetworkCmdModifier(
                modifier ->
                    modifier.withIpam(new Ipam().withConfig(new Config().withSubnet(subnet))))
            .build();

    checkState(network.getId() != null);
    checkState(!network.getId().isBlank());

    return network;
  }
}
