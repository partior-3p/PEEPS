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

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class Hash {

  private final String hash;

  @JsonCreator
  public Hash(final String hash) {
    checkArgument(hash != null, "A null hash is not allowed");
    checkArgument(!hash.isBlank(), "An empty hash is not allowed");

    this.hash = hash;
  }

  @Override
  @JsonValue
  public String toString() {
    return hash;
  }

  @Override
  public int hashCode() {
    return hash.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    return other instanceof Hash && hash.equals(((Hash) other).hash);
  }
}
