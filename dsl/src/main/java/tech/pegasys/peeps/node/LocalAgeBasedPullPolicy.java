/*
 * Copyright 2022 ConsenSys AG.
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
package tech.pegasys.peeps.node;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.images.AbstractImagePullPolicy;
import org.testcontainers.images.ImageData;
import org.testcontainers.utility.DockerImageName;

class LocalAgeBasedPullPolicy extends AbstractImagePullPolicy {

  private final Duration maxAge;

  DockerClient dockerClient = DockerClientFactory.lazyClient();

  public LocalAgeBasedPullPolicy(final Duration maxAge) {
    this.maxAge = maxAge;
  }

  @Override
  protected boolean shouldPullCached(
      final DockerImageName imageName, final ImageData localImageData) {
    InspectImageResponse response = null;
    try {
      response = dockerClient.inspectImageCmd(imageName.asCanonicalNameString()).exec();
    } catch (NotFoundException e) {
      return shouldPullCached(localImageData);
    }

    if (response != null) {
      Duration localImageAge =
          Duration.between(ZonedDateTime.parse(response.getCreated()).toInstant(), Instant.now());
      return localImageAge.compareTo(maxAge) > 0;
    } else {
      return shouldPullCached(localImageData);
    }
  }

  private boolean shouldPullCached(final ImageData localImageData) {
    Duration imageAge = Duration.between(localImageData.getCreatedAt(), Instant.now());
    return imageAge.compareTo(maxAge) > 0;
  }
}
