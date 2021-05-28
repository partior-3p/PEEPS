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
import tech.pegasys.peeps.node.rpc.qbft.BesuQbftProposeValidatorVoteResponse;
import tech.pegasys.peeps.node.rpc.qbft.QbftGetValidatorsResponse;

import java.util.List;

import org.apache.tuweni.eth.Address;

public class BesuQbftRpcClient implements QbftRpc {

  private final JsonRpcClient rpcClient;

  public BesuQbftRpcClient(final JsonRpcClient rpcClient) {
    this.rpcClient = rpcClient;
  }

  @Override
  public boolean qbftProposeValidatorVote(final Address validator, final VoteType vote) {
    return rpcClient
        .post(
            "qbft_proposeValidatorVote",
            BesuQbftProposeValidatorVoteResponse.class,
            validator.toHexString(),
            vote == VoteType.ADD)
        .getResult();
  }

  @Override
  public List<Address> qbftGetValidatorsByBlockBlockNumber(final String blockNumber) {
    return rpcClient
        .post("qbft_getValidatorsByBlockNumber", QbftGetValidatorsResponse.class, blockNumber)
        .getResult();
  }
}
