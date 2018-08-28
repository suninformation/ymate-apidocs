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

import net.ymate.apidocs.annotation.ApiLicense;
import net.ymate.apidocs.core.IMarkdown;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 描述一个授权协议
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/8 上午1:28
 * @version 1.0
 */
public class LicenseInfo implements IMarkdown, Serializable {

    public static LicenseInfo create(String name) {
        return new LicenseInfo(name);
    }

    public static LicenseInfo create(ApiLicense license) {
        if (license != null && StringUtils.isNotBlank(license.name())) {
            return new LicenseInfo(license.name()).setUrl(license.url());
        }
        return null;
    }

    /**
     * 协议名称
     */
    private String name;

    /**
     * 协议URL地址
     */
    private String url;

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

    @Override
    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        if (StringUtils.isNotBlank(url)) {
            md.append("[").append(name).append("](").append(url).append(")");
        } else {
            md.append(name);
        }
        return md.toString();
    }
}
