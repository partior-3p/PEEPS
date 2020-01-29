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
package tech.pegasys.peeps.network;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.peeps.util.Await.await;

import tech.pegasys.peeps.network.subnet.Subnet;
import tech.pegasys.peeps.node.Account;
import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.BesuConfigurationBuilder;
import tech.pegasys.peeps.node.genesis.BesuGenesisFile;
import tech.pegasys.peeps.node.genesis.Genesis;
import tech.pegasys.peeps.node.genesis.GenesisAccount;
import tech.pegasys.peeps.node.genesis.GenesisConfig;
import tech.pegasys.peeps.node.genesis.GenesisExtraData;
import tech.pegasys.peeps.node.genesis.clique.CliqueConfig;
import tech.pegasys.peeps.node.genesis.clique.GenesisConfigClique;
import tech.pegasys.peeps.node.genesis.clique.GenesisExtraDataClique;
import tech.pegasys.peeps.node.genesis.ethhash.EthHashConfig;
import tech.pegasys.peeps.node.genesis.ethhash.GenesisConfigEthHash;
import tech.pegasys.peeps.node.genesis.ibft2.GenesisConfigIbft2;
import tech.pegasys.peeps.node.genesis.ibft2.GenesisExtraDataIbft2;
import tech.pegasys.peeps.node.genesis.ibft2.Ibft2Config;
import tech.pegasys.peeps.node.model.GenesisAddress;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.NodeIdentifier;
import tech.pegasys.peeps.node.model.NodeKey;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.rpc.NodeRpcExpectingData;
import tech.pegasys.peeps.node.verification.AccountValue;
import tech.pegasys.peeps.privacy.Orion;
import tech.pegasys.peeps.privacy.OrionConfiguration;
import tech.pegasys.peeps.privacy.OrionConfigurationBuilder;
import tech.pegasys.peeps.privacy.OrionConfigurationFile;
import tech.pegasys.peeps.privacy.PrivacyGroupVerify;
import tech.pegasys.peeps.privacy.model.PrivacyKeyPair;
import tech.pegasys.peeps.privacy.model.PrivacyManagerIdentifier;
import tech.pegasys.peeps.privacy.model.PrivacyPublicKeyResource;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.EthSignerConfigurationBuilder;
import tech.pegasys.peeps.signer.model.SignerIdentifier;
import tech.pegasys.peeps.signer.model.WalletFileResources;
import tech.pegasys.peeps.signer.rpc.SignerRpcExpectingData;
import tech.pegasys.peeps.util.PathGenerator;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;
import io.vertx.core.Vertx;
import org.apache.tuweni.eth.Address;

public class Network implements Closeable {

  private enum NetworkState {
    STARTED,
    STOPPED,
    CLOSED
  }

  private final List<NetworkMember> members;
  private final Map<PrivacyManagerIdentifier, Orion> privacyManagers;
  private final Map<SignerIdentifier, EthSigner> signers;
  private final Map<NodeIdentifier, Besu> nodes;

  private final Subnet subnet;
  private final PathGenerator pathGenerator;
  private final Vertx vertx;
  private final BesuGenesisFile genesisFile;

  private Genesis genesis;
  private NetworkState state;

  public Network(final Path configurationDirectory, final Subnet subnet) {
    checkArgument(configurationDirectory != null, "Path to configuration directory is mandatory");

    this.privacyManagers = new HashMap<>();
    this.members = new ArrayList<>();
    this.signers = new HashMap<>();
    this.nodes = new HashMap<>();
    this.pathGenerator = new PathGenerator(configurationDirectory);
    this.vertx = Vertx.vertx();
    this.subnet = subnet;
    this.genesisFile = new BesuGenesisFile(pathGenerator.uniqueFile());
    this.state = NetworkState.STOPPED;

    set(ConsensusMechanism.ETH_HASH);
  }

  // TODO validate state transition better - encapsulate

  public void start() {
    checkState(state != NetworkState.STARTED, "Cannot start an already started Network");
    genesisFile.ensureExists(genesis);
    everyMember(NetworkMember::start);
    awaitConnectivity();
    state = NetworkState.STARTED;
  }

  public void stop() {
    checkState(state != NetworkState.STOPPED, "Cannot stop an already stopped Network");
    everyMember(NetworkMember::stop);
    state = NetworkState.STOPPED;
  }

  @Override
  public void close() {
    if (state == NetworkState.STARTED) {
      everyMember(NetworkMember::stop);
    }
    vertx.close();
    subnet.close();
    state = NetworkState.CLOSED;
  }

  // TODO temporary hack to support overloading of set with varargs
  public void set(final ConsensusMechanism consensus) {
    set(consensus, (Besu) null);
  }

  public void set(final ConsensusMechanism consensus, final NodeIdentifier... validators) {
    // TODO check arg - validators must be in nodes (perform during steam mapping
    set(
        consensus,
        Stream.of(validators)
            .parallel()
            .map(validator -> nodes.get(validator))
            .toArray((Besu[]::new)));
  }

