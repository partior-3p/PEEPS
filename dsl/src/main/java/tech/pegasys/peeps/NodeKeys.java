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
package tech.pegasys.peeps;

import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import tech.pegasys.peeps.util.ClasspathResources;

// TODO comment expectations of how directories/files under resources are structured
// TODO may don't use an enum?
public enum NodeKeys {
  BOOTNODE("keys/bootnode");

  private static final String PRIVATE_KEY_FILENAME = "/key.priv";
  private static final String PUBLIC_KEY_FILENAME = "/key.pub";

  private final String pubKey;
  private final String privKeyFile;

  NodeKeys(final String keysDirectory) {

    this.pubKey = removeAnyHexPrefix(ClasspathResources.read(keysDirectory + PUBLIC_KEY_FILENAME));
    this.privKeyFile = keysDirectory + PRIVATE_KEY_FILENAME;
  }

  public String getEnodeAddress(final String hostIp, final String port) {
    return String.format("enode://%s@%s:%s", pubKey, hostIp, port);
  }

  public String getPrivateKeyFile() {
    return privKeyFile;
  }
}
