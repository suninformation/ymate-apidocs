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
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 描述一个请求/响应头信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/8 下午4:58
 * @version 1.0
 */
public class HeaderInfo implements Serializable {

    public static HeaderInfo create(String name) {
        return new HeaderInfo(name);
    }

    public static HeaderInfo create(ApiHeader header) {
        if (header != null && StringUtils.isNotBlank(header.name())) {
            HeaderInfo _headerInfo = new HeaderInfo(header.name()).setDescription(header.description());
            if (!Void.class.equals(header.type())) {
                _headerInfo.setType(header.type().getSimpleName());
            }
            return _headerInfo;
        }
        return null;
    }

    /**
     * 名称
     */
    private String name;

    /**
     * 数据类型
     */
    private String type;

    /**
     * 内容描述
     */
    private String description;

    public HeaderInfo(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public HeaderInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public HeaderInfo setDescription(String description) {
        this.description = description;
        return this;
    }
}
