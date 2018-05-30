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

import net.ymate.apidocs.annotation.ApiExtension;
import net.ymate.apidocs.annotation.ApiGroup;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述一个接口/方法分组
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/15 下午5:01
 * @version 1.0
 */
public class GroupInfo implements Serializable {

    public static GroupInfo create(String name) {
        return new GroupInfo(name);
    }

    public static GroupInfo create(ApiGroup group) {
        GroupInfo _groupInfo = new GroupInfo(group.name()).setDescription(group.description());
        for (ApiExtension _extension : group.extensions()) {
            _groupInfo.addExtensions(ExtensionInfo.create(_extension));
        }
        return _groupInfo;
    }

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组描述
     */
    private String description;

    /**
     * 扩展信息
     */
    private List<ExtensionInfo> extensions;

    public GroupInfo(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
        this.extensions = new ArrayList<ExtensionInfo>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public GroupInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<ExtensionInfo> getExtensions() {
        return extensions;
    }

    public GroupInfo setExtensions(List<ExtensionInfo> extensions) {
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
        return this;
    }

    public GroupInfo addExtensions(ExtensionInfo extension) {
        if (extension != null) {
            this.extensions.add(extension);
        }
        return this;
    }
}
