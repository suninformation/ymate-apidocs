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

import net.ymate.apidocs.annotation.ApiExtensionProperty;
import net.ymate.apidocs.core.IMarkdown;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 描述一个自定义属性
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/8 下午2:54
 * @version 1.0
 */
public class PropertyInfo implements IMarkdown, Serializable {

    public static PropertyInfo create() {
        return new PropertyInfo();
    }

    public static PropertyInfo create(ApiExtensionProperty property) {
        if (property != null) {
            return new PropertyInfo()
                    .setName(property.name())
                    .setValue(property.value())
                    .setDescription(property.description());
        }
        return null;
    }

    private String name;

    private String value;

    private String description;

    public String getName() {
        return name;
    }

    public PropertyInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public PropertyInfo setValue(String value) {
        this.value = value;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PropertyInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        if (StringUtils.isNotBlank(name)) {
            md.append("> **").append(name).append("**");
            if (StringUtils.isNotBlank(description)) {
                md.append(" _").append(description).append("_\n");
            } else {
                md.append("\n");
            }
        }
        if (StringUtils.isNotBlank(value)) {
            if (md.length() > 0) {
                md.append(">\n");
            }
            md.append(">").append(value).append("\n");
        }
        return md.toString();
    }
}
