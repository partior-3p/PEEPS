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

import tech.pegasys.peeps.node.rpc.NodeRpcExpectingData;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class ValueReceived implements NodeValueTransition {

  private final Address receiver;
  private final Wei before;
  private final Wei value;

  public ValueReceived(final Address receiver, final Wei before, final Wei value) {
    this.receiver = receiver;
    this.before = before;
    this.value = value;
  }

  @Override
  public void verify(final NodeRpcExpectingData rpc) {
    final Wei after = rpc.getBalance(receiver);
    assertThat(after).isEqualTo(before.add(value));
  }
}
