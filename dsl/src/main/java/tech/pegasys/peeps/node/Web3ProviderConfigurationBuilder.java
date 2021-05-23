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

import tech.pegasys.peeps.network.subnet.SubnetAddress;
import tech.pegasys.peeps.node.genesis.GenesisFile;
import tech.pegasys.peeps.privacy.PrivateTransactionManager;
import tech.pegasys.peeps.signer.SignerConfiguration;

import io.vertx.core.Vertx;
import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.testcontainers.containers.Network;

public class Web3ProviderConfigurationBuilder {

  // TODO move these into the test
  private static final String DEFAULT_PRIVACY_MARKER_SIGNER_PRIVATE_KEY_FILE =
      "node/keys/pmt_signing.priv";

  private GenesisFile genesisFile;
  private String identity;

  // TODO better typing then String
  private String privacyManagerPublicKeyFile;
  private String privacyTransactionManagerUrl;
  private String cors;
  private String bootnodeEnodeAddress;
  private KeyPair nodeKeys;
  private SignerConfiguration wallet;
  private final String privacyMarkerSigningPrivateKeyFile;
  private StaticNodesFile staticNodesFile;

  // TODO these into their own builder, not node related but test container related
  private Network containerNetwork;
  private SubnetAddress ipAddress;
  private Vertx vertx;

  public Web3ProviderConfigurationBuilder() {
    this.privacyMarkerSigningPrivateKeyFile = DEFAULT_PRIVACY_MARKER_SIGNER_PRIVATE_KEY_FILE;
  }

  public Web3ProviderConfigurationBuilder withGenesisFile(final GenesisFile genesisFile) {
    this.genesisFile = genesisFile;
    return this;
  }

  public Web3ProviderConfigurationBuilder withCors(final String cors) {
    this.cors = cors;
    return this;
  }

  public Web3ProviderConfigurationBuilder withContainerNetwork(final Network containerNetwork) {
    this.containerNetwork = containerNetwork;
    return this;
  }

  public Web3ProviderConfigurationBuilder withIpAddress(final SubnetAddress ipAddress) {
    this.ipAddress = ipAddress;
    return this;
  }

  public Web3ProviderConfigurationBuilder withBootnodeEnodeAddress(
      final String bootnodeEnodeAddress) {
    this.bootnodeEnodeAddress = bootnodeEnodeAddress;
    return this;
  }

  public Web3ProviderConfigurationBuilder withIdentity(final String identity) {
    this.identity = identity;
    return this;
  }

  public Web3ProviderConfigurationBuilder withVertx(final Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  public Web3ProviderConfigurationBuilder withPrivacyUrl(
      final PrivateTransactionManager privacyTransactionManager) {
    this.privacyTransactionManagerUrl = privacyTransactionManager.getNetworkRpcAddress();
    return this;
  }

  public Web3ProviderConfigurationBuilder withPrivacyManagerPublicKey(
      final String privacyManagerPublicKeyFile) {
    this.privacyManagerPublicKeyFile = privacyManagerPublicKeyFile;
    return this;
  }

  public Web3ProviderConfigurationBuilder withNodeKey(final KeyPair nodeKeys) {
    this.nodeKeys = nodeKeys;
    return this;
  }

  public Web3ProviderConfigurationBuilder withWallet(final SignerConfiguration wallet) {
    this.wallet = wallet;
    return this;
  }

  public Web3ProviderConfigurationBuilder withStaticNodesFile(
      final StaticNodesFile staticNodesFile) {
    this.staticNodesFile = staticNodesFile;
    return this;
  }

  public Web3ProviderConfiguration build() {
    checkNotNull(genesisFile, "A genesis file path is mandatory");
    checkNotNull(identity, "An identity is mandatory");
    checkNotNull(vertx, "A Vertx instance is mandatory");
    checkNotNull(ipAddress, "Container IP address is mandatory");
    checkNotNull(containerNetwork, "Container network is mandatory");
    checkNotNull(nodeKeys, "Node Key is mandatory");
    checkNotNull(staticNodesFile, "Static nodes file path is mandatory");

    return new Web3ProviderConfiguration(
        genesisFile.getGenesisFile(),
        privacyManagerPublicKeyFile,
        privacyTransactionManagerUrl,
        privacyMarkerSigningPrivateKeyFile,
        cors,
        containerNetwork,
        vertx,
        ipAddress,
        identity,
        nodeKeys,
        bootnodeEnodeAddress,
        wallet,
        staticNodesFile.getStaticNodesFile());
  }
}
