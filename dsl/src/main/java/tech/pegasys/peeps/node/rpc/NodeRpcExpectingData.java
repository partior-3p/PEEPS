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

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class NodeRpcExpectingData {

  private final NodeRpc rpc;

  public NodeRpcExpectingData(final NodeRpc rpc) {
    this.rpc = rpc;
  }

  public PrivacyTransactionReceipt getPrivacyTransactionReceipt(final Hash receipt) {
    return awaitData(
            () -> rpc.getPrivacyTransactionReceipt(receipt),
            "Failed to retrieve the private transaction receipt with hash: %s",
            receipt)
        .get();
  }

  public TransactionReceipt getTransactionReceipt(final Hash receipt) {
    return awaitData(
            () -> rpc.getTransactionReceipt(receipt),
            "Failed to retrieve the transaction receipt with hash: %s",
            receipt)
        .get();
  }

  public Transaction getTransactionByHash(final Hash transaction) {
    return awaitData(
            () -> rpc.getTransactionByHash(transaction),
            "Failed to retrieve the transaction with hash: %s",
            transaction)
        .get();
  }

  public Wei getBalance(final Address account) {
    return awaitData(
        () -> rpc.getBalance(account), "Failed to retrieve the balance for address: %s", account);
  }
}
