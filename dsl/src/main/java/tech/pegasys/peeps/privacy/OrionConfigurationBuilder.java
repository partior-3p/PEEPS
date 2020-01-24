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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import tech.pegasys.peeps.privacy.model.PrivacyKeyPair;
import tech.pegasys.peeps.privacy.model.PrivacyPrivateKeyResource;
import tech.pegasys.peeps.privacy.model.PrivacyPublicKeyResource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.Vertx;
import org.testcontainers.containers.Network;

public class OrionConfigurationBuilder {

  private List<PrivacyPrivateKeyResource> privKeys;
  private List<PrivacyPublicKeyResource> pubKeys;
  private List<String> bootnodeUrls;
  private Path fileSystemConfigFile;

  // TODO these into their own builder, not node related but test container related
  private Network containerNetwork;
  private String ipAddress;
  private Vertx vertx;

  public OrionConfigurationBuilder withKeyPairs(final PrivacyKeyPair... keyPairs) {
    this.pubKeys = new ArrayList<>();
    this.privKeys = new ArrayList<>();

    for (final PrivacyKeyPair keyPair : keyPairs) {
      pubKeys.add(keyPair.getPublicKey());
      privKeys.add(keyPair.getPrivateKey());
    }

    return this;
  }

  public OrionConfigurationBuilder withBootnodeUrls(final List<String> bootnodeUrls) {
    this.bootnodeUrls = bootnodeUrls;
    return this;
  }

  public OrionConfigurationBuilder withContainerNetwork(final Network containerNetwork) {
    this.containerNetwork = containerNetwork;
    return this;
  }

  public OrionConfigurationBuilder withIpAddress(final String networkIpAddress) {
    this.ipAddress = networkIpAddress;
    return this;
  }

  public OrionConfigurationBuilder withFileSystemConfigurationFile(
      final Path fileSystemConfigFile) {
    this.fileSystemConfigFile = fileSystemConfigFile;
    return this;
  }

  public OrionConfigurationBuilder withVertx(final Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  public OrionConfiguration build() {
    checkNotNull(privKeys, "Private keys are mandatory");
    checkArgument(privKeys.size() > 0, "At least one private key is required");
    checkNotNull(pubKeys, "Public keys are mandatory");
    checkArgument(pubKeys.size() > 0, "At least one public key is required");
    checkNotNull(fileSystemConfigFile, "A file system configuration file path is mandatory");
    checkNotNull(containerNetwork, "Container network Address is mandatory");
    checkNotNull(vertx, "A Vertx instance is mandatory");
    checkNotNull(ipAddress, "Container IP Address is mandatory");

    return new OrionConfiguration(
        privKeys, pubKeys, bootnodeUrls, ipAddress, containerNetwork, vertx, fileSystemConfigFile);
  }
}
