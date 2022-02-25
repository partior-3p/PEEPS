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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.errorprone.annotations.FormatMethod;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.ThrowingRunnable;

public class Await {

  public static final int DEFAULT_TIMEOUT_IN_SECONDS = 30;

  @FormatMethod
  public static <T> Optional<T> awaitPresence(
      final Supplier<Optional<T>> operation,
      final String errorMessage,
      final Object... errorMessageParameters) {

    await(
        () -> {
          assertThat(operation.get()).isPresent();
        },
        errorMessage,
        errorMessageParameters);

    return operation.get();
  }

  @FormatMethod
  public static <T> T awaitData(
      final Supplier<T> operation,
      final String errorMessage,
      final Object... errorMessageParameters) {

    await(
        () -> {
          assertThat(operation.get()).isNotNull();
        },
        errorMessage,
        errorMessageParameters);

    return operation.get();
  }

  @FormatMethod
  public static void await(
      final ThrowingRunnable condition,
      final String errorMessage,
      final Object... errorMessageParameters) {
    try {
      await(DEFAULT_TIMEOUT_IN_SECONDS, condition);
    } catch (final ConditionTimeoutException e) {
      throw new AssertionError(String.format(errorMessage, errorMessageParameters));
    }
  }

  @FormatMethod
  public static void await(
      final ThrowingRunnable condition,
      final int timeout,
      final String errorMessage,
      final Object... errorMessageParameters) {
    try {
      await(timeout, condition);
    } catch (final ConditionTimeoutException e) {
      throw new AssertionError(String.format(errorMessage, errorMessageParameters));
    }
  }

  private static void await(final int timeout, final ThrowingRunnable condition) {
    Awaitility.await()
        .ignoreExceptions()
        .atMost(Duration.ofSeconds(timeout))
        .untilAsserted(condition);
  }
}
