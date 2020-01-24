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
import tech.pegasys.peeps.node.rpc.NodeRpcExpectingData;
import tech.pegasys.peeps.privacy.model.PrivacyAddreess;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;

public class SignerRpcExpectingData extends NodeRpcExpectingData {

  private static final Logger LOG = LogManager.getLogger();

  private final SignerRpc rpc;
  private final Supplier<String> signerLogs;
  private final Supplier<String> downstreamLogs;

  public SignerRpcExpectingData(
      final SignerRpc rpc,
      final Supplier<String> signerLogs,
      final Supplier<String> downstreamLogs) {
    super(rpc);
    this.rpc = rpc;
    this.downstreamLogs = downstreamLogs;
    this.signerLogs = signerLogs;
  }

  public Hash deployContractToPrivacyGroup(
      final Address sender,
      final String binary,
      final PrivacyAddreess privacySender,
      final PrivacyAddreess... privacyRecipients) {
    final String[] privateRecipients = new String[privacyRecipients.length];
    for (int i = 0; i < privacyRecipients.length; i++) {
      privateRecipients[i] = privacyRecipients[i].get();
    }

    try {
      return rpc.deployContractToPrivacyGroup(
          sender, binary, privacySender.get(), privateRecipients);
    } catch (final RuntimeException e) {
      LOG.error(signerLogs.get());
      LOG.error(downstreamLogs.get());
      throw e;
    }
  }

  public Hash transfer(final Address sender, final Address receiver, final Wei amount) {
    return rpc.transfer(sender, receiver, amount);
  }
}
