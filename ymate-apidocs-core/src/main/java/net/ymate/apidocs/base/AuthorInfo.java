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

import net.ymate.apidocs.annotation.ApiAuthor;
import net.ymate.apidocs.annotation.ApiAuthors;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 描述开发者信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 13:07
 */
public class AuthorInfo implements IMarkdown {

    public static AuthorInfo create(String name) {
        return new AuthorInfo(name);
    }

    public static List<AuthorInfo> create(ApiAuthors apiAuthors) {
        if (apiAuthors != null) {
            return Arrays.stream(apiAuthors.value()).map(AuthorInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static AuthorInfo create(ApiAuthor author) {
        if (author != null && StringUtils.isNotBlank(author.value())) {
            return new AuthorInfo(author.value())
                    .setEmail(author.email())
                    .setUrl(author.url());
        }
        return null;
    }

    public static String toMarkdown(List<AuthorInfo> authors) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        int idx = 0;
        for (AuthorInfo author : authors) {
            if (idx > 0) {
                markdownBuilder.br();
            }
            markdownBuilder.append(author.toMarkdown());
            idx++;
        }
        return markdownBuilder.toMarkdown();
    }

    /**
     * 开发者名称
     */
    private String name;

    /**
     * 开发者主页地址
     */
    private String url;

    /**
     * 开发者联系邮箱地址
     */
    private String email;

    public AuthorInfo(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public AuthorInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public AuthorInfo setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public String toMarkdown() {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (StringUtils.isNotBlank(url)) {
            markdownBuilder.link(name, url);
        } else {
            markdownBuilder.append(name);
        }
        if (StringUtils.isNotBlank(email)) {
            markdownBuilder.space().append("(").link(email, String.format("mailto:%s", email)).append(")");
        }
        return markdownBuilder.toMarkdown();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return name.equals(((AuthorInfo) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
