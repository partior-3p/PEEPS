/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.peeps.signer.rpc;

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.rpc.NodeRpc;
import tech.pegasys.peeps.privacy.model.PrivacyAddreess;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public interface SignerRpc extends NodeRpc {

  Hash transfer(Address sender, Address receiver, Wei amount);

  String enode();

  Hash deployContractToPrivacyGroup(
      Address sender, String binary, PrivacyAddreess string, PrivacyAddreess... privateRecipients);
}
