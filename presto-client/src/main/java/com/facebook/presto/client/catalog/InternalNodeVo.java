/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.client.catalog;

import com.facebook.drift.annotations.ThriftConstructor;
import com.facebook.drift.annotations.ThriftField;
import com.facebook.drift.annotations.ThriftStruct;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * 内部节点信息
 *
 * @author Li.Wei by 2022/11/8
 */
@ThriftStruct
@Immutable
public class InternalNodeVo implements Serializable {
    private final String nodeIdentifier;
    private final String internalUri;
    private final String nodeVersion;
    private final boolean coordinator;

    @ThriftConstructor
    @JsonCreator
    public InternalNodeVo(@JsonProperty("nodeIdentifier") String nodeIdentifier,
                          @JsonProperty("internalUri") String internalUri,
                          @JsonProperty("nodeVersion") String nodeVersion,
                          @JsonProperty("coordinator") boolean coordinator) {
        this.nodeIdentifier = nodeIdentifier;
        this.internalUri = internalUri;
        this.nodeVersion = nodeVersion;
        this.coordinator = coordinator;
    }

    @ThriftField(1)
    @JsonProperty
    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    @ThriftField(2)
    @JsonProperty
    public String getInternalUri() {
        return internalUri;
    }

    @ThriftField(3)
    @JsonProperty
    public String getNodeVersion() {
        return nodeVersion;
    }

    @ThriftField(4)
    @JsonProperty
    public boolean isCoordinator() {
        return coordinator;
    }
}
