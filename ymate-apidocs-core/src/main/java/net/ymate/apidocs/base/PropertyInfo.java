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

import net.ymate.apidocs.annotation.ApiProperty;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Table;
import net.ymate.platform.commons.markdown.Text;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 描述一个自定义属性
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/08 14:54
 */
public class PropertyInfo implements IMarkdown {

    public static PropertyInfo create() {
        return new PropertyInfo();
    }

    public static PropertyInfo create(ApiProperty property) {
        if (property != null) {
            return new PropertyInfo()
                    .setName(property.name())
                    .setValue(property.value())
                    .setDescription(property.description());
        }
        return null;
    }

    public static PropertyInfo create(ApiProperty property, Field field) {
        return create(property, null, field);
    }

    public static PropertyInfo create(ApiProperty property, String prefix, Field field) {
        if (property != null) {
            if (StringUtils.isNotBlank(prefix)) {
                prefix += ".";
            }
            return new PropertyInfo()
                    .setName(StringUtils.trimToEmpty(prefix) + StringUtils.defaultIfBlank(property.name(), field.getName()))
                    .setValue(StringUtils.defaultIfBlank(property.value(), field.getType().getTypeName()))
                    .setDescription(property.description());
        }
        return null;
    }

    public static String toMarkdown(List<PropertyInfo> properties) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!properties.isEmpty()) {
            properties.forEach(markdownBuilder::append);
        }
        return markdownBuilder.toMarkdown();
    }

    public static String toMarkdownTable(List<PropertyInfo> properties) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!properties.isEmpty()) {
            Table table = Table.create()
                    .addHeader("Name", Table.Align.LEFT)
                    .addHeader("Type", Table.Align.LEFT)
                    .addHeader("Description", Table.Align.LEFT);
            for (PropertyInfo property : properties) {
                table.addRow().addColumn(property.getName()).addColumn(property.getValue()).addColumn(property.getDescription());
            }
            markdownBuilder.append(table);
        }
        return markdownBuilder.toMarkdown();
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
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (StringUtils.isNotBlank(name)) {
            markdownBuilder.text(name, Text.Style.BOLD).space();
        }
        if (StringUtils.isNotBlank(description)) {
            markdownBuilder.text(description, Text.Style.ITALIC).br();
        }
        if (StringUtils.isNotBlank(value)) {
            markdownBuilder.append(value).br();
        }
        if (markdownBuilder.length() > 0) {
            return MarkdownBuilder.create().quote(markdownBuilder).br().toMarkdown();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
