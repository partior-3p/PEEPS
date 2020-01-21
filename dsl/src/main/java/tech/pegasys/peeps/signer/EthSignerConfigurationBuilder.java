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
package tech.pegasys.peeps.signer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import tech.pegasys.peeps.node.Besu;

import io.vertx.core.Vertx;
import org.testcontainers.containers.Network;

public class EthSignerConfigurationBuilder {

  private long chainId;
  private Besu downstream;

  // TODO these into their own builder, not node related but test container related
  private Network containerNetwork;
  private String ipAddress;
  private Vertx vertx;

  // TOFO move these file specific ones out into their own config, encapsulate (i.e refactor
  // EthSigner)
  private String keyFile;
  private String passwordFile;

  public EthSignerConfigurationBuilder withContainerNetwork(final Network containerNetwork) {
    this.containerNetwork = containerNetwork;
    return this;
  }

  public EthSignerConfigurationBuilder withVertx(final Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  public EthSignerConfigurationBuilder withIpAddress(final String networkIpAddress) {
    this.ipAddress = networkIpAddress;
    return this;
  }

  public EthSignerConfigurationBuilder withChainId(final long chainId) {
    this.chainId = chainId;
    return this;
  }

  public EthSignerConfigurationBuilder withDownstream(final Besu downstream) {
    this.downstream = downstream;
    return this;
  }

  public EthSignerConfigurationBuilder witWallet(final SignerWallet wallet) {
    this.keyFile = wallet.keyResource();
    this.passwordFile = wallet.passwordResource();
    return this;
  }

  public EthSignerConfiguration build() {
    checkArgument(chainId > 0, "Chain ID must be set as larger than zero");
    checkNotNull(downstream, "Downstream node mandatory");
    checkNotNull(vertx, "A Vertx instance is mandatory");
    checkNotNull(ipAddress, "Container IP Address is mandatory");
    checkNotNull(containerNetwork, "Container network is mandatory");
    checkNotNull(keyFile, "The key file is mandatory");
    checkNotNull(passwordFile, "The password file is mandatory");

    return new EthSignerConfiguration(
        chainId, downstream, containerNetwork, ipAddress, vertx, keyFile, passwordFile);
  }
}
