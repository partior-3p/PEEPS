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
import tech.pegasys.peeps.node.GoQuorum;
import tech.pegasys.peeps.node.NodeVerify;
import tech.pegasys.peeps.node.StaticNodesFile;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.Web3ProviderConfigurationBuilder;
import tech.pegasys.peeps.node.Web3ProviderType;
import tech.pegasys.peeps.node.genesis.Genesis;
import tech.pegasys.peeps.node.genesis.GenesisAccount;
import tech.pegasys.peeps.node.genesis.GenesisConfig;
import tech.pegasys.peeps.node.genesis.GenesisExtraData;
import tech.pegasys.peeps.node.genesis.GenesisFile;
import tech.pegasys.peeps.node.genesis.bft.BftConfig;
import tech.pegasys.peeps.node.genesis.clique.CliqueConfig;
import tech.pegasys.peeps.node.genesis.clique.GenesisConfigClique;
import tech.pegasys.peeps.node.genesis.clique.GenesisExtraDataClique;
import tech.pegasys.peeps.node.genesis.ethhash.EthHashConfig;
import tech.pegasys.peeps.node.genesis.ethhash.GenesisConfigEthHash;
import tech.pegasys.peeps.node.genesis.ibft.BesuConfigIbft;
import tech.pegasys.peeps.node.genesis.ibft.BesuLegacyIbftOptions;
import tech.pegasys.peeps.node.genesis.ibft.GenesisExtraDataIbftLegacy;
import tech.pegasys.peeps.node.genesis.ibft.GoQuorumIbftConfig;
import tech.pegasys.peeps.node.genesis.ibft.GoQuorumIbftOptions;
import tech.pegasys.peeps.node.genesis.ibft2.GenesisConfigIbft2;
import tech.pegasys.peeps.node.genesis.ibft2.GenesisExtraDataIbft2;
import tech.pegasys.peeps.node.genesis.qbft.GenesisConfigQbft;
import tech.pegasys.peeps.node.genesis.qbft.GenesisExtraDataQbft;
import tech.pegasys.peeps.node.genesis.qbft.GoQuorumConfigQbft;
import tech.pegasys.peeps.node.model.GenesisAddress;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.verification.AccountValue;
import tech.pegasys.peeps.privacy.Orion;
import tech.pegasys.peeps.privacy.OrionConfigurationFile;
import tech.pegasys.peeps.privacy.PrivacyGroupVerify;
import tech.pegasys.peeps.privacy.PrivateTransactionManager;
import tech.pegasys.peeps.privacy.PrivateTransactionManagerConfiguration;
import tech.pegasys.peeps.privacy.PrivateTransactionManagerConfigurationBuilder;
import tech.pegasys.peeps.privacy.PrivateTransactionManagerType;
import tech.pegasys.peeps.privacy.Tessera;
import tech.pegasys.peeps.privacy.TesseraConfigurationFile;
import tech.pegasys.peeps.privacy.model.PrivacyGroup;
import tech.pegasys.peeps.privacy.model.PrivacyKeyPair;
import tech.pegasys.peeps.privacy.model.PrivacyManagerIdentifier;
import tech.pegasys.peeps.privacy.model.PrivacyPublicKeyResource;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.EthSignerConfigurationBuilder;
import tech.pegasys.peeps.signer.SignerConfiguration;
import tech.pegasys.peeps.signer.model.WalletFileResources;
import tech.pegasys.peeps.signer.rpc.SignerRpcSenderKnown;
import tech.pegasys.peeps.util.PathGenerator;

