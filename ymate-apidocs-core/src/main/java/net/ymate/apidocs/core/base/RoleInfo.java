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

import net.ymate.apidocs.core.IMarkdown;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 描述一个角色
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/9 下午10:39
 * @version 1.0
 */
public class RoleInfo implements IMarkdown, Serializable {

    public static RoleInfo create(String name, String description) {
        return new RoleInfo(name, description);
    }

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    public RoleInfo(String name, String description) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        if (StringUtils.isBlank(description)) {
            throw new NullArgumentException("description");
        }
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toMarkdown() {
        return name + "(" + description + ")";
    }
}
