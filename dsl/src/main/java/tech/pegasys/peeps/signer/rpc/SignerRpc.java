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
package tech.pegasys.peeps.signer.rpc;

import tech.pegasys.peeps.json.rpc.JsonRpcClient;
import tech.pegasys.peeps.signer.rpc.eea.SendPrivateTransactionResponse;
import tech.pegasys.peeps.signer.rpc.eea.SendTransactionRequest;
import tech.pegasys.peeps.signer.rpc.net.EnodeResponse;

import java.time.Duration;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignerRpc extends JsonRpcClient {

  private static final Logger LOG = LogManager.getLogger();
  private static final String NO_RECIPIENT = null;

  public SignerRpc(final Vertx vertx, final Duration timeout) {
    super(vertx, timeout, LOG);
  }

  public String deployContractToPrivacyGroup(
      final String sender,
      final String binary,
      final String privateSender,
      final String[] privateRecipients) {
    return post(
            "eea_sendTransaction",
            new SendTransactionRequest(
                sender, NO_RECIPIENT, binary, privateSender, privateRecipients),
            SendPrivateTransactionResponse.class)
        .getResult();
  }

  public String enode() {
    return post("net_enode", EnodeResponse.class).getResult();
  }
}
