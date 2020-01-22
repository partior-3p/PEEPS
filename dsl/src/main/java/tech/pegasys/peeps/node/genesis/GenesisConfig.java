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

import com.fasterxml.jackson.annotation.JsonGetter;

public abstract class GenesisConfig {

  private static final int GENESIS_BLOCK_NUMBER = 0;
  private static final String GENESIS_BLOCK_HASH =
      "0x0000000000000000000000000000000000000000000000000000000000000000";

  private final long chainId;

  public GenesisConfig(final long chainId) {
    this.chainId = chainId;
  }

  @JsonGetter("chainId")
  public long getChainId() {
    return chainId;
  }

  @JsonGetter("contractSizeLimit")
  public int getContractSizeLimit() {
    return Integer.MAX_VALUE;
  }

  @JsonGetter("homesteadBlock")
  public int getHomesteadBlock() {
    return GENESIS_BLOCK_NUMBER;
  }

  @JsonGetter("daoForkBlock")
  public int getDaoForkBlock() {
    return GENESIS_BLOCK_NUMBER;
  }

  @JsonGetter("eip150Block")
  public int getEip150Block() {
    return GENESIS_BLOCK_NUMBER;
  }

  @JsonGetter("eip150Hash")
  public String getEip150Hash() {
    return GENESIS_BLOCK_HASH;
  }

  @JsonGetter("eip155Block")
  public int getEip155Block() {
    return GENESIS_BLOCK_NUMBER;
  }

  @JsonGetter("eip158Block")
  public int getEip158Block() {
    return GENESIS_BLOCK_NUMBER;
  }

  @JsonGetter("byzantiumBlock")
  public int getByzantiumBlock() {
    return GENESIS_BLOCK_NUMBER;
  }

  @JsonGetter("constantinopleBlock")
  public int getConstantinopleBlock() {
    return GENESIS_BLOCK_NUMBER;
  }

  @JsonGetter("constantinopleFixBlock")
  public int getConstantinopleFixBlock() {
    return GENESIS_BLOCK_NUMBER;
  }
}
