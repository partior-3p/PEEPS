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
package tech.pegasys.peeps.node.genesis.ibft2;

import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.genesis.GenesisExtraData;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.Hash;
import org.apache.tuweni.crypto.SECP256K1.Signature;
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.rlp.RLP;

public class GenesisExtraDataIbft2 extends GenesisExtraData {

  public GenesisExtraDataIbft2(final Web3Provider... validators) {
    super(encode(validators));
  }

  public static Bytes encode(final Web3Provider... validators) {

    return encode(
        Stream.of(validators)
            .parallel()
            .map(
                validator ->
                    Address.fromBytes(
                        Hash.keccak256(Bytes.fromHexString(validator.nodePublicKey()))
                            .slice(12, 20)))
            .collect(Collectors.toList()));
  }

  private static Bytes encode(final List<Address> validators) {
    final byte[] vanityData = new byte[32];
    final byte[] round = new byte[4];
    final byte[] votes = new byte[0];
    final List<Signature> seals = Collections.emptyList();

    return RLP.encode(
        writer ->
            writer.writeList(
                listWriter -> {
                  listWriter.writeByteArray(vanityData);
                  listWriter.writeList(
                      validators, (rlp, validator) -> rlp.writeValue(validator.toBytes()));
                  listWriter.writeByteArray(votes);
                  listWriter.writeByteArray(round);
                  listWriter.writeList(
                      seals, (rlp, committer) -> rlp.writeValue(committer.bytes()));
                }));
  }
}
