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

import net.ymate.apidocs.annotation.ApiGroup;
import net.ymate.apidocs.annotation.ApiGroups;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 描述一个接口/方法分组
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 17:01
 */
public class GroupInfo implements Serializable {

    public static GroupInfo create(String name) {
        return new GroupInfo(name);
    }

    public static List<GroupInfo> create(ApiGroups apiGroups) {
        if (apiGroups != null) {
            return Arrays.stream(apiGroups.value()).map(GroupInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static GroupInfo create(ApiGroup group) {
        if (group != null && StringUtils.isNotBlank(group.value())) {
            return new GroupInfo(group.value())
                    .setDescription(group.description());
        }
        return null;
    }

    /**
     * 分组名称
     */
    private final String name;

    /**
     * 分组描述
     */
    private String description;

    public GroupInfo(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
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

    @Override
    public String toString() {
        return String.format("GroupInfo{name='%s', description='%s'}", name, description);
    }
}
