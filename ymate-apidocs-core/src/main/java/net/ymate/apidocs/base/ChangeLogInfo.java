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
import net.ymate.apidocs.annotation.ApiChangeLog;
import net.ymate.apidocs.annotation.ApiChangeLogs;
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
 * 描述变更记录
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 17:04
 */
public class ChangeLogInfo implements IMarkdown {

    public static ChangeLogInfo create(String date, String action, AuthorInfo author) {
        return new ChangeLogInfo(date, action, author);
    }

    public static List<ChangeLogInfo> create(ApiChangeLogs apiChangeLogs) {
        if (apiChangeLogs != null) {
            return Arrays.stream(apiChangeLogs.value()).map(ChangeLogInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static ChangeLogInfo create(ApiChangeLog changelog) {
        if (changelog != null && StringUtils.isNotBlank(changelog.date())) {
            return new ChangeLogInfo(changelog.date(), changelog.action().name(), AuthorInfo.create(changelog.author()))
                    .setDescription(changelog.description());
        }
        return null;
    }

    public static String toMarkdown(IDocs owner, List<ChangeLogInfo> changeLogs) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!changeLogs.isEmpty()) {
            markdownBuilder.append(Table.create()
                    .addHeader(AbstractMarkdown.i18nText(owner, "changelog.date", "Date"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "changelog.action", "Action"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "changelog.description", "Description"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "changelog.author", "Author"), Table.Align.LEFT));
            changeLogs.forEach(markdownBuilder::append);
        }
        return markdownBuilder.toMarkdown();
    }

    /**
     * 日期，格式如：2018-04-15 01:37
     */
    private final String date;

    /**
     * 变更动作
     */
    private final String action;

    /**
     * 变更作者信息
     */
    private final AuthorInfo author;

    /**
     * 变更内容描述
     */
    private String description;

    public ChangeLogInfo(String date, String action, AuthorInfo author) {
        if (StringUtils.isBlank(date)) {
            throw new NullArgumentException("date");
        }
        if (StringUtils.isBlank(action)) {
            throw new NullArgumentException("action");
        }
        if (author == null) {
            throw new NullArgumentException("author");
        }
        this.date = date;
        this.action = action;
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public String getAction() {
        return action;
    }

    public AuthorInfo getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public ChangeLogInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toMarkdown() {
        return Table.create().addRow().addColumn(date).addColumn(action).addColumn(description).addColumn(author.getName()).build().toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
