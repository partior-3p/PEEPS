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

import tech.pegasys.peeps.node.genesis.BesuGenesisFile;
import tech.pegasys.peeps.node.model.NodeIdentifier;
import tech.pegasys.peeps.node.model.NodeKey;
import tech.pegasys.peeps.privacy.Orion;

import io.vertx.core.Vertx;
import org.testcontainers.containers.Network;

public class BesuConfigurationBuilder {

  // TODO move these into the test
  private static final String DEFAULT_PRIVACY_MARKER_SIGNER_PRIVATE_KEY_FILE =
      "node/keys/pmt_signing.priv";

  private BesuGenesisFile genesisFile;
  private NodeIdentifier frameworkIdentity;

  // TODO better typing then String
  private String privacyManagerPublicKeyFile;
  private String privacyMarkerSigningPrivateKeyFile;
  private String privacyTransactionManagerUrl;
  private String cors;
  private String bootnodeEnodeAddress;
  private NodeKey ethereumIdentity;

  // TODO these into their own builder, not node related but test container related
  private Network containerNetwork;
  private String ipAddress;
  private Vertx vertx;

  public BesuConfigurationBuilder() {
    this.privacyMarkerSigningPrivateKeyFile = DEFAULT_PRIVACY_MARKER_SIGNER_PRIVATE_KEY_FILE;
  }

  public BesuConfigurationBuilder withGenesisFile(final BesuGenesisFile genesisFile) {
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

  public BesuConfigurationBuilder withIdentity(final NodeIdentifier identity) {
    this.frameworkIdentity = identity;
    return this;
  }

  public BesuConfigurationBuilder withVertx(final Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  public BesuConfigurationBuilder withPrivacyUrl(final Orion privacyTransactionManager) {
    this.privacyTransactionManagerUrl = privacyTransactionManager.getNetworkRpcAddress();
    return this;
  }

  public BesuConfigurationBuilder withPrivacyManagerPublicKey(
      final String privacyManagerPublicKeyFile) {
    this.privacyManagerPublicKeyFile = privacyManagerPublicKeyFile;
    return this;
  }

  public BesuConfigurationBuilder withNodeKey(final NodeKey ethereumIdentity) {
    this.ethereumIdentity = ethereumIdentity;
    return this;
  }

  public BesuConfiguration build() {
    checkNotNull(genesisFile, "A genesis file path is mandatory");
    checkNotNull(frameworkIdentity, "A NodeKey is mandatory");
    checkNotNull(vertx, "A Vertx instance is mandatory");
    checkNotNull(ipAddress, "Container IP address is mandatory");
    checkNotNull(containerNetwork, "Container network is mandatory");
    checkNotNull(ethereumIdentity, "Ethereum identity is mandatory");
    checkNotNull(
        ethereumIdentity.nodePrivateKeyResource(),
        "Private key resource for the Node Key is mandatory");
    checkNotNull(
        ethereumIdentity.nodePublicKeyResource(),
        "Public key resource for the Node Key is mandatory");

    return new BesuConfiguration(
        genesisFile.getGenesisFile(),
        privacyManagerPublicKeyFile,
        privacyTransactionManagerUrl,
        privacyMarkerSigningPrivateKeyFile,
        cors,
        containerNetwork,
        vertx,
        ipAddress,
        frameworkIdentity,
        ethereumIdentity,
        bootnodeEnodeAddress);
  }
}
