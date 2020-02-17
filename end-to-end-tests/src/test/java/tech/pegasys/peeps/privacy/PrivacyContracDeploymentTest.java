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
import tech.pegasys.peeps.NodeConfiguration;
import tech.pegasys.peeps.PrivacyManagerConfiguration;
import tech.pegasys.peeps.SignerConfiguration;
import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.privacy.model.PrivacyGroup;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;

public class PrivacyContracDeploymentTest extends NetworkTest {

  private final NodeConfiguration nodeAlpha = NodeConfiguration.ALPHA;
  private final SignerConfiguration signer = SignerConfiguration.ALPHA;
  private final PrivacyManagerConfiguration privacyManagerAlpha = PrivacyManagerConfiguration.ALPHA;
  private final PrivacyManagerConfiguration privacyManagerBeta = PrivacyManagerConfiguration.BETA;

  @Override
  protected void setUpNetwork(final Network network) {
    network.addPrivacyManager(privacyManagerAlpha.id(), privacyManagerAlpha.keyPair());
    network.addPrivacyManager(privacyManagerBeta.id(), privacyManagerBeta.keyPair());
    network.addNode(
        nodeAlpha.id(),
        nodeAlpha.keys(),
        privacyManagerAlpha.id(),
        privacyManagerAlpha.keyPair().getPublicKey());
    network.addNode(
        NodeConfiguration.BETA.id(),
        NodeConfiguration.BETA.keys(),
        privacyManagerBeta.id(),
        privacyManagerBeta.keyPair().getPublicKey());
    network.addSigner(signer.id(), signer.resources(), nodeAlpha.id());
  }

  @Test
  public void deploymentMustSucceed() throws DecoderException {
    final PrivacyGroup group = new PrivacyGroup(privacyManagerAlpha.id(), privacyManagerBeta.id());

    final Hash pmt =
        execute(signer)
            .deployContractToPrivacyGroup(
                SimpleStorage.BINARY, privacyManagerAlpha.address(), privacyManagerBeta.address());

    await().consensusOnTransactionReceipt(pmt);

    verifyOn(nodeAlpha).successfulTransactionReceipt(pmt);
    verify().consensusOnTransaction(pmt);
    verify().consensusOnPrivacyTransactionReceipt(pmt);
    verify()
        .privacyGroup(group)
        .consensusOnPrivacyPayload(execute(nodeAlpha).getTransactionByHash(pmt));
  }
}
