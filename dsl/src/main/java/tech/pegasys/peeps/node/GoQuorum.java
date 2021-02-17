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

import tech.pegasys.peeps.util.DockerLogs;

import java.time.Duration;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.MountableFile;

public class GoQuorum extends Web3Provider {

  private static final Logger LOG = LogManager.getLogger();

  //  private static final String IMAGE_NAME = "hyperledger/besu:latest";
  private static final String IMAGE_NAME = "quorumengineering/quorum:latest";
  private static final String CONTAINER_GENESIS_FILE = "/etc/genesis.json";
  private static final String CONTAINER_NODE_PRIVATE_KEY_FILE = "/etc/keys/node.priv";
  private static final String DATA_DIR = "/eth";

  public GoQuorum(final Web3ProviderConfiguration config) {
    super(
        config,
        new GenericContainer<>(IMAGE_NAME)
            .withImagePullPolicy(PullPolicy.ageBased(Duration.ofHours(1))));
    final List<String> commandLineOptions = standardCommandLineOptions();

    addCorsOrigins(config, commandLineOptions);
    addBootnodeAddress(config, commandLineOptions);
    addContainerNetwork(config, container);
    addContainerIpAddress(ipAddress(), container);
    commandLineOptions.addAll(List.of("--datadir", "\"" + DATA_DIR + "\""));
    commandLineOptions.addAll(List.of("--networkid", "15"));

    container.withCopyFileToContainer(
        MountableFile.forHostPath(config.getGenesisFile()), CONTAINER_GENESIS_FILE);
    final List<String> entryPoint = Lists.newArrayList("/bin/sh", "-c");
    final String initCmd =
        "mkdir -p '"
            + DATA_DIR
            + "/geth' && geth --datadir \""
            + DATA_DIR
            + "\" init "
            + CONTAINER_GENESIS_FILE
            + " && echo '##### GoQuorum INITIALISED #####' && ";

    addNodePrivateKey(config, commandLineOptions, container);
    //    if (config.isPrivacyEnabled()) {
    //      addPrivacy(config, commandLineOptions, dockerContainer);
    //    }

    final String goCommandLine = initCmd + "geth " + String.join(" ", commandLineOptions);
    LOG.info("GoQuorum command line: {}", goCommandLine);

    entryPoint.add(goCommandLine);

    container
        .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint(entryPoint))
        .waitingFor(liveliness());
  }

  @Override
  public String getNodeName() {
    return "GoQuorum";
  }

  @Override
  public String getLogs() {
    return DockerLogs.format("GoQuorum", container);
  }

  private AbstractWaitStrategy liveliness() {
    return Wait.forLogMessage(".*endpoint=0.0.0.0:8545.*", 1);
  }

  private List<String> standardCommandLineOptions() {
    return Lists.newArrayList(
        "--nousb",
        "--verbosity",
        "5",
        "--rpccorsdomain",
        "\"*\"",
        "--syncmode",
        "full",
        //        "--mine",
        //        "--miner.etherbase",
        //        "fe3b557e8fb62b89f4916b721be55ceb828dbd73",
        "--rpc",
        "--rpcaddr",
        "\"0.0.0.0\"",
        "--rpcport",
        "8545",
        "--rpcapi",
        "admin,debug,web3,eth,txpool,personal,clique,miner,net",
        "--ws",
        "--debug");
  }

  private void addBootnodeAddress(
      final Web3ProviderConfiguration config, final List<String> commandLineOptions) {
    config
        .getBootnodeEnodeAddress()
        .ifPresent(
            enode -> {
              if (!enode.isEmpty()) {
                commandLineOptions.addAll(Lists.newArrayList("--bootnodes", enode));
              } else {
                commandLineOptions.addAll(Lists.newArrayList("--bootnodes", "\"\""));
              }
            });
  }

  private void addContainerNetwork(
      final Web3ProviderConfiguration config, final GenericContainer<?> container) {
    container.withNetwork(config.getContainerNetwork());
  }

  private void addCorsOrigins(
      final Web3ProviderConfiguration config, final List<String> commandLineOptions) {
    config
        .getCors()
        .ifPresent(cors -> commandLineOptions.addAll(Lists.newArrayList("--rpccorsdomain", cors)));
  }

  private void addNodePrivateKey(
      final Web3ProviderConfiguration config,
      final List<String> commandLineOptions,
      final GenericContainer<?> container) {
    container.withClasspathResourceMapping(
        config.getNodeKeyPrivateKeyResource().get(),
        CONTAINER_NODE_PRIVATE_KEY_FILE,
        BindMode.READ_ONLY);
    commandLineOptions.addAll(Lists.newArrayList("--nodekey", CONTAINER_NODE_PRIVATE_KEY_FILE));
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
