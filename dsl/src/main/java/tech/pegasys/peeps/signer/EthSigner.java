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
package tech.pegasys.peeps.signer;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.peeps.util.Await.await;

import tech.pegasys.peeps.json.rpc.JsonRpcClient;
import tech.pegasys.peeps.network.NetworkMember;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.model.EnodeHelpers;
import tech.pegasys.peeps.node.rpc.BesuQbftRpcClient;
import tech.pegasys.peeps.signer.rpc.SignerRpc;
import tech.pegasys.peeps.signer.rpc.SignerRpcClient;
import tech.pegasys.peeps.signer.rpc.SignerRpcMandatoryResponse;
import tech.pegasys.peeps.util.DockerLogs;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

public class EthSigner implements NetworkMember {

  private static final Logger LOG = LogManager.getLogger();

  private static final String AM_I_ALIVE_ENDPOINT = "/upcheck";
  private static final int ALIVE_STATUS_CODE = 200;

  private static final String ETH_SIGNER_IMAGE = "consensys/quorum-ethsigner:develop";
  private static final int CONTAINER_HTTP_RPC_PORT = 8545;
  private static final Duration DOWNSTREAM_TIMEOUT = Duration.ofSeconds(10);
  private static final String CONTAINER_KEY_FILE = "/etc/ethsigner/key_file.v3";
  private static final String CONTAINER_PASSWORD_FILE = "/etc/ethsigner/password_file.txt";

  private final GenericContainer<?> ethSigner;
  private final JsonRpcClient jsonRpcClient;
  private final SignerRpc rpc;
  private final Web3Provider downstream;

  public EthSigner(final EthSignerConfiguration config) {

    final GenericContainer<?> container = new GenericContainer<>(ETH_SIGNER_IMAGE);
    final List<String> commandLineOptions = standardCommandLineOptions();

    addChainId(config, commandLineOptions);
    addDownstreamPort(config, commandLineOptions);
    addDownstreamHost(config, commandLineOptions);
    addContainerNetwork(config, container);
    addContainerIpAddress(config, container);
    addFileBasedSigner(config, commandLineOptions, container);

    LOG.info("EthSigner command line: {}", commandLineOptions);

    this.downstream = config.getDownstream();
    this.ethSigner =
        container.withCommand(commandLineOptions.toArray(new String[0])).waitingFor(liveliness());

    jsonRpcClient = new JsonRpcClient(config.getVertx(), DOWNSTREAM_TIMEOUT, LOG, dockerLogs());
    final BesuQbftRpcClient qbftRpc = new BesuQbftRpcClient(jsonRpcClient);
    final SignerRpcClient signerRpc = new SignerRpcClient(jsonRpcClient, qbftRpc);
    this.rpc = new SignerRpcMandatoryResponse(signerRpc);
  }

  @Override
  public void start() {
    try {
      ethSigner.start();

      jsonRpcClient.bind(
          ethSigner.getContainerId(),
          ethSigner.getContainerIpAddress(),
          ethSigner.getMappedPort(CONTAINER_HTTP_RPC_PORT));

      // TODO validate the node has the expected state, e.g. consensus, genesis, networkId,
      // protocol(s), ports, listen address

      logPortMappings();
      logContainerNetworkDetails();
    } catch (final ContainerLaunchException e) {
      LOG.error(ethSigner.getLogs());
      throw e;
    }
  }

  @Override
  public void stop() {
    if (ethSigner != null) {
      ethSigner.stop();
    }
    if (jsonRpcClient != null) {
      jsonRpcClient.close();
    }
  }

  public SignerRpc rpc() {
    return rpc;
  }

  public void awaitConnectivityToDownstream() {
    await(
        () ->
            assertThat(EnodeHelpers.extractPubKeyFromEnode(rpc.nodeInfo().getEnode()))
                .isEqualTo(EnodeHelpers.extractPubKeyFromEnode(downstream.getEnodeId())),
        "Failed to connect to node: %s",
        downstream.getEnodeId());
  }

  private String getLogs() {
    return DockerLogs.format("EthSigner", ethSigner);
  }

  private HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_RPC_PORT);
  }

  private Set<Supplier<String>> dockerLogs() {
    return Set.of(this::getLogs, downstream::getLogs);
  }

  private List<String> standardCommandLineOptions() {
    return Lists.newArrayList(
        "--logging",
        "DEBUG",
        "--http-listen-host",
        "0.0.0.0",
        "--http-listen-port",
        String.valueOf(CONTAINER_HTTP_RPC_PORT),
        "--downstream-http-request-timeout",
        String.valueOf(DOWNSTREAM_TIMEOUT.toMillis()));
  }

  private void logContainerNetworkDetails() {
    if (ethSigner.getNetwork() == null) {
      LOG.info("EthSigner Container: {}, has no network", ethSigner.getContainerId());
    } else {
      LOG.info(
          "EthSigner Container: {}, IP address: {}, Network: {}",
          ethSigner.getContainerId(),
          ethSigner.getContainerIpAddress(),
          ethSigner.getNetwork().getId());
    }
  }

  private void logPortMappings() {
    LOG.info(
        "EthSigner Container: {}, HTTP RPC port mapping: {} -> {}",
        ethSigner.getContainerId(),
        CONTAINER_HTTP_RPC_PORT,
        ethSigner.getMappedPort(CONTAINER_HTTP_RPC_PORT));
  }

  private void addChainId(
      final EthSignerConfiguration config, final List<String> commandLineOptions) {
    commandLineOptions.add("--chain-id");
    commandLineOptions.add(String.valueOf(config.getChainId()));
  }

  private void addDownstreamPort(
      final EthSignerConfiguration config, final List<String> commandLineOptions) {
    commandLineOptions.add("--downstream-http-port");
    commandLineOptions.add(String.valueOf(config.getDownstream().httpRpcPort()));
  }

  private void addDownstreamHost(
      final EthSignerConfiguration config, final List<String> commandLineOptions) {
    commandLineOptions.add("--downstream-http-host");
    commandLineOptions.add(config.getDownstream().ipAddress().get());
  }

  private void addContainerNetwork(
      final EthSignerConfiguration config, final GenericContainer<?> container) {
    container.withNetwork(config.getContainerNetwork());
  }

  private void addContainerIpAddress(
      final EthSignerConfiguration config, final GenericContainer<?> container) {
    container.withCreateContainerCmdModifier(
        modifier -> modifier.withIpv4Address(config.getIpAddress().get()));
  }

  private void addFileBasedSigner(
      final EthSignerConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {
    commandLineOptions.add("file-based-signer");

    commandLineOptions.add("--key-file");
    commandLineOptions.add(CONTAINER_KEY_FILE);
    container.withClasspathResourceMapping(
        config.getKeyFile().get(), CONTAINER_KEY_FILE, BindMode.READ_ONLY);

    commandLineOptions.add("--password-file");
    commandLineOptions.add(CONTAINER_PASSWORD_FILE);
    container.withClasspathResourceMapping(
        config.getPasswordFile().get(), CONTAINER_PASSWORD_FILE, BindMode.READ_ONLY);
  }
}
