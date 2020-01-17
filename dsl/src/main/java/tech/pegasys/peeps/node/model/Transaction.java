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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.ethereum.Gas;
import org.apache.tuweni.units.ethereum.Wei;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

  private final Address sender;
  private final Gas gas;
  private final Wei gasPrice;
  private final Wei value;

  // TODO stricter typing than String
  private final String blockHash;
  private final String blockNumber;
  private final String hash;
  private final String input;
  private final String nonce;
  private final String v;
  private final String r;
  private final String s;

  private Address recipient;
  private String transactionIndex;

  @JsonCreator
  public Transaction(
      @JsonProperty("from") final String sender,
      @JsonProperty("blockHash") final String blockHash,
      @JsonProperty("blockNumber") final String blockNumber,
      @JsonProperty("gas") final String gas,
      @JsonProperty("gasPrice") final String gasPrice,
      @JsonProperty("hash") final String hash,
      @JsonProperty("input") final String input,
      @JsonProperty("nonce") final String nonce,
      @JsonProperty("value") final String value,
      @JsonProperty("v") final String v,
      @JsonProperty("r") final String r,
      @JsonProperty("s") final String s) {
    this.sender = Address.fromHexString(sender);
    this.blockHash = blockHash;
    this.blockNumber = blockNumber;
    this.gas = Gas.valueOf(UInt256.fromHexString(gas));
    this.gasPrice = Wei.valueOf(UInt256.fromHexString(gasPrice));
    this.hash = hash;
    this.input = input;
    this.nonce = nonce;
    this.value = Wei.valueOf(UInt256.fromHexString(value));
    this.v = v;
    this.r = r;
    this.s = s;
  }

  @JsonSetter("to")
  public void setRecipient(final String recipient) {
    this.recipient = recipient == null ? null : Address.fromHexString(recipient);
  }

  @JsonSetter("transactionIndex")
  public void setTransactionIndex(final String transactionIndex) {
    this.transactionIndex = transactionIndex;
  }

  public Address getSender() {
    return sender;
  }

  public String getBlockHash() {
    return blockHash;
  }

  public String getBlockNumber() {
    return blockNumber;
  }

  public Gas getGas() {
    return gas;
  }

  public Wei getGasPrice() {
    return gasPrice;
  }

  public String getHash() {
    return hash;
  }

  public String getInput() {
    return input;
  }

  public String getNonce() {
    return nonce;
  }

  public Wei getValue() {
    return value;
  }

  public String getV() {
    return v;
  }

  public String getR() {
    return r;
  }

  public String getS() {
    return s;
  }

  public Address getRecipient() {
    return recipient;
  }

  public boolean isProcessed() {
    return transactionIndex != null;
  }
}
