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

import net.ymate.apidocs.annotation.ApiExample;
import net.ymate.apidocs.annotation.ApiExamples;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Text;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 描述一段接口或代码示例
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/08 15:57
 */
public class ExampleInfo implements IMarkdown {

    public static ExampleInfo create(String content) {
        return new ExampleInfo(content);
    }

    public static List<ExampleInfo> create(ApiExample[] apiExamples) {
        if (apiExamples != null) {
            return Arrays.stream(apiExamples).map(ExampleInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static List<ExampleInfo> create(ApiExamples apiExamples) {
        if (apiExamples != null) {
            return Arrays.stream(apiExamples.value()).map(ExampleInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static ExampleInfo create(ApiExample example) {
        if (example != null && StringUtils.isNotBlank(example.value())) {
            return new ExampleInfo(example.value())
                    .setName(example.name())
                    .setDescription(example.description())
                    .setType(example.type());
        }
        return null;
    }

    public static String toMarkdown(List<ExampleInfo> examples) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        examples.forEach(markdownBuilder::append);
        return markdownBuilder.toMarkdown();
    }

    /**
     * 示例名称
     */
    private String name;

    /**
     * 示例描述
     */
    private String description;

    /**
     * 类型，如：json, xml或java等
     */
    private String type;

    /**
     * 示例内容
     */
    private final String content;

    public ExampleInfo(String content) {
        if (StringUtils.isBlank(content)) {
            throw new NullArgumentException("content");
        }
        this.content = ExtensionInfo.loadFromFile(content);
    }

    public String getName() {
        return name;
    }

    public ExampleInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ExampleInfo setDescription(String description) {
        this.description = description;
        return this;
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

    @Override
    public String toMarkdown() {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (StringUtils.isNotBlank(name)) {
            markdownBuilder.text(name, Text.Style.BOLD).p();
        }
        if (StringUtils.isNotBlank(description)) {
            markdownBuilder.text(description, Text.Style.ITALIC).p();
        }
        if (StringUtils.isNotBlank(content)) {
            return markdownBuilder.code(content, type).p().toMarkdown();
        }
        return markdownBuilder.toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
