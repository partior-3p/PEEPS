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

import tech.pegasys.peeps.json.rpc.JsonRpcClient;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.rpc.NodeRpcClient;
import tech.pegasys.peeps.node.rpc.QbftRpc;
import tech.pegasys.peeps.privacy.model.PrivacyAddreess;
import tech.pegasys.peeps.signer.rpc.eea.SendPrivacyTransactionRequest;
import tech.pegasys.peeps.signer.rpc.eea.SendPrivacyTransactionResponse;
import tech.pegasys.peeps.signer.rpc.eth.SendTransactionRequest;
import tech.pegasys.peeps.signer.rpc.eth.SendTransactionResponse;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class SignerRpcClient extends NodeRpcClient {

  private static final Address NO_RECIPIENT = null;

  public SignerRpcClient(final JsonRpcClient jsonRpcClient, final QbftRpc qbftRpc) {
    super(jsonRpcClient, qbftRpc);
  }

  public Hash deployContractToPrivacyGroup(
      final Address sender,
      final String binary,
      final PrivacyAddreess privateSender,
      final PrivacyAddreess... privateRecipients) {
    return rpcClient
        .post(
            "eea_sendTransaction",
            SendPrivacyTransactionResponse.class,
            new SendPrivacyTransactionRequest(
                sender, NO_RECIPIENT, binary, privateSender, privateRecipients))
        .getResult();
  }

  public Hash transfer(final Address sender, final Address receiver, final Wei amount) {
    return rpcClient
        .post(
            "eth_sendTransaction",
            SendTransactionResponse.class,
            new SendTransactionRequest(sender, receiver, null, amount))
        .getResult();
  }
}
