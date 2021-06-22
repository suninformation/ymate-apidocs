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

import net.ymate.apidocs.AbstractMarkdown;
import net.ymate.apidocs.IDocs;
import net.ymate.apidocs.annotation.ApiProperty;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Table;
import net.ymate.platform.commons.markdown.Text;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.base.EntityMeta;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 描述一个自定义属性
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/08 14:54
 */
public class PropertyInfo implements IMarkdown {

    public static String parseText(String text) {
        if (StringUtils.contains(text, '{')) {
            MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
            markdownBuilder.append(StringUtils.substringBefore(text, "{"));
            String content = StringUtils.substringBetween(text, "{", "}");
            if (StringUtils.containsWhitespace(content)) {
                markdownBuilder.br().append(StringUtils.join(parseText(StringUtils.split(content)), StringUtils.LF));
            } else if (StringUtils.contains(content, '|')) {
                Arrays.stream(StringUtils.split(content, '|')).forEach(part -> markdownBuilder.code(part).space());
            }
            return markdownBuilder.toMarkdown();
        }
        return text;
    }

    private static List<String> parseText(String[] textArr) {
        List<String> returnValue = new ArrayList<>();
        for (String text : textArr) {
            if (StringUtils.countMatches(text, '-') == 1) {
                String[] contentArr = StringUtils.split(text, '-');
                if (ArrayUtils.isNotEmpty(contentArr) && contentArr.length == 2) {
                    returnValue.add(String.format("`%s` %s", contentArr[0], contentArr[1]));
                } else {
                    returnValue.add(text);
                }
            }
        }
        return returnValue;
    }

    public static PropertyInfo create() {
        return new PropertyInfo();
    }

    public static List<PropertyInfo> create(String prefix, Class<?> targetClass, boolean snakeCase) {
        List<PropertyInfo> properties = new ArrayList<>();
        ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapper(targetClass);
        for (Field field : beanWrapper.getFields()) {
            ApiProperty apiProperty = field.getAnnotation(ApiProperty.class);
            if (apiProperty != null) {
                if (apiProperty.model()) {
                    if (StringUtils.isNotBlank(prefix)) {
                        prefix += ".";
                    }
                    String propName = StringUtils.defaultIfBlank(apiProperty.name(), field.getName());
                    propName = snakeCase ? EntityMeta.fieldNameToPropertyName(propName, 0) : propName;
                    List<PropertyInfo> props = create(StringUtils.trimToEmpty(prefix) + propName, Void.class.equals(apiProperty.modelClass()) ? field.getType() : apiProperty.modelClass(), snakeCase);
                    if (!props.isEmpty()) {
                        properties.addAll(props);
                    }
                } else {
                    properties.add(PropertyInfo.create(apiProperty, prefix, field, snakeCase));
                }
            }
        }
        return properties;
    }

    public static PropertyInfo create(ApiProperty property, boolean snakeCase) {
        if (property != null) {
            return new PropertyInfo()
                    .setName(snakeCase ? EntityMeta.fieldNameToPropertyName(property.name(), 0) : property.name())
                    .setValue(property.value())
                    .setDemoValue(property.demoValue())
                    .setDescription(property.description());
        }
        return null;
    }

    public static PropertyInfo create(ApiProperty property, Field field, boolean snakeCase) {
        return create(property, null, field, snakeCase);
    }

    public static PropertyInfo create(ApiProperty property, String prefix, Field field, boolean snakeCase) {
        if (property != null) {
            if (StringUtils.isNotBlank(prefix)) {
                prefix += ".";
            }
            String propName = StringUtils.defaultIfBlank(property.name(), field.getName());
            propName = snakeCase ? EntityMeta.fieldNameToPropertyName(propName, 0) : propName;
            return new PropertyInfo()
                    .setName(StringUtils.trimToEmpty(prefix) + propName)
                    .setValue(Void.class.equals(property.valueClass()) ? StringUtils.defaultIfBlank(property.value(), field.getType().getSimpleName()) : property.valueClass().getSimpleName())
                    .setDemoValue(property.demoValue())
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

    public static String toMarkdownTable(IDocs owner, List<PropertyInfo> properties) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!properties.isEmpty()) {
            Table table = Table.create()
                    .addHeader(AbstractMarkdown.i18nText(owner, "header.name", "Name"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "header.type", "Type"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "header.description", "Description"), Table.Align.LEFT);
            for (PropertyInfo property : properties) {
                table.addRow().addColumn(property.getName()).addColumn(property.getValue()).addColumn(parseText(property.getDescription()));
            }
            markdownBuilder.append(table);
        }
        return markdownBuilder.toMarkdown();
    }

    private String name;

    private String value;

    private String demoValue;

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

    public String getDemoValue() {
        return demoValue;
    }

    public PropertyInfo setDemoValue(String demoValue) {
        this.demoValue = demoValue;
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
        if (StringUtils.isNotBlank(value)) {
            markdownBuilder.text(value, Text.Style.ITALIC).br();
        }
        if (StringUtils.isNotBlank(description)) {
            markdownBuilder.append(description).br();
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
