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
package tech.pegasys.peeps;

import tech.pegasys.peeps.node.Account;
import tech.pegasys.peeps.signer.model.SignerIdentifier;
import tech.pegasys.peeps.signer.model.SignerKeyFileResource;
import tech.pegasys.peeps.signer.model.SignerPasswordFileResource;
import tech.pegasys.peeps.signer.model.WalletFileResources;

import org.apache.tuweni.eth.Address;

public enum SignerConfiguration {
  ALPHA(
      "signer/account/funded/wallet_a.v3",
      "signer/account/funded/wallet_a.pass",
      Account.ALPHA.address()),
  BETA(
      "signer/account/funded/wallet_b.v3",
      "signer/account/funded/wallet_b.pass",
      Account.BETA.address());

  private final Address address;
  private final SignerIdentifier id;
  private final WalletFileResources resources;

  SignerConfiguration(
      final String keyResource, final String passwordResource, final Address address) {
    this.address = address;

    this.resources =
        new WalletFileResources() {

          @Override
          public SignerPasswordFileResource getPassword() {
            return new SignerPasswordFileResource(passwordResource);
          }

          @Override
          public SignerKeyFileResource getKey() {
            return new SignerKeyFileResource(keyResource);
          }
        };

    this.id = new SignerIdentifier(name());
  }

  public SignerIdentifier id() {
    return id;
  }

  public Address address() {
    return address;
  }

  public WalletFileResources resources() {
    return resources;
  }
}
