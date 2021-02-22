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

import static java.util.Map.entry;

import tech.pegasys.peeps.privacy.model.PrivacyPrivateKeyResource;
import tech.pegasys.peeps.privacy.model.PrivacyPublicKeyResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TesseraConfigurationFile {

  private static final Logger LOG = LogManager.getLogger();
  private static final String CONTAINER_WORKING_DIRECTORY_PREFIX = "/opt/tessera/";
  private static final int HTTP_RPC_PORT = 8888;
  private static final int THIRD_PARTY_RPC_PORT = 8890;
  private static final int PEER_TO_PEER_PORT = 8080;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static void write(PrivateTransactionManagerConfiguration config) {
    final Map<String, Object> content = new HashMap<>();
    content.put("useWhiteList", false);
    content.put(
        "jdbc",
        Map.ofEntries(
            entry("username", "sa"),
            entry("password", ""),
            entry("url", "jdbc:h2:/tmp/tessera;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=0"),
            entry("autoCreateTables", true)));
    content.put(
        "serverConfigs",
        List.of(
            Map.ofEntries(
                entry("app", "ThirdParty"),
                entry("enabled", true),
                entry("serverAddress", "http://localhost:" + THIRD_PARTY_RPC_PORT),
                entry("communicationType", "REST")),
            Map.ofEntries(
                entry("app", "Q2T"),
                entry("enabled", true),
                entry("serverAddress", "http://localhost:" + HTTP_RPC_PORT),
                entry("communicationType", "REST")),
            Map.ofEntries(
                entry("app", "P2P"),
                entry("enabled", true),
                entry("serverAddress", "http://localhost:" + PEER_TO_PEER_PORT),
                entry("communicationType", "REST"))));

    content.put("mode", "orion");
    content.put("alwaysSendTo", List.of());
    content.put("peer", List.of());

    final List<Map<String, String>> keyData =
        Streams.zip(
                config.getPrivateKeys().stream(),
                config.getPublicKeys().stream(),
                TesseraConfigurationFile::createKeyDataEntry)
            .collect(Collectors.toList());
    content.put("keys", Map.of("passwords", List.of(), "keyData", keyData));

    LOG.info(
        "Creating Tessera config file\n\tLocation: {} \n\tContents: {}",
        config.getFileSystemConfigurationFile(),
        content.toString());

    try {
      Files.write(
          config.getFileSystemConfigurationFile(),
          MAPPER.writeValueAsString(content).getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.CREATE);
    } catch (final IOException e) {
      final String message =
          String.format(
              "Problem creating the Tessera config file in the file system: %s, %s",
              config.getFileSystemConfigurationFile(), e.getLocalizedMessage());
      LOG.error(message);
      throw new IllegalStateException(message);
    }
  }

  private static Map<String, String> createKeyDataEntry(
      final PrivacyPrivateKeyResource privateKey, final PrivacyPublicKeyResource publicKey) {
    return Map.ofEntries(
        entry(
            "privateKeyPath",
            Path.of(CONTAINER_WORKING_DIRECTORY_PREFIX, privateKey.get()).toString()),
        entry(
            "publicKeyPath",
            Path.of(CONTAINER_WORKING_DIRECTORY_PREFIX, publicKey.get()).toString()));
  }
}
