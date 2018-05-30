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

import net.ymate.apidocs.annotation.ApiExample;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 描述一段接口或代码示例
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/8 下午3:57
 * @version 1.0
 */
public class ExampleInfo implements Serializable {

    public static ExampleInfo create(String content) {
        return new ExampleInfo(content);
    }

    public static ExampleInfo create(ApiExample example) {
        if (example != null && StringUtils.isNotBlank(example.value())) {
            return new ExampleInfo(example.value()).setType(example.type());
        }
        return null;
    }

    /**
     * 类型，如：json, xml或java等
     */
    private String type;

    /**
     * 示例内容
     */
    private String content;

    public ExampleInfo(String content) {
        if (StringUtils.isBlank(content)) {
            throw new NullArgumentException("content");
        }
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public ExampleInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getContent() {
        return content;
    }
}