  // TODO validators hacky, dynamically figure out after the nodes are all added
  public void set(final ConsensusMechanism consensus, final Besu... validators) {
    checkState(
        state != NetworkState.STARTED,
        "Cannot set consensus mechanism while the Network is already started");
    checkState(signers.isEmpty(), "Cannot change consensus mechanism after creating signers");

    this.genesis =
        createGenesis(
            consensus, Account.of(Account.ALPHA, Account.BETA, Account.GAMMA), validators);
  }

  public Besu addNode(final NodeIdentifier frameworkIdentity, final NodeKey ethereumIdentiity) {
    return addNode(
        new BesuConfigurationBuilder()
            .withIdentity(frameworkIdentity)
            .withNodeKey(ethereumIdentiity));
  }

  public Besu addNode(
      final NodeIdentifier identity,
      final NodeKey ethereumIdentiity,
      final PrivacyManagerIdentifier privacyManager,
      final PrivacyPublicKeyResource privacyAddressResource) {
    checkArgument(
        privacyManagers.containsKey(privacyManager),
        "Privacy Manager: {}, is not a member of the Network",
        privacyManager);

    return addNode(
        new BesuConfigurationBuilder()
            .withIdentity(identity)
            .withNodeKey(ethereumIdentiity)
            .withPrivacyUrl(privacyManagers.get(privacyManager))
            .withPrivacyManagerPublicKey(privacyAddressResource.get()));
  }

  private Besu addNode(final BesuConfigurationBuilder config) {
    final Besu besu =
        new Besu(
            config
                .withVertx(vertx)
                .withContainerNetwork(subnet.network())
                .withIpAddress(subnet.getAddressAndIncrement())
                .withGenesisFile(genesisFile)
                .withBootnodeEnodeAddress(bootnodeEnodeAddresses())
                .build());

    return addNode(besu);
  }

  public Orion addPrivacyManager(
      final PrivacyManagerIdentifier identity, final PrivacyKeyPair... keys) {
    final OrionConfiguration configuration =
        new OrionConfigurationBuilder()
            .withVertx(vertx)
            .withContainerNetwork(subnet.network())
            .withIpAddress(subnet.getAddressAndIncrement())
            .withFileSystemConfigurationFile(pathGenerator.uniqueFile())
            .withBootnodeUrls(privacyManagerBootnodeUrls())
            .withKeyPairs(keys)
            .build();

    // TODO encapsulate?
    OrionConfigurationFile.write(configuration);

    final Orion manager = new Orion(configuration);

    privacyManagers.put(identity, manager);
    members.add(manager);

    return manager;
  }

  public EthSigner addSigner(
      final SignerIdentifier wallet,
      final WalletFileResources resources,
      final NodeIdentifier downstream) {
    checkNodeExistsFor(downstream);
    return addSigner(wallet, resources, nodes.get(downstream));
  }

  private EthSigner addSigner(
      final SignerIdentifier wallet, final WalletFileResources resources, final Besu downstream) {
    final EthSigner signer =
        new EthSigner(
            new EthSignerConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(subnet.network())
                .withIpAddress(subnet.getAddressAndIncrement())
                .withDownstream(downstream)
                .withChainId(genesis.getConfig().getChainId())
                .witWallet(resources)
                .build());

    signers.put(wallet, signer);
    members.add(signer);

    return signer;
  }

  /**
   * Waits until either all nodes in the network reach consensus on the Transaction Receipt (that
   * includes a block hash), or exceptions when wait time has been exceeded.
   */
  public void awaitConsensusOnTransactionReciept(final Hash transaction) {
    checkState(nodes.size() > 1, "There must be two or more nodes to be able to wait on consensus");

    await(
        () -> {
          final List<TransactionReceipt> receipts =
              nodes
                  .values()
                  .parallelStream()
                  .map(node -> node.rpc().getTransactionReceipt(transaction))
                  .collect(Collectors.toList());

          assertThat(receipts.size()).isEqualTo(nodes.size());
          final TransactionReceipt firstReceipt = receipts.get(0);

          for (final TransactionReceipt receipt : receipts) {
            assertThat(receipt).isNotNull();
            assertThat(receipt.isSuccess()).isTrue();
            assertThat(receipt).usingRecursiveComparison().isEqualTo(firstReceipt);
          }
        },
        "Consensus was not reached in time for Transaction Receipt with hash: " + transaction);
  }

  public void verifyConsensusOnValue(final Address... accounts) {
    checkState(
        nodes.size() > 1, "There must be two or more nodes to be able to verify on consensus");

    final Besu firstNode = nodes.values().iterator().next();
    final Set<AccountValue> values =
        Stream.of(accounts)
            .parallel()
            .map(account -> new AccountValue(account, firstNode.rpc().getBalance(account)))
            .collect(Collectors.toSet());

    nodes.values().parallelStream().forEach(node -> node.verifyValue(values));
  }

