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
package tech.pegasys.peeps.node.rpc.priv;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivacyTransactionReceipt {

  private final String contractAddress;
  private final String sender;

  private final String output;
  private final String[] logs;
  private final String status;

  private String recipient;
  // TODO better typing than String

  @JsonCreator
  public PrivacyTransactionReceipt(
      @JsonProperty("contractAddress") final String contractAddress,
      @JsonProperty("from") final String sender,
      @JsonProperty("output") final String output,
      @JsonProperty("logs") final String[] logs,
      @JsonProperty("status") final String status) {
    this.contractAddress = contractAddress;
    this.sender = sender;
    this.output = output;
    this.logs = logs;
    this.status = status;
  }

  @JsonSetter("to")
  public void setRecipient(final String recipient) {
    this.recipient = recipient;
  }

  public String getContractAddress() {
    return contractAddress;
  }

  public String getSender() {
    return sender;
  }

  public Optional<String> getRecipient() {
    return Optional.ofNullable(recipient);
  }

  public String getOutput() {
    return output;
  }

  public String[] getLogs() {
    return logs;
  }

  public String getStatus() {
    return status;
  }
}
