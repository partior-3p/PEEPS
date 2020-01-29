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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SubnetAddressesTest {

  private static final String ADDRESS_FORMAT = "172.20.0.%d";

  @Test
  public void firstAddressHostOctetMustBeTwo() {
    final SubnetAddresses addresses = new SubnetAddresses(ADDRESS_FORMAT);

    assertThat(addresses.getAddressAndIncrement()).isEqualTo(new SubnetAddress("172.20.0.2"));
  }

  @Test
  public void mustLoopAroundOn256() {
    final SubnetAddresses addresses = new SubnetAddresses(ADDRESS_FORMAT);

    for (int i = 2; i < 256; i++) {
      assertThat(addresses.getAddressAndIncrement())
          .isEqualTo(new SubnetAddress(String.format(ADDRESS_FORMAT, i)));
    }

    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              addresses.getAddressAndIncrement();
            });

    assertThat(exception.getMessage()).isEqualTo("Subnet addresses have been exhaused");
  }

  @Test
  public void missingAddressFormatMustException() {
    final Exception exception =
        assertThrows(
            NullPointerException.class,
            () -> {
              new SubnetAddresses(null);
            });

    assertThat(exception.getMessage()).isEqualTo("An address format is required");
  }

  @Test
  public void incorrectAddressFormatMustException() {
    final Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new SubnetAddresses("172.20.0.0");
            });

    assertThat(exception.getMessage())
        .isEqualTo(
            "Given address format: '172.20.0.0' does not conform to IPv4 pattern: '^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.%d$'");
  }
}
