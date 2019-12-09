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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

public class PeepsTemporaryDirectory implements Closeable {

  private static final Logger LOG = LogManager.getLogger();
  private static final Path PEEPS_WORKING_DIRECTORY_BASE =
      new File(System.getProperty("user.dir")).toPath().resolve("peeps-tmp");
  private static final AtomicLong DIRECTORY_UNIQUIFIER = new AtomicLong();
  private static final AtomicLong FILE_UNIQUIFIER = new AtomicLong();
  private static final String UNIQUE_FILENAME_FORMAT = "unique-file-%s.tmp";

  static {
    PEEPS_WORKING_DIRECTORY_BASE.toFile().deleteOnExit();
  }

  private final Path temp;

  public PeepsTemporaryDirectory() {
    temp =
        PEEPS_WORKING_DIRECTORY_BASE.resolve(
            String.valueOf(DIRECTORY_UNIQUIFIER.getAndIncrement()));

    if (doesNotExistCannotCreate(temp)) {
      throw new IllegalStateException("Cannot create subdirectory under the user.dir: " + temp);
    } else {
      LOG.info("Created PEEPS temporary working directory: {}", temp);
    }
  }

  private boolean doesNotExistCannotCreate(final Path workingDirectory) {
    final File directory = workingDirectory.toFile();
    return !directory.exists() && !directory.mkdirs();
  }

  public Path getUniqueFile() {
    return temp.resolve(String.format(UNIQUE_FILENAME_FORMAT, FILE_UNIQUIFIER.getAndIncrement()));
  }

  @Override
  public void close() {
    LOG.info("Removing PEEPS temporary directory: {}", temp);

    try {
      FileUtils.deleteDirectory(temp.toFile());
    } catch (final IOException e) {
      LOG.error("Cleanup of PEEPS temporary directory failed", e);
    }
  }
}
