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
import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.NodeConfigurationBuilder;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

// TODO rename & move after
public class DrivingDevelopmentTest {

  // TODO start node A
  static final Besu besuA = new Besu(new NodeConfigurationBuilder().build());
  static final EthSigner signerA = new EthSigner();
  static final Orion orionA = new Orion();

  // TODO start node B
  static final Besu besuB = new Besu(new NodeConfigurationBuilder().build());
  static final EthSigner signerB = new EthSigner();
  static final Orion orionB = new Orion();

  // TODO ensure clean up even on crash
  // Runtime.getRuntime().addShutdownHook(new Thread(AcceptanceTestBase::tearDownBase));

  @AfterAll
  public static void tearDown() {
    // TODO wrap up elsewhere
    besuA.stop();
    besuB.stop();
  }

  @Test
  public void a() {

    // TODO network config / port discovery & bootnodes
    besuA.start();
    besuB.start();

    // TODO assert connectivity (peeps)
    besuA.awaitConnectivity(besuB);
    besuB.awaitConnectivity(besuA);
    orionA.awaitConnectivity(orionB);
    orionB.awaitConnectivity(orionA);

    // TODO send unsigned transaction to EhSigner of node A, store receipt hash
    final String receiptHash = signerA.deployContract(SimpleStorage.BINARY);

    // TODO get transaction receipt for private transaction from node B
    final String receipt = signerB.getTransactionReceipt(receiptHash);

    // TODO verify receipt is valid, contains a contract address
    assertThat(receipt).isNull();

    // TODO verify the state of the Orions & state of each Besu - side effects
  }
}
