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
package tech.pegasys.peeps.json.rpc;

import java.time.Duration;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.Logger;

public abstract class JsonRpcClient extends RpcClient {

  private static final String JSON_RPC_VERSION = "2.0";
  private static final String JSON_RPC_CONTEXT_PATH = "/";

  public JsonRpcClient(final Vertx vertx, final Duration timeout, final Logger log) {
    super(vertx, timeout, log);
  }

  protected <T> T post(final String method, final Class<T> clazz) {
    return this.post(method, new Object[0], clazz);
  }

  @Override
  protected <T> T post(final String method, final Object params, final Class<T> clazz) {
    return super.post(
        JSON_RPC_CONTEXT_PATH,
        new JsonRpcRequest(
            JSON_RPC_VERSION, method, new Object[] {params}, new JsonRpcRequestId(1)),
        clazz);
  }
}
