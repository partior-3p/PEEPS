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
package tech.pegasys.peeps.signer.rpc.eea;

import tech.pegasys.peeps.privacy.model.PrivacyAddreess;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.tuweni.eth.Address;

public class SendPrivacyTransactionRequest {

  /* Default gas price of 1000 wei.*/
  private static final String DEFAULT_GAS_PRICE = "0x3E8";

  /* Default gas limit of 3000000 wei. */
  private static final String DEFAULT_GAS_LIMIT = "0x2DC6C0";

  private final Address sender;
  private final Address recipient;
  private final String data;
  private final PrivacyAddreess privateSender;
  private final PrivacyAddreess[] privateRecipients;

  public SendPrivacyTransactionRequest(
      final Address sender,
      final Address recipient,
      final String data,
      final PrivacyAddreess privateSender,
      final PrivacyAddreess[] privateRecipients) {
    this.sender = sender;
    this.recipient = recipient;
    this.data = data;
    this.privateSender = privateSender;
    this.privateRecipients = privateRecipients;
  }

  @JsonGetter("from")
  public String getSender() {
    return sender.toHexString();
  }

  @JsonInclude(Include.NON_NULL)
  @JsonGetter("to")
  public String getRecipient() {
    return recipient != null ? recipient.toHexString() : null;
  }

  @JsonGetter("data")
  public String getData() {
    return data;
  }

  @JsonGetter("privateFrom")
  public PrivacyAddreess getPrivateSender() {
    return privateSender;
  }

  @JsonGetter("privateFor")
  public PrivacyAddreess[] getPrivateRecipients() {
    return privateRecipients;
  }

  @JsonGetter("restriction")
  public String getRestriction() {
    return "restricted";
  }

  @JsonGetter("gas")
  public String getGas() {
    return DEFAULT_GAS_LIMIT;
  }

  @JsonGetter("gasPrice")
  public String getGasPrice() {
    return DEFAULT_GAS_PRICE;
  }
}
