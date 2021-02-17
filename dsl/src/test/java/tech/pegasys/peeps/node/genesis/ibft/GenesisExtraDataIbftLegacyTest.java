/*
 * Copyright 2021 ConsenSys AG.
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
package tech.pegasys.peeps.node.genesis.ibft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.peeps.node.Web3Provider;

import java.security.Security;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
class GenesisExtraDataIbftLegacyTest {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Test
  public void specifiedValidatorsProduceFixedExtraDataString() {

    final String expectedExtraData =
        "0x0000000000000000000000000000000000000000000000000000000000000000f882f87e949f66f8a0f0a6537e4a36aa1799673ea7ae97a1669464d9be4177f418bcf4e56adad85f33e3a64efe2294f4bbfd32c11c9d63e9b4c77bb225810f840342df94a7f25969fb6f3d5ac09a88862c90b5ff664557a7944f6c4f757c480624bb0baf37d8f1879769a926df945aa7791b7100d5d3160a978e32e6ebb879f70af680c0";

    final List<String> validatorPublicKeys =
        List.of(
            "b81c7d5207f96ee4239ba7597798f1e1ec6db494ead9aac0a55836b75bf8580cde01c577e406de12c057de23411e5a9fc723ad6dcd2e75262cf7d04c5613b9c7",
            "c6cd164524a7f80bd02f7b3d7f61aa6f052e1be36ffe5655944281fb5999fe59a3551d52139cf7370111001429d965e1617a4574390e5eab8232dd15bcdfadd0",
            "4daa1048a351545279fb851bd123b2f396592733a56e2fdda399c1892f0debab0e338e88d4e62e6445a94df7dc62776c55bfa0ecccfa2539e722496e8190ee65",
            "c1599da6b1f64e24f872e8bdd1835750446b38cdcc66dd93ac4d0566409b39b188dd64dd4529eaa17ce4acf9c3286047c5736be44095c0c5ac17998e49cda986",
            "e3c2670260d8fd8a7d7892a8244e88a59b3ea757443084d782c290350da71c9d3dec0832d8478709b589bdd94a69c29dbc3932a1f7cb54f1b9cf23a469259e6a",
            "5ff0d5c853281a0ca02fafe8f11164fef42bfd3c74199151885094792f37c7cd3a42b76e4361545e024e0479c9d42c1cdcb4952779be9b147fb9e674e22117de");

    final Web3Provider[] mockProviders = new Web3Provider[validatorPublicKeys.size()];
    for (int i = 0; i < validatorPublicKeys.size(); i++) {
      mockProviders[i] = mock(Web3Provider.class);
      when(mockProviders[i].nodePublicKey()).thenReturn(validatorPublicKeys.get(i));
    }

    final Bytes extraDataBytes = GenesisExtraDataIbftLegacy.encode(mockProviders);
    final String actualExtraData = extraDataBytes.toHexString();
    assertThat(expectedExtraData).isEqualTo(actualExtraData);
  }
}
