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

import net.ymate.apidocs.annotation.ApiExtensionProperty;

import java.io.Serializable;

/**
 * 描述一个自定义属性
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/8 下午2:54
 * @version 1.0
 */
public class PropertyInfo implements Serializable {

    public static PropertyInfo create() {
        return new PropertyInfo();
    }

    public static PropertyInfo create(ApiExtensionProperty property) {
        if (property != null) {
            return new PropertyInfo()
                    .setName(property.name())
                    .setValue(property.value())
                    .setDescription(property.description());
        }
        return null;
    }

    private String name;

    private String value;

    private String description;

    public String getName() {
        return name;
    }

    public PropertyInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public PropertyInfo setValue(String value) {
        this.value = value;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PropertyInfo setDescription(String description) {
        this.description = description;
        return this;
    }
}
