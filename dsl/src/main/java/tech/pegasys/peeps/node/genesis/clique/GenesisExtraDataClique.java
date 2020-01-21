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
package tech.pegasys.peeps.node.genesis.clique;

import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.genesis.GenesisExtraData;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.Hash;
import org.apache.tuweni.eth.Address;

public class GenesisExtraDataClique extends GenesisExtraData {

  public GenesisExtraDataClique(final Besu... validators) {
    super(encode(validators));
  }

  private static Bytes encode(final Besu... validators) {

    return encode(
        Stream.of(validators)
            .parallel()
            .map(
                validator ->
                    Address.fromBytes(
                        Hash.keccak256(Bytes.fromHexString(validator.identity().getPublicKey()))
                            .slice(12, 20)))
            .collect(Collectors.toList()));
  }

  private static Bytes encode(final List<Address> validators) {
    final Bytes vanityData = Bytes.wrap(new byte[32]);
    final Bytes proposerSeal = Bytes.wrap(new byte[65]);
    final Bytes genesisValidators =
        Bytes.concatenate(
            validators
                .parallelStream()
                .map(validator -> validator.toBytes())
                .toArray(Bytes[]::new));

    return Bytes.concatenate(vanityData, genesisValidators, proposerSeal);
  }
}
