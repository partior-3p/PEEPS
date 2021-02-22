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
package tech.pegasys.peeps.node.genesis;

import tech.pegasys.peeps.node.model.GenesisAddress;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonInclude(Include.NON_NULL)
public class Genesis {

  private final GenesisConfig config;
  private final Map<GenesisAddress, GenesisAccount> accountBalances;
  private GenesisExtraData extraData;

  public Genesis(
      final GenesisConfig config, final Map<GenesisAddress, GenesisAccount> accountBalances) {
    this(config, accountBalances, null);
  }

  public Genesis(
      final GenesisConfig config,
      final Map<GenesisAddress, GenesisAccount> accountBalances,
      final GenesisExtraData extraData) {
    this.config = config;
    this.accountBalances = accountBalances;
    this.extraData = extraData;
  }

  @JsonGetter("config")
  public GenesisConfig getConfig() {
    return config;
  }

  @JsonGetter("extraData")
  public GenesisExtraData getExtraData() {
    return extraData;
  }

  @JsonSetter("extraData")
  public void setExtraData(final GenesisExtraData extraData) {
    this.extraData = extraData;
  }

  @JsonGetter("alloc")
  public Map<GenesisAddress, GenesisAccount> getAccounts() {
    return accountBalances;
  }

  @JsonGetter("nonce")
  public String getNonce() {
    return "0x0";
  }

  @JsonGetter("timestamp")
  public String getTimestamp() {
    return "0x58ee40ba";
  }

  @JsonGetter("gasLimit")
  public String getGasLimit() {
    return "0x29B92700";
  }

  @JsonGetter("difficulty")
  public String getDifficulty() {
    return "0x1";
  }

  @JsonGetter("mixHash")
  public String getMixHash() {
    // This is REQUIRED for ibft, but doesn't hurt anything else (I hope).
    return "0x63746963616c2062797a616e74696e65206661756c7420746f6c6572616e6365";
  }

  @JsonGetter("coinbase")
  public String getCoinbase() {
    return "0x0000000000000000000000000000000000000000";
  }

  @JsonGetter("number")
  public String getNumber() {
    return "0x0";
  }

  @JsonGetter("gasUsed")
  public String getGasUsed() {
    return "0x0";
  }

  @JsonGetter("parentHash")
  public String getParentHash() {
    return "0x0000000000000000000000000000000000000000000000000000000000000000";
  }
}
