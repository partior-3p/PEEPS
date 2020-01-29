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

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.network.subnet.Subnet;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Network;

public class SubnetTest {

  private List<Subnet> cleanUp = new ArrayList<>();

  @BeforeEach
  public void setUp() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDown));
    cleanUp.clear();
  }

  @AfterEach
  public void tearDown() {
    cleanUp.parallelStream().forEach(Subnet::close);
  }

  @Test
  public void canCreateNetwork() {
    final Network alpha = createNetwork();

    assertValidNetwork(alpha);
  }

  @Test
  public void canCreateNetworkWhenFirstSubnetUnavailable() {
    final Network alpha = createNetwork();
    assertValidNetwork(alpha);

    final Network beta = createNetwork();
    assertValidNetwork(beta);

    assertThat(alpha.getId()).isNotEqualTo(beta.getId());
  }

  @Test
  public void canCreateConcurrentNetworks() {
    final Network alpha = createNetwork();
    final Network beta = createNetwork();

    assertThat(alpha).isNotEqualTo(beta);
    assertThat(alpha.getId()).isNotEqualTo(beta.getId());
  }

  private void assertValidNetwork(final Network network) {
    assertThat(network).isNotNull();
    assertThat(network.getId()).isNotBlank();
  }

  private Network createNetwork() {
    final Subnet subnet = new Subnet();
    cleanUp.add(subnet);
    return subnet.network();
  }
}
