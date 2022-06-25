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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.tuweni.eth.Address;
import org.web3j.crypto.Credentials;

// TODO split into separate responsibilities - account & the of()
public enum Account {
  ALPHA(
      "aa0d0e05224a38ab153f905b84707c1c6ee8ba4e",
      "9b121f26641894cc0195dd14efd8b2a801556b4835fa2bdb11a8d0372c3bea28"),

  BETA(
      "6af824121a351296311edc611f8c5ed0186b1e9b",
      "57ae2fde66db7dafd354dc37f6c2ec11ffeba6af94dd7e723276a049a60e0232"),

  GAMMA(
      "fe3b557e8fb62b89f4916b721be55ceb828dbd73",
      "8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63"),
  DELTA(
      "e6bab007421a837500df3124e53557bf85b6b319",
      "9eb388ff9aaea5dd4646bed06a684d3b33015678c49349dff48c6088c5838f09");
  private static final String DEFAULT_BALANCE = "0xad78ebc5ac6200000";

  private final GenesisAddress genesisAddres;
  private final Credentials credentials;

  Account(final String address, final String privateKey) {
    this.genesisAddres = new GenesisAddress(address);
    this.credentials = Credentials.create(privateKey);
  }

  public static Map<GenesisAddress, GenesisAccount> of(final Account... accounts) {
    final Map<GenesisAddress, GenesisAccount> mapped = new HashMap<>();

    for (final Account account : accounts) {
      mapped.put(account.genesisAddres, new GenesisAccount(DEFAULT_BALANCE));
    }

    return Collections.unmodifiableMap(mapped);
  }

  public Credentials credentials() {
    return credentials;
  }

  public Address address() {
    return Address.fromHexString(credentials.getAddress());
  }
}
