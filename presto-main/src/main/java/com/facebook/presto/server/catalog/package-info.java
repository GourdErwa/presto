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
/**
 * 二次开发支持 HTTP 动态更新连接器。
 * <br>
 * <p>
 * 修改内容说明：
 * <li>presto-main/src/main/java/com/facebook/presto/server/catalog 该包所有内容为新增内容，主要提供 HTTP 接口访问</li>
 * <li>presto-client/src/main/java/com/facebook/presto/client/catalog 该包所有内容为新增内容，主要提供 HTTP 接口访问请求返回对象封装</li>
 * <li>presto-main/src/main/java/com/facebook/presto/server/CoordinatorModule.java 注入 CatalogResource 类</li>
 * <li>presto-main/src/main/java/com/facebook/presto/metadata/Catalog.java 新增字段 config、connectorName、createTime
 * 方便获取元数据信息，同时在 ConnectorManager#addCatalogConnector 方法中进行创建对象时设值</li>
 *
 * <br>
 * <p>
 * 所有二次开发内容为了实现 {@link com.facebook.presto.server.catalog.CatalogResource} 内接口方法，具体逻辑参考实现方法。
 *
 * @author Li.Wei by 2022/11/15
 */
package com.facebook.presto.server.catalog;