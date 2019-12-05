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
package tech.pegasys.peeps.privacy.rpc.send;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SendRequest {

  private final String payload;
  private final String from;
  private final String[] to;

  public SendRequest(final String from, final String[] to, final String payload) {
    this.payload = Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    this.to = to;
    this.from = from;
  }

  @JsonGetter("payload")
  public String getPayload() {
    return payload;
  }

  @JsonGetter("from")
  public String getFrom() {
    return from;
  }

  @JsonGetter("to")
  public String[] getTo() {
    return to;
  }
}
