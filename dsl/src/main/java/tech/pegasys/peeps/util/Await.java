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

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.ThrowingRunnable;

public class Await {

  private static final int DEFAULT_TIMEOUT_IN_SECONDS = 20;

  public static void await(final ThrowingRunnable condition, final String conditionTimeoutMessage) {
    try {
      await(DEFAULT_TIMEOUT_IN_SECONDS, condition);
    } catch (final ConditionTimeoutException e) {
      throw new AssertionError(conditionTimeoutMessage);
    }
  }

  private static void await(final int timeout, final ThrowingRunnable condition) {
    Awaitility.await()
        .ignoreExceptions()
        .atMost(timeout, TimeUnit.SECONDS)
        .untilAsserted(condition);
  }
}
