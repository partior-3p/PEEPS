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
package tech.pegasys.peeps.signer;

import tech.pegasys.peeps.node.Account;

import org.apache.tuweni.eth.Address;

public enum SignerWallet {
  ALPHA(
      "signer/account/funded/wallet_a.v3",
      "signer/account/funded/wallet_a.pass",
      Account.ALPHA.address()),
  BETA(
      "signer/account/funded/wallet_b.v3",
      "signer/account/funded/wallet_b.pass",
      Account.BETA.address());

  private final String keyResource;
  private final String passwordResource;
  private final Address address;

  SignerWallet(final String keyResource, final String passwordResource, final Address address) {
    this.keyResource = keyResource;
    this.passwordResource = passwordResource;
    this.address = address;
  }

  public String keyResource() {
    return keyResource;
  }

  public String passwordResource() {
    return passwordResource;
  }

  public Address address() {
    return address;
  }
}
