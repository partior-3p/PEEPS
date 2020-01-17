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
package tech.pegasys.peeps.node.rpc.eth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.ethereum.Wei;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetBalanceResponse {

  private final Wei result;

  @JsonCreator
  public GetBalanceResponse(@JsonProperty("result") final String balance) {
    this.result = Wei.valueOf(UInt256.fromHexString(balance));
  }

  public Wei getResult() {
    return result;
  }
}
