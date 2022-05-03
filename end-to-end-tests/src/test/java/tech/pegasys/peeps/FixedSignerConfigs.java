/*
 * Copyright 2021 ConsenSys AG.
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
import tech.pegasys.peeps.signer.SignerConfiguration;

public class FixedSignerConfigs {

  public static final SignerConfiguration ALPHA =
      new SignerConfiguration(
          "alpha",
          "signer/account/funded/wallet_a.v3",
          "signer/account/funded/wallet_a.pass",
          Account.ALPHA.credentials());

  public static final SignerConfiguration BETA =
      new SignerConfiguration(
          "beta",
          "signer/account/funded/wallet_b.v3",
          "signer/account/funded/wallet_b.pass",
          Account.BETA.credentials());
}
