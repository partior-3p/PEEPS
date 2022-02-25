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
package tech.pegasys.peeps.network;

import static tech.pegasys.peeps.util.Await.DEFAULT_TIMEOUT_IN_SECONDS;

import tech.pegasys.peeps.node.model.Hash;

public class NetworkAwait {

  private final Network network;

  public NetworkAwait(final Network network) {
    this.network = network;
  }

  public void consensusOnTransactionReceipt(final Hash receipt) {
    network.awaitConsensusOnTransactionReceipt(receipt, DEFAULT_TIMEOUT_IN_SECONDS);
  }

  public void consensusOnTransactionReceipt(final Hash receipt, final int timeout) {
    network.awaitConsensusOnTransactionReceipt(receipt, timeout);
  }
}
