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

import tech.pegasys.peeps.node.rpc.NodeJsonRpcClient;
import tech.pegasys.peeps.node.rpc.admin.NodeInfo;
import tech.pegasys.peeps.util.Await;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
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
  private final NodeJsonRpcClient jsonRpc;
  private String nodeId;

  public Besu(final NodeConfiguration config) {

    final GenericContainer<?> container = new GenericContainer<>(BESU_IMAGE);
    final List<String> commandLineOptions = standardCommandLineOptions();

    addPeerToPeerHost(config, commandLineOptions);
    addCorsOrigins(config, commandLineOptions);
    addBootnodeAddress(config, commandLineOptions);
    addContainerNetwork(config, container);
    addContainerIpAddress(config, container);
    addNodePrivateKey(config, commandLineOptions, container);
    addGenesisFile(config, commandLineOptions, container);
    addPrivacy(config, commandLineOptions, container);

    LOG.debug("besu command line {}", commandLineOptions);

    this.besu =
        container.withCommand(commandLineOptions.toArray(new String[0])).waitingFor(liveliness());

    this.jsonRpc = new NodeJsonRpcClient(config.getVertx());
  }

  public void start() {
    try {
      besu.start();

      jsonRpc.bind(
          besu.getContainerId(),
          besu.getContainerIpAddress(),
          besu.getMappedPort(CONTAINER_HTTP_RPC_PORT));

      final NodeInfo info = jsonRpc.nodeInfo();
      nodeId = info.getId();

      // TODO validate the node has the expected state, e.g. consensus, genesis, networkId,
      // protocol(s), ports, listen address

      logPortMappings();
      logContainerNetworkDetails();
    } catch (final ContainerLaunchException e) {
      LOG.error(besu.getLogs());
      throw e;
    }
  }

  public void stop() {
    besu.stop();
    jsonRpc.close();
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
        () -> assertThat(jsonRpc.connectedPeerIds().containsAll(peerIds)).isTrue(),
        String.format("Failed to connect in time to peers: %s", peerIds));
  }

  private Set<String> expectedPeerIds(final Besu... peers) {
    return Arrays.stream(peers)
        .map(node -> ensureHexPrefix(node.getNodeId()))
        .collect(Collectors.toSet());
  }

  private HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_RPC_PORT);
  }

  private void logPortMappings() {
    LOG.info(
        "Container {}, HTTP RPC port mapping: {} -> {}, WS RPC port mapping: {} -> {}, p2p port mapping: {} -> {}",
        besu.getContainerId(),
        CONTAINER_HTTP_RPC_PORT,
        besu.getMappedPort(CONTAINER_HTTP_RPC_PORT),
        CONTAINER_WS_RPC_PORT,
        besu.getMappedPort(CONTAINER_WS_RPC_PORT),
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

  private List<String> standardCommandLineOptions() {
    return Lists.newArrayList(
        "--logging",
        "INFO",
        "--miner-enabled",
        "--miner-coinbase",
        "1b23ba34ca45bb56aa67bc78be89ac00ca00da00",
        "--host-whitelist",
        "*",
        "--rpc-http-enabled",
        "--rpc-ws-enabled",
        "--rpc-http-apis",
        "ADMIN,ETH,NET,WEB3,EEA");
  }

  private void addPeerToPeerHost(
      final NodeConfiguration config, final List<String> commandLineOptions) {
    commandLineOptions.add("--p2p-host");
    commandLineOptions.add(config.getIpAddress());
  }

  private void addBootnodeAddress(
      final NodeConfiguration config, final List<String> commandLineOptions) {
    config
        .getBootnodeEnodeAddress()
        .ifPresent(enode -> commandLineOptions.addAll(Lists.newArrayList("--bootnodes", enode)));
  }

  private void addContainerNetwork(
      final NodeConfiguration config, final GenericContainer<?> container) {
    config.getContainerNetwork().ifPresent(container::withNetwork);
  }

  private void addCorsOrigins(
      final NodeConfiguration config, final List<String> commandLineOptions) {

    config
        .getCors()
        .ifPresent(
            cors -> commandLineOptions.addAll(Lists.newArrayList("--rpc-http-cors-origins", cors)));
  }

  private void addNodePrivateKey(
      final NodeConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {
    config
        .getNodePrivateKeyFile()
        .ifPresent(
            file -> {
              container.withClasspathResourceMapping(
                  file, CONTAINER_NODE_PRIVATE_KEY_FILE, BindMode.READ_ONLY);
              commandLineOptions.addAll(
                  Lists.newArrayList("--node-private-key-file", CONTAINER_NODE_PRIVATE_KEY_FILE));
            });
  }

  private void addGenesisFile(
      final NodeConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {
    commandLineOptions.add("--genesis-file");
    commandLineOptions.add(CONTAINER_GENESIS_FILE);
    container.withClasspathResourceMapping(
        config.getGenesisFile(), CONTAINER_GENESIS_FILE, BindMode.READ_ONLY);
  }

  private void addPrivacy(
      final NodeConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {
    commandLineOptions.add("--privacy-enabled");
    commandLineOptions.add("--privacy-public-key-file");
    commandLineOptions.add(CONTAINER_PRIVACY_PUBLIC_KEY_FILE);
    container.withClasspathResourceMapping(
        config.getEnclavePublicKeyFile(), CONTAINER_PRIVACY_PUBLIC_KEY_FILE, BindMode.READ_ONLY);
  }

  private void addContainerIpAddress(
      final NodeConfiguration config, final GenericContainer<?> container) {
    container.withCreateContainerCmdModifier(
        modifier -> modifier.withIpv4Address(config.getIpAddress()));
  }
}
