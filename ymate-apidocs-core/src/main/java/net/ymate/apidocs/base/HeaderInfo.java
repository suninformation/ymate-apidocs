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
import net.ymate.apidocs.annotation.ApiHeader;
import net.ymate.apidocs.annotation.ApiRequestHeaders;
import net.ymate.apidocs.annotation.ApiResponseHeaders;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Table;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 描述一个请求/响应头信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/08 16:58
 */
public class HeaderInfo implements IMarkdown {

    public static HeaderInfo create(String name) {
        return new HeaderInfo(name);
    }

    public static List<HeaderInfo> create(ApiHeader[] apiHeaders) {
        if (apiHeaders != null && apiHeaders.length > 0) {
            return Arrays.stream(apiHeaders).map(HeaderInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static List<HeaderInfo> create(ApiRequestHeaders requestHeaders) {
        if (requestHeaders != null) {
            return Arrays.stream(requestHeaders.value()).map(HeaderInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static List<HeaderInfo> create(ApiResponseHeaders requestHeaders) {
        if (requestHeaders != null) {
            return Arrays.stream(requestHeaders.value()).map(HeaderInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static HeaderInfo create(ApiHeader apiHeader) {
        if (apiHeader != null && StringUtils.isNotBlank(apiHeader.name())) {
            return new HeaderInfo(apiHeader.name())
                    .setValue(apiHeader.value())
                    .setType(apiHeader.type().getSimpleName())
                    .setDescription(apiHeader.description());
        }
        return null;
    }

    public static String toMarkdown(IDocs owner, List<HeaderInfo> headers) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!headers.isEmpty()) {
            markdownBuilder.append(Table.create()
                    .addHeader(AbstractMarkdown.i18nText(owner, "header.name", "Name"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "header.value", "Value"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "header.type", "Type"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "header.description", "Description"), Table.Align.LEFT));
            headers.forEach(markdownBuilder::append);
        }
        return markdownBuilder.toMarkdown();
    }

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private String value;

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

    public String getValue() {
        return value;
    }

    public HeaderInfo setValue(String value) {
        this.value = value;
        return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return name.equals(((HeaderInfo) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toMarkdown() {
        return Table.create().addRow().addColumn(name).addColumn(value).addColumn(type).addColumn(description).build().toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
