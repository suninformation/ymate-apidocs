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

import net.ymate.apidocs.annotation.ApiLicense;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

/**
 * 描述一个授权协议
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/08 01:28
 */
public class LicenseInfo implements IMarkdown {

    public static LicenseInfo create(String name) {
        return new LicenseInfo(name);
    }

    public static LicenseInfo create(ApiLicense license) {
        if (license != null && StringUtils.isNotBlank(license.value())) {
            return new LicenseInfo(license.value())
                    .setUrl(license.url())
                    .setDescription(license.description());
        }
        return null;
    }

    /**
     * 协议名称
     */
    private final String name;

    /**
     * 协议URL地址
     */
    private String url;

    /**
     * 描述信息
     */
    private String description;

    public LicenseInfo(String name) {
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

    public LicenseInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public LicenseInfo setDescription(String description) {
        this.description = description;
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
        return markdownBuilder.toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
