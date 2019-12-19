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

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.node.rpc.priv.PrivacyTransactionReceipt;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// TODO extract common network setup into superclass
public class PrivacyNetworkContracDeploymentTest {

  @TempDir Path configurationDirectory;

  private Network network;

  @BeforeEach
  public void startUp() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDown));
    network = new Network(configurationDirectory);
    network.start();
  }

  @AfterEach
  public void tearDown() {
    network.close();
  }

  @Test
  public void deploymentMustSucceed() {
    final String receiptHash =
        network
            .getSignerA()
            .deployContractToPrivacyGroup(
                SimpleStorage.BINARY, network.getOrionA(), network.getOrionB());

    final PrivacyTransactionReceipt receiptNodeA =
        network.getNodeA().getPrivacyContractReceipt(receiptHash);
    final PrivacyTransactionReceipt receiptNodeB =
        network.getNodeB().getPrivacyContractReceipt(receiptHash);

    assertThat(receiptNodeA.isSuccess()).isTrue();
    assertThat(receiptNodeA).usingRecursiveComparison().isEqualTo(receiptNodeB);

    // TODO verify the state of the Orions & state of each Besu - side effects
  }
}
