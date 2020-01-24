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

import tech.pegasys.peeps.privacy.model.PrivacyAddreess;
import tech.pegasys.peeps.privacy.model.PrivacyKeyPair;
import tech.pegasys.peeps.privacy.model.PrivacyManagerIdentifier;
import tech.pegasys.peeps.privacy.model.PrivacyPrivateKeyResource;
import tech.pegasys.peeps.privacy.model.PrivacyPublicKeyResource;
import tech.pegasys.peeps.util.ClasspathResources;

public enum PrivacyManagerConfiguration {
  ALPHA("privacy/keys/key_0"),
  BETA("privacy/keys/key_1"),
  GAMMA("privacy/keys/key_2");

  private static final String PRIVATE_KEY_FILENAME = "%s.priv";
  private static final String PUBLIC_KEY_FILENAME = "%s.pub";

  private final PrivacyKeyPair keyPair;
  private final PrivacyAddreess address;
  private final PrivacyManagerIdentifier id;

  PrivacyManagerConfiguration(final String name) {
    final String privKeyResource = String.format(PRIVATE_KEY_FILENAME, name);
    final String pubKeyResource = String.format(PUBLIC_KEY_FILENAME, name);
    final String pubKey = ClasspathResources.read(pubKeyResource);

    this.keyPair =
        new PrivacyKeyPair() {

          @Override
          public PrivacyPublicKeyResource getPublicKey() {
            return new PrivacyPublicKeyResource(pubKeyResource);
          }

          @Override
          public PrivacyPrivateKeyResource getPrivateKey() {

            return new PrivacyPrivateKeyResource(privKeyResource);
          }
        };

    this.address = new PrivacyAddreess(pubKey);
    this.id = new PrivacyManagerIdentifier(name());
  }

  public PrivacyManagerIdentifier id() {
    return id;
  }

  public PrivacyKeyPair keyPair() {
    return keyPair;
  }

  public PrivacyAddreess address() {
    return address;
  }
}
