/*
 * Copyright 2020 ConsenSys AG.
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
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.rpc.QbftRpc.VoteType;
import tech.pegasys.peeps.node.rpc.admin.ConnectedPeer;
import tech.pegasys.peeps.node.rpc.admin.ConnectedPeersResponse;
import tech.pegasys.peeps.node.rpc.admin.NodeInfo;
import tech.pegasys.peeps.node.rpc.admin.NodeInfoResponse;
import tech.pegasys.peeps.node.rpc.eth.GetBalanceResponse;
import tech.pegasys.peeps.node.rpc.eth.GetBlockNumberResponse;
import tech.pegasys.peeps.node.rpc.eth.GetTransactionByHashResponse;
import tech.pegasys.peeps.node.rpc.eth.GetTransactionReceiptResponse;
import tech.pegasys.peeps.node.rpc.priv.GetPrivateTransactionResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class NodeRpcClient {

  protected final JsonRpcClient rpcClient;
  private final QbftRpc qbftRpc;

  public NodeRpcClient(final JsonRpcClient rpcClient, final QbftRpc qbftRpc) {
    this.rpcClient = rpcClient;
    this.qbftRpc = qbftRpc;
  }

  public Set<String> getConnectedPeerEnodes() {
    return Arrays.stream(connectedPeers()).map(ConnectedPeer::getEnode).collect(Collectors.toSet());
  }

  public NodeInfo nodeInfo() {
    return rpcClient.post("admin_nodeInfo", NodeInfoResponse.class).getResult();
  }

  private ConnectedPeer[] connectedPeers() {
    return rpcClient.post("admin_peers", ConnectedPeersResponse.class).getResult();
  }

  public Optional<PrivacyTransactionReceipt> getPrivacyTransactionReceipt(final Hash receipt) {
    return rpcClient
        .post("priv_getTransactionReceipt", GetPrivateTransactionResponse.class, receipt)
        .getResult();
  }

  public Optional<TransactionReceipt> getTransactionReceipt(final Hash receipt) {
    return rpcClient
        .post("eth_getTransactionReceipt", GetTransactionReceiptResponse.class, receipt)
        .getResult();
  }

  public Optional<Transaction> getTransactionByHash(final Hash transaction) {
    return rpcClient
        .post("eth_getTransactionByHash", GetTransactionByHashResponse.class, transaction)
        .getResult();
  }

  public Wei getBalance(final Address account) {
    return rpcClient
        .post("eth_getBalance", GetBalanceResponse.class, account.toHexString(), "latest")
        .getResult();
  }

  public long getBlockNumber() {
    return rpcClient.post("eth_blockNumber", GetBlockNumberResponse.class).getResult();
  }

  public boolean qbftProposeValidatorVote(final Address validator, final VoteType vote) {
    return qbftRpc.qbftProposeValidatorVote(validator, vote);
  }

  public List<Address> qbftGetValidatorsByBlockNumber(final String blockNumber) {
    return qbftRpc.qbftGetValidatorsByBlockBlockNumber(blockNumber);
  }
}
