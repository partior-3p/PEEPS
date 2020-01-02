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
package tech.pegasys.peeps.node;

import static com.google.common.base.Preconditions.checkNotNull;

import io.vertx.core.Vertx;
import org.testcontainers.containers.Network;

public class BesuConfigurationBuilder {

  // TODO move these into the test
  private static final String DEFAULT_GENESIS_FILE = "node/genesis/eth_hash_4004.json";

  // TODO move these into the test
  private static final String DEFAULT_PRIVACY_MARKER_SIGNER_PRIVATE_KEY_FILE =
      "node/keys/pmt_signing.priv";

  private String genesisFile;
  private String privacyManagerPublicKeyFile;
  private String privacyMarkerSigningPrivateKeyFile;
  private String privacyTransactionManagerUrl;
  private String cors;
  private String nodePrivateKeyFile;
  private String bootnodeEnodeAddress;

  // TODO these into their own builder, not node related but test container related
  private Network containerNetwork;
  private String ipAddress;
  private Vertx vertx;

  public BesuConfigurationBuilder() {
    this.genesisFile = DEFAULT_GENESIS_FILE;
    this.privacyMarkerSigningPrivateKeyFile = DEFAULT_PRIVACY_MARKER_SIGNER_PRIVATE_KEY_FILE;
  }

  public BesuConfigurationBuilder withGenesisFile(final String genesisFile) {
    this.genesisFile = genesisFile;
    return this;
  }

  public BesuConfigurationBuilder withCors(final String cors) {
    this.cors = cors;
    return this;
  }

  public BesuConfigurationBuilder withContainerNetwork(final Network containerNetwork) {
    this.containerNetwork = containerNetwork;
    return this;
  }

  public BesuConfigurationBuilder withIpAddress(final String ipAddress) {
    this.ipAddress = ipAddress;
    return this;
  }

  public BesuConfigurationBuilder withBootnodeEnodeAddress(final String bootnodeEnodeAddress) {
    this.bootnodeEnodeAddress = bootnodeEnodeAddress;
    return this;
  }

  public BesuConfigurationBuilder withNodePrivateKeyFile(final String nodePrivateKeyFile) {
    this.nodePrivateKeyFile = nodePrivateKeyFile;
    return this;
  }

  public BesuConfigurationBuilder withVertx(final Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  public BesuConfigurationBuilder withPrivacyUrl(final String privacyTransactionManagerUrl) {
    this.privacyTransactionManagerUrl = privacyTransactionManagerUrl;
    return this;
  }

  public BesuConfigurationBuilder withPrivacyManagerPublicKey(
      final String privacyManagerPublicKeyFile) {
    this.privacyManagerPublicKeyFile = privacyManagerPublicKeyFile;
    return this;
  }

  public BesuConfiguration build() {
    checkNotNull(genesisFile, "A genesis file path is mandatory");
    checkNotNull(privacyManagerPublicKeyFile, "An privacy manager key file is mandatory");
    checkNotNull(vertx, "A Vertx instance is mandatory");
    checkNotNull(ipAddress, "Container IP address is mandatory");
    checkNotNull(containerNetwork, "Container network is mandatory");

    return new BesuConfiguration(
        genesisFile,
        privacyManagerPublicKeyFile,
        privacyTransactionManagerUrl,
        privacyMarkerSigningPrivateKeyFile,
        cors,
        containerNetwork,
        vertx,
        ipAddress,
        nodePrivateKeyFile,
        bootnodeEnodeAddress);
  }
}
