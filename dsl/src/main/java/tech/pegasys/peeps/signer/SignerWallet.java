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

public enum SignerWallet {
  ALPHA("signer/account/funded/wallet_a.v3", "signer/account/funded/wallet_a.pass"),
  BETA("signer/account/funded/wallet_b.v3", "signer/account/funded/wallet_b.pass");

  private final String keyResource;
  private final String passwordResource;

  SignerWallet(final String keyResource, final String passwordResource) {
    this.keyResource = keyResource;
    this.passwordResource = passwordResource;
  }

  public String getKeyResource() {
    return keyResource;
  }

  public String getPasswordResource() {
    return passwordResource;
  }
}
