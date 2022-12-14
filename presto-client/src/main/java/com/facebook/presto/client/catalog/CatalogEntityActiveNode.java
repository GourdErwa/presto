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
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * catalog 对应激活节点信息
 *
 * @author Li.Wei by 2022/11/7
 */
@ThriftStruct
@Immutable
public class CatalogEntityActiveNode implements Serializable {

    private final CatalogEntityVo catalogEntityVo;
    private final List<InternalNodeVo> internalNodes;

    @ThriftConstructor
    @JsonCreator
    public CatalogEntityActiveNode(
            @JsonProperty("catalogEntity") CatalogEntityVo catalogEntityVo,
            @JsonProperty("internalNodes") List<InternalNodeVo> internalNodes
    ) {
        this.catalogEntityVo = requireNonNull(catalogEntityVo, "catalogEntityVo is null");
        this.internalNodes = requireNonNull(internalNodes, "internalNodes is null");
    }

    @ThriftField(1)
    @JsonProperty
    public CatalogEntityVo getCatalogEntityVo() {
        return catalogEntityVo;
    }

    @ThriftField(2)
    @JsonProperty
    public List<InternalNodeVo> getInternalNodes() {
        return internalNodes;
    }
}
