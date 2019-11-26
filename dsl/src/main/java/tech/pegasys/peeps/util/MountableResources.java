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

import static com.google.common.base.Preconditions.checkState;
import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MountableResources {

  // TODO move knowledge of the path to the MountableResources into here

  public static String getCanonicalPath(final String path) {
    final File resource = new File(path);
    checkState(resource.exists(), String.format("'%s' is not found", resource.getAbsolutePath()));

    try {
      return resource.getCanonicalPath();
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static String readHexDroppingAnyPrefix(final String path) {
    return removeAnyHexPrefix(readString(path));
  }

  private static String readString(final String path) {
    final File resource = new File(path);
    checkState(resource.exists(), String.format("'%s' is not found", resource.getAbsolutePath()));

    try {
      return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + path, e);
    }
  }
}
