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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static org.testcontainers.shaded.com.github.dockerjava.core.MediaType.APPLICATION_JSON;

import tech.pegasys.peeps.json.Json;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.Logger;

public abstract class RpcClient {

  private static final List<Integer> HTTP_OK_STATUSES = List.of(200, 201);

  private final Vertx vertx;
  private final Logger log;
  private final Duration connectionTimeout;
  private final Set<Supplier<String>> dockerLogs;

  private HttpClient rpc;
  private String containerId;

  public RpcClient(
      final Vertx vertx,
      final Duration connectionTimeout,
      final Logger log,
      final Set<Supplier<String>> dockerLogs) {
    this.connectionTimeout = connectionTimeout;
    this.vertx = vertx;
    this.log = log;
    this.dockerLogs = dockerLogs;
  }

  public void bind(final String containerId, final String ipAddress, final int httpJsonRpcPort) {
    this.containerId = containerId;

    checkNotNull(ipAddress, "Container IP address must be set");
    checkState(httpJsonRpcPort > 0, "Container HTTP PRC port must be set");
    checkState(
        rpc == null,
        "The underlying HttpClient is still open. Perform close() before a creating new binding.");

    log.info("Binding HttpClient on {}:{}", ipAddress, httpJsonRpcPort);

    rpc =
        vertx.createHttpClient(
            new WebClientOptions()
                .setDefaultPort(httpJsonRpcPort)
                .setDefaultHost(ipAddress)
                .setConnectTimeout((int) connectionTimeout.toMillis()));
  }

  public void close() {
    if (rpc != null) {
      rpc.close();
      rpc = null;
    }
  }

  protected <T> T post(final String relativeUri, final Object requestPojo, final Class<T> clazz) {
    try {
      return performPost(relativeUri, requestPojo, clazz);

    } catch (final RuntimeException e) {
      dockerLogs.forEach(dockerLog -> log.error(dockerLog.get()));
      log.error("Post request failed", e);
      throw e;
    }
  }

  private <T> T performPost(
      final String relativeUri, final Object requestPojo, final Class<T> clazz) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    final String json = Json.encode(requestPojo);

    @SuppressWarnings("deprecation")
    final HttpClientRequest request =
        rpc.post(
            relativeUri,
            result -> {
              if (HTTP_OK_STATUSES.contains(result.statusCode())) {
                result.bodyHandler(
                    body -> {
                      log.trace(
                          "Container {}, relative URL: {}, request: {}, response: {}",
                          containerId,
                          relativeUri,
                          json,
                          body);
                      try {
                        future.complete(Json.decode(body, clazz));
                      } catch (Exception e) {
                        future.completeExceptionally(
                            new IllegalStateException(
                                String.format("Failed decoding json rpc response %s", body), e));
                      }
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
