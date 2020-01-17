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

import tech.pegasys.peeps.network.AwaitNetwork;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.network.VerifyNetwork;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class NetworkTest {

  @TempDir Path configurationDirectory;

  private Network network;
  private AwaitNetwork await;
  private VerifyNetwork verify;

  @BeforeEach
  public void setUpNetwork() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDownNetwork));
    network = new Network(configurationDirectory);
    setUpNetwork(network);
    network.start();

    await = new AwaitNetwork(network);
    verify = new VerifyNetwork(network);
  }

  @AfterEach
  public void tearDownNetwork() {
    network.close();
  }

  protected abstract void setUpNetwork(Network network);

  protected AwaitNetwork await() {
    return await;
  }

  protected VerifyNetwork verify() {
    return verify;
  }
}
