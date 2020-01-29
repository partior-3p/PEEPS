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
package tech.pegasys.peeps.node.verification;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.node.rpc.NodeRpc;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class AccountValue {

  private final Address account;
  private final Wei value;

  public AccountValue(final Address account, final Wei value) {
    this.account = account;
    this.value = value;
  }

  public void verify(final NodeRpc rpc) {
    assertThat(rpc.getBalance(account)).isEqualTo(value);
  }
}
