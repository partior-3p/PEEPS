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
package tech.pegasys.peeps.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.common.base.MoreObjects;
import com.google.common.io.ByteSource;

public class ClasspathResources {

  public static String read(final String path) {
    final InputStream input = classpathLoader().getResourceAsStream(path);
    checkNotNull(input, String.format("'%s' is not found", path));

    final ByteSource byteSource =
        new ByteSource() {
          @Override
          public InputStream openStream() {
            return input;
          }
        };

    try {
      return byteSource.asCharSource(StandardCharsets.UTF_8).read();
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + path, e);
    }
  }

  private static ClassLoader classpathLoader() {
    return MoreObjects.firstNonNull(
        Thread.currentThread().getContextClassLoader(), ClasspathResources.class.getClassLoader());
  }
}
