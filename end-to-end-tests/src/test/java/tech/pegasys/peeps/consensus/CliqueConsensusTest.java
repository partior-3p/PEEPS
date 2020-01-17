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
package tech.pegasys.peeps.consensus;

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.network.Network;

import org.junit.jupiter.api.Test;

public class CliqueConsensusTest extends NetworkTest {
  @Override
  protected void setUpNetwork(final Network network) {
    // TODO Auto-generated method stub

  }

  @Test
  public void consensusAfterMiningMustHappen() {

    // TODO no in-line comments - implement clean code!

    // Network Two Besus, no EthSigners or Orions

    // Choose Clique as consensus mechanism

    // Mine: transfer

    // After suitable time / number of blocks both nodes but agree on state change &
    // have identical receipt (block number)

  }
}
