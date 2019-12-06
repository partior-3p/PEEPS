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
package tech.pegasys.peeps.util;

import static com.github.dockerjava.core.MediaType.APPLICATION_JSON;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import tech.pegasys.peeps.json.Json;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.Logger;

public abstract class RpcClient {

  private static final int HTTP_STATUS_OK = 200;

  private final Vertx vertx;
  private final Logger log;

  private HttpClient rpc;
  private String containerId;

  public RpcClient(final Vertx vertx, final Logger log) {
    this.vertx = vertx;
    this.log = log;
  }

  public void bind(final String containerId, final String ipAddress, final int httpJsonRpcPort) {
    this.containerId = containerId;

    if (rpc != null) {
      rpc.close();
    }

    checkNotNull(ipAddress, "Container IP address must be set");
    checkState(httpJsonRpcPort > 0, "Container HTTP PRC port must be set");
    log.info("Binding HttpClient on {}:{}", ipAddress, httpJsonRpcPort);

    rpc =
        vertx.createHttpClient(
            new WebClientOptions().setDefaultPort(httpJsonRpcPort).setDefaultHost(ipAddress));
  }

  public void close() {
    if (rpc != null) {
      rpc.close();
    }
  }

  public <T> T post(final String relativeUri, final Object requestPojo, final Class<T> clazz) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    final String json = Json.encode(requestPojo);

    final HttpClientRequest request =
        rpc.post(
            relativeUri,
            result -> {
              if (result.statusCode() == HTTP_STATUS_OK) {
                result.bodyHandler(
                    body -> {
                      log.info("Container {}, {}: {}, {}", containerId, relativeUri, json, body);
                      future.complete(Json.decode(body, clazz));
                    });
              } else {
                final String errorMessage =
                    String.format(
                        "Post request: %s, to '%s' failed: %s, %s",
                        json, relativeUri, result.statusCode(), result.statusMessage());
                result.bodyHandler(body -> log.error("{}, {}", errorMessage, body));
                future.completeExceptionally(new IllegalStateException(errorMessage));
              }
            });

    request.putHeader(CONTENT_TYPE, APPLICATION_JSON.getMediaType());

    request.setChunked(true);
    request.end(json);

    try {
      return future.get();
    } catch (final InterruptedException | ExecutionException e) {
      throw new RuntimeException("No response receive from: " + relativeUri, e);
    }
  }
}
