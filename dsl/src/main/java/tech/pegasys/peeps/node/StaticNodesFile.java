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
package tech.pegasys.peeps.node;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StaticNodesFile {

  private static final Logger LOG = LogManager.getLogger();

  private final Path staticNodesFile;
  private static final ObjectMapper objectMapper =
      new ObjectMapper()
          .registerModule(new Jdk8Module())
          .setSerializationInclusion(Include.NON_ABSENT);

  public StaticNodesFile(final Path staticNodesFile) {
    this.staticNodesFile = staticNodesFile;
  }

  public void ensureExists(
      final Web3Provider web3Provider, final List<Web3Provider> web3Providers) {
    write(web3Provider, web3Providers);
  }

  public Path getStaticNodesFile() {
    return staticNodesFile;
  }

  private void write(final Web3Provider web3Provider, final List<Web3Provider> web3Providers) {
    final List<String> enodeAddresses =
        web3Providers.stream()
            .map(Web3Provider::enodeAddress)
            .filter(enodeAddress -> !enodeAddress.equals(web3Provider.enodeAddress()))
            .collect(Collectors.toList());
    try {
      objectMapper.writeValue(staticNodesFile.toFile(), enodeAddresses);
      LOG.info(
          "Creating static nodes file\n\tLocation: {} \n\tContents: {}",
          staticNodesFile,
          enodeAddresses);
    } catch (final IOException e) {
      throw new IllegalStateException("Failed creating static nodes file " + staticNodesFile);
    }
  }
}
