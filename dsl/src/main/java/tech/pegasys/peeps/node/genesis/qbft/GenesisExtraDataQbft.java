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
package tech.pegasys.peeps.node.genesis.qbft;

import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.genesis.GenesisExtraData;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.consensus.qbft.QbftExtraDataCodec;
import org.hyperledger.besu.ethereum.core.Address;

public class GenesisExtraDataQbft extends GenesisExtraData {

  public GenesisExtraDataQbft(final Web3Provider... validators) {
    super(encode(validators));
  }

  static Bytes encode(final Web3Provider... validators) {
    final List<Address> addresses =
        Stream.of(validators)
            .parallel()
            .map(GenesisExtraData::extractAddress)
            .collect(Collectors.toList());

    // delegate to Besu QBFT ExtraData implementation
    return QbftExtraDataCodec.encodeFromAddresses(addresses);
  }
}
