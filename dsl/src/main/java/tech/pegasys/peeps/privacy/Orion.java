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
package tech.pegasys.peeps.privacy;

import org.testcontainers.containers.GenericContainer;

public class Orion extends PrivateTransactionManager {

  private static final String CONTAINER_WORKING_DIRECTORY_PREFIX = "/opt/orion/";
  private static final String ORION_IMAGE = "consensys/quorum-orion:develop";

  public Orion(final PrivateTransactionManagerConfiguration config) {
    super(config, new GenericContainer<>(ORION_IMAGE));
    addContainerNetwork(config, container);
    addContainerIpAddress(config, container);
    addPrivateKeys(config, CONTAINER_WORKING_DIRECTORY_PREFIX, container);
    addPublicKeys(config, CONTAINER_WORKING_DIRECTORY_PREFIX, container);
    addConfigurationFile(config, container);
    container.withCommand(CONTAINER_CONFIG_FILE).waitingFor(liveliness());
  }

  @Override
  public String getNodeName() {
    return "Orion";
  }
}
