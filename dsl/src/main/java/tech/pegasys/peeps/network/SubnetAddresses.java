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
package tech.pegasys.peeps.network;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class SubnetAddresses {

  private static final Pattern IPV4 =
      Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.%d$");

  private static final int HOST_MAXIMUM = 255;

  /** First address is reserved for the TestContainer routing container. */
  private static final int FIRST_AVAILABLE_HOST_ADDRESS = 2;

  private final String addressFormat;
  private AtomicInteger hostAddress;

  public SubnetAddresses(final String addressFormat) {
    checkNotNull(addressFormat, "An address format is required");
    checkArgument(
        IPV4.matcher(addressFormat).matches(),
        "Given address format: '%s' does not conform to IPv4 pattern: '%s'",
        addressFormat,
        IPV4.pattern());

    this.addressFormat = addressFormat;
    this.hostAddress = new AtomicInteger(FIRST_AVAILABLE_HOST_ADDRESS);
  }

  /** Retrieves the next available IP address and now considers it as unavailable. */
  public String getAddressAndIncrement() {
    if (hostAddress.get() > HOST_MAXIMUM) {
      throw new IllegalStateException("Subnet addresses have been exhaused");
    }

    return String.format(addressFormat, hostAddress.getAndIncrement());
  }
}
