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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Resources {

  private static final Logger LOG = LogManager.getLogger();

  public static String getCanonicalPath(final String relativePath) {
    final URL resource = Thread.currentThread().getContextClassLoader().getResource(relativePath);

    if (resource == null) {
      final String message = String.format("File '%s' not found on classpath", relativePath);
      LOG.error(message);
      throw new IllegalArgumentException(message);
    }

    try {
      return URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8.name());
    } catch (final UnsupportedEncodingException ex) {
      LOG.error("Unsupported encoding used to decode {}, filepath.", resource);
      throw new RuntimeException("Illegal string decoding");
    }
  }
}
