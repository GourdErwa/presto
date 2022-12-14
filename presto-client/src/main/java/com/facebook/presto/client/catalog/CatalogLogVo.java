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
 * catalog log 信息封装
 *
 * @author Li.Wei by 2022/11/7
 */
@ThriftStruct
@Immutable
public class CatalogLogVo implements Serializable {

    private final String catalogName;
    private final String connectorName;
    private Boolean state = true;
    private String msg = "";

    @ThriftConstructor
    @JsonCreator
    public CatalogLogVo(@JsonProperty("catalogName") String catalogName,
                        @JsonProperty("connectorName") String connectorName) {
        this.catalogName = catalogName;
        this.connectorName = connectorName;
    }

    public static CatalogLogVo create(CatalogEntity entity) {
        return new CatalogLogVo(entity.getCatalogName(), entity.getConnectorName());
    }

    @ThriftField(1)
    @JsonProperty
    public String getCatalogName() {
        return catalogName;
    }

    @ThriftField(2)
    @JsonProperty
    public String getConnectorName() {
        return connectorName;
    }

    public CatalogLogVo setState(Boolean state) {
        this.state = state;
        return this;
    }

    public CatalogLogVo appendMsg(String msg) {
        this.msg += msg;
        return this;
    }

    @ThriftField(3)
    @JsonProperty
    public Boolean getState() {
        return state;
    }

    @ThriftField(4)
    @JsonProperty
    public String getMsg() {
        return msg;
    }
}
