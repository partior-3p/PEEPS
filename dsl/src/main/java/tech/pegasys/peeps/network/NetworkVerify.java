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

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.privacy.PrivacyGroupVerify;
import tech.pegasys.peeps.privacy.model.PrivacyGroup;

import java.util.List;

import org.apache.tuweni.eth.Address;

public class NetworkVerify {
  private final Network network;

  public NetworkVerify(final Network network) {
    this.network = network;
  }

  public void consensusOnValueAt(final Address... accounts) {
    network.verifyConsensusOnValue(accounts);
  }

  public void consensusOnTransaction(final Hash transaction) {
    network.verifyConsensusOnTransaction(transaction);
  }

  public void consensusOnBlockNumberIsAtLeast(final long blockNumber) {
    network.verifyConsensusOnBlockNumberIsAtLeast(blockNumber);
  }

  // TODO perhaps a separate specialisation - privacy?
  public void consensusOnPrivacyTransactionReceipt(final Hash transaction) {
    network.verifyConsensusOnPrivacyTransactionReceipt(transaction);
  }

  public PrivacyGroupVerify privacyGroup(final PrivacyGroup group) {
    return network.privacyGroup(group);
  }

  public void consensusOnValidators(final List<Address> validators) {
    network.verifyConsensusOnValidators(validators);
  }

  public void gasRewardsAreTransferredToValidator(final Hash receipt) {
    network.verifyGasRewardsAreTransferredToValidator(receipt);
  }
}
