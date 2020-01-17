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
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.ethereum.Gas;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionReceipt {

  private final Address sender;
  private final Hash blockHash;
  private final Hash transactionHash;
  private final Gas gasUsed;

  // TODO stricter typing than String
  private final String blockNumber;
  private final String cumulativeGasUsed;
  private final String logsBloom;
  private final String transactionIndex;
  private final String status;

  private Address recipient;
  private Address contract;

  @JsonCreator
  public TransactionReceipt(
      @JsonProperty("blockHash") final Hash blockHash,
      @JsonProperty("blockNumber") final String blockNumber,
      @JsonProperty("from") final String sender,
      @JsonProperty("transactionHash") final Hash transactionHash,
      @JsonProperty("transactionIndex") final String transactionIndex,
      @JsonProperty("status") final String status,
      @JsonProperty("cumulativeGasUsed") final String cumulativeGasUsed,
      @JsonProperty("gasUsed") final String gasUsed,
      @JsonProperty("logsBloom") final String logsBloom) {
    this.blockHash = blockHash;
    this.blockNumber = blockNumber;
    this.sender = Address.fromHexString(sender);
    this.transactionHash = transactionHash;
    this.transactionIndex = transactionIndex;
    this.status = status;
    this.cumulativeGasUsed = cumulativeGasUsed;
    this.gasUsed = Gas.valueOf(UInt256.fromHexString(gasUsed));
    this.logsBloom = logsBloom;
  }

  @JsonSetter("to")
  public void setRecipient(final String recipient) {
    this.recipient = recipient == null ? null : Address.fromHexString(recipient);
  }

  @JsonSetter("contractAddress")
  public void setContractAddress(final String contract) {
    this.contract = contract == null ? null : Address.fromHexString(contract);
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

  public Hash getBlockHash() {
    return blockHash;
  }

  public String getBlockNumber() {
    return blockNumber;
  }

  public String getTransactionIndex() {
    return transactionIndex;
  }

  public Hash getTransactionHash() {
    return transactionHash;
  }

  public String getCumulativeGasUsed() {
    return cumulativeGasUsed;
  }

  public Gas getGasUsed() {
    return gasUsed;
  }

  public String getLogsBloom() {
    return logsBloom;
  }

  public boolean isSuccess() {
    return status.contentEquals("0x1");
  }
}
