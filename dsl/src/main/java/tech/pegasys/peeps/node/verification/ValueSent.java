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
package tech.pegasys.peeps.node.verification;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.rpc.NodeRpcExpectingData;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Gas;
import org.apache.tuweni.units.ethereum.Wei;

public class ValueSent implements NodeValueTransition {

  private final Address sender;
  private final Wei before;
  private final Hash transactionReceipt;

  public ValueSent(final Address sender, final Wei before, final Hash transactionReceipt) {
    this.sender = sender;
    this.before = before;
    this.transactionReceipt = transactionReceipt;
  }

  @Override
  public void verify(final NodeRpcExpectingData rpc) {

    final Wei after = rpc.getBalance(sender);
    final TransactionReceipt receipt = transactionReceipt(rpc);
    final Wei cost = transactionCost(rpc, receipt);

    assertThat(after).isEqualTo(before.subtract(cost));
  }

  private TransactionReceipt transactionReceipt(final NodeRpcExpectingData rpc) {
    final TransactionReceipt transferReceipt = rpc.getTransactionReceipt(transactionReceipt);
    assertThat(transferReceipt).isNotNull();
    assertThat(transferReceipt.isSuccess()).isTrue();
    return transferReceipt;
  }

  private Wei transactionCost(
      final NodeRpcExpectingData rpc, final TransactionReceipt transferReceipt) {
    assertThat(transferReceipt.getTransactionHash()).isNotNull();
    final Hash transaction = transferReceipt.getTransactionHash();

    assertThat(transferReceipt.getGasUsed()).isNotNull();
    final Gas used = transferReceipt.getGasUsed();

    final Transaction transfer = rpc.getTransactionByHash(transaction);
    assertThat(transfer).isNotNull();
    assertThat(transfer.getGasPrice()).isNotNull();
    final Wei eachUnit = transfer.getGasPrice();

    final Wei transactionCost = used.priceFor(eachUnit);
    final Wei transderAmount = transfer.getValue();

    return transderAmount.add(transactionCost);
  }
}
