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
package tech.pegasys.peeps.node;

import tech.pegasys.peeps.node.genesis.GenesisAccount;
import tech.pegasys.peeps.node.model.GenesisAddress;
import tech.pegasys.peeps.util.HexFormatter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.tuweni.eth.Address;

// TODO split into separate responsibilities - account & the of()
public enum Account {
  /*
   * publicKey: "f17f52151ebef6c7334fad080c5704d77216b732"
   * privateKey: "ae6ae8e5ccbfb04590405997ee2d52d2b330726137b875053c36d94e974d162f"
   */
  ALPHA("f17f52151EbEF6C7334FAD080c5704D77216b732"),
  /*
   * publicKey: "627306090abab3a6e1400e9345bc60c78a8bef57"
   * privateKey: "c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3"
   */
  BETA("627306090abaB3A6e1400e9345bC60c78a8BEf57"),
  /*
   * publicKey: "fe3b557e8fb62b89f4916b721be55ceb828dbd73"
   * privateKey: "8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63"
   */
  GAMMA("fe3b557e8fb62b89f4916b721be55ceb828dbd73");

  private static final String DEFAULT_BALANCE = "0xad78ebc5ac6200000";

  private final GenesisAddress genesisAddres;
  private final Address address;

  private Account(final String address) {
    this.genesisAddres = new GenesisAddress(address);
    this.address = Address.fromHexString(HexFormatter.ensureHexPrefix(address));
  }

  public Address address() {
    return address;
  }

  public static Map<GenesisAddress, GenesisAccount> of(final Account... accounts) {
    final Map<GenesisAddress, GenesisAccount> mapped = new HashMap<>();

    for (final Account account : accounts) {
      mapped.put(account.genesisAddres, new GenesisAccount(DEFAULT_BALANCE));
    }

    return Collections.unmodifiableMap(mapped);
  }
}
