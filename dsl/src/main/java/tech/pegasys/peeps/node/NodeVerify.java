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
package tech.pegasys.peeps.node;

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.verification.NodeValueTransition;

public class NodeVerify {
  private final Besu node;

  public NodeVerify(final Besu node) {
    this.node = node;
  }

  public void transistion(final NodeValueTransition... changes) {
    node.verifyTransition(changes);
  }

  public void successfulTransactionReceipt(final Hash transaction) {
    node.verifySuccessfulTransactionReceipt(transaction);
  }
}
