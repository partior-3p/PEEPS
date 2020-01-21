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

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.NodeKey;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.signer.SignerWallet;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;

public class PrivacyContracDeploymentTest extends NetworkTest {

  private final NodeKey nodeAlpha = NodeKey.ALPHA;
  private final SignerWallet signerAlpha = SignerWallet.ALPHA;
  private final OrionKeyPair privacyManagerAlpha = OrionKeyPair.ALPHA;
  private final OrionKeyPair privacyManagerBeta = OrionKeyPair.BETA;

  @Override
  protected void setUpNetwork(final Network network) {
    network.addPrivacyManager(privacyManagerAlpha);
    network.addPrivacyManager(privacyManagerBeta);
    network.addNode(nodeAlpha, privacyManagerAlpha);
    network.addNode(NodeKey.BETA, privacyManagerBeta);
    network.addSigner(SignerWallet.ALPHA, nodeAlpha);
  }

  @Test
  public void deploymentMustSucceed() throws DecoderException {

    final Hash pmt =
        execute(signerAlpha)
            .deployContractToPrivacyGroup(
                signerAlpha.address(),
                SimpleStorage.BINARY,
                privacyManagerAlpha,
                privacyManagerBeta);

    await().consensusOnTransactionReciept(pmt);

    verify(nodeAlpha).successfulTransactionReceipt(pmt);
    verify().consensusOnTransaction(pmt);
    verify().consensusOnPrivacyTransactionReceipt(pmt);
    verify()
        .privacyGroup(privacyManagerAlpha, privacyManagerBeta)
        .consensusOnPrivacyPayload(execute(nodeAlpha).getTransactionByHash(pmt));
  }
}
