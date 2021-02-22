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

import tech.pegasys.peeps.network.subnet.SubnetAddress;
import tech.pegasys.peeps.privacy.model.PrivacyPrivateKeyResource;
import tech.pegasys.peeps.privacy.model.PrivacyPublicKeyResource;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import io.vertx.core.Vertx;
import org.testcontainers.containers.Network;

public class PrivateTransactionManagerConfiguration {

  private final List<PrivacyPrivateKeyResource> privKeys;
  private final List<PrivacyPublicKeyResource> pubKeys;
  private final List<String> bootnodeUrls;
  private final Path fileSystemConfigurationFile;

  // TODO move these out, they are not related to the node, but test container setups
  private final Network containerNetwork;
  private final SubnetAddress ipAddress;
  private final Vertx vertx;

  public PrivateTransactionManagerConfiguration(
      final List<PrivacyPrivateKeyResource> privKeys,
      final List<PrivacyPublicKeyResource> pubKeys,
      final List<String> bootnodeUrls,
      final SubnetAddress ipAddress,
      final Network containerNetwork,
      final Vertx vertx,
      final Path fileSystemConfigurationFile) {
    this.privKeys = privKeys;
    this.pubKeys = pubKeys;
    this.bootnodeUrls = bootnodeUrls;
    this.ipAddress = ipAddress;
    this.containerNetwork = containerNetwork;
    this.vertx = vertx;
    this.fileSystemConfigurationFile = fileSystemConfigurationFile;
  }

  public Path getFileSystemConfigurationFile() {
    return fileSystemConfigurationFile;
  }

  public SubnetAddress getIpAddress() {
    return ipAddress;
  }

  public Network getContainerNetwork() {
    return containerNetwork;
  }

  public List<PrivacyPrivateKeyResource> getPrivateKeys() {
    return privKeys;
  }

  public List<PrivacyPublicKeyResource> getPublicKeys() {
    return pubKeys;
  }

  public Optional<List<String>> getBootnodeUrls() {
    return Optional.ofNullable(bootnodeUrls);
  }

  public Vertx getVertx() {
    return vertx;
  }
}
