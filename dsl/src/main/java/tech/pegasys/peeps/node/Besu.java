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
package tech.pegasys.peeps.node;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.peeps.util.HexFormatter.ensureHexPrefix;

import tech.pegasys.peeps.node.rpc.ConnectedPeer;
import tech.pegasys.peeps.node.rpc.ConnectedPeersResponse;
import tech.pegasys.peeps.node.rpc.JsonRpcRequest;
import tech.pegasys.peeps.node.rpc.JsonRpcRequestId;
import tech.pegasys.peeps.node.rpc.NodeInfo;
import tech.pegasys.peeps.node.rpc.NodeInfoResponse;
import tech.pegasys.peeps.util.Await;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

public class Besu {

  private static final Logger LOG = LogManager.getLogger();

  private static final String AM_I_ALIVE_ENDPOINT = "/liveness";
  private static final int ALIVE_STATUS_CODE = 200;

  private static final String BESU_IMAGE = "hyperledger/besu:latest";
  private static final int CONTAINER_HTTP_RPC_PORT = 8545;
  private static final int CONTAINER_WS_RPC_PORT = 8546;
  private static final int CONTAINER_P2P_PORT = 30303;
  private static final String CONTAINER_GENESIS_FILE = "/etc/besu/genesis.json";
  private static final String CONTAINER_PRIVACY_PUBLIC_KEY_FILE =
      "/etc/besu/privacy_public_key.pub";
  private static final String CONTAINER_NODE_PRIVATE_KEY_FILE = "/etc/besu/keys/key.priv";

  private final GenericContainer<?> besu;
  private HttpClient jsonRpc;
  private String nodeId;

  public Besu(final NodeConfiguration config) {

    final List<String> commandLineOptions =
        Lists.newArrayList(
            "--genesis-file",
            CONTAINER_GENESIS_FILE,
            "--logging",
            "DEBUG",
            "--miner-enabled",
            "--miner-coinbase",
            "1b23ba34ca45bb56aa67bc78be89ac00ca00da00",
            "--host-whitelist",
            "*",
            "--p2p-host",
            config.getIpAddress(),
            "--rpc-http-enabled",
            "--rpc-ws-enabled",
            "--rpc-http-apis",
            "ADMIN,ETH,NET,WEB3,EEA",
            "--privacy-enabled",
            "--privacy-public-key-file",
            CONTAINER_PRIVACY_PUBLIC_KEY_FILE);

    GenericContainer<?> container = besuContainer(config);

    // TODO move the other bonds & args out e.g. genesis & encalve

    // TODO refactor these into private helpers
    config
        .getCors()
        .ifPresent(
            cors -> commandLineOptions.addAll(Lists.newArrayList("--rpc-http-cors-origins", cors)));

    config
        .getNodePrivateKeyFile()
        .ifPresent(
            file -> {
              container.withClasspathResourceMapping(
                  file, CONTAINER_NODE_PRIVATE_KEY_FILE, BindMode.READ_ONLY);
              commandLineOptions.addAll(
                  Lists.newArrayList("--node-private-key-file", CONTAINER_NODE_PRIVATE_KEY_FILE));
            });

    config
        .getBootnodeEnodeAddress()
        .ifPresent(enode -> commandLineOptions.addAll(Lists.newArrayList("--bootnodes", enode)));

    LOG.debug("besu command line {}", config);

    this.besu =
        container
            .withCreateContainerCmdModifier(
                modifier -> modifier.withIpv4Address(config.getIpAddress()))
            .withCommand(commandLineOptions.toArray(new String[0]))
            .waitingFor(liveliness());
  }

  public void start() {
    try {
      besu.start();

      // TODO get the node info & store

      final NodeInfo info = nodeInfo();
      nodeId = info.getId();

      // TODO validate the node has the expected state, e.g. consensus, genesis, networkId,
      // protocol(s), ports, listen address

      logHttpRpcPortMapping();
      logWsRpcPortMapping();
      logPeerToPeerPortMapping();
      logContainerNetworkDetails();
    } catch (final ContainerLaunchException e) {
      LOG.error(besu.getLogs());
      throw e;
    }
  }

  public void stop() {
    besu.stop();
  }

  public void awaitConnectivity(final Besu... peers) {
    awaitPeerIdConnections(expectedPeerIds(peers));
  }

  private String getNodeId() {
    checkNotNull(nodeId, "NodeId only exists after the node has started");
    return nodeId;
  }

  private void awaitPeerIdConnections(final Set<String> peerIds) {
    Await.await(
        () -> assertThat(connectedPeerIds().containsAll(peerIds)).isTrue(),
        String.format("Failed to connect in time to peers: %s", peerIds));
  }

  private Set<String> expectedPeerIds(final Besu... peers) {
    return Arrays.stream(peers)
        .map(node -> ensureHexPrefix(node.getNodeId()))
        .collect(Collectors.toSet());
  }

  private Set<String> connectedPeerIds() {
    return Arrays.stream(connectedPeers()).map(ConnectedPeer::getId).collect(Collectors.toSet());
  }

