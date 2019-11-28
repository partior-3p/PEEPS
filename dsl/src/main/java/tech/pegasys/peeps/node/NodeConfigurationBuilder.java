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

import io.vertx.core.Vertx;
import org.testcontainers.containers.Network;

public class NodeConfigurationBuilder {

  // TODO move these into the test
  private static final String DEFAULT_GENESIS_FILE = "genesis/eth_hash_4004.json";

  // TODO enclave key.priv to be passed (without default)
  private static final String DEFAULT_ENCLAVE_PUBLIC_KEY_FILE = "node/enclave_key.pub";

  private String genesisFile;
  private String enclaveKeyFile;
  private String cors;
  private String ipAddress;
  private String nodePrivateKeyFile;
  private String bootnodeEnodeAddress;

  // TODO these into their own builder, not node related but test container related
  private Network containerNetwork;
  private Vertx vertx;

  public NodeConfigurationBuilder() {
    this.genesisFile = DEFAULT_GENESIS_FILE;
    this.enclaveKeyFile = DEFAULT_ENCLAVE_PUBLIC_KEY_FILE;
  }

  public NodeConfigurationBuilder withGenesisFile(final String genesisFile) {
    this.genesisFile = genesisFile;
    return this;
  }

  public NodeConfigurationBuilder withCors(final String cors) {
    this.cors = cors;
    return this;
  }

  public NodeConfigurationBuilder withContainerNetwork(final Network containerNetwork) {
    this.containerNetwork = containerNetwork;
    return this;
  }

  public NodeConfigurationBuilder withIpAddress(final String ipAddress) {
    this.ipAddress = ipAddress;
    return this;
  }

  public NodeConfigurationBuilder withBootnodeEnodeAddress(final String bootnodeEnodeAddress) {
    this.bootnodeEnodeAddress = bootnodeEnodeAddress;
    return this;
  }

  public NodeConfigurationBuilder withNodePrivateKeyFile(final String nodePrivateKeyFile) {
    this.nodePrivateKeyFile = nodePrivateKeyFile;
    return this;
  }

  public NodeConfigurationBuilder withVertx(final Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  public NodeConfiguration build() {
    // TODO assert mandatory parameters are not null

    return new NodeConfiguration(
        genesisFile,
        enclaveKeyFile,
        cors,
        containerNetwork,
        vertx,
        ipAddress,
        nodePrivateKeyFile,
        bootnodeEnodeAddress);
  }
}
