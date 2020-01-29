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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class NetworkStateTest {

  @Test
  public void beginningStateMustBeUnititialized() {
    final NetworkState state = new NetworkState();

    assertThat(state.isUninitialized()).isTrue();
    assertThat(state.isStarted()).isFalse();
    assertThat(state.isStopped()).isFalse();
    assertThat(state.isClosed()).isFalse();
  }

  @Test
  public void transitionFromUninitializedToStartedMustSucceed() {
    final NetworkState state = new NetworkState();
    assertThat(state.isStarted()).isFalse();

    state.start();

    assertThat(state.isUninitialized()).isFalse();
    assertThat(state.isStarted()).isTrue();
    assertThat(state.isStopped()).isFalse();
    assertThat(state.isClosed()).isFalse();
  }

  @Test
  public void transitionFromStartedToStoppedMustSucceed() {
    final NetworkState state = new NetworkState();
    assertThat(state.isStarted()).isFalse();

    state.start();
    state.stop();

    assertThat(state.isUninitialized()).isFalse();
    assertThat(state.isStarted()).isFalse();
    assertThat(state.isStopped()).isTrue();
    assertThat(state.isClosed()).isFalse();
  }

  @Test
  public void transitionFromStoppedToStartedMustSucceed() {
    final NetworkState state = new NetworkState();
    assertThat(state.isStarted()).isFalse();

    state.start();
    state.stop();
    state.start();

    assertThat(state.isUninitialized()).isFalse();
    assertThat(state.isStarted()).isTrue();
    assertThat(state.isStopped()).isFalse();
    assertThat(state.isClosed()).isFalse();
  }

  @Test
  public void transitionFromStoppedToCloseddMustSucceed() {
    final NetworkState state = new NetworkState();
    assertThat(state.isClosed()).isFalse();

    state.start();
    state.stop();
    state.close();

    assertThat(state.isUninitialized()).isFalse();
    assertThat(state.isStarted()).isFalse();
    assertThat(state.isStopped()).isFalse();
    assertThat(state.isClosed()).isTrue();
  }

  @Test
  public void transitionFromUninitializedToStoppedMustException() {
    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              new NetworkState().stop();
            });

    assertThat(exception.getMessage())
        .isEqualTo(
            "Only a Network in a started state can be stopped. Current state: UNINITIALIZED");
  }

  @Test
  public void transitionFromUninitializedToClosedMustException() {
    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              new NetworkState().close();
            });

    assertThat(exception.getMessage())
        .isEqualTo("Only a Network in a stopped state can be closed. Current state: UNINITIALIZED");
  }

  @Test
  public void transitionFromStartedToStartedMustException() {
    final NetworkState state = new NetworkState();
    state.start();

    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              state.start();
            });

    assertThat(exception.getMessage())
        .isEqualTo(
            "Only a Network in an uninitialized or stopped state can be started. Current state: STARTED");
  }

  @Test
  public void transitionFromStartedToClosedMustException() {
    final NetworkState state = new NetworkState();
    state.start();

    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              state.close();
            });

    assertThat(exception.getMessage())
        .isEqualTo("Only a Network in a stopped state can be closed. Current state: STARTED");
  }

  @Test
  public void transitionFromStoppedToStoppedMustException() {
    final NetworkState state = new NetworkState();
    state.start();
    state.stop();

    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              state.stop();
            });

    assertThat(exception.getMessage())
        .isEqualTo("Only a Network in a started state can be stopped. Current state: STOPPED");
  }

  @Test
  public void transitionFromClosedToStoppedMustException() {
    final NetworkState state = new NetworkState();
    state.start();
    state.stop();
    state.close();

    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              state.stop();
            });

    assertThat(exception.getMessage())
        .isEqualTo("Only a Network in a started state can be stopped. Current state: CLOSED");
  }

  @Test
  public void transitionFromClosedToStartdMustException() {
    final NetworkState state = new NetworkState();
    state.start();
    state.stop();
    state.close();

    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              state.start();
            });

    assertThat(exception.getMessage())
        .isEqualTo(
            "Only a Network in an uninitialized or stopped state can be started. Current state: CLOSED");
  }

  @Test
  public void transitionFromClosedToClosedMustException() {
    final NetworkState state = new NetworkState();
    state.start();
    state.stop();
    state.close();

    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              state.close();
            });

    assertThat(exception.getMessage())
        .isEqualTo("Only a Network in a stopped state can be closed. Current state: CLOSED");
  }
}
