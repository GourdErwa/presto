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
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * catalog 请求信息封装
 *
 * @author Li.Wei by 2022/11/7
 */
@ThriftStruct
@Immutable
public class CatalogEntity implements Serializable {
    /**
     * @see #checkPrefix(String, String)
     */
    private final String catalogName;
    private final String connectorName;
    /**
     * 存在密码配置时，默认隐藏
     *
     * @see #getProperties()
     */
    private final Map<String, String> properties;

    /**
     * @param catalogName   命名规则 space_catalogName , 不能出现 [.-] 等特殊字符，不能以数子开头。目的为了区分不同空间数据源发布统一查询管理
     * @param connectorName connectorName
     * @param properties    properties
     */
    @ThriftConstructor
    @JsonCreator
    public CatalogEntity(
            @JsonProperty("catalogName") String catalogName,
            @JsonProperty("connectorName") String connectorName,
            @JsonProperty("properties") Map<String, String> properties
    ) {
        this.catalogName = requireNonNull(catalogName, "catalogName is null");
        this.connectorName = requireNonNull(connectorName, "connectorName is null");
        this.properties = requireNonNull(properties, "properties is null");
    }

    /**
     * 判断 catalogName 是否是属于 spaceId
     *
     * @param catalogName       catalogName
     * @param catalogNamePrefix catalogNamePrefix
     * @return boolean
     */
    public static boolean checkPrefix(String catalogName, String catalogNamePrefix) {
        return catalogName != null && catalogName.startsWith(catalogNamePrefix);
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

    @ThriftField(3)
    @JsonProperty
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "CatalogEntity{" +
                "catalogName='" + catalogName + '\'' +
                ", connectorName='" + connectorName + '\'' +
                ", properties=" + properties +
                '}';
    }
}
