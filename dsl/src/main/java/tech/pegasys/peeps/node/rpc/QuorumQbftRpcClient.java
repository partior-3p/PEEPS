/*
 * Copyright 2021 ConsenSys AG.
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
package tech.pegasys.peeps.node.rpc;

import tech.pegasys.peeps.json.rpc.JsonRpcClient;
import tech.pegasys.peeps.node.rpc.qbft.QbftGetValidatorsResponse;

import java.util.List;
import java.util.Map;

import org.apache.tuweni.eth.Address;

public class QuorumQbftRpcClient implements QbftRpc {

  private final JsonRpcClient rpcClient;

  public QuorumQbftRpcClient(final JsonRpcClient rpcClient) {
    this.rpcClient = rpcClient;
  }

  @Override
  public boolean qbftProposeValidatorVote(final Address validator, final VoteType vote) {
    rpcClient.post("istanbul_propose", Map.class, validator.toHexString(), vote == VoteType.ADD);
    // returns a null value for the result so no need for capture this value
    return true;
  }

  @Override
  public List<Address> qbftGetValidatorsByBlockBlockNumber(final String blockNumber) {
    return rpcClient
        .post("istanbul_getValidators", QbftGetValidatorsResponse.class, blockNumber)
        .getResult();
  }
}
