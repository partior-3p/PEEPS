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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import tech.pegasys.peeps.network.subnet.Subnet;
import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.model.NodeIdentifier;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NetworkTest {

  @Mock private NodeIdentifier nodeId;
  @Mock private Besu node;
  @Mock private Subnet subnet;
  @TempDir Path configurationDirectory;

  private Network network;

  @BeforeEach
  public void setUp() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDown));
    network = new Network(configurationDirectory, subnet);

    lenient().when(node.identity()).thenReturn(nodeId);
  }

  @AfterEach
  public void tearDown() {
    network.close();
  }

  @Test
  public void missingConfigurationDirectoryMustException() {
    final Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new Network(null, subnet);
            });

    assertThat(exception.getMessage()).isEqualTo("Path to configuration directory is mandatory");
  }

  @Test
  public void startWhenAlreadyStartedMustException() {
    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              network.start();
              network.start();
            });

    assertThat(exception.getMessage()).isEqualTo("Cannot start an already started Network");
  }

  @Test
  public void stopWhenAlreadyStoppedMustException() {
    final Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              network.start();
              network.stop();
              network.stop();
            });

    assertThat(exception.getMessage()).isEqualTo("Cannot stop an already stopped Network");
  }

  @Test
  public void lifecycleMustAffectNode() {
    network.addNode(node);
    network.start();
    network.close();

    verify(node).identity();
    verify(node).awaitConnectivity(anyCollection());
    verify(node).start();
    verify(node).stop();
    verifyNoMoreInteractions(node);
  }

  @Test
  public void stopThenClosenMustStopNodeOnlyOnce() {
    network.addNode(node);
    network.start();
    network.stop();
    network.close();

    verify(node).identity();
    verify(node).awaitConnectivity(anyCollection());
    verify(node).start();
    verify(node).stop();
    verifyNoMoreInteractions(node);
  }
}
