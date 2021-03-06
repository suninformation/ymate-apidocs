/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.apidocs.core.base;

import net.ymate.apidocs.annotation.ApiHeader;
import net.ymate.apidocs.annotation.ApiResponse;
import net.ymate.apidocs.core.IMarkdown;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述一个接口方法响应信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/9 下午3:29
 * @version 1.0
 */
public class ResponseInfo implements IMarkdown, Serializable {

    public static ResponseInfo create(String code, String message) {
        return new ResponseInfo(code, message);
    }

    public static ResponseInfo create(ApiResponse response) {
        if (response != null) {
            ResponseInfo responseInfo = new ResponseInfo(response.code(), response.message()).setHttpStatus(response.httpStatus());
            for (ApiHeader header : response.headers()) {
                responseInfo.addHeader(HeaderInfo.create(header));
            }
            return responseInfo;
        }
        return null;
    }

    /**
     * HTTP响应状态值
     */
    private int httpStatus;

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
    private List<HeaderInfo> headers;

    public ResponseInfo(String code, String message) {
        if (StringUtils.isBlank(code)) {
            throw new NullArgumentException("code");
        }
        if (StringUtils.isBlank(message)) {
            throw new NullArgumentException("message");
        }
        this.code = code;
        this.message = message;
        this.headers = new ArrayList<HeaderInfo>();
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

    public ResponseInfo setHeaders(List<HeaderInfo> headers) {
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
    public String toMarkdown() {
        return "|`" + code + "`|" + StringUtils.replaceEach(message, new String[]{"\r\n", "\r", "\n", "\t"}, new String[]{"[\\r][\\n]", "[\\r]", "[\\n]", "[\\t]"}) + "|";
    }
}
