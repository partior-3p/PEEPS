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

import java.nio.file.Path;
import java.util.Optional;

import io.vertx.core.Vertx;
import org.testcontainers.containers.Network;

public class BesuConfiguration {

  private final Path genesisFile;
  private final String enclavePublicKeyFile;
  private final String cors;
  private final NodeKey identity;
  private final String bootnodeEnodeAddress;
  private final String privacyUrl;
  private final String privacyMarkerSigningPrivateKeyFile;

  // TODO move these out, they are not related to the node, but test container setups
  private final Network containerNetwork;
  private final String ipAddress;
  private final Vertx vertx;

  public BesuConfiguration(
      final Path genesisFile,
      final String privacyManagerPublicKeyFile,
      final String privacyUrl,
      final String privacyMarkerSigningPrivateKeyFile,
      final String cors,
      final Network containerNetwork,
      final Vertx vertx,
      final String ipAddress,
      final NodeKey identity,
      final String bootnodeEnodeAddress) {
    this.genesisFile = genesisFile;
    this.enclavePublicKeyFile = privacyManagerPublicKeyFile;
    this.privacyMarkerSigningPrivateKeyFile = privacyMarkerSigningPrivateKeyFile;
    this.privacyUrl = privacyUrl;
    this.cors = cors;
    this.containerNetwork = containerNetwork;
    this.vertx = vertx;
    this.ipAddress = ipAddress;
    this.identity = identity;
    this.bootnodeEnodeAddress = bootnodeEnodeAddress;
  }

  public Path getGenesisFile() {
    return genesisFile;
  }

  public String getPrivacyPublicKeyFile() {
    return enclavePublicKeyFile;
  }

  public Optional<String> getCors() {
    return Optional.ofNullable(cors);
  }

  public Network getContainerNetwork() {
    return containerNetwork;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public NodeKey getIdentity() {
    return identity;
  }

  public Optional<String> getBootnodeEnodeAddress() {
    return Optional.ofNullable(bootnodeEnodeAddress);
  }

  public Vertx getVertx() {
    return vertx;
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
}
