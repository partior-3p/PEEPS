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
package tech.pegasys.peeps.signer.rpc;

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.rpc.admin.NodeInfo;
import tech.pegasys.peeps.privacy.model.PrivacyAddreess;

import java.util.Set;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class SignerRpcSenderKnown {

  private final SignerRpc rpc;
  private final Address sender;

  public SignerRpcSenderKnown(final SignerRpc rpc, final Address sender) {
    this.rpc = rpc;
    this.sender = sender;
  }

  public Set<String> getConnectedPeerIds() {
    return rpc.getConnectedPeerIds();
  }

  public NodeInfo nodeInfo() {
    return rpc.nodeInfo();
  }

  public PrivacyTransactionReceipt getPrivacyTransactionReceipt(final Hash receipt) {
    return rpc.getPrivacyTransactionReceipt(receipt);
  }

  public TransactionReceipt getTransactionReceipt(final Hash receipt) {
    return rpc.getTransactionReceipt(receipt);
  }

  public Transaction getTransactionByHash(final Hash transaction) {
    return rpc.getTransactionByHash(transaction);
  }

  public Wei getBalance(final Address account) {
    return rpc.getBalance(account);
  }

  public Hash transferTo(final Address receiver, final Wei amount) {
    return rpc.transfer(sender, receiver, amount);
  }

  public Hash deployContractToPrivacyGroup(
      final String binary,
      final PrivacyAddreess string,
      final PrivacyAddreess... privateRecipients) {
    return rpc.deployContractToPrivacyGroup(sender, binary, string, privateRecipients);
  }
}
