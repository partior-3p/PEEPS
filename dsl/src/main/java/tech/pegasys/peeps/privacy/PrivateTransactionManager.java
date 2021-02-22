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
package tech.pegasys.peeps.privacy;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.peeps.privacy.rpc.send.SendPayload.generateUniquePayload;

import tech.pegasys.peeps.network.NetworkMember;
import tech.pegasys.peeps.privacy.model.PrivacyPrivateKeyResource;
import tech.pegasys.peeps.privacy.model.PrivacyPublicKeyResource;
import tech.pegasys.peeps.privacy.model.TransactionManagerKey;
import tech.pegasys.peeps.privacy.rpc.TransactionManagerRpc;
import tech.pegasys.peeps.privacy.rpc.TransactionManagerRpcExpectingData;
import tech.pegasys.peeps.util.ClasspathResources;
import tech.pegasys.peeps.util.DockerLogs;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public abstract class PrivateTransactionManager implements NetworkMember {
  private static final Logger LOG = LogManager.getLogger();

  private static final int CONTAINER_PEER_TO_PEER_PORT = 8080;
  private static final int CONTAINER_HTTP_RPC_PORT = 8888;
  private static final int ALIVE_STATUS_CODE = 200;
  private static final String AM_I_ALIVE_ENDPOINT = "/upcheck";
  protected static final String CONTAINER_CONFIG_FILE = "/etc/transaction_manager.conf";

  protected final GenericContainer<?> container;
  private final TransactionManagerRpc transactionManagerRpc;
  private final TransactionManagerRpcExpectingData rpc;

  private final String networkP2PAddress;
  private final String networkRpcAddress;

  protected final String id;

  public PrivateTransactionManager(
      final PrivateTransactionManagerConfiguration config, final GenericContainer<?> container) {
    this.container = container;
    this.networkP2PAddress =
        String.format("http://%s:%s", config.getIpAddress().get(), CONTAINER_PEER_TO_PEER_PORT);
    this.networkRpcAddress =
        String.format("http://%s:%s", config.getIpAddress().get(), CONTAINER_HTTP_RPC_PORT);

    // TODO just using the first key, selecting the identity could be an option for
    // multi-key TransactionManager
    this.id = ClasspathResources.read(config.getPublicKeys().get(0).get());
    this.transactionManagerRpc = new TransactionManagerRpc(config.getVertx(), id, dockerLogs());
    this.rpc = new TransactionManagerRpcExpectingData(transactionManagerRpc);
  }

  public void awaitConnectivity(final Collection<PrivateTransactionManager> collection) {
    collection.parallelStream().forEach(this::awaitConnectivity);
  }

  @Override
  public void start() {
    try {
      container.start();

      container.followOutput(
          outputFrame -> LOG.info("{}: {}", getNodeName(), outputFrame.getUtf8String()));

      transactionManagerRpc.bind(
          container.getContainerId(),
          container.getContainerIpAddress(),
          container.getMappedPort(CONTAINER_HTTP_RPC_PORT));

      // TODO validate the node has the expected state, e.g. consensus, genesis,
      // networkId,
      // protocol(s), ports, listen address

      logTransactionManagerDetails();
      logPortMappings();
      logContainerNetworkDetails();
    } catch (final ContainerLaunchException e) {
      LOG.error(container.getLogs());
      throw e;
    }
  }

  @Override
  public void stop() {
    if (container != null) {
      container.stop();
    }
    if (transactionManagerRpc != null) {
      transactionManagerRpc.close();
    }
  }

  public abstract String getNodeName();

  public String getPeerNetworkAddress() {
    return networkP2PAddress;
  }

  public String getId() {
    return id;
  }

  public TransactionManagerRpcExpectingData getRpc() {
    return rpc;
  }

  public String getNetworkRpcAddress() {
    return networkRpcAddress;
  }

  // TODO stronger typing than String
  public String getPayload(final TransactionManagerKey key) {
    return rpc.receive(key);
  }

  private Set<Supplier<String>> dockerLogs() {
    return Set.of(this::getLogs);
  }

  protected HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_RPC_PORT);
  }

  private void awaitConnectivity(final PrivateTransactionManager peer) {
    final String message = generateUniquePayload();

    final TransactionManagerKey key = rpc.send(peer.getId(), message);
    assertThat(key).isNotNull();

    assertReceived(rpc, key, message);
    assertReceived(peer.getRpc(), key, message);
  }

  private void assertReceived(
      final TransactionManagerRpcExpectingData rpc,
      final TransactionManagerKey key,
      final String sentMessage) {
    assertThat(rpc.receive(key)).isEqualTo(sentMessage);
  }

  protected void addPrivateKeys(
      final PrivateTransactionManagerConfiguration config,
      final String containerWorkingDir,
      final GenericContainer<?> container) {
    for (final PrivacyPrivateKeyResource key : config.getPrivateKeys()) {
      final String location = key.get();
      container.withClasspathResourceMapping(
          location, Path.of(containerWorkingDir, location).toString(), BindMode.READ_ONLY);
    }
  }

  protected void addPublicKeys(
      final PrivateTransactionManagerConfiguration config,
      final String containerWorkingDir,
      final GenericContainer<?> container) {
    for (final PrivacyPublicKeyResource key : config.getPublicKeys()) {
      final String location = key.get();
      container.withClasspathResourceMapping(
          location, Path.of(containerWorkingDir, location).toString(), BindMode.READ_ONLY);
    }
  }

  public String getLogs() {
    return DockerLogs.format("TransactionManager", container);
  }

  private void logTransactionManagerDetails() {
    LOG.info("TransactionManager Container: {}, ID: {}", container.getContainerId(), id);
  }

  private void logContainerNetworkDetails() {
    if (container.getNetwork() == null) {
      LOG.info("TransactionManager Container: {}, has no network", container.getContainerId());
    } else {
      LOG.info(
          "TransactionManager Container: {}, IP address: {}, Network: {}",
          container.getContainerId(),
          container.getContainerIpAddress(),
          container.getNetwork().getId());
    }
  }

  private void logPortMappings() {
    LOG.info(
        "TransactionManager Container: {}, HTTP RPC port mapping: {} -> {}, p2p port mapping: {} -> {}",
        container.getContainerId(),
        CONTAINER_HTTP_RPC_PORT,
        container.getMappedPort(CONTAINER_HTTP_RPC_PORT),
        CONTAINER_PEER_TO_PEER_PORT,
        container.getMappedPort(CONTAINER_PEER_TO_PEER_PORT));
  }

  protected void addContainerNetwork(
      final PrivateTransactionManagerConfiguration config, final GenericContainer<?> container) {
    container.withNetwork(config.getContainerNetwork());
  }

  protected void addContainerIpAddress(
      final PrivateTransactionManagerConfiguration config, final GenericContainer<?> container) {
    container.withCreateContainerCmdModifier(
        modifier -> modifier.withIpv4Address(config.getIpAddress().get()));
  }

  protected void addConfigurationFile(
      final PrivateTransactionManagerConfiguration config, final GenericContainer<?> container) {
    container.withCopyFileToContainer(
        MountableFile.forHostPath(config.getFileSystemConfigurationFile()), CONTAINER_CONFIG_FILE);
  }
}
