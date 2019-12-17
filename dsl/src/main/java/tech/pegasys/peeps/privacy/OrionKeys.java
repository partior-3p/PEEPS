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

// TODO maybe not use an enum?
public enum OrionKeys {
  ONE("privacy/keys/key_0"),
  TWO("privacy/keys/key_1"),
  THREE("privacy/keys/key_2");

  private static final String PRIVATE_KEY_FILENAME = "%s.priv";
  private static final String PUBLIC_KEY_FILENAME = "%s.pub";

  private final String pubKey;
  private final String privKey;

  OrionKeys(final String name) {
    privKey = String.format(PRIVATE_KEY_FILENAME, name);
    pubKey = String.format(PUBLIC_KEY_FILENAME, name);
  }

  public String getPublicKey() {
    return pubKey;
  }

  public String getPrivateKey() {
    return privKey;
  }
}
