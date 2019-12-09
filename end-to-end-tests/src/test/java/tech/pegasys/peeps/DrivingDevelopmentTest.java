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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// TODO rename & move after
public class DrivingDevelopmentTest {

  private final Network network = new Network();

  @BeforeEach
  public void startUp() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDown));
  }

  @AfterEach
  public void tearDown() {
    network.close();
  }

  @Test
  public void a() {

    network.start();

    // TODO create privacy group between OrionA and OrionB

    // TODO send unsigned transaction to privacy group using EhSigner of node A,, store receipt hash
    final String receiptHash = network.getSignerA().deployContract(SimpleStorage.BINARY);

    // TODO get transaction receipt for private transaction from node B
    final String receipt = network.getSignerB().getTransactionReceipt(receiptHash);

    // TODO verify receipt is valid, contains a contract address
    assertThat(receipt).isNull();

    // TODO verify the state of the Orions & state of each Besu - side effects
  }
}
