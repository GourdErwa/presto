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
 * 二次开发返回信息封装，方便请求方反序列化，仅限 catalog api 交互使用。
 *
 * @author Li.Wei by 2022/11/19
 */
@ThriftStruct
@Immutable
public class InternalResult<T> implements Serializable {
    public static final Integer SUCCESS = 200;
    // 200 表示成功
    private final Integer code;
    private final String msg;
    private final T data;

    @ThriftConstructor
    @JsonCreator
    public InternalResult(
            @JsonProperty("code") Integer code,
            @JsonProperty("msg") String msg,
            @JsonProperty("data") T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public InternalResult(String msg, T data) {
        this(SUCCESS, msg, data);
    }

    public InternalResult(T data) {
        this(SUCCESS, null, data);
    }

    @ThriftField(1)
    @JsonProperty
    public Integer getCode() {
        return code;
    }

    @ThriftField(2)
    @JsonProperty
    public String getMsg() {
        return msg;
    }

    @ThriftField(3)
    @JsonProperty
    public T getData() {
        return data;
    }
}
