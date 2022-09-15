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

import tech.pegasys.peeps.node.rpc.QbftRpc;
import tech.pegasys.peeps.node.rpc.QuorumQbftRpcClient;
import tech.pegasys.peeps.util.DockerLogs;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public class GoQuorum extends Web3Provider {

  private static final Logger LOG = LogManager.getLogger();

  private static final String IMAGE_NAME = "quorumengineering/quorum:%s";
  private static final String CONTAINER_GENESIS_FILE = "/etc/genesis.json";
  private static final String CONTAINER_STATIC_NODES_FILE = "/eth/geth/static-nodes.json";
  private static final String CONTAINER_NODE_PRIVATE_KEY_FILE = "/etc/keys/node.priv";
  private static final String DATA_DIR = "/eth";
  private static final String KEYSTORE_DIR = "/eth/keystore/";
  private static final String CONTAINER_PASSWORD_FILE = KEYSTORE_DIR + "password";

  public GoQuorum(final Web3ProviderConfiguration config) {
    super(
        config,
        new GenericContainer<>(String.format(IMAGE_NAME, config.getImageVersion()))
            .withImagePullPolicy(new LocalAgeBasedPullPolicy(Duration.ofHours(1))));

    final List<String> commandLineOptions = standardCommandLineOptions();
    addCorsOrigins(config, commandLineOptions);
    addContainerNetwork(config, container);
    addContainerIpAddress(ipAddress(), container);
    addWallets(config, commandLineOptions, container);
    addStaticNodesFile(config, container);
    commandLineOptions.addAll(List.of("--datadir", "\"" + DATA_DIR + "\""));
    commandLineOptions.addAll(List.of("--networkid", "15"));
    commandLineOptions.addAll(List.of("--identity", config.getIdentity()));

    // Note: this copy occurs on container start (not now, as the genesis file is empty at this
    // stage)
    container.withCopyFileToContainer(
        MountableFile.forHostPath(config.getGenesisFile()), CONTAINER_GENESIS_FILE);
    final List<String> entryPoint = Lists.newArrayList("/bin/sh", "-c");
    final String initCmd =
        "mkdir -p '"
            + DATA_DIR
            + "/geth' && "
            + "mkdir -p '"
            + KEYSTORE_DIR
            + "' && "
            + "geth --datadir \""
            + DATA_DIR
            + "\" init "
            + CONTAINER_GENESIS_FILE
            + " && "
            + " echo '##### GoQuorum INITIALISED #####\n\n' && ";

    addNodePrivateKey(config, commandLineOptions, container);
    //    if (config.isPrivacyEnabled()) {
    //      addPrivacy(config, commandLineOptions, dockerContainer);
    //    }

    final String goCommandLine =
        initCmd + "exec geth " + String.join(" ", commandLineOptions) + " 2>&1\n";

    LOG.info("GoQuorum command line: {}", goCommandLine);

    entryPoint.add(goCommandLine);

    container
        .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint(entryPoint))
        .waitingFor(liveliness());
  }

  @Override
  public String getLogs() {
    return DockerLogs.format("GoQuorum", container);
  }

  @Override
  public void setQBFTValidatorSmartContractTransition(
      final BigInteger blockNumber, final String contractAddress) {

    try {
      final ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(genesisFile);

      ArrayNode transitions = ((ArrayNode) jsonNode.path("config").path("transitions"));

      if (transitions.isEmpty()) {
        ObjectNode transition = mapper.createObjectNode();
        transition.put("block", blockNumber);
        transition.put("validatorselectionmode", "contract");
        transition.put("validatorcontractaddress", contractAddress);
        transitions.add(transition);
      }

      mapper.writeValue(genesisFile, jsonNode);
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  @Override
  protected QbftRpc qbftRpc(final Web3ProviderConfiguration config) {
    return new QuorumQbftRpcClient(jsonRpcClient);
  }

  private AbstractWaitStrategy liveliness() {
    return Wait.forLogMessage(".*endpoint=0.0.0.0:8545.*", 1);
  }

  private List<String> standardCommandLineOptions() {
    return Lists.newArrayList(
        "--allow-insecure-unlock",
        "--verbosity",
        "3",
        "--syncmode",
        "full",
        "--mine",
        "--http",
        "--http.addr",
        "\"0.0.0.0\"",
        "--http.port",
        "8545",
        "--http.api",
        "admin,debug,web3,eth,txpool,personal,clique,miner,net,istanbul",
        "--ws",
        // TODO: put back when [Upgrade] Go-Ethereum release v1.10.2 #1391 is merged
        // "--log.debug",
        "--nodiscover",
        "--rpc.allow-unprotected-txs");
  }

  private void addContainerNetwork(
      final Web3ProviderConfiguration config, final GenericContainer<?> container) {
    container.withNetwork(config.getContainerNetwork());
  }

  private void addCorsOrigins(
      final Web3ProviderConfiguration config, final List<String> commandLineOptions) {
    config
        .getCors()
        .ifPresent(
            cors -> commandLineOptions.addAll(Lists.newArrayList("--http.corsdomain", cors)));
  }

  private void addNodePrivateKey(
      final Web3ProviderConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {
    final Path keyFile =
        createMountableTempFile(
            Bytes.wrap(
                config
                    .getNodeKeys()
                    .secretKey()
                    .bytes()
                    .toUnprefixedHexString()
                    .getBytes(StandardCharsets.UTF_8)));

    container.withCopyFileToContainer(
        MountableFile.forHostPath(keyFile), CONTAINER_NODE_PRIVATE_KEY_FILE);
    commandLineOptions.addAll(Lists.newArrayList("--nodekey", CONTAINER_NODE_PRIVATE_KEY_FILE));
  }

  private void addWallets(
      final Web3ProviderConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {
    config
        .getWallet()
        .ifPresent(
            wallet -> {
              container.withClasspathResourceMapping(
                  wallet.resources().getKey().get(), KEYSTORE_DIR, BindMode.READ_ONLY);
              container.withClasspathResourceMapping(
                  wallet.resources().getPassword().get(),
                  CONTAINER_PASSWORD_FILE,
                  BindMode.READ_ONLY);
              commandLineOptions.addAll(
                  List.of(
                      "--unlock",
                      wallet.getCredentials().getAddress(),
                      "--password",
                      CONTAINER_PASSWORD_FILE));
              commandLineOptions.addAll(
                  List.of("--miner.etherbase", wallet.getCredentials().getAddress()));
            });
  }

  private void addStaticNodesFile(
      final Web3ProviderConfiguration config, final GenericContainer<?> container) {
    container.withCopyFileToContainer(
        MountableFile.forHostPath(config.getStaticNodesFile()), CONTAINER_STATIC_NODES_FILE);
  }

  //  private void addPrivacy(
  //      final Web3ProviderConfiguration config,
  //      final List<String> commandLineOptions,
  //      final GenericContainer<?> container) {
  //
  //    checkArgument(
  //        config.getPrivacyUrl() != null && config.getPrivacyUrl().isPresent(),
  //        "Privacy URL is mandatory when using Privacy");
  //    checkArgument(
  //        config.getPrivacyMarkerSigningPrivateKeyFile() != null
  //            && config.getPrivacyMarkerSigningPrivateKeyFile().isPresent(),
  //        "Private Marker Transaction key file is mandatory when using Privacy");
  //
  //    commandLineOptions.add("--privacy-enabled");
  //    commandLineOptions.add("--privacy-url");
  //    commandLineOptions.add(config.getPrivacyUrl().get());
  //    commandLineOptions.add("--privacy-public-key-file");
  //    commandLineOptions.add(CONTAINER_PRIVACY_PUBLIC_KEY_FILE);
  //    container.withClasspathResourceMapping(
  //        config.getPrivacyPublicKeyResource(),
  //        CONTAINER_PRIVACY_PUBLIC_KEY_FILE,
  //        BindMode.READ_ONLY);
  //    commandLineOptions.add("--privacy-marker-transaction-signing-key-file");
  //    commandLineOptions.add(CONTAINER_PRIVACY_SIGNING_PRIVATE_KEY_FILE);
  //
  //    container.withClasspathResourceMapping(
  //        config.getPrivacyMarkerSigningPrivateKeyFile().get(),
  //        CONTAINER_PRIVACY_SIGNING_PRIVATE_KEY_FILE,
  //        BindMode.READ_ONLY);
  //  }
}
