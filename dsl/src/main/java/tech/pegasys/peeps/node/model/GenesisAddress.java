/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.peeps.node.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonValue;

public class GenesisAddress {

  private static final Pattern REGEX = Pattern.compile("^[a-fA-F0-9]{40}$");

  private final String hex;

  public GenesisAddress(final String address) {
    checkNotNull(address);
    checkArgument(
        REGEX.matcher(address).matches(),
        "Address: %s, does not comply with expected regex for an Ethereum genesis address: %s.",
        address,
        REGEX);

    this.hex = address;
  }

  @JsonValue
  public String getAddress() {
    return hex;
  }

  @Override
  public String toString() {
    return "GenesisAddress [hex=" + hex + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((hex == null) ? 0 : hex.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GenesisAddress other = (GenesisAddress) obj;
    if (hex == null) {
      if (other.hex != null) return false;
    } else if (!hex.equals(other.hex)) return false;
    return true;
  }
}
