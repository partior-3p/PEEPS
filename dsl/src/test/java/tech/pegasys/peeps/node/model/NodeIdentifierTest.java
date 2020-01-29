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
package tech.pegasys.peeps.node.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class NodeIdentifierTest {

  @Test
  public void missingIdMustException() {
    final Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new NodeIdentifier(null);
            });

    assertThat(exception.getMessage()).isEqualTo("Identifier is mandatory");
  }

  @Test
  public void hashCodeMustMatchIdHashCode() {
    final String id = "A very unique node identifier";

    final NodeIdentifier nodeId = new NodeIdentifier(id);

    assertThat(nodeId.hashCode()).isEqualTo(id.hashCode());
  }

  @Test
  public void selfReferenceEqualityMustSucced() {
    final NodeIdentifier nodeId = new NodeIdentifier("I am a real identity value!");

    assertThat(nodeId).isEqualTo(nodeId);
  }

  @Test
  public void identicalIdEqualityMustSucced() {
    final String id = "A not so unique identifier";
    final NodeIdentifier nodeIdAlpha = new NodeIdentifier(id);
    final NodeIdentifier nodeIdBeta = new NodeIdentifier(id);

    final boolean isEquals = nodeIdAlpha.equals(nodeIdBeta);

    assertThat(isEquals).isTrue();
  }

  @Test
  public void noReferenceEqualityMustFail() {
    final NodeIdentifier nodeId = new NodeIdentifier("I am a real identity value!");

    final boolean isEquals = nodeId.equals(null);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void differentTypeEqualityMustFail() {
    final NodeIdentifier nodeId = new NodeIdentifier("I am a real identity value!");
    final Object other = "Type of String";

    final boolean isEquals = nodeId.equals(other);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void differentIdEqualityMustFail() {
    final NodeIdentifier nodeIdAlpha = new NodeIdentifier("First identifier");
    final NodeIdentifier nodeIdBeta = new NodeIdentifier("Second identifier");

    final boolean isEquals = nodeIdAlpha.equals(nodeIdBeta);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void hashCodeMustEqualIdHashCode() {
    final String id = "The one and only idenitity!";
    final NodeIdentifier nodeId = new NodeIdentifier(id);

    final int expected = id.hashCode();
    final int actual = nodeId.hashCode();

    assertThat(actual).isEqualTo(expected);
  }
}
