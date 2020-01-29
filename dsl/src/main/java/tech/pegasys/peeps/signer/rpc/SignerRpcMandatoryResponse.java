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
package tech.pegasys.peeps.signer.rpc;

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.rpc.NodeRpcMandatoryResponse;
import tech.pegasys.peeps.privacy.model.PrivacyAddreess;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class SignerRpcMandatoryResponse extends NodeRpcMandatoryResponse implements SignerRpc {

  private final SignerRpcClient rpc;

  public SignerRpcMandatoryResponse(final SignerRpcClient rpc) {
    super(rpc);
    this.rpc = rpc;
  }

  @Override
  public Hash deployContractToPrivacyGroup(
      final Address sender,
      final String binary,
      final PrivacyAddreess privacySender,
      final PrivacyAddreess... privacyRecipients) {
    return rpc.deployContractToPrivacyGroup(sender, binary, privacySender, privacyRecipients);
  }

  @Override
  public Hash transfer(final Address sender, final Address receiver, final Wei amount) {
    return rpc.transfer(sender, receiver, amount);
  }

  @Override
  public String enode() {
    return rpc.enode();
  }
}
