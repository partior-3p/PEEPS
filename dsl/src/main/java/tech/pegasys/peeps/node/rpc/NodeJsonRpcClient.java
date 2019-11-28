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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import tech.pegasys.peeps.json.Json;
import tech.pegasys.peeps.node.rpc.admin.ConnectedPeer;
import tech.pegasys.peeps.node.rpc.admin.ConnectedPeersResponse;
import tech.pegasys.peeps.node.rpc.admin.NodeInfo;
import tech.pegasys.peeps.node.rpc.admin.NodeInfoResponse;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeJsonRpcClient {

  private static final Logger LOG = LogManager.getLogger();

  private static final String JSON_RPC_CONTEXT_PATH = "/";
  private static final String JSON_RPC_VERSION = "2.0";
  private static int HTTP_STATUS_OK = 200;

  private final Vertx vertx;

  private HttpClient jsonRpc;
  private String containerId;

  public NodeJsonRpcClient(final Vertx vertx) {
    this.vertx = vertx;
  }

  public Set<String> connectedPeerIds() {
    return Arrays.stream(connectedPeers()).map(ConnectedPeer::getId).collect(Collectors.toSet());
  }

  public NodeInfo nodeInfo() {
    return post("admin_nodeInfo", NodeInfoResponse.class).getResult();
  }

  // TODO rewrite to take advantage od async - many nodes performing simultaneously
  private <T> T post(final String method, final Class<T> clazz) {
    final JsonRpcRequest jsonRpcRequest =
        new JsonRpcRequest(JSON_RPC_VERSION, method, new Object[0], new JsonRpcRequestId(1));
    final CompletableFuture<T> future = new CompletableFuture<>();
    final String json = Json.encode(jsonRpcRequest);

    final HttpClientRequest request =
        jsonRpc.post(
            JSON_RPC_CONTEXT_PATH,
            result -> {
              if (result.statusCode() == HTTP_STATUS_OK) {
                result.bodyHandler(
                    body -> {
                      LOG.info("Container {}, {}: {}", containerId, method, body);
                      future.complete(Json.decode(body, clazz));
                    });
              } else {
                final String errorMessage =
                    String.format(
                        "Querying %s failed: %s, %s",
                        method, result.statusCode(), result.statusMessage());
                LOG.error(errorMessage);
                future.completeExceptionally(new IllegalStateException(errorMessage));
              }
            });

    request.setChunked(true);
    request.end(json);

    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Failed to receive a response from `admin_nodeInfo`", e);
    }
  }

  public void bind(final String containerId, final String ipAddress, final int httpJsonRpcPort) {
    this.containerId = containerId;

    if (jsonRpc != null) {
      jsonRpc.close();
    }

    checkNotNull(ipAddress, "Container IP address must be set");
    checkState(httpJsonRpcPort > 0, "Container HTTP PRC port must be set");
    jsonRpc =
        vertx.createHttpClient(
            new WebClientOptions().setDefaultPort(httpJsonRpcPort).setDefaultHost(ipAddress));
  }

  public void close() {
    if (jsonRpc != null) {
      jsonRpc.close();
    }
  }

  private ConnectedPeer[] connectedPeers() {
    return post("admin_peers", ConnectedPeersResponse.class).getResult();
  }
}
