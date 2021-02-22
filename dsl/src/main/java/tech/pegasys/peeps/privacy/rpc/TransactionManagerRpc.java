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
package tech.pegasys.peeps.privacy.rpc;

import tech.pegasys.peeps.json.rpc.RpcClient;
import tech.pegasys.peeps.privacy.model.TransactionManagerKey;
import tech.pegasys.peeps.privacy.rpc.receive.ReceiveRequest;
import tech.pegasys.peeps.privacy.rpc.receive.ReceiveResponse;
import tech.pegasys.peeps.privacy.rpc.send.SendRequest;
import tech.pegasys.peeps.privacy.rpc.send.SendResponse;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransactionManagerRpc extends RpcClient {

  private static final Logger LOG = LogManager.getLogger();
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(4);

  private final String pubKey;

  public TransactionManagerRpc(
      final Vertx vertx, final String pubKey, final Set<Supplier<String>> dockerLogs) {
    super(vertx, DEFAULT_TIMEOUT, LOG, dockerLogs);
    this.pubKey = pubKey;
  }

  public TransactionManagerKey send(final String to, final String payload) {
    return post("/send", new SendRequest(pubKey, new String[] {to}, payload), SendResponse.class)
        .getKey();
  }

  public String receive(final TransactionManagerKey key) {
    return post("/receive", new ReceiveRequest(pubKey, key), ReceiveResponse.class).getPayload();
  }
}
