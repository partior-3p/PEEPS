/*
 * Copyright 2021 ConsenSys AG.
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
import static tech.pegasys.peeps.util.Await.await;
import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import tech.pegasys.peeps.json.rpc.JsonRpcClient;
import tech.pegasys.peeps.network.NetworkMember;
import tech.pegasys.peeps.network.subnet.SubnetAddress;
import tech.pegasys.peeps.node.model.EnodeHelpers;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.rpc.QbftRpc;
import tech.pegasys.peeps.node.rpc.admin.NodeInfo;
import tech.pegasys.peeps.node.verification.AccountValue;
import tech.pegasys.peeps.node.verification.NodeValueTransition;
import tech.pegasys.peeps.signer.rpc.SignerRpcClient;
import tech.pegasys.peeps.signer.rpc.SignerRpcMandatoryResponse;
import tech.pegasys.peeps.util.AddressConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.eth.Address;
import org.testcontainers.containers.GenericContainer;

public abstract class Web3Provider implements NetworkMember {

  private static final Logger LOG = LogManager.getLogger();

  public static final int CONTAINER_HTTP_RPC_PORT = 8545;
  public static final int CONTAINER_WS_RPC_PORT = 8546;
  public static final int CONTAINER_P2P_PORT = 30303;

  protected final SignerRpcMandatoryResponse signerRpcResponse;
  protected final JsonRpcClient jsonRpcClient;

  protected GenericContainer<?> container;
  private final SubnetAddress ipAddress;
  private final String identity;
  private final String enodeAddress;
  private final String pubKey;

  private String nodeId;
  private String enodeId;

  public Web3Provider(final Web3ProviderConfiguration config, final GenericContainer<?> container) {
    this.container = container;
    this.jsonRpcClient =
        new JsonRpcClient(config.getVertx(), Duration.ofSeconds(10), LOG, dockerLogs());
    final SignerRpcClient signerRpcClient = new SignerRpcClient(jsonRpcClient, qbftRpc(config));
    this.signerRpcResponse = new SignerRpcMandatoryResponse(signerRpcClient);
    this.ipAddress = config.getIpAddress();

    this.identity = config.getIdentity();
    this.pubKey = removeAnyHexPrefix(config.getNodeKeys().publicKey().toHexString());
    this.enodeAddress = enodeAddress(config);
  }

  protected abstract QbftRpc qbftRpc(final Web3ProviderConfiguration config);

  @Override
  public void start() {
    try {
      container.start();

      container.followOutput(
          outputFrame -> LOG.info("{}: {}", identity, outputFrame.getUtf8String().stripTrailing()));

      jsonRpcClient.bind(
          container.getContainerId(),
          container.getContainerIpAddress(),
          container.getMappedPort(CONTAINER_HTTP_RPC_PORT));

      final NodeInfo info = signerRpcResponse.nodeInfo();
      nodeId = info.getId();

      // TODO enode must match enodeAddress - otherwise error
      // TODO remove enodeId - then rename enodeAddress to enodeId
      enodeId = info.getEnode();

      // TODO validate the node has the expected state, e.g. consensus, genesis,
      // networkId,
      // protocol(s), ports, listen address

      logPortMappings();
      logContainerNetworkDetails();
    } catch (final Throwable e) {
      LOG.error(container.getLogs());
      throw e;
    }
  }

  @Override
  public void stop() {
    if (container != null) {
      container.stop();
    }
    if (jsonRpcClient != null) {
      jsonRpcClient.close();
    }
  }

  public SubnetAddress ipAddress() {
    return ipAddress;
  }

  // TODO these may not have a value, i.e. node not started :. optional
  public String getEnodeId() {
    return enodeId;
  }

  // TODO stricter typing then String
  public String enodeAddress() {
    return enodeAddress;
  }

  public String nodePublicKey() {
    return pubKey;
  }

  public String identity() {
    return identity;
  }

  public int httpRpcPort() {
    return CONTAINER_HTTP_RPC_PORT;
  }

  public int p2pPort() {
    return CONTAINER_P2P_PORT;
  }

  public Address address() {
    return AddressConverter.fromPublicKey(pubKey);
  }

  public void awaitConnectivity(final Collection<Web3Provider> peers) {
    awaitPeerIdConnections(excludeSelf(expectedEnodes(peers)));
  }

  private void awaitPeerIdConnections(final Set<String> peerEnodes) {
    await(
        () -> {
          final Set<String> peerPubKeys = EnodeHelpers.extractPubKeysFromEnodes(peerEnodes);
          final Set<String> connectedPeerPubKeys =
              EnodeHelpers.extractPubKeysFromEnodes(signerRpcResponse.getConnectedPeerIds());
          LOG.info(
              "Connected peersPubKeys {} expected peersPubKeys {}",
              connectedPeerPubKeys,
              peerPubKeys);
          assertThat(connectedPeerPubKeys).containsExactlyInAnyOrderElementsOf(peerPubKeys);
        },
        "Failed to connect in time to peers: %s",
        peerEnodes);
  }

  private Set<String> expectedEnodes(final Collection<Web3Provider> peers) {
    return peers.parallelStream().map(Web3Provider::getEnodeId).collect(Collectors.toSet());
  }

  private Set<String> excludeSelf(final Set<String> peers) {
    return peers
        .parallelStream()
        .filter(peer -> !peer.contains(getEnodeId()))
        .collect(Collectors.toSet());
  }

  public String getNodeId() {
    checkNotNull(nodeId, "NodeId only exists after the node has started");
    return nodeId;
  }

  public void verifyValue(final Set<AccountValue> values) {
    values.parallelStream().forEach(value -> value.verify(signerRpcResponse));
  }

  protected Set<Supplier<String>> dockerLogs() {
    return Set.of(this::getLogs);
  }

  public abstract String getLogs();

  public SignerRpcMandatoryResponse rpc() {
    return signerRpcResponse;
  }

  public void verifyTransition(final NodeValueTransition... changes) {
    Stream.of(changes).parallel().forEach(change -> change.verify(signerRpcResponse));
  }

  public void verifySuccessfulTransactionReceipt(final Hash transaction) {
    final TransactionReceipt receipt = signerRpcResponse.getTransactionReceipt(transaction);

    assertThat(receipt.getTransactionHash()).isEqualTo(transaction);
    assertThat(receipt.isSuccess()).isTrue();
  }

  private String enodeAddress(final Web3ProviderConfiguration config) {
    return String.format(
        "enode://%s@%s:%d", pubKey, config.getIpAddress().get(), CONTAINER_P2P_PORT);
  }

  private void logPortMappings() {
    LOG.info(
        "Web3Provider Container: {}, HTTP RPC port mapping: {} -> {}, WS RPC port mapping: {} -> {}, p2p port mapping: {} -> {}",
        container.getContainerId(),
        CONTAINER_HTTP_RPC_PORT,
        container.getMappedPort(CONTAINER_HTTP_RPC_PORT),
        CONTAINER_WS_RPC_PORT,
        container.getMappedPort(CONTAINER_WS_RPC_PORT),
        CONTAINER_P2P_PORT,
        container.getMappedPort(CONTAINER_P2P_PORT));
  }

  private void logContainerNetworkDetails() {
    if (container.getNetwork() == null) {
      LOG.info("Web3Provider Container: {}, has no network", container.getContainerId());
    } else {
      LOG.info(
          "Web3Provider Container: {}, IP address: {}, Network: {}",
          container.getContainerId(),
          container.getContainerIpAddress(),
          container.getNetwork().getId());
    }
  }

  protected void addContainerIpAddress(
      final SubnetAddress ipAddress, final GenericContainer<?> container) {
    container.withCreateContainerCmdModifier(modifier -> modifier.withIpv4Address(ipAddress.get()));
  }

  protected Path createMountableTempFile(final Bytes content) {
    final Path tempFile;
    try {
      tempFile = Files.createTempFile("nodekey", ".priv");
      Files.setPosixFilePermissions(tempFile, PosixFilePermissions.fromString("rwxrwxrwx"));
      Files.write(tempFile, content.toArray());
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create node key file", e);
    }
    return tempFile;
  }
}
