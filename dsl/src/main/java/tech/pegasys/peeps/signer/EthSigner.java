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

import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.privacy.Orion;
import tech.pegasys.peeps.signer.rpc.SignerRpcClient;
import tech.pegasys.peeps.util.Await;

import java.time.Duration;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

public class EthSigner {

  private static final Logger LOG = LogManager.getLogger();

  private static final String AM_I_ALIVE_ENDPOINT = "/upcheck";
  private static final int ALIVE_STATUS_CODE = 200;

  //  private static final String ETH_SIGNER_IMAGE = "pegasyseng/ethsigner:latest";
  private static final String ETH_SIGNER_IMAGE = "pegasyseng/ethsigner:develop";
  private static final String CONTAINER_DATA_PATH = "/etc/ethsigner/tmp/";
  private static final int CONTAINER_HTTP_RPC_PORT = 8545;
  private static final Duration DOWNSTREAM_TIMEOUT = Duration.ofSeconds(10);

  private static final String CONTAINER_KEY_FILE = "/etc/ethsigner/key_file.v3";
  private static final String CONTAINER_PASSWORD_FILE = "/etc/ethsigner/password_file.txt";

  // TODO need a rpcClient to send stuff to the signer
  private final GenericContainer<?> ethSigner;
  private final SignerRpcClient rpc;

  // TODO better typing

  // TODO enter this perhaps
  // TODO this is stored in the wallet file as address - can be read in EthSigner
  private final String senderAccount = "0xf17f52151ebef6c7334fad080c5704d77216b732";
  //  private final String senderAccount = "0x627306090abab3a6e1400e9345bc60c78a8bef57";

  // TODO need to know about the Besu we are talking to, can output docker logs
  // TODO for privacy transaction need the Orion logs too
  public EthSigner(final EthSignerConfiguration config) {

    final GenericContainer<?> container = new GenericContainer<>(ETH_SIGNER_IMAGE);
    final List<String> commandLineOptions = standardCommandLineOptions();

    addChainId(config, commandLineOptions);
    addDownstreamPort(config, commandLineOptions);
    addDownstreamHost(config, commandLineOptions);
    addContainerNetwork(config, container);
    addContainerIpAddress(config, container);
    addFileBasedSigner(config, commandLineOptions, container);

    LOG.info("EthSigner command line {}", commandLineOptions);

    this.ethSigner =
        container.withCommand(commandLineOptions.toArray(new String[0])).waitingFor(liveliness());

    this.rpc = new SignerRpcClient(config.getVertx(), DOWNSTREAM_TIMEOUT);
  }

  public void start() {
    try {
      ethSigner.start();

      rpc.bind(
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

  public void stop() {
    if (ethSigner != null) {
      ethSigner.stop();
    }
    if (rpc != null) {
      rpc.close();
    }
  }

  // TODO could config a EthSigner to be bound to a node & orion setup?
  public String deployContractToPrivacyGroup(
      final String binary, final Orion sender, final Orion... recipients) {
    final String[] privateRecipients = new String[recipients.length];
    for (int i = 0; i < recipients.length; i++) {
      privateRecipients[i] = recipients[i].getId();
    }

    // TODO catch error & log EthSigner & Besu & Orion docker logs
    return rpc.deployContractToPrivacyGroup(
        senderAccount, binary, sender.getId(), privateRecipients);
  }

  private HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_RPC_PORT);
  }

  private List<String> standardCommandLineOptions() {
    return Lists.newArrayList(
        "--logging",
        "INFO",
        "--data-path",
        CONTAINER_DATA_PATH,
        "--http-listen-host",
        "0.0.0.0",
        "--http-listen-port",
        String.valueOf(CONTAINER_HTTP_RPC_PORT),
        "--downstream-http-request-timeout",
        String.valueOf(DOWNSTREAM_TIMEOUT.toMillis()));
  }

  private void logContainerNetworkDetails() {
    if (ethSigner.getNetwork() == null) {
      LOG.info("EthSigner Container {} has no network", ethSigner.getContainerId());
    } else {
      LOG.info(
          "EthSigner Container {}, IP address: {}, Network: {}",
          ethSigner.getContainerId(),
          ethSigner.getContainerIpAddress(),
          ethSigner.getNetwork().getId());
    }
  }

  private void logPortMappings() {
    LOG.info(
        "EthSigner Container {}, HTTP RPC port mapping: {} -> {}",
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
    commandLineOptions.add(String.valueOf(config.getDownstreamPort()));
  }

  private void addDownstreamHost(
      final EthSignerConfiguration config, final List<String> commandLineOptions) {
    commandLineOptions.add("--downstream-http-host");
    commandLineOptions.add(config.getDownstreamHost());
  }

  private void addContainerNetwork(
      final EthSignerConfiguration config, final GenericContainer<?> container) {
    container.withNetwork(config.getContainerNetwork());
  }

  private void addContainerIpAddress(
      final EthSignerConfiguration config, final GenericContainer<?> container) {
    container.withCreateContainerCmdModifier(
        modifier -> modifier.withIpv4Address(config.getIpAddress()));
  }

  private void addFileBasedSigner(
      final EthSignerConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {
    commandLineOptions.add("file-based-signer");

    commandLineOptions.add("--key-file");
    commandLineOptions.add(CONTAINER_KEY_FILE);
    container.withClasspathResourceMapping(
        config.getKeyFile(), CONTAINER_KEY_FILE, BindMode.READ_ONLY);

    commandLineOptions.add("--password-file");
    commandLineOptions.add(CONTAINER_PASSWORD_FILE);
    container.withClasspathResourceMapping(
        config.getPasswordFile(), CONTAINER_PASSWORD_FILE, BindMode.READ_ONLY);
  }

  public void awaitConnectivity(final Besu node) {
    Await.await(
        () -> assertThat(rpc.enode()).isEqualTo(node.getEnodeId()),
        String.format("Failed to connect to node: %s", node.getEnodeId()));
  }
}
