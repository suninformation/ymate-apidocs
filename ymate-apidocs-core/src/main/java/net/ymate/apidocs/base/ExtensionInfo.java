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

import net.ymate.apidocs.annotation.ApiExtension;
import net.ymate.apidocs.annotation.ApiExtensions;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述一个扩展信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/08 01:29
 */
public class ExtensionInfo implements IMarkdown {

    public static ExtensionInfo create(PropertyInfo... properties) {
        return new ExtensionInfo(properties);
    }

    public static List<ExtensionInfo> create(ApiExtensions apiExtensions) {
        if (apiExtensions != null) {
            return Arrays.stream(apiExtensions.value()).map(ExtensionInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static ExtensionInfo create(ApiExtension extension) {
        if (extension != null) {
            if (StringUtils.isNotBlank(extension.name()) || StringUtils.isNotBlank(extension.description()) || ArrayUtils.isNotEmpty(extension.value())) {
                ExtensionInfo extensionInfo = new ExtensionInfo()
                        .setName(extension.name())
                        .setDescription(extension.description());
                Arrays.stream(extension.value()).map(PropertyInfo::create).forEachOrdered(extensionInfo::addProperty);
                return extensionInfo;
            }
        }
        return null;
    }

    public static String toMarkdown(List<ExtensionInfo> extensions) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!extensions.isEmpty()) {
            extensions.forEach(markdownBuilder::append);
        }
        return markdownBuilder.toMarkdown();
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
    private final List<PropertyInfo> properties = new ArrayList<>();

    public ExtensionInfo(PropertyInfo... properties) {
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

    public ExtensionInfo addProperties(List<PropertyInfo> properties) {
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
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (StringUtils.isNotBlank(name)) {
            markdownBuilder.text(name, Text.Style.BOLD).p();
        }
        if (StringUtils.isNotBlank(description)) {
            markdownBuilder.append(description).p();
        }
        if (!properties.isEmpty()) {
            markdownBuilder.append(PropertyInfo.toMarkdown(properties));
        }
        return markdownBuilder.toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
