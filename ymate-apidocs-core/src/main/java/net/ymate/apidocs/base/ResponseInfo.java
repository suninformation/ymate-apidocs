/*
 * Copyright 2007-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.apidocs.base;

import net.ymate.apidocs.annotation.ApiResponse;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Table;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 描述一个接口方法响应信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/09 15:29
 */
public class ResponseInfo implements IMarkdown {

    public static ResponseInfo create(String code, String message) {
        return new ResponseInfo(code, message);
    }

    public static ResponseInfo create(ApiResponse response) {
        if (response != null) {
            return new ResponseInfo(response.code(), response.message())
                    .setHttpStatus(response.httpStatus());
        }
        return null;
    }

    public static String toMarkdown(List<ResponseInfo> responses) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!responses.isEmpty()) {
            markdownBuilder.append(Table.create()
                    .addHeader("Code", Table.Align.LEFT)
                    .addHeader("Http status", Table.Align.LEFT)
                    .addHeader("Message", Table.Align.LEFT));
            responses.forEach(markdownBuilder::append);
        }
        return markdownBuilder.toMarkdown();
    }

    /**
     * HTTP响应状态值
     */
    private int httpStatus = 200;

    /**
     * 业务响应码
     */
    private String code;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 响应头信息集合
     */
    private final List<HeaderInfo> headers = new ArrayList<>();

    public ResponseInfo(String code, String message) {
        if (StringUtils.isBlank(code)) {
            throw new NullArgumentException("code");
        }
        if (StringUtils.isBlank(message)) {
            throw new NullArgumentException("message");
        }
        this.code = code;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public ResponseInfo setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<HeaderInfo> getHeaders() {
        return headers;
    }

    public ResponseInfo addHeaders(List<HeaderInfo> headers) {
        if (headers != null) {
            this.headers.addAll(headers);
        }
        return this;
    }

    public ResponseInfo addHeader(HeaderInfo header) {
        if (header != null) {
            this.headers.add(header);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return code.equals(((ResponseInfo) o).code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toMarkdown() {
        return Table.create().addRow().addColumn(code).addColumn(String.valueOf(httpStatus)).addColumn(message).build().toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