  public void verifyConsensusOnTransaction(final Hash transaction) {
    checkState(
        nodes.size() > 1, "There must be two or more nodes to be able to verify on consensus");

    final Set<Transaction> transactions =
        nodes
            .values()
            .parallelStream()
            .map(node -> node.rpc().getTransactionByHash(transaction))
            .collect(Collectors.toSet());

    assertThat(transactions).isNotEmpty();
    final Transaction firstTx = transactions.iterator().next();

    for (final Transaction tx : transactions) {
      assertThat(tx).isNotNull();
      assertThat(tx.isProcessed()).isTrue();
      assertThat(tx).usingRecursiveComparison().isEqualTo(firstTx);
    }
  }

  public void verifyConsensusOnPrivacyTransactionReceipt(final Hash transaction) {
    checkState(
        nodes.size() > 1, "There must be two or more nodes to be able to verify on consensus");

    final Set<PrivacyTransactionReceipt> transactions =
        nodes
            .values()
            .parallelStream()
            .map(node -> node.rpc().getPrivacyTransactionReceipt(transaction))
            .collect(Collectors.toSet());

    assertThat(transactions).isNotEmpty();
    final PrivacyTransactionReceipt firstTx = transactions.iterator().next();

    for (final PrivacyTransactionReceipt tx : transactions) {
      assertThat(tx).isNotNull();
      assertThat(tx).usingRecursiveComparison().isEqualTo(firstTx);
    }
  }

  // TODO these Mediator method could be refactored elsewhere?
  public NodeVerify verify(final NodeIdentifier id) {
    checkNodeExistsFor(id);

    return new NodeVerify(nodes.get(id));
  }

  public SignerRpcExpectingData rpc(final SignerIdentifier id) {
    checkNotNull(id, "Signer Identifier is mandatory");
    checkState(
        signers.containsKey(id),
        "Signer Identifier: {}, does not match any available: {}",
        id,
        signers.keySet());

    return signers.get(id).rpc();
  }

  public NodeRpcExpectingData rpc(final NodeIdentifier id) {
    checkNodeExistsFor(id);

    return nodes.get(id).rpc();
  }

  public PrivacyGroupVerify privacyGroup(final PrivacyManagerIdentifier... members) {
    return new PrivacyGroupVerify(
        Stream.of(members)
            .parallel()
            .map(manager -> privacyManagers.get(manager))
            .collect(Collectors.toSet()));
  }

  @VisibleForTesting
  Besu addNode(final Besu besu) {
    nodes.put(besu.identity(), besu);
    members.add(besu);

    return besu;
  }

  private void checkNodeExistsFor(final NodeIdentifier id) {
    checkNotNull(id, "Node Identifier is mandatory");
    checkState(
        nodes.containsKey(id),
        "Node Identifier: {}, does not match any available: {}",
        id,
        nodes.keySet());
  }

  private String bootnodeEnodeAddresses() {
    return nodes
        .values()
        .parallelStream()
        .map(node -> node.enodeAddress())
        .collect(Collectors.joining(","));
  }

  private void everyMember(Consumer<NetworkMember> action) {
    members.parallelStream().forEach(action);
  }

  private Genesis createGenesis(
      final ConsensusMechanism consensus,
      final Map<GenesisAddress, GenesisAccount> genesisAccounts,
      final Besu... validators) {
    final long chainId = Math.round(Math.random() * Long.MAX_VALUE);

    final GenesisConfig genesisConfig;

    switch (consensus) {
      case CLIQUE:
        genesisConfig = new GenesisConfigClique(chainId, new CliqueConfig());
        break;
      case IBFT2:
        genesisConfig = new GenesisConfigIbft2(chainId, new Ibft2Config());
        break;
      case ETH_HASH:
      default:
        genesisConfig = new GenesisConfigEthHash(chainId, new EthHashConfig());
        break;
    }

    final GenesisExtraData extraData;

    switch (consensus) {
      case CLIQUE:
        extraData = new GenesisExtraDataClique(validators);
        break;
      case IBFT2:
        extraData = new GenesisExtraDataIbft2(validators);
        break;
      case ETH_HASH:
      default:
        extraData = null;
        break;
    }

    return new Genesis(genesisConfig, genesisAccounts, extraData);
  }

  private void awaitConnectivity() {
    nodes.values().parallelStream().forEach(node -> node.awaitConnectivity(nodes.values()));

    privacyManagers
        .values()
        .parallelStream()
        .distinct()
        .forEach(privacyManger -> privacyManger.awaitConnectivity(privacyManagers.values()));

    signers.values().parallelStream().forEach(signer -> signer.awaitConnectivityToDownstream());
  }

  private List<String> privacyManagerBootnodeUrls() {
    return privacyManagers
        .values()
        .parallelStream()
        .distinct()
        .map(manager -> manager.getPeerNetworkAddress())
        .collect(Collectors.toList());
  }
}
