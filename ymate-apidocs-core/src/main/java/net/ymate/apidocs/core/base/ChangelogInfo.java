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

import net.ymate.apidocs.annotation.ApiChangelog;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 描述变更记录
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/15 下午5:04
 * @version 1.0
 */
public class ChangelogInfo implements Serializable {

    public static ChangelogInfo create(String date, String action, AuthorInfo author) {
        return new ChangelogInfo(date, action, author);
    }

    public static ChangelogInfo create(ApiChangelog changelog) {
        if (changelog != null && StringUtils.isNotBlank(changelog.date())) {
            return new ChangelogInfo(changelog.date(), changelog.action().name(), AuthorInfo.create(changelog.author()))
                    .setDescription(changelog.description());
        }
        return null;
    }

    /**
     * 日期，格式如：2018-04-15 01:37
     */
    private String date;

    /**
     * 变更动作
     */
    private String action;

    /**
     * 变更作者信息
     */
    private AuthorInfo author;

    /**
     * 变更内容描述
     */
    private String description;

    public ChangelogInfo(String date, String action, AuthorInfo author) {
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

    public ChangelogInfo setDescription(String description) {
        this.description = description;
        return this;
    }
}
