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

import java.util.Optional;

import org.testcontainers.containers.Network;

public class NodeConfiguration {

  private final String genesisFile;
  private final String enclavePublicKeyFile;
  private final String cors;
  private final Network containerNetwork;
  private final String ipAddress;
  private final String nodePrivateKeyFile;
  private final String bootnodeEnodeAddress;

  public NodeConfiguration(
      final String genesisFile,
      final String enclavePublicKeyFile,
      final String cors,
      final Network containerNetwork,
      final String ipAddress,
      final String nodePrivateKeyFile,
      final String bootnodeEnodeAddress) {
    this.genesisFile = genesisFile;
    this.enclavePublicKeyFile = enclavePublicKeyFile;
    this.cors = cors;
    this.containerNetwork = containerNetwork;
    this.ipAddress = ipAddress;
    this.nodePrivateKeyFile = nodePrivateKeyFile;
    this.bootnodeEnodeAddress = bootnodeEnodeAddress;
  }

  public String getGenesisFile() {
    return genesisFile;
  }

  public String getEnclavePublicKeyFile() {
    return enclavePublicKeyFile;
  }

  public Optional<String> getCors() {
    return Optional.ofNullable(cors);
  }

  public Optional<Network> getContainerNetwork() {
    return Optional.ofNullable(containerNetwork);
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public Optional<String> getNodePrivateKeyFile() {
    return Optional.ofNullable(nodePrivateKeyFile);
  }

  public Optional<String> getBootnodeEnodeAddress() {
    return Optional.ofNullable(bootnodeEnodeAddress);
  }
}
