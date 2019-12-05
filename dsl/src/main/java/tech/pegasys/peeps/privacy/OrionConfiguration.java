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

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import io.vertx.core.Vertx;
import org.testcontainers.containers.Network;

public class OrionConfiguration {

  private final List<String> privKeys;
  private final List<String> pubKeys;
  private final List<String> bootnodeUrls;
  private final String ipAddress;
  private final Path fileSystemConfigurationFile;

  // TODO move these out, they are not related to the node, but test container setups
  private final Network containerNetwork;
  private final Vertx vertx;

  public OrionConfiguration(
      final List<String> privKeys,
      final List<String> pubKeys,
      final List<String> bootnodeUrls,
      final String ipAddress,
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

  public String getFileSystemConfigurationFile() {
    try {
      return fileSystemConfigurationFile.toUri().toURL().getPath();
    } catch (final MalformedURLException e) {
      throw new IllegalStateException(
          "Problem forming a URL from a URI from " + fileSystemConfigurationFile.toUri());
    }
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public Network getContainerNetwork() {
    return containerNetwork;
  }

  public List<String> getPrivateKeys() {
    return privKeys;
  }

  public List<String> getPublicKeys() {
    return pubKeys;
  }

  public Optional<List<String>> getBootnodeUrls() {
    return Optional.ofNullable(bootnodeUrls);
  }

  public Vertx getVertx() {
    return vertx;
  }
}
