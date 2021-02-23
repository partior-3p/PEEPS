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

import tech.pegasys.peeps.network.subnet.SubnetAddress;
import tech.pegasys.peeps.signer.SignerConfiguration;

import java.nio.file.Path;
import java.util.Optional;

import io.vertx.core.Vertx;
import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.testcontainers.containers.Network;

public class Web3ProviderConfiguration {

  private final Path genesisFile;
  private final String enclavePublicKeyResource;
  private final String cors;
  private final String identity;
  private final String bootnodeEnodeAddress;
  private final Optional<SignerConfiguration> wallet;
  private final String privacyUrl;
  private final String privacyMarkerSigningPrivateKeyFile;
  private final KeyPair nodeKeys;

  // TODO move these out, they are not related to the node, but test container setups
  private final Network containerNetwork;
  private final SubnetAddress ipAddress;
  private final Vertx vertx;

  public Web3ProviderConfiguration(
      final Path genesisFile,
      final String privacyManagerPublicKeyResource,
      final String privacyUrl,
      final String privacyMarkerSigningPrivateKeyFile,
      final String cors,
      final Network containerNetwork,
      final Vertx vertx,
      final SubnetAddress ipAddress,
      final String identity,
      final KeyPair nodeKeys,
      final String bootnodeEnodeAddress,
      final SignerConfiguration wallet) {
    this.genesisFile = genesisFile;
    this.enclavePublicKeyResource = privacyManagerPublicKeyResource;
    this.privacyMarkerSigningPrivateKeyFile = privacyMarkerSigningPrivateKeyFile;
    this.privacyUrl = privacyUrl;
    this.cors = cors;
    this.containerNetwork = containerNetwork;
    this.vertx = vertx;
    this.ipAddress = ipAddress;
    this.identity = identity;
    this.nodeKeys = nodeKeys;
    this.bootnodeEnodeAddress = bootnodeEnodeAddress;
    this.wallet = Optional.ofNullable(wallet);
  }

  public Path getGenesisFile() {
    return genesisFile;
  }

  public String getPrivacyPublicKeyResource() {
    return enclavePublicKeyResource;
  }

  public Optional<String> getCors() {
    return Optional.ofNullable(cors);
  }

  public Network getContainerNetwork() {
    return containerNetwork;
  }

  public SubnetAddress getIpAddress() {
    return ipAddress;
  }

  public String getIdentity() {
    return identity;
  }

  public Optional<String> getBootnodeEnodeAddress() {
    return Optional.ofNullable(bootnodeEnodeAddress);
  }

  public Vertx getVertx() {
    return vertx;
  }

  public KeyPair getNodeKeys() {
    return nodeKeys;
  }

  // TODO maybe split out privacy
  public boolean isPrivacyEnabled() {
    return privacyUrl != null;
  }

  public Optional<String> getPrivacyUrl() {
    return Optional.ofNullable(privacyUrl);
  }

  public Optional<String> getPrivacyMarkerSigningPrivateKeyFile() {
    return Optional.ofNullable(privacyMarkerSigningPrivateKeyFile);
  }

  public Optional<SignerConfiguration> getWallet() {
    return wallet;
  }
}
