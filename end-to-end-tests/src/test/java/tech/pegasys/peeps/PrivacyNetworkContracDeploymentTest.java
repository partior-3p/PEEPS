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
import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// TODO extract common network setup into superclass
public class PrivacyNetworkContracDeploymentTest {

  @TempDir Path configurationDirectory;

  private Network network;

  @BeforeEach
  public void setUp() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDown));
    network = new Network(configurationDirectory);
    network.start();
  }

  @AfterEach
  public void tearDown() {
    network.close();
  }

  @Test
  public void deploymentMustSucceed() throws DecoderException {

    // TODO no in-line comments - implement clean code!

    final String receiptHash =
        network
            .getSignerA()
            .deployContractToPrivacyGroup(
                SimpleStorage.BINARY, network.getOrionA(), network.getOrionB());

    // Valid transaction receipt for the privacy contract deployment
    network.awaitConsensusOn(receiptHash);
    final TransactionReceipt pmtReceiptNodeA =
        network.getNodeA().getTransactionReceipt(receiptHash);

    // Valid privacy marker transaction
    final String hash = pmtReceiptNodeA.getTransactionHash();
    final Transaction pmtNodeA = network.getNodeA().getTransactionByHash(hash);
    final Transaction pmtNodeB = network.getNodeB().getTransactionByHash(hash);

    assertThat(pmtNodeA.isProcessed()).isTrue();
    assertThat(pmtNodeA).usingRecursiveComparison().isEqualTo(pmtNodeB);
    assertThat(pmtNodeA).isNotNull();

    // Convert from Hex String to Base64 UTF_8 String for Orion
    final byte[] decodedHex = Hex.decodeHex(removeAnyHexPrefix(pmtNodeA.getInput()).toCharArray());
    final byte[] encodedHexB64 = Base64.encodeBase64(decodedHex);
    final String key = new String(encodedHexB64, StandardCharsets.UTF_8);

    // Valid privacy transaction receipt
    final PrivacyTransactionReceipt receiptNodeA =
        network.getNodeA().getPrivacyContractReceipt(receiptHash);
    final PrivacyTransactionReceipt receiptNodeB =
        network.getNodeB().getPrivacyContractReceipt(receiptHash);

    assertThat(receiptNodeA.isSuccess()).isTrue();
    assertThat(receiptNodeA).usingRecursiveComparison().isEqualTo(receiptNodeB);

    // Valid entries in both Orions
    final String payloadOrionA = network.getOrionA().getPayload(key);
    final String payloadOrionB = network.getOrionB().getPayload(key);

    assertThat(payloadOrionA).isNotNull();
    assertThat(payloadOrionA).isEqualTo(payloadOrionB);
  }
}
