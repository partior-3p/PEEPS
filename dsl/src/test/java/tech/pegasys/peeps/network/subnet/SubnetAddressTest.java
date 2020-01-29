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

public class SubnetAddressTest {

  @Test
  public void missingIdMustException() {
    final Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new SubnetAddress(null);
            });

    assertThat(exception.getMessage()).isEqualTo("Address is mandatory");
  }

  @Test
  public void alphaCharacterInAddressMustException() {
    final Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new SubnetAddress("1.1.1.a");
            });

    assertThat(exception.getMessage()).isEqualTo("Address must be in an IP4 Address format");
  }

  @Test
  public void fewerThanFourDatsInAddressMustException() {
    final Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new SubnetAddress("1.1.1");
            });

    assertThat(exception.getMessage()).isEqualTo("Address must be in an IP4 Address format");
  }

  @Test
  public void moreThanFourDatsInAddressMustException() {
    final Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new SubnetAddress("1.1.1.1.1");
            });

    assertThat(exception.getMessage()).isEqualTo("Address must be in an IP4 Address format");
  }

  @Test
  public void hashCodeMustMatchUnderlyingHashCode() {
    final String ip = "100.222.303.4";

    final SubnetAddress address = new SubnetAddress(ip);

    assertThat(address.hashCode()).isEqualTo(ip.hashCode());
  }

  @Test
  public void selfReferenceEqualityMustSucced() {
    final SubnetAddress address = new SubnetAddress("1.1.1.1");

    assertThat(address).isEqualTo(address);
  }

  @Test
  public void identicalIdEqualityMustSucced() {
    final String address = "101.0.0.0";
    final SubnetAddress addressAlpha = new SubnetAddress(address);
    final SubnetAddress addressBeta = new SubnetAddress(address);

    final boolean isEquals = addressAlpha.equals(addressBeta);

    assertThat(isEquals).isTrue();
  }

  @Test
  public void noReferenceEqualityMustFail() {
    final SubnetAddress address = new SubnetAddress("9.99.99.9");

    final boolean isEquals = address.equals(null);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void differentTypeEqualityMustFail() {
    final SubnetAddress nodeId = new SubnetAddress("222.222.222.222");
    final Object other = "Type of String";

    final boolean isEquals = nodeId.equals(other);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void differentIdEqualityMustFail() {
    final SubnetAddress addressAlpha = new SubnetAddress("4.4.4.4");
    final SubnetAddress addressdBeta = new SubnetAddress("5.5.5.5");

    final boolean isEquals = addressAlpha.equals(addressdBeta);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void hashCodeMustEqualIdHashCode() {
    final String ip = "100.200.200.9";
    final SubnetAddress address = new SubnetAddress(ip);

    final int expected = ip.hashCode();
    final int actual = address.hashCode();

    assertThat(actual).isEqualTo(expected);
  }
}
