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

import tech.pegasys.peeps.privacy.model.PrivacyPrivateKeyResource;
import tech.pegasys.peeps.privacy.model.PrivacyPublicKeyResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrionConfigurationFile {

  private static final Logger LOG = LogManager.getLogger();
  private static final int HTTP_RPC_PORT = 8888;
  private static final int PEER_TO_PEER_PORT = 8080;

  public static void write(PrivateTransactionManagerConfiguration config) {

    final StringBuilder content = new StringBuilder();
    content.append(
        String.format(
            "nodeurl = \"http://%s:%d\"\n", config.getIpAddress().get(), PEER_TO_PEER_PORT));
    content.append(
        String.format(
            "clienturl = \"http://%s:%d\"\n", config.getIpAddress().get(), HTTP_RPC_PORT));
    content.append(String.format("nodeport = %d\n", PEER_TO_PEER_PORT));
    content.append(String.format("clientport = %d\n", HTTP_RPC_PORT));
    content.append(String.format("publickeys = [%s]\n", flattenPublicKeys(config.getPublicKeys())));
    content.append(
        String.format("privatekeys = [%s]\n", flattenPrivateKeys(config.getPrivateKeys())));

    content.append("nodenetworkinterface = \"0.0.0.0\"\n");
    content.append("clientnetworkinterface = \"0.0.0.0\"\n");

    if (config.getBootnodeUrls().isPresent()) {
      content.append(
          String.format("othernodes  = [%s]\n", flatten(config.getBootnodeUrls().get())));
    }

    LOG.info(
        "Creating Orion config file\n\tLocation: {} \n\tContents: {}",
        config.getFileSystemConfigurationFile(),
        content.toString());

    try {
      Files.write(
          config.getFileSystemConfigurationFile(),
          content.toString().getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.CREATE);
    } catch (final IOException e) {
      final String message =
          String.format(
              "Problem creating the Orion config file in the file system: %s, %s",
              config.getFileSystemConfigurationFile(), e.getLocalizedMessage());
      LOG.error(message);
      throw new IllegalStateException(message);
    }
  }

  private static String flattenPrivateKeys(final List<PrivacyPrivateKeyResource> values) {
    return flatten(
        values.parallelStream().map(PrivacyPrivateKeyResource::get).collect(Collectors.toList()));
  }

  private static String flattenPublicKeys(final List<PrivacyPublicKeyResource> values) {
    return flatten(
        values.parallelStream().map(PrivacyPublicKeyResource::get).collect(Collectors.toList()));
  }

  private static String flatten(final List<String> values) {
    return values.stream().map(v -> "\"" + v + "\"").collect(Collectors.joining(","));
  }
}
