/*
 * Copyright 2022 ConsenSys AG.
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
package tech.pegasys.peeps.node.genesis.qbft;

import tech.pegasys.peeps.node.genesis.transitions.Transition;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.apache.tuweni.eth.Address;

public class MiningBeneficiaryTransition implements Transition {
  private final long block;
  private final Address miningBeneficiary;

  public MiningBeneficiaryTransition(final long block, final Address miningBeneficiary) {
    this.block = block;
    this.miningBeneficiary = miningBeneficiary;
  }

  @JsonGetter("block")
  public long getBlock() {
    return block;
  }

  @JsonGetter("miningBeneficiary")
  public String getBlockReward() {
    return miningBeneficiary.toHexString();
  }
}
