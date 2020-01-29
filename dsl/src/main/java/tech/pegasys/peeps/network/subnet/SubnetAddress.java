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
package tech.pegasys.peeps.network.subnet;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Pattern;

public class SubnetAddress {

  private static final Pattern VALID_IP4_ADDRESS =
      Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

  private final String address;

  public SubnetAddress(final String address) {
    checkArgument(address != null, "Address is mandatory");
    checkArgument(
        VALID_IP4_ADDRESS.matcher(address).matches(), "Address must be in an IP4 Address format");

    this.address = address;
  }

  public String get() {
    return address;
  }

  @Override
  public int hashCode() {
    return address.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    return other instanceof SubnetAddress && address.equals(((SubnetAddress) other).address);
  }
}