import java.io.Closeable;
import java.math.BigInteger;
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
import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class Network implements Closeable {

  private final Map<PrivacyManagerIdentifier, PrivateTransactionManager> privacyManagers;
  private final Map<String, EthSigner> signers;
  private final List<Web3Provider> nodes;
  private final List<NetworkMember> members;

  private final Map<Web3ProviderType, GenesisFile> genesisFiles;
  private final Map<Web3Provider, StaticNodesFile> staticNodesFiles;
  private final PathGenerator pathGenerator;
  private final Subnet subnet;
  private final Vertx vertx;

  private final NetworkState state;
  private final Map<Web3ProviderType, Genesis> genesisConfigurations = new HashMap<>();
  private Wei minGasPrice = Wei.valueOf(0);
  private long blockRewardTransitionBlock;
  private Wei blockReward = Wei.valueOf(0);
  private long miningBeneficiaryBlock;
  private Address miningBeneficiary;

  public Network(final Path configurationDirectory, final Subnet subnet) {
    checkArgument(configurationDirectory != null, "Path to configuration directory is mandatory");

    this.privacyManagers = new HashMap<>();
    this.members = new ArrayList<>();
    this.signers = new HashMap<>();
    this.nodes = new ArrayList<>();
    this.pathGenerator = new PathGenerator(configurationDirectory);
    this.vertx = Vertx.vertx();
    this.subnet = subnet;
    this.genesisFiles =
        Map.of(
            Web3ProviderType.BESU,
            new GenesisFile(pathGenerator.uniqueFile()),
            Web3ProviderType.GOQUORUM,
            new GenesisFile(pathGenerator.uniqueFile()));

    this.state = new NetworkState();
    this.staticNodesFiles = new HashMap<>();

    set(ConsensusMechanism.ETH_HASH);
  }

  public void start() {
    state.start();
    genesisFiles.forEach((k, v) -> v.ensureExists(genesisConfigurations.get(k)));
    staticNodesFiles.forEach((k, v) -> v.ensureExists(k, nodes));
    if (members.size() != 0) {
      members.stream().parallel().forEach(NetworkMember::start);
    }
    awaitConnectivity();
  }

  public void stop() {
    state.stop();
    everyMember(NetworkMember::stop);
  }

  @Override
  public void close() {
    if (state.isStarted()) {
      everyMember(NetworkMember::stop);
    }
    vertx.close();
    subnet.close();
  }

  // TODO temporary hack to support overloading of set with varargs
  public void set(final ConsensusMechanism consensus) {
    set(consensus, (Besu) null);
  }

  // TODO validators hacky, dynamically figure out after the nodes are all added
  public void set(final ConsensusMechanism consensus, final Web3Provider... validators) {
    checkState(
        state.isUninitialized(),
        "Cannot set consensus mechanism while the Network is already started");
    checkState(signers.isEmpty(), "Cannot change consensus mechanism after creating signers");

    this.genesisConfigurations.putAll(
        createGenesis(
            consensus,
            minGasPrice,
            Account.of(Account.ALPHA, Account.BETA, Account.GAMMA, Account.DELTA),
            validators));
  }

  public Web3Provider addNode(final String nodeIdentifier, final KeyPair nodeKeys) {
    return addNode(
        new Web3ProviderConfigurationBuilder().withIdentity(nodeIdentifier).withNodeKey(nodeKeys),
        Web3ProviderType.BESU);
  }

  public Web3Provider addNode(
      final String nodeIdentifier, final KeyPair nodeKeys, final Web3ProviderType providerType) {
    return addNode(
        new Web3ProviderConfigurationBuilder().withIdentity(nodeIdentifier).withNodeKey(nodeKeys),
        providerType);
  }

  public Web3Provider addNode(
      final String nodeIdentifier,
      final KeyPair nodeKeys,
      final Web3ProviderType providerType,
      final Wei minGasPrice) {
    this.minGasPrice = minGasPrice;
    return addNode(
        new Web3ProviderConfigurationBuilder()
            .withIdentity(nodeIdentifier)
            .withNodeKey(nodeKeys)
            .withMinGasPrice(minGasPrice),
        providerType);
  }

  public Web3Provider addNode(
      final String nodeIdentifier,
      final KeyPair nodeKeys,
      final Web3ProviderType providerType,
      final String version) {
    return addNode(
        new Web3ProviderConfigurationBuilder()
            .withIdentity(nodeIdentifier)
            .withNodeKey(nodeKeys)
            .withImageVersion(version),
        providerType);
  }

  public Web3Provider addNode(
          final String nodeIdentifier,
          final KeyPair nodeKeys,
          final Web3ProviderType providerType,
          final String version,
          final SignerConfiguration wallet) {
    return addNode(
            new Web3ProviderConfigurationBuilder()
                    .withIdentity(nodeIdentifier)
                    .withNodeKey(nodeKeys)
                    .withImageVersion(version)
                    .withWallet(wallet),
            providerType);
  }

  public Web3Provider addNode(
      final String nodeIdentifier,
      final KeyPair nodeKey,
      final Web3ProviderType nodeType,
      final SignerConfiguration wallet) {
    return addNode(
        new Web3ProviderConfigurationBuilder()
            .withIdentity(nodeIdentifier)
            .withNodeKey(nodeKey)
            .withWallet(wallet),
        nodeType);
  }

  public Web3Provider addNode(
      final String identity,
      final KeyPair nodeKeys,
      final PrivacyManagerIdentifier privacyManager,
      final PrivacyPublicKeyResource privacyAddressResource) {
    checkArgument(
        privacyManagers.containsKey(privacyManager),
        "Privacy Manager: {}, is not a member of the Network",
        privacyManager);

    return addNode(
        new Web3ProviderConfigurationBuilder()
            .withIdentity(identity)
            .withNodeKey(nodeKeys)
            .withPrivacyUrl(privacyManagers.get(privacyManager))
            .withPrivacyManagerPublicKey(privacyAddressResource.get()),
        Web3ProviderType.BESU);
  }

  private Web3Provider addNode(
      final Web3ProviderConfigurationBuilder config, final Web3ProviderType providerType) {
    final Web3Provider web3Provider;
    final StaticNodesFile staticNodesFile = new StaticNodesFile(pathGenerator.uniqueFile());
    config
        .withVertx(vertx)
        .withContainerNetwork(subnet.network())
        .withIpAddress(subnet.getAddressAndIncrement())
        .withGenesisFile(genesisFiles.get(providerType))
        .withStaticNodesFile(staticNodesFile)
        .withBootnodeEnodeAddress(bootnodeEnodeAddresses());
    if (providerType.equals(Web3ProviderType.BESU)) {
      web3Provider = new Besu(config.build());
    } else {
      web3Provider = new GoQuorum(config.build());
    }

    staticNodesFiles.put(web3Provider, staticNodesFile);
    return addNode(web3Provider);
  }

  public PrivateTransactionManager addPrivacyManager(
      final PrivacyManagerIdentifier identity,
      final List<PrivacyKeyPair> keys,
      final PrivateTransactionManagerType privateTransactionManagerType) {
    final PrivateTransactionManager manager;
    final PrivateTransactionManagerConfiguration configuration =
        new PrivateTransactionManagerConfigurationBuilder()
            .withVertx(vertx)
            .withContainerNetwork(subnet.network())
            .withIpAddress(subnet.getAddressAndIncrement())
            .withFileSystemConfigurationFile(pathGenerator.uniqueFile())
            .withBootnodeUrls(privacyManagerBootnodeUrls())
            .withKeyPairs(keys)
            .build();
    if (privateTransactionManagerType.equals(PrivateTransactionManagerType.ORION)) {
      OrionConfigurationFile.write(configuration);
      manager = new Orion(configuration);
    } else {
      TesseraConfigurationFile.write(configuration);
      manager = new Tessera(configuration);
    }

    privacyManagers.put(identity, manager);
    members.add(manager);

    return manager;
  }

  public EthSigner addSigner(
      final String wallet, final WalletFileResources resources, final Web3Provider downstream) {
    final EthSigner signer =
        new EthSigner(
            new EthSignerConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(subnet.network())
                .withIpAddress(subnet.getAddressAndIncrement())
                .withDownstream(downstream)
                .withChainId(
                    genesisConfigurations
                        .get(Web3ProviderType.BESU)
                        .getConfig()
                        .getChainId()) // yeah, this is a bit of a hack.
                .witWallet(resources)
                .build());

    signers.put(wallet, signer);
    members.add(signer);

    return signer;
  }

  public EthSigner addSigner(
      final String wallet,
      final WalletFileResources resources,
      final Web3Provider downstream,
      final Wei minGasPrice) {
    final EthSigner signer =
        new EthSigner(
            new EthSignerConfigurationBuilder()
                .withVertx(vertx)
                .withContainerNetwork(subnet.network())
                .withIpAddress(subnet.getAddressAndIncrement())
                .withDownstream(downstream)
                .withMinGasPrice(minGasPrice)
                .withChainId(
                    genesisConfigurations
                        .get(Web3ProviderType.BESU)
                        .getConfig()
                        .getChainId()) // yeah, this is a bit of a hack.
                .witWallet(resources)
                .build());

    signers.put(wallet, signer);
    members.add(signer);

    return signer;
  }

  /**
   * Waits until either all nodes in the network reach consensus on the Transaction Receipt (that
   * includes a block hash), or exceptions when wait time has been exceeded.
   *
   * @param transaction the hash of the transaction who's receipt is being checked.
   * @param timeout how long to wait for the transaction hash
   */
  public void awaitConsensusOnTransactionReceipt(final Hash transaction, final int timeout) {
    checkState(nodes.size() > 1, "There must be two or more nodes to be able to wait on consensus");

    await(
        () -> {
          final List<TransactionReceipt> receipts =
              nodes
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
        timeout,
        "Consensus was not reached in time for Transaction Receipt with hash: %s",
        transaction);
  }

  public void verifyConsensusOnValue(final Address... accounts) {
    checkState(
        nodes.size() > 1, "There must be two or more nodes to be able to verify on consensus");

    final Web3Provider firstNode = nodes.iterator().next();
    final Set<AccountValue> values =
        Stream.of(accounts)
            .parallel()
            .map(account -> new AccountValue(account, firstNode.rpc().getBalance(account)))
            .collect(Collectors.toSet());

    nodes.parallelStream().forEach(node -> node.verifyValue(values));
  }

  public void verifyConsensusOnTransaction(final Hash transaction) {
    checkState(
        nodes.size() > 1, "There must be two or more nodes to be able to verify on consensus");

    final Set<Transaction> transactions =
        nodes
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

  public void verifyConsensusOnBlockNumberIsAtLeast(final long blockNumber) {
    checkState(
        nodes.size() > 1, "There must be two or more nodes to be able to verify on consensus");

    await(
        () ->
            assertThat(
                    nodes
                        .parallelStream()
                        .map(node -> node.rpc().getBlockNumber())
                        .allMatch(block -> block >= blockNumber))
                .isTrue(),
        120,
        "Failed to achieve consensus on block number being at least %s",
        blockNumber);
  }

  public void verifyConsensusOnValidators(final List<Address> expectedValidators) {
    checkState(
        nodes.size() > 1, "There must be two or more nodes to be able to verify on consensus");

    await(
        () ->
            assertThat(
                    nodes
                        .parallelStream()
                        .map(node -> node.rpc().qbftGetValidatorsByBlockBlockNumber("latest"))
                        .allMatch(validators -> validators.containsAll(expectedValidators)))
                .isTrue(),
        "Failed to achieve consensus on validators %s",
        expectedValidators);
  }

  public void verifyGasRewardsAreTransferredToValidator(final Hash receipt) {
    nodes.forEach(node -> node.verifyGasRewardsAreTransferredToValidator(receipt));
  }

  // TODO these Mediator method could be refactored elsewhere?
  public NodeVerify verify(final Web3Provider node) {
    return new NodeVerify(node);
  }

  public SignerRpcSenderKnown rpc(final String signerName, final Address sender) {
    checkNotNull(signerName, "Signer Identifier is mandatory");
    checkState(
        signers.containsKey(signerName),
        "Signer Identifier: {}, does not match any available: {}",
        signerName,
        signers.keySet());

    return new SignerRpcSenderKnown(signers.get(signerName).rpc(), sender);
  }

  public PrivacyGroupVerify privacyGroup(final PrivacyGroup group) {
    return new PrivacyGroupVerify(
        group.parallelStream().map(privacyManagers::get).collect(Collectors.toSet()));
  }

  @VisibleForTesting
  Web3Provider addNode(final Web3Provider web3Provider) {
    nodes.add(web3Provider);
    members.add(web3Provider);

    return web3Provider;
  }

  private String bootnodeEnodeAddresses() {
    return nodes.parallelStream().map(Web3Provider::enodeAddress).collect(Collectors.joining(","));
  }

  private void everyMember(final Consumer<NetworkMember> action) {
    members.parallelStream().forEach(action);
  }

  private Map<Web3ProviderType, Genesis> createGenesis(
      final ConsensusMechanism consensus,
      final Wei minGasPrice,
      final Map<GenesisAddress, GenesisAccount> genesisAccounts,
      final Web3Provider... validators) {
    final long chainId = Math.round(Math.random() * Long.MAX_VALUE);

    final Map<Web3ProviderType, Genesis> result = new HashMap<>();

    Stream.of(Web3ProviderType.values())
        .forEach(
            e -> {
              final GenesisConfig genesisConfig;
              final GenesisExtraData extraData;

              switch (consensus) {
                case CLIQUE:
                  genesisConfig = new GenesisConfigClique(chainId, new CliqueConfig());
                  extraData = new GenesisExtraDataClique(validators);
                  break;
                case IBFT2:
                  genesisConfig = new GenesisConfigIbft2(chainId, new BftConfig());
                  extraData = new GenesisExtraDataIbft2(validators);
                  break;
                case IBFT:
                  if (e == Web3ProviderType.BESU) {
                    genesisConfig = new BesuConfigIbft(chainId, new BesuLegacyIbftOptions());
                  } else {
                    genesisConfig = new GoQuorumIbftConfig(chainId, new GoQuorumIbftOptions());
                  }
                  extraData = new GenesisExtraDataIbftLegacy(validators);
                  break;
                case QBFT:
                  if (e == Web3ProviderType.BESU) {
                    genesisConfig =
                        new GenesisConfigQbft(
                            chainId,
                            new BftConfig(),
                            blockRewardTransitionBlock,
                            blockReward,
                            miningBeneficiaryBlock,
                            miningBeneficiary);
                  } else {
                    genesisConfig =
                        new GoQuorumConfigQbft(
                            chainId,
                            new BftConfig(),
                            minGasPrice,
                            blockRewardTransitionBlock,
                            blockReward,
                            miningBeneficiaryBlock,
                            miningBeneficiary);
                  }
                  extraData = new GenesisExtraDataQbft(validators);
                  break;
                case ETH_HASH:
                default:
                  extraData = null;
                  genesisConfig = new GenesisConfigEthHash(chainId, new EthHashConfig());
                  break;
              }

              result.put(e, new Genesis(genesisConfig, genesisAccounts, extraData));
            });

    return result;
  }

  private void awaitConnectivity() {
    nodes.parallelStream().forEach(node -> node.awaitConnectivity(nodes));

    privacyManagers
        .values()
        .parallelStream()
        .distinct()
        .forEach(privacyManger -> privacyManger.awaitConnectivity(privacyManagers.values()));

    signers.values().parallelStream().forEach(EthSigner::awaitConnectivityToDownstream);
  }

  private List<String> privacyManagerBootnodeUrls() {
    return privacyManagers
        .values()
        .parallelStream()
        .distinct()
        .map(PrivateTransactionManager::getPeerNetworkAddress)
        .collect(Collectors.toList());
  }

  public void setValidatorContractValidatorTransaction(
      final BigInteger blockNumber, final String contractAddress) {
    nodes.forEach(
        node -> node.setQBFTValidatorSmartContractTransition(blockNumber, contractAddress));
  }

  public void restart() {
    everyMember(NetworkMember::stop);
    everyMember(NetworkMember::start);
  }

  public void addBlockRewardTransition(
      final long blockRewardTransitionBlock, final Wei blockReward) {
    this.blockRewardTransitionBlock = blockRewardTransitionBlock;
    this.blockReward = blockReward;
  }

  public void addMiningBeneficiaryTransition(
      final long miningBeneficiaryBlock, final Address miningBeneficiary) {
    this.miningBeneficiaryBlock = miningBeneficiaryBlock;
    this.miningBeneficiary = miningBeneficiary;
  }
}
