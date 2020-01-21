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

import static com.google.common.base.Preconditions.checkArgument;
import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.Transaction;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class OrionKey {

  private final String key;

  @JsonCreator
  public OrionKey(final String key) {
    checkArgument(key != null, "A null key is not allowed");
    checkArgument(!key.isBlank(), "An empty key is not allowed");
    checkArgument(
        !key.startsWith("0x"), "Key cannot be a hex value, it needs to be a Base64 encoded String");

    this.key = key;
  }

  @Override
  @JsonValue
  public String toString() {
    return key;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    return other instanceof Hash && key.equals(((OrionKey) other).key);
  }

  public static OrionKey from(final Transaction tx) {
    return new OrionKey(
        new String(Base64.encodeBase64(decodeHex(tx.getInput())), StandardCharsets.UTF_8));
  }

  private static byte[] decodeHex(final String hex) {
    try {
      return Hex.decodeHex(removeAnyHexPrefix(hex).toCharArray());
    } catch (final DecoderException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
