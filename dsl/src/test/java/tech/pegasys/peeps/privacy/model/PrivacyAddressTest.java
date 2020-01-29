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
package tech.pegasys.peeps.privacy.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PrivacyAddressTest {

  @Test
  public void missingIdMustException() {
    final Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new PrivacyAddreess(null);
            });

    assertThat(exception.getMessage()).isEqualTo("Address is mandatory");
  }

  @Test
  public void hashCodeMustMatchIdHashCode() {
    final String id = "A very unique address";

    final PrivacyAddreess address = new PrivacyAddreess(id);

    assertThat(address.hashCode()).isEqualTo(id.hashCode());
  }

  @Test
  public void selfReferenceEqualityMustSucced() {
    final PrivacyAddreess id = new PrivacyAddreess("I am a real address!");

    assertThat(id).isEqualTo(id);
  }

  @Test
  public void identicalIdEqualityMustSucced() {
    final String id = "A not so unique address";
    final PrivacyAddreess addressAlpha = new PrivacyAddreess(id);
    final PrivacyAddreess addressdBeta = new PrivacyAddreess(id);

    final boolean isEquals = addressAlpha.equals(addressdBeta);

    assertThat(isEquals).isTrue();
  }

  @Test
  public void noReferenceEqualityMustFail() {
    final PrivacyAddreess address = new PrivacyAddreess("I am a real address value!");

    final boolean isEquals = address.equals(null);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void differentTypeEqualityMustFail() {
    final PrivacyAddreess address = new PrivacyAddreess("I am a real address value!");
    final Object other = "Type of String";

    final boolean isEquals = address.equals(other);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void differentIdEqualityMustFail() {
    final PrivacyAddreess addressAlpha = new PrivacyAddreess("First address");
    final PrivacyAddreess addressBeta = new PrivacyAddreess("Second address");

    final boolean isEquals = addressAlpha.equals(addressBeta);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void hashCodeMustEqualIdHashCode() {
    final String id = "The one and only address!";
    final PrivacyAddreess address = new PrivacyAddreess(id);

    final int expected = id.hashCode();
    final int actual = address.hashCode();

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getMustReturnAddress() {
    final String id = "The address!";
    final PrivacyAddreess address = new PrivacyAddreess(id);

    final String actual = address.get();

    assertThat(actual).isEqualTo(id);
  }
}
