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

import static tech.pegasys.peeps.privacy.PrivateTransactionManagerType.TESSERA;

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.PrivacyManagerConfiguration;
import tech.pegasys.peeps.SignerConfiguration;
import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.model.Hash;

import java.util.List;

import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.junit.jupiter.api.Test;

public class TesseraPrivacyContractDeploymentTest extends NetworkTest {

  private Web3Provider nodeAlpha;
  private final SignerConfiguration signer = SignerConfiguration.ALPHA;
  private final PrivacyManagerConfiguration privacyManagerAlpha = PrivacyManagerConfiguration.ALPHA;

  @Override
  protected void setUpNetwork(final Network network) {
    network.addPrivacyManager(
        privacyManagerAlpha.id(), List.of(privacyManagerAlpha.keyPair()), TESSERA);
    nodeAlpha =
        network.addNode(
            "alpha",
            KeyPair.random(),
            privacyManagerAlpha.id(),
            privacyManagerAlpha.keyPair().getPublicKey());
    network.addSigner(signer.id(), signer.resources(), nodeAlpha);
  }

  @Test
  public void deploymentMustSucceed() {
    final Hash pmt =
        execute(signer)
            .deployContractToPrivacyGroup(SimpleStorage.BINARY, privacyManagerAlpha.address());

    verifyOn(nodeAlpha).successfulTransactionReceipt(pmt);
  }
}
