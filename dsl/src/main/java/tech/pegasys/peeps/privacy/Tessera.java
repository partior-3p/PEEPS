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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.PullPolicy;

public class Tessera extends PrivateTransactionManager {
  private static final String CONTAINER_WORKING_DIRECTORY_PREFIX = "/opt/tessera/";

  private static final String TESSERA_IMAGE = "quorumengineering/tessera:latest";

  private static final int CONTAINER_PEER_TO_PEER_PORT = 8080;
  private static final int CONTAINER_HTTP_RPC_PORT = 8888;

  public Tessera(final PrivateTransactionManagerConfiguration config) {
    super(
        config,
        new GenericContainer<>(TESSERA_IMAGE)
            .withReuse(false)
            .withImagePullPolicy(PullPolicy.ageBased(Duration.ofHours(1))));
    addContainerNetwork(config, container);
    addContainerIpAddress(config, container);
    addPrivateKeys(config, CONTAINER_WORKING_DIRECTORY_PREFIX, container);
    addPublicKeys(config, CONTAINER_WORKING_DIRECTORY_PREFIX, container);
    addConfigurationFile(config, container);
    container.addExposedPort(CONTAINER_PEER_TO_PEER_PORT);
    container.addExposedPort(CONTAINER_HTTP_RPC_PORT);

    final List<String> commandLineOptions = new ArrayList<>();
    commandLineOptions.add("-configfile");
    commandLineOptions.add(CONTAINER_CONFIG_FILE);

    container.withCommand(commandLineOptions.toArray(new String[0])).waitingFor(liveliness());
  }

  @Override
  public String getNodeName() {
    return "Tessera";
  }
}
