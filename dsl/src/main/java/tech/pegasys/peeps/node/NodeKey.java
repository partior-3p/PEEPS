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
package tech.pegasys.peeps.node;

import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import tech.pegasys.peeps.util.ClasspathResources;

public enum NodeKey {
  ALPHA("node/keys/alpha"),
  BETA("node/keys/beta");

  private static final String PRIVATE_KEY_FILENAME = "/key.priv";
  private static final String PUBLIC_KEY_FILENAME = "/key.pub";

  private final String pubKey;
  private final String privKeyFile;

  NodeKey(final String keysDirectory) {

    this.pubKey = removeAnyHexPrefix(ClasspathResources.read(keysDirectory + PUBLIC_KEY_FILENAME));
    this.privKeyFile = keysDirectory + PRIVATE_KEY_FILENAME;
  }

  public String enodeAddress(final String hostIp, final int port) {
    return String.format("enode://%s@%s:%d", pubKey, hostIp, port);
  }

  public String getPrivateKeyFile() {
    return privKeyFile;
  }

  public String getPublicKey() {
    return pubKey;
  }
}
