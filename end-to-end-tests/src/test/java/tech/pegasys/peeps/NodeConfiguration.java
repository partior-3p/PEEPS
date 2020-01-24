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

import tech.pegasys.peeps.node.model.NodeIdentifier;
import tech.pegasys.peeps.node.model.NodeKey;
import tech.pegasys.peeps.node.model.NodePrivateKeyResource;
import tech.pegasys.peeps.node.model.NodePublicKeyResource;

public enum NodeConfiguration {
  ALPHA("node/keys/alpha"),
  BETA("node/keys/beta");

  private static final String PRIVATE_KEY_FILENAME = "/key.priv";
  private static final String PUBLIC_KEY_FILENAME = "/key.pub";

  private final NodePublicKeyResource pubKeyResource;
  private final NodePrivateKeyResource privKeyResource;
  private final NodeIdentifier id;

  NodeConfiguration(final String keysDirectoryResource) {

    this.pubKeyResource = new NodePublicKeyResource(keysDirectoryResource + PUBLIC_KEY_FILENAME);
    this.privKeyResource = new NodePrivateKeyResource(keysDirectoryResource + PRIVATE_KEY_FILENAME);
    this.id = new NodeIdentifier(name());
  }

  public NodeIdentifier id() {
    return id;
  }

  public NodeKey keys() {
    return new NodeKey() {

      @Override
      public NodePublicKeyResource nodePublicKeyResource() {
        return pubKeyResource;
      }

      @Override
      public NodePrivateKeyResource nodePrivateKeyResource() {
        return privKeyResource;
      }
    };
  }
}
