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

import net.ymate.apidocs.annotation.ApiAuthor;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 描述开发者信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/15 下午5:07
 * @version 1.0
 */
public class AuthorInfo implements Serializable {

    public static AuthorInfo create(String name) {
        return new AuthorInfo(name);
    }

    public static AuthorInfo create(ApiAuthor author) {
        if (author != null && StringUtils.isNotBlank(author.name())) {
            return new AuthorInfo(author.name()).setEmail(author.email()).setUrl(author.url());
        }
        return null;
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
}
