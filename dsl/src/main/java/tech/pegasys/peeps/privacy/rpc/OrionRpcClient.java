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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import tech.pegasys.peeps.json.Json;
import tech.pegasys.peeps.privacy.rpc.receive.ReceiveRequest;
import tech.pegasys.peeps.privacy.rpc.receive.ReceiveResponse;
import tech.pegasys.peeps.privacy.rpc.send.SendRequest;
import tech.pegasys.peeps.privacy.rpc.send.SendResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrionRpcClient {

  private static final Logger LOG = LogManager.getLogger();

  private static final int HTTP_STATUS_OK = 200;

  private final Vertx vertx;
  private final String pubKey;

  private HttpClient jsonRpc;
  private String containerId;

  public OrionRpcClient(final Vertx vertx, final String pubKey) {
    this.vertx = vertx;
    this.pubKey = pubKey;
  }

  public void bind(final String containerId, final String ipAddress, final int httpJsonRpcPort) {
    this.containerId = containerId;

    if (jsonRpc != null) {
      jsonRpc.close();
    }

    checkNotNull(ipAddress, "Container IP address must be set");
    checkState(httpJsonRpcPort > 0, "Container HTTP PRC port must be set");
    LOG.info("Binding Orion Rpc HttpClient on {}:{}", ipAddress, httpJsonRpcPort);

    jsonRpc =
        vertx.createHttpClient(
            new WebClientOptions().setDefaultPort(httpJsonRpcPort).setDefaultHost(ipAddress));
  }

  public void close() {
    if (jsonRpc != null) {
      jsonRpc.close();
    }
  }

  public void verifyConnectivity() {}

  public String send(final String to, final String payload) {
    return post("/send", new SendRequest(pubKey, new String[] {to}, payload), SendResponse.class)
        .getKey();
  }

  public String receive(final String receipt) {
    return post("/receive", new ReceiveRequest(pubKey, receipt), ReceiveResponse.class)
        .getPayload();
  }

  // TODO method commonality with NodeRpcClient - refactor/utility class
  // TODO can make the decode/encode by suppliers
  private <T> T post(final String relativeUri, final Object requestPojo, final Class<T> clazz) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    final String json = Json.encode(requestPojo);

    final HttpClientRequest request =
        jsonRpc.post(
            relativeUri,
            result -> {
              if (result.statusCode() == HTTP_STATUS_OK) {
                result.bodyHandler(
                    body -> {
                      LOG.info("Container {}, {}: {}, {}", containerId, relativeUri, json, body);
                      future.complete(Json.decode(body, clazz));
                    });
              } else {
                final String errorMessage =
                    String.format(
                        "Post request: %s, to '%s' failed: %s, %s",
                        json, relativeUri, result.statusCode(), result.statusMessage());

                result.bodyHandler(body -> LOG.error("{}, {}", errorMessage, body));

                future.completeExceptionally(new IllegalStateException(errorMessage));
              }
            });

    request.putHeader("Content-Type", "application/json");

    request.setChunked(true);
    request.end(json);

    try {
      return future.get();
    } catch (final InterruptedException | ExecutionException e) {
      throw new RuntimeException("No response receive from: " + relativeUri, e);
    }
  }
}
