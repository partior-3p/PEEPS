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
package tech.pegasys.peeps.network;

import static com.google.common.base.Preconditions.checkState;

public class NetworkState {

  /**
   * Transition rules: UNINITIALIZED -> STARTED STARTED -> STOPPED STOPPED -> STARTED STOPPED ->
   * CLOSED
   */
  enum State {
    UNINITIALIZED,
    STARTED,
    STOPPED,
    CLOSED
  }

  private State current = State.UNINITIALIZED;

  public void start() {
    checkState(
        current == State.UNINITIALIZED || current == State.STOPPED,
        "Only a Network in an uninitialized or stopped state can be started. Current state: %s",
        current);

    current = State.STARTED;
  }

  public void stop() {
    checkState(
        current == State.STARTED,
        "Only a Network in a started state can be stopped. Current state: %s",
        current);

    current = State.STOPPED;
  }

  public void close() {
    checkState(
        current == State.STOPPED,
        "Only a Network in a stopped state can be closed. Current state: %s",
        current);

    current = State.CLOSED;
  }

  public boolean isUninitialized() {
    return current == State.UNINITIALIZED;
  }

  public boolean isStarted() {
    return current == State.STARTED;
  }

  public boolean isStopped() {
    return current == State.STOPPED;
  }

  public boolean isClosed() {
    return current == State.CLOSED;
  }
}
