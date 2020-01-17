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
package tech.pegasys.peeps.privacy;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.BesuConfigurationBuilder;
import tech.pegasys.peeps.node.GenesisAccounts;
import tech.pegasys.peeps.node.NodeKey;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.SignerWallet;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.tuweni.eth.Address;
import org.junit.jupiter.api.Test;

public class PrivacyContracDeploymentTest extends NetworkTest {

  private Besu nodeAlpha;
  private Orion privacyManagerAlpha;
  private EthSigner signerAlpha;

  private Besu nodeBeta;
  private EthSigner signerBeta;
  private Orion privacyManagerBeta;

  @Override
  protected void setUpNetwork(final Network network) {

    this.privacyManagerAlpha = network.addPrivacyManager(OrionKeyPair.ALPHA);

    // TODO Besu -> Orion
    // TODO Orion can expose it's public keys, choose the first
    this.nodeAlpha =
        network.addNode(
            new BesuConfigurationBuilder()
                .withPrivacyUrl(privacyManagerAlpha)
                .withIdentity(NodeKey.ALPHA)
                .withPrivacyManagerPublicKey(OrionKeyPair.ALPHA.getPublicKey()));

    this.signerAlpha = network.addSigner(SignerWallet.ALPHA, nodeAlpha);

    this.privacyManagerBeta = network.addPrivacyManager(OrionKeyPair.BETA);

    this.nodeBeta =
        network.addNode(
            new BesuConfigurationBuilder()
                .withPrivacyUrl(privacyManagerBeta)
                .withIdentity(NodeKey.BETA)
                .withPrivacyManagerPublicKey(OrionKeyPair.BETA.getPublicKey()));

    this.signerBeta = network.addSigner(SignerWallet.BETA, nodeBeta);
  }

  @Test
  public void deploymentMustSucceed() throws DecoderException {

    // TODO why gamma unlocked in signerAlpha?
    final Address sender = GenesisAccounts.GAMMA.address();
    final Hash pmt =
        signerAlpha
            .rpc()
            .deployContractToPrivacyGroup(
                sender, SimpleStorage.BINARY, privacyManagerAlpha, privacyManagerBeta);

    // TODO no in-line comments - implement clean code!

    await().consensusOnTransactionReciept(pmt);

    // Valid privacy marker transaction
    final TransactionReceipt pmtReceiptNodeA = nodeAlpha.rpc().getTransactionReceipt(pmt);

    assertThat(pmtReceiptNodeA.getTransactionHash()).isEqualTo(pmt);
    assertThat(pmtReceiptNodeA.isSuccess()).isTrue();

    final Transaction pmtNodeA = nodeAlpha.rpc().getTransactionByHash(pmt);
    final Transaction pmtNodeB = nodeBeta.rpc().getTransactionByHash(pmt);

    assertThat(pmtNodeA.isProcessed()).isTrue();
    assertThat(pmtNodeA).usingRecursiveComparison().isEqualTo(pmtNodeB);
    assertThat(pmtNodeA).isNotNull();

    // Convert from Hex String to Base64 UTF_8 String for Orion
    final byte[] decodedHex = Hex.decodeHex(removeAnyHexPrefix(pmtNodeA.getInput()).toCharArray());
    final byte[] encodedHexB64 = Base64.encodeBase64(decodedHex);
    final String key = new String(encodedHexB64, StandardCharsets.UTF_8);

    // Valid privacy transaction receipt
    final PrivacyTransactionReceipt receiptNodeA = nodeAlpha.rpc().getPrivacyContractReceipt(pmt);
    final PrivacyTransactionReceipt receiptNodeB = nodeBeta.rpc().getPrivacyContractReceipt(pmt);

    assertThat(receiptNodeA.isSuccess()).isTrue();
    assertThat(receiptNodeA).usingRecursiveComparison().isEqualTo(receiptNodeB);

    final PrivacyTransactionReceipt receiptNodeC = signerBeta.rpc().getPrivacyContractReceipt(pmt);
    assertThat(receiptNodeA).usingRecursiveComparison().isEqualTo(receiptNodeC);

    // Valid entries in both Orions
    final String payloadOrionA = privacyManagerAlpha.getPayload(key);
    final String payloadOrionB = privacyManagerBeta.getPayload(key);

    assertThat(payloadOrionA).isNotNull();
    assertThat(payloadOrionA).isEqualTo(payloadOrionB);
  }
}
