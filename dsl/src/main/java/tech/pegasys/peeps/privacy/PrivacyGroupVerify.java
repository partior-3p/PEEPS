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
package tech.pegasys.peeps.privacy;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.privacy.model.TransactionManagerKey;

import java.util.Set;

public class PrivacyGroupVerify {

  private final Set<PrivateTransactionManager> group;

  public PrivacyGroupVerify(final Set<PrivateTransactionManager> group) {
    // TODO check args
    this.group = group;
  }

  public void consensusOnPrivacyPayload(final Transaction pmt) {
    // TODO check key != null

    final TransactionManagerKey key = TransactionManagerKey.from(pmt);
    // TODO check key != null

    final String firstPayload = group.iterator().next().getPayload(key);

    for (final PrivateTransactionManager privacyManager : group) {
      final String payload = privacyManager.getPayload(key);
      assertThat(payload).isNotNull();
      assertThat(payload).isEqualTo(firstPayload);
    }
  }
}
