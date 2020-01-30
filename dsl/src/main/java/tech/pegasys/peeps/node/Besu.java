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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.peeps.util.Await.await;
import static tech.pegasys.peeps.util.HexFormatter.ensureHexPrefix;
import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import tech.pegasys.peeps.network.NetworkMember;
import tech.pegasys.peeps.network.subnet.SubnetAddress;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.NodeIdentifier;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.rpc.NodeRpc;
import tech.pegasys.peeps.node.rpc.NodeRpcClient;
import tech.pegasys.peeps.node.rpc.NodeRpcMandatoryResponse;
import tech.pegasys.peeps.node.rpc.admin.NodeInfo;
import tech.pegasys.peeps.node.verification.AccountValue;
import tech.pegasys.peeps.node.verification.NodeValueTransition;
import tech.pegasys.peeps.util.ClasspathResources;
import tech.pegasys.peeps.util.DockerLogs;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public class Besu implements NetworkMember {

  private static final Logger LOG = LogManager.getLogger();

  private static final String AM_I_ALIVE_ENDPOINT = "/liveness";
  private static final int ALIVE_STATUS_CODE = 200;

  //  private static final String BESU_IMAGE = "hyperledger/besu:latest";
  private static final String BESU_IMAGE = "hyperledger/besu:develop";
  private static final int CONTAINER_HTTP_RPC_PORT = 8545;
  private static final int CONTAINER_WS_RPC_PORT = 8546;
  private static final int CONTAINER_P2P_PORT = 30303;
  private static final String CONTAINER_GENESIS_FILE = "/etc/besu/genesis.json";
  private static final String CONTAINER_PRIVACY_PUBLIC_KEY_FILE =
      "/etc/besu/privacy_public_key.pub";
  private static final String CONTAINER_NODE_PRIVATE_KEY_FILE = "/etc/besu/keys/node.priv";
  private static final String CONTAINER_PRIVACY_SIGNING_PRIVATE_KEY_FILE =
      "/etc/besu/keys/pmt_signing.priv";

  private final GenericContainer<?> besu;
  private final NodeRpcClient nodeRpc;
  private final NodeRpc rpc;
  private final SubnetAddress ipAddress;
  private final NodeIdentifier identity;
  private final String enodeAddress;

  private String nodeId;
  private String enodeId;
  private String pubKey;

  public Besu(final BesuConfiguration config) {

    final GenericContainer<?> container = new GenericContainer<>(BESU_IMAGE);
    final List<String> commandLineOptions = standardCommandLineOptions();

    this.ipAddress = config.getIpAddress();

    addPeerToPeerHost(config, commandLineOptions);
    addCorsOrigins(config, commandLineOptions);
    addBootnodeAddress(config, commandLineOptions);
    addContainerNetwork(config, container);
    addContainerIpAddress(ipAddress, container);
    addNodePrivateKey(config, commandLineOptions, container);
    addGenesisFile(config, commandLineOptions, container);

    if (config.isPrivacyEnabled()) {
      addPrivacy(config, commandLineOptions, container);
    }

    LOG.info("Besu command line: {}", commandLineOptions);

    this.besu =
        container.withCommand(commandLineOptions.toArray(new String[0])).waitingFor(liveliness());

    this.nodeRpc = new NodeRpcClient(config.getVertx(), dockerLogs());
    this.rpc = new NodeRpcMandatoryResponse(nodeRpc);
    this.identity = config.getIdentity();
    this.pubKey = nodePublicKey(config);
    this.enodeAddress = enodeAddress(config);
  }

  @Override
  public void start() {
    try {
      besu.start();

      nodeRpc.bind(
          besu.getContainerId(),
          besu.getContainerIpAddress(),
          besu.getMappedPort(CONTAINER_HTTP_RPC_PORT));

      final NodeInfo info = nodeRpc.nodeInfo();
      nodeId = info.getId();

      // TODO enode must match enodeAddress - otherwise error
      // TODO remove enodeId - then rename enodeAddress to enodeId
      enodeId = info.getEnode();

      // TODO validate the node has the expected state, e.g. consensus, genesis,
      // networkId,
      // protocol(s), ports, listen address

      logPortMappings();
      logContainerNetworkDetails();
    } catch (final ContainerLaunchException e) {
      LOG.error(besu.getLogs());
      throw e;
    }
  }

  @Override
  public void stop() {
    if (besu != null) {
      besu.stop();
    }
    if (nodeRpc != null) {
      nodeRpc.close();
    }
  }

  public SubnetAddress ipAddress() {
    return ipAddress;
  }

  // TODO these may not have a value, i.e. node not started :. optional
  public String enodeId() {
    return enodeId;
  }

  // TODO stricter typing then String
  public String enodeAddress() {
    return enodeAddress;
  }

  public String nodePublicKey() {
    return pubKey;
  }

  public NodeIdentifier identity() {
    return identity;
  }

  public int httpRpcPort() {
    return CONTAINER_HTTP_RPC_PORT;
  }

  public int p2pPort() {
    return CONTAINER_P2P_PORT;
  }

  public void awaitConnectivity(final Collection<Besu> peers) {
    awaitPeerIdConnections(excludeSelf(expectedPeerIds(peers)));
  }

  public String getLogs() {
    return DockerLogs.format("Besu", besu);
  }

  public NodeRpc rpc() {
    return rpc;
  }

  public void verifyTransition(final NodeValueTransition... changes) {
    Stream.of(changes).parallel().forEach(change -> change.verify(rpc));
  }

  public void verifyValue(final Set<AccountValue> values) {
    values.parallelStream().forEach(value -> value.verify(rpc));
  }

  public void verifySuccessfulTransactionReceipt(final Hash transaction) {
    final TransactionReceipt receipt = rpc.getTransactionReceipt(transaction);

    assertThat(receipt.getTransactionHash()).isEqualTo(transaction);
    assertThat(receipt.isSuccess()).isTrue();
  }

  private String getNodeId() {
    checkNotNull(nodeId, "NodeId only exists after the node has started");
    return nodeId;
  }

  private void awaitPeerIdConnections(final Set<String> peerIds) {
    await(
        () -> assertThat(nodeRpc.getConnectedPeerIds().containsAll(peerIds)).isTrue(),
        "Failed to connect in time to peers: %s",
        peerIds);
  }

  private Set<String> expectedPeerIds(final Collection<Besu> peers) {
    return peers
        .parallelStream()
        .map(node -> ensureHexPrefix(node.getNodeId()))
        .collect(Collectors.toSet());
  }

  private Set<String> excludeSelf(final Set<String> peers) {
    return peers
        .parallelStream()
        .filter(peer -> !peer.contains(nodeId))
        .collect(Collectors.toSet());
  }

  private HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_RPC_PORT);
  }

  private Set<Supplier<String>> dockerLogs() {
    return Set.of(() -> getLogs());
  }

  private void logPortMappings() {
    LOG.info(
        "Besu Container: {}, HTTP RPC port mapping: {} -> {}, WS RPC port mapping: {} -> {}, p2p port mapping: {} -> {}",
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
      LOG.info("Besu Container: {}, has no network", besu.getContainerId());
    } else {
      LOG.info(
          "Besu Container: {}, IP address: {}, Network: {}",
          besu.getContainerId(),
          besu.getContainerIpAddress(),
          besu.getNetwork().getId());
    }
  }

  private List<String> standardCommandLineOptions() {
    return Lists.newArrayList(
        "--logging",
        "DEBUG",
        "--miner-enabled",
        "--miner-coinbase",
        "1b23ba34ca45bb56aa67bc78be89ac00ca00da00",
        "--host-whitelist",
        "*",
        "--rpc-http-enabled",
        "--rpc-ws-enabled",
        "--rpc-http-apis",
        "ADMIN,ETH,NET,WEB3,EEA,PRIV");
  }

  private void addPeerToPeerHost(
      final BesuConfiguration config, final List<String> commandLineOptions) {
    commandLineOptions.add("--p2p-host");
    commandLineOptions.add(config.getIpAddress().get());
  }

  private void addBootnodeAddress(
      final BesuConfiguration config, final List<String> commandLineOptions) {
    config
        .getBootnodeEnodeAddress()
        .ifPresent(enode -> commandLineOptions.addAll(Lists.newArrayList("--bootnodes", enode)));
  }

  private void addContainerNetwork(
      final BesuConfiguration config, final GenericContainer<?> container) {
    container.withNetwork(config.getContainerNetwork());
  }

  private void addCorsOrigins(
      final BesuConfiguration config, final List<String> commandLineOptions) {

    config
        .getCors()
        .ifPresent(
            cors -> commandLineOptions.addAll(Lists.newArrayList("--rpc-http-cors-origins", cors)));
  }

  private void addNodePrivateKey(
      final BesuConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {

    container.withClasspathResourceMapping(
        config.getNodeKeyPrivateKeyResource().get(),
        CONTAINER_NODE_PRIVATE_KEY_FILE,
        BindMode.READ_ONLY);
    commandLineOptions.addAll(
        Lists.newArrayList("--node-private-key-file", CONTAINER_NODE_PRIVATE_KEY_FILE));
  }

  private void addGenesisFile(
      final BesuConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {
    commandLineOptions.add("--genesis-file");
    commandLineOptions.add(CONTAINER_GENESIS_FILE);
    container.withCopyFileToContainer(
        MountableFile.forHostPath(config.getGenesisFile()), CONTAINER_GENESIS_FILE);
  }

  private void addPrivacy(
      final BesuConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {

    checkArgument(
        config.getPrivacyUrl() != null && config.getPrivacyUrl().isPresent(),
        "Privacy URL is mandatory when using Privacy");
    checkArgument(
        config.getPrivacyMarkerSigningPrivateKeyFile() != null
            && config.getPrivacyMarkerSigningPrivateKeyFile().isPresent(),
        "Private Marker Transaction key file is mandatory when using Privacy");

    commandLineOptions.add("--privacy-enabled");
    commandLineOptions.add("--privacy-url");
    commandLineOptions.add(config.getPrivacyUrl().get());
    commandLineOptions.add("--privacy-public-key-file");
    commandLineOptions.add(CONTAINER_PRIVACY_PUBLIC_KEY_FILE);
    container.withClasspathResourceMapping(
        config.getPrivacyPublicKeyResource(),
        CONTAINER_PRIVACY_PUBLIC_KEY_FILE,
        BindMode.READ_ONLY);
    commandLineOptions.add("--privacy-marker-transaction-signing-key-file");
    commandLineOptions.add(CONTAINER_PRIVACY_SIGNING_PRIVATE_KEY_FILE);

    container.withClasspathResourceMapping(
        config.getPrivacyMarkerSigningPrivateKeyFile().get(),
        CONTAINER_PRIVACY_SIGNING_PRIVATE_KEY_FILE,
        BindMode.READ_ONLY);
  }

  private void addContainerIpAddress(
      final SubnetAddress ipAddress, final GenericContainer<?> container) {
    container.withCreateContainerCmdModifier(modifier -> modifier.withIpv4Address(ipAddress.get()));
  }

  private String nodePublicKey(final BesuConfiguration config) {
    return removeAnyHexPrefix(ClasspathResources.read(config.getNodeKeyPublicKeyResource().get()));
  }

  private String enodeAddress(final BesuConfiguration config) {
    return String.format(
        "enode://%s@%s:%d", pubKey, config.getIpAddress().get(), CONTAINER_P2P_PORT);
  }
}
