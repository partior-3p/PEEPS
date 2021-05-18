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

import tech.pegasys.peeps.network.subnet.SubnetAddress;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.signer.model.SignerKeyFileResource;
import tech.pegasys.peeps.signer.model.SignerPasswordFileResource;

import io.vertx.core.Vertx;
import org.testcontainers.containers.Network;

public class EthSignerConfiguration {

  private final long chainId;
  private final Web3Provider downstream;

  // TODO move these out, they are not related to the node, but test container setups
  private final Network containerNetwork;
  private final SubnetAddress ipAddress;
  private final Vertx vertx;

  // TODO move these file specific ones out into their own config, encapsulate (i.e refactor
  // EthSigner)
  private final SignerKeyFileResource keyFile;
  private final SignerPasswordFileResource passwordFile;

  public EthSignerConfiguration(
      final long chainId,
      final Web3Provider downstream,
      final Network containerNetwork,
      final SubnetAddress ipAddress,
      final Vertx vertx,
      final SignerKeyFileResource keyFile,
      final SignerPasswordFileResource passwordFile) {
    this.chainId = chainId;
    this.downstream = downstream;
    this.containerNetwork = containerNetwork;
    this.ipAddress = ipAddress;
    this.vertx = vertx;
    this.keyFile = keyFile;
    this.passwordFile = passwordFile;
  }

  public Network getContainerNetwork() {
    return containerNetwork;
  }

  public SubnetAddress getIpAddress() {
    return ipAddress;
  }

  public Vertx getVertx() {
    return vertx;
  }

  public long getChainId() {
    return chainId;
  }

  public Web3Provider getDownstream() {
    return downstream;
  }

  public SignerKeyFileResource getKeyFile() {
    return keyFile;
  }

  public SignerPasswordFileResource getPasswordFile() {
    return passwordFile;
  }
}
