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

import tech.pegasys.peeps.util.ClasspathResources;

// TODO maybe not use an enum?
public enum OrionKeyPair {
  ALPHA("privacy/keys/key_0"),
  BETA("privacy/keys/key_1"),
  GAMMA("privacy/keys/key_2");

  private static final String PRIVATE_KEY_FILENAME = "%s.priv";
  private static final String PUBLIC_KEY_FILENAME = "%s.pub";

  private final String pubKeyResource;
  private final String privKeyResource;
  private final String pubKey;

  OrionKeyPair(final String name) {
    this.privKeyResource = String.format(PRIVATE_KEY_FILENAME, name);
    this.pubKeyResource = String.format(PUBLIC_KEY_FILENAME, name);
    this.pubKey = ClasspathResources.read(pubKeyResource);
  }

  public String getPublicKeyResource() {
    return pubKeyResource;
  }

  public String getPrivateKeyResource() {
    return privKeyResource;
  }

  public String getPublicKey() {
    return pubKey;
  }
}
