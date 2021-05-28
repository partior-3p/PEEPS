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

import static tech.pegasys.peeps.util.Await.awaitData;
import static tech.pegasys.peeps.util.Await.awaitPresence;

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.rpc.admin.NodeInfo;

import java.util.List;
import java.util.Set;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class NodeRpcMandatoryResponse implements NodeRpc {

  private final NodeRpcClient rpc;

  public NodeRpcMandatoryResponse(final NodeRpcClient rpc) {
    this.rpc = rpc;
  }

  @Override
  public PrivacyTransactionReceipt getPrivacyTransactionReceipt(final Hash receipt) {
    return awaitPresence(
            () -> rpc.getPrivacyTransactionReceipt(receipt),
            "Failed to retrieve the private transaction receipt with hash: %s",
            receipt)
        .get();
  }

  @Override
  public TransactionReceipt getTransactionReceipt(final Hash receipt) {
    return awaitPresence(
            () -> rpc.getTransactionReceipt(receipt),
            "Failed to retrieve the transaction receipt with hash: %s",
            receipt)
        .get();
  }

  @Override
  public Transaction getTransactionByHash(final Hash transaction) {
    return awaitPresence(
            () -> rpc.getTransactionByHash(transaction),
            "Failed to retrieve the transaction with hash: %s",
            transaction)
        .get();
  }

  @Override
  public Wei getBalance(final Address account) {
    return awaitData(
        () -> rpc.getBalance(account), "Failed to retrieve the balance for address: %s", account);
  }

  @Override
  public long getBlockNumber() {
    return awaitData(rpc::getBlockNumber, "Failed to retrieve block number");
  }

  @Override
  public boolean qbftProposeValidatorVote(final Address validator, final VoteType vote) {
    return awaitData(
        () -> rpc.qbftProposeValidatorVote(validator, vote), "Failed to cast qbft vote");
  }

  @Override
  public List<Address> qbftGetValidatorsByBlockBlockNumber(final String blockNumber) {
    return awaitData(
        () -> rpc.qbftGetValidatorsByBlockNumber(blockNumber),
        "Failed to retrieve qbft validators for block %s",
        blockNumber);
  }

  @Override
  public Set<String> getConnectedPeerIds() {
    return rpc.getConnectedPeerEnodes();
  }

  @Override
  public NodeInfo nodeInfo() {
    return rpc.nodeInfo();
  }
}
