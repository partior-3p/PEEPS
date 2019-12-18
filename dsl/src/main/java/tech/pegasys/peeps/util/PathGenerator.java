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

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

public class PathGenerator {

  private static final AtomicLong FILE_UNIQUIFIER = new AtomicLong();
  private static final String UNIQUE_FILENAME_FORMAT = "unique-file-%s.tmp";

  private final Path directory;

  public PathGenerator(final Path directory) {
    this.directory = directory;
  }

  public Path uniqueFile() {
    return directory.resolve(
        String.format(UNIQUE_FILENAME_FORMAT, FILE_UNIQUIFIER.getAndIncrement()));
  }
}
