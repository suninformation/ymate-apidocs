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

import net.ymate.apidocs.annotation.ApiExtension;
import net.ymate.apidocs.annotation.ApiExtensionProperty;
import net.ymate.apidocs.core.IMarkdown;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 描述一个扩展信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/8 上午1:29
 * @version 1.0
 */
public class ExtensionInfo implements IMarkdown, Serializable {

    public static ExtensionInfo create(PropertyInfo... properties) {
        return new ExtensionInfo(properties);
    }

    public static ExtensionInfo create(ApiExtension extension) {
        ExtensionInfo _extensionInfo = new ExtensionInfo().setName(extension.name()).setDescription(extension.description());
        for (ApiExtensionProperty _property : extension.properties()) {
            _extensionInfo.addProperty(PropertyInfo.create(_property));
        }
        return _extensionInfo;
    }

    /**
     * 扩展名称
     */
    private String name;

    /**
     * 扩展描述
     */
    private String description;

    /**
     * 自定义属性
     */
    private List<PropertyInfo> properties;

    public ExtensionInfo(PropertyInfo... properties) {
        this.properties = new ArrayList<PropertyInfo>();
        if (ArrayUtils.isNotEmpty(properties)) {
            this.properties.addAll(Arrays.asList(properties));
        }
    }

    public String getName() {
        return name;
    }

    public ExtensionInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ExtensionInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<PropertyInfo> getProperties() {
        return properties;
    }

    public ExtensionInfo setProperties(List<PropertyInfo> properties) {
        if (properties != null) {
            this.properties.addAll(properties);
        }
        return this;
    }

    public ExtensionInfo addProperty(PropertyInfo property) {
        if (property != null) {
            this.properties.add(property);
        }
        return this;
    }

    @Override
    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        if (StringUtils.isNotBlank(name)) {
            md.append(name).append("\n");
        }
        if (StringUtils.isNotBlank(description)) {
            if (md.length() > 0) {
                md.append("\n");
            }
            md.append(description).append("\n");
        }
        for (PropertyInfo property : properties) {
            md.append("\n").append(property.toMarkdown()).append("\n");
        }
        return md.toString();
    }
}
