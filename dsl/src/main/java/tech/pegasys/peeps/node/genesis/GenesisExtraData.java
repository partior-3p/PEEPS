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

import tech.pegasys.peeps.node.Web3Provider;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.crypto.SECP256K1;
import org.hyperledger.besu.ethereum.core.Address;

public abstract class GenesisExtraData {
  private static final SECP256K1 SECP_256_K_1 = new SECP256K1();
  private final Bytes extraData;

  public GenesisExtraData(final Bytes extraData) {
    this.extraData = extraData;
  }

  @JsonValue
  public String getExtraData() {
    return extraData.toString();
  }

  protected static Address extractAddress(final Web3Provider validator) {
    return Address.extract(
        SECP_256_K_1.createPublicKey(Bytes.fromHexString(validator.nodePublicKey())));
  }
}
