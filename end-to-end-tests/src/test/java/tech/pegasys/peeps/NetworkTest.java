/*
 * Copyright 2020 ConsenSys AG.
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
package tech.pegasys.peeps;

import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.network.NetworkAwait;
import tech.pegasys.peeps.network.NetworkVerify;
import tech.pegasys.peeps.network.subnet.Subnet;
import tech.pegasys.peeps.node.NodeVerify;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.signer.SignerConfiguration;
import tech.pegasys.peeps.signer.rpc.SignerRpcMandatoryResponse;
import tech.pegasys.peeps.signer.rpc.SignerRpcSenderKnown;

import java.nio.file.Path;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class NetworkTest {

  // TODO this may not be the best place to be adding Security providers
  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @TempDir Path configurationDirectory;

  public Network network;
  private NetworkAwait await;
  private NetworkVerify verify;

  @BeforeEach
  public void setUpNetwork() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDownNetwork));
    network = new Network(configurationDirectory, new Subnet());
    setUpNetwork(network);
    network.start();

    await = new NetworkAwait(network);
    verify = new NetworkVerify(network);
  }

  @AfterEach
  public void tearDownNetwork() {
    if (network != null) {
      network.close();
    }
  }

  protected abstract void setUpNetwork(Network network);

  // TODO not sure about having these here, maybe somewhere else?
  protected NetworkAwait await() {
    return await;
  }

  protected NetworkVerify verify() {
    return verify;
  }

  protected NodeVerify verifyOn(final Web3Provider node) {
    return network.verify(node);
  }

  protected SignerRpcSenderKnown execute(final SignerConfiguration signerConfig) {
    return network.rpc(signerConfig.name(), signerConfig.address());
  }

  protected SignerRpcMandatoryResponse execute(final Web3Provider web3Provider) {
    return web3Provider.rpc();
  }
}
