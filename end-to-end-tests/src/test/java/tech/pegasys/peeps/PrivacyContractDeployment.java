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
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// TODO extract common network setup into superclass
public class PrivacyContractDeployment {

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
  public void a() {
    final String receiptHash =
        network
            .getSignerA()
            .deployContractToPrivacyGroup(
                SimpleStorage.BINARY, network.getOrionA(), network.getOrionB());

    final Optional<PrivacyTransactionReceipt> receiptNodeA =
        network.getNodeA().getPrivacyTransactionReceipt(receiptHash);

    final Optional<PrivacyTransactionReceipt> receiptNodeB =
        network.getNodeB().getPrivacyTransactionReceipt(receiptHash);

    // TODO verify receipt is valid, contains a contract address
    assertThat(receiptNodeA).isNotNull();
    assertThat(receiptNodeB).isNotNull();

    assertThat(receiptNodeA).isPresent();
    assertThat(receiptNodeB).isPresent();

    assertThat(receiptNodeA.get().getContractAddress()).isNotEmpty();
    assertThat(receiptNodeA.get().getStatus()).isEqualTo("0x1");

    assertThat(receiptNodeA.get()).usingRecursiveComparison().isEqualTo(receiptNodeB.get());

    // TODO verify the state of the Orions & state of each Besu - side effects
  }
}
