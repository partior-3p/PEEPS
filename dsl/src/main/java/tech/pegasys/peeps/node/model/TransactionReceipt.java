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
package tech.pegasys.peeps.node.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionReceipt {

  private final Address sender;
  private final String transactionIndex;
  private final String status;

  // TODO stricter typing than String
  private final String blockHash;
  private final String blockNumber;
  private final String cumulativeGasUsed;
  private final String gasUsed;
  private final String transactionHash;
  private final String logsBloom;

  private Address recipient;
  private Address contract;

  @JsonCreator
  public TransactionReceipt(
      @JsonProperty("blockHash") final String blockHash,
      @JsonProperty("blockNumber") final String blockNumber,
      @JsonProperty("from") final Address from,
      @JsonProperty("transactionHash") final String transactionHash,
      @JsonProperty("transactionIndex") final String transactionIndex,
      @JsonProperty("status") final String status,
      @JsonProperty("cumulativeGasUsed") final String cumulativeGasUsed,
      @JsonProperty("gasUsed") final String gasUsed,
      @JsonProperty("logsBloom") final String logsBloom) {
    this.blockHash = blockHash;
    this.blockNumber = blockNumber;
    this.sender = from;
    this.transactionHash = transactionHash;
    this.transactionIndex = transactionIndex;
    this.status = status;
    this.cumulativeGasUsed = cumulativeGasUsed;
    this.gasUsed = gasUsed;
    this.logsBloom = logsBloom;
  }

  @JsonSetter("to")
  public void setRecipient(final Address recipient) {
    this.recipient = recipient;
  }

  @JsonSetter("contractAddress")
  public void setContractAddress(final Address contract) {
    this.contract = contract;
  }

  public Optional<Address> getContractAddress() {
    return Optional.ofNullable(contract);
  }

  public Address getSender() {
    return sender;
  }

  public Optional<Address> getRecipient() {
    return Optional.ofNullable(recipient);
  }

  public String getBlockHash() {
    return blockHash;
  }

  public String getBlockNumber() {
    return blockNumber;
  }

  public String getTransactionIndex() {
    return transactionIndex;
  }

  public String getTransactionHash() {
    return transactionHash;
  }

  public String getCumulativeGasUsed() {
    return cumulativeGasUsed;
  }

  public String getGasUsed() {
    return gasUsed;
  }

  public String getLogsBloom() {
    return logsBloom;
  }

  public boolean isSuccess() {
    return status.contentEquals("0x1");
  }
}
