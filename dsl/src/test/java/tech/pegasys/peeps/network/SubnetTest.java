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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Network;

public class SubnetTest {

  private final List<Network> cleanUp = new ArrayList<>();

  @BeforeEach
  public void setUp() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDown));
    cleanUp.clear();
  }

  @AfterEach
  public void tearDown() {
    cleanUp.stream().forEach(network -> network.close());
  }

  @Test
  public void canCreateNetwork() {
    final Network network = create();

    assertThat(network).isNotNull();
    assertThat(network.getId()).isNotBlank();
  }

  @Test
  public void canCreateNetworkWhenFirstSubnetUnavailable() {
    createDockerNetwork(new Subnet().nextSubnet());

    final Network network = create();

    assertThat(network).isNotNull();
    assertThat(network.getId()).isNotBlank();
  }

  @Test
  public void canCreateConcurrentNetworks() {
    final Network networkA = create();
    final Network networkB = create();

    assertThat(networkA).isNotEqualTo(networkB);
    assertThat(networkA.getId()).isNotEqualTo(networkB.getId());
  }

  private Network create() {
    final Network network = new Subnet().createContainerNetwork();
    cleanUp.add(network);
    return network;
  }

  private void createDockerNetwork(final String subnet) {
    final Network network =
        Network.builder()
            .createNetworkCmdModifier(
                modifier ->
                    modifier.withIpam(new Ipam().withConfig(new Config().withSubnet(subnet))))
            .build();

    checkState(network.getId() != null);
    checkState(!network.getId().isBlank());
  }
}
