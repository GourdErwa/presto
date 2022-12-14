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

import com.facebook.airlift.json.JsonCodec;
import com.facebook.presto.client.JsonResponse;
import com.google.common.reflect.TypeToken;
import okhttp3.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.List;

import static com.facebook.airlift.json.JsonCodec.jsonCodec;
import static java.util.Objects.requireNonNull;

/**
 * NORN 二次开发。
 * 在 JDBC 基础上提供 catalog client 管理 API，所有实现基于 main 模块下 CatalogResource 提供接口方法
 *
 * @author Li.Wei by 2022/11/19
 */
@ThreadSafe
public class CatalogResourceClientV1 {
    private static final JsonCodec<InternalResult<List<CatalogEntityActiveNode>>> CODEC_LIST_CATALOG_ENTITY_ACTIVE_NODE =
            jsonCodec(new TypeToken<InternalResult<List<CatalogEntityActiveNode>>>() {
            });
    private static final JsonCodec<InternalResult<CatalogEntityActiveNode>> CODEC_CATALOG_ENTITY_ACTIVE_NODE =
            jsonCodec(new TypeToken<InternalResult<CatalogEntityActiveNode>>() {
            });
    private static final JsonCodec<List<CatalogEntity>> CODEC_LIST_CATALOG_ENTITY =
            jsonCodec(new TypeToken<List<CatalogEntity>>() {
            });
    private static final JsonCodec<InternalResult<List<CatalogLogVo>>> CODEC_LIST_CATALOG_LOG_VO =
            jsonCodec(new TypeToken<InternalResult<List<CatalogLogVo>>>() {
            });
    private static final JsonCodec<InternalResult<String>> CODEC_STRING =
            jsonCodec(new TypeToken<InternalResult<String>>() {
            });


    private final OkHttpClient client;
    private final String httpUri;

    public CatalogResourceClientV1(OkHttpClient client, String httpUri) {
        this.client = requireNonNull(client, "client is null");
        this.httpUri = requireNonNull(httpUri, "httpUri is null");
    }

    public InternalResult<List<CatalogEntityActiveNode>> getAllTest() {
        final Request request = new Request.Builder()
                .url(httpUri + "/v1/catalog/test")
                .addHeader("content-type", "application/json")
                .get()
                .build();
        return JsonResponse.execute(CODEC_LIST_CATALOG_ENTITY_ACTIVE_NODE, client, request).getValue();
    }

    public InternalResult<List<CatalogEntityActiveNode>> getAll() {
        final Request request = new Request.Builder()
                .url(httpUri + "/v1/catalog")
                .get()
                .build();
        return JsonResponse.execute(CODEC_LIST_CATALOG_ENTITY_ACTIVE_NODE, client, request).getValue();
    }

    public InternalResult<CatalogEntityActiveNode> getInfo(String catalogName) {
        final Request request = new Request.Builder()
                .url(httpUri + "/v1/catalog/info/" + catalogName)
                .get()
                .build();
        return JsonResponse.execute(CODEC_CATALOG_ENTITY_ACTIVE_NODE, client, request).getValue();
    }

    //
    public InternalResult<List<CatalogEntityActiveNode>> getInfoByPrefix(String catalogNamePrefix) {
        final Request request = new Request.Builder()
                .url(httpUri + "/v1/catalog/info_like/" + catalogNamePrefix)
                .get()
                .build();
        return JsonResponse.execute(CODEC_LIST_CATALOG_ENTITY_ACTIVE_NODE, client, request).getValue();
    }

    //
    public InternalResult<String> delete(String catalogName) {
        final Request request = new Request.Builder()
                .url(httpUri + "/v1/catalog/" + catalogName)
                .delete()
                .build();
        return JsonResponse.execute(CODEC_STRING, client, request).getValue();
    }

    //
    public InternalResult<List<CatalogLogVo>> createCatalog(List<CatalogEntity> catalogEntity) {
        final Request request = new Request.Builder()
                .addHeader("content-type", "application/json")
                .url(httpUri + "/v1/catalog/add")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), CODEC_LIST_CATALOG_ENTITY.toJson(catalogEntity)))
                .build();
        return JsonResponse.execute(CODEC_LIST_CATALOG_LOG_VO, client, request).getValue();
    }
}
