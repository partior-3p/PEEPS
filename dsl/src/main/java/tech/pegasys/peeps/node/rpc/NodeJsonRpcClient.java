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
package tech.pegasys.peeps.node.rpc;

import tech.pegasys.peeps.json.rpc.JsonRpcClient;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.rpc.admin.ConnectedPeer;
import tech.pegasys.peeps.node.rpc.admin.ConnectedPeersResponse;
import tech.pegasys.peeps.node.rpc.admin.NodeInfo;
import tech.pegasys.peeps.node.rpc.admin.NodeInfoResponse;
import tech.pegasys.peeps.node.rpc.eth.GetTransactionByHashResponse;
import tech.pegasys.peeps.node.rpc.eth.GetTransactionResponse;
import tech.pegasys.peeps.node.rpc.priv.GetPrivateTransactionResponse;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeJsonRpcClient extends JsonRpcClient {

  private static final Logger LOG = LogManager.getLogger();
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

  public NodeJsonRpcClient(final Vertx vertx) {
    super(vertx, DEFAULT_TIMEOUT, LOG);
  }

  public Set<String> getConnectedPeerIds() {
    return Arrays.stream(connectedPeers()).map(ConnectedPeer::getId).collect(Collectors.toSet());
  }

  public NodeInfo nodeInfo() {
    return post("admin_nodeInfo", NodeInfoResponse.class).getResult();
  }

  private ConnectedPeer[] connectedPeers() {
    return post("admin_peers", ConnectedPeersResponse.class).getResult();
  }

  public Optional<PrivacyTransactionReceipt> getPrivacyTransactionReceipt(final String hash) {
    return post("priv_getTransactionReceipt", hash, GetPrivateTransactionResponse.class)
        .getResult();
  }

  public Optional<TransactionReceipt> getTransactionReceipt(final String hash) {
    return post("eth_getTransactionReceipt", hash, GetTransactionResponse.class).getResult();
  }

  public Optional<Transaction> getTransactionByHash(final String hash) {
    return post("eth_getTransactionByHash", hash, GetTransactionByHashResponse.class).getResult();
  }
}
