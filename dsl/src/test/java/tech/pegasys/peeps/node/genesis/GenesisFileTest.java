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
package tech.pegasys.peeps.node.genesis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tech.pegasys.peeps.json.Json;
import tech.pegasys.peeps.node.Account;
import tech.pegasys.peeps.node.genesis.ethhash.EthHashConfig;
import tech.pegasys.peeps.node.genesis.ethhash.GenesisConfigEthHash;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import io.vertx.core.json.DecodeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.shaded.org.bouncycastle.util.Arrays;

public class GenesisFileTest {

  @Test
  public void matchingGenesisFileMustNotBeRecreated(@TempDir Path directory) throws IOException {
    final Path location = directory.resolve("matchingGenesisFileMustNotBeRecreated.json");
    final GenesisFile genesisFile = new GenesisFile(location);
    final Genesis genesis = createGenesis(Account.ALPHA, Account.BETA);

    genesisFile.ensureExists(genesis);
    final FileTime genesisAlpaModified = Files.getLastModifiedTime(location);
    genesisFile.ensureExists(genesis);

    assertThat(genesisAlpaModified).isEqualTo(Files.getLastModifiedTime(location));
  }

  @Test
  public void nonMatchingGenesisFileMsutException(@TempDir Path directory) {
    final Path location = directory.resolve("nonMatchingGenesisFileMsutException.json");
    final GenesisFile genesisFile = new GenesisFile(location);
    final Genesis genesisAlpha = createGenesis(Account.ALPHA);
    final Genesis genesisBeta = createGenesis(Account.ALPHA, Account.BETA);

    genesisFile.ensureExists(genesisAlpha);

    assertThatThrownBy(() -> genesisFile.ensureExists(genesisBeta))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("The latest genesis does not match the genesis file created");
  }

  @Test
  public void createdGenesisMustDeserialize(@TempDir Path directory)
      throws DecodeException, IOException {
    final Path location = directory.resolve("createdGenesisMustDeserialize.json");
    final GenesisFile genesisFile = new GenesisFile(location);
    final Genesis genesis = createGenesis(Account.ALPHA, Account.BETA);

    genesisFile.ensureExists(genesis);

    assertThat(Arrays.areEqual(bytes(genesis), bytes(location))).isTrue();
  }

  private Genesis createGenesis(final Account... accounts) {
    return new Genesis(new GenesisConfigEthHash(123, new EthHashConfig()), Account.of(accounts));
  }

  private byte[] bytes(final Genesis genesis) {
    return Json.encode(genesis).getBytes(StandardCharsets.UTF_8);
  }

  private byte[] bytes(final Path location) throws IOException {
    return Files.readAllBytes(location);
  }
}