  // TODO common - post, generics, method name
  // TODO no more magic strings!
  // TODO rewrite to take advantage od async - many nodes performing simultaneously
  private ConnectedPeer[] connectedPeers() {

    final JsonRpcRequest jsonRpcRequest =
        new JsonRpcRequest("2.0", "admin_peers", new Object[0], new JsonRpcRequestId(1));

    CompletableFuture<ConnectedPeersResponse> info = new CompletableFuture<>();

    final String json = Json.encode(jsonRpcRequest);

    // TODO use a configured Json mapper instance - enforce creation parameters
    final HttpClientRequest request =
        jsonRpcClient()
            .post(
                "/",
                result -> {
                  if (result.statusCode() == 200) {
                    result.bodyHandler(
                        body -> {
                          info.complete(Json.decodeValue(body, ConnectedPeersResponse.class));
                        });
                  } else {
                    final String errorMessage =
                        String.format(
                            "Querying 'admin_peers failed: %s, %s",
                            result.statusCode(), result.statusMessage());
                    info.completeExceptionally(new IllegalStateException(errorMessage));
                  }
                });

    request.setChunked(false);
    request.end(json);

    try {
      return info.get().getResult();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Failed to receive a response from `admin_nodeInfo`", e);
    }
  }

  // TODO no more magic strings!
  // TODO rewrite to take advantage od async - many nodes performing simultaneously
  private NodeInfo nodeInfo() {
    final JsonRpcRequest jsonRpcRequest =
        new JsonRpcRequest("2.0", "admin_nodeInfo", new Object[0], new JsonRpcRequestId(1));

    CompletableFuture<NodeInfoResponse> info = new CompletableFuture<>();

    final String json = Json.encode(jsonRpcRequest);

    // TODO use a configured Json mapper instance - enforce creation parameters
    final HttpClientRequest request =
        jsonRpcClient()
            .post(
                "/",
                result -> {
                  if (result.statusCode() == 200) {
                    result.bodyHandler(
                        body -> {
                          LOG.info("Container {}, admin_nodeInfo: {}", besu.getContainerId(), body);
                          info.complete(Json.decodeValue(body, NodeInfoResponse.class));
                        });
                  } else {
                    final String errorMessage =
                        String.format(
                            "Querying 'admin_nodInfo failed: %s, %s",
                            result.statusCode(), result.statusMessage());
                    LOG.error(errorMessage);
                    info.completeExceptionally(new IllegalStateException(errorMessage));
                  }
                });

    request.setChunked(false);
    request.end(json);

    try {
      return info.get().getResult();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Failed to receive a response from `admin_nodeInfo`", e);
    }
  }

  private HttpClient jsonRpcClient() {
    if (jsonRpc == null) {
      // TODO move the vertx to network & close on stop()
      jsonRpc =
          Vertx.vertx()
              .createHttpClient(
                  new WebClientOptions()
                      .setDefaultPort(besu.getMappedPort(CONTAINER_HTTP_RPC_PORT))
                      .setDefaultHost(besu.getContainerIpAddress()));
    }

    return jsonRpc;
  }

  // TODO reduce the args - exposed ports maybe not needed
  private GenericContainer<?> besuContainer(final NodeConfiguration config) {
    return new GenericContainer<>(BESU_IMAGE)
        .withNetwork(config.getContainerNetwork().orElse(null))
        .withExposedPorts(CONTAINER_HTTP_RPC_PORT, CONTAINER_WS_RPC_PORT, CONTAINER_P2P_PORT)
        .withClasspathResourceMapping(
            config.getGenesisFile(), CONTAINER_GENESIS_FILE, BindMode.READ_ONLY)
        .withClasspathResourceMapping(
            config.getEnclavePublicKeyFile(),
            CONTAINER_PRIVACY_PUBLIC_KEY_FILE,
            BindMode.READ_ONLY);
  }

  // TODO liveliness should be move to network or configurable to allow parallel besu container
  // startups
  private HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_RPC_PORT);
  }

  // TODO a single log line with all details!
  private void logHttpRpcPortMapping() {
    LOG.info(
        "Container {}, HTTP RPC port mapping: {} -> {}",
        besu.getContainerId(),
        CONTAINER_HTTP_RPC_PORT,
        besu.getMappedPort(CONTAINER_HTTP_RPC_PORT));
  }

  private void logWsRpcPortMapping() {
    LOG.info(
        "Container {}, WS RPC port mapping: {} -> {}",
        besu.getContainerId(),
        CONTAINER_WS_RPC_PORT,
        besu.getMappedPort(CONTAINER_WS_RPC_PORT));
  }

  private void logPeerToPeerPortMapping() {
    LOG.info(
        "Container {}, p2p port mapping: {} -> {}",
        besu.getContainerId(),
        CONTAINER_P2P_PORT,
        besu.getMappedPort(CONTAINER_P2P_PORT));
  }

  private void logContainerNetworkDetails() {
    if (besu.getNetwork() == null) {
      LOG.info("Container {} has no network", besu.getContainerId());
    } else {
      LOG.info(
          "Container {}, IP address: {}, Network: {}",
          besu.getContainerId(),
          besu.getContainerIpAddress(),
          besu.getNetwork().getId());
    }
  }
}
