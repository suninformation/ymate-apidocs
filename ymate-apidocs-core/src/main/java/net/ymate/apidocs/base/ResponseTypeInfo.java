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

import net.ymate.apidocs.annotation.ApiProperty;
import net.ymate.apidocs.annotation.ApiResponses;
import net.ymate.platform.commons.util.ClassUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/10/04 12:26
 */
public class ResponseTypeInfo implements Serializable {

    public static ResponseTypeInfo create(ApiResponses responses) {
        ResponseTypeInfo responseTypeInfo = new ResponseTypeInfo()
                .setName(responses.name())
                .setDescription(responses.description());
        Arrays.stream(responses.properties()).map(PropertyInfo::create).forEachOrdered(responseTypeInfo::addProperty);
        if (!Void.class.equals(responses.type())) {
            responseTypeInfo.setName(StringUtils.defaultIfBlank(responseTypeInfo.getName(), responses.type().getSimpleName()));
            processProperties(responseTypeInfo, null, responses.type());
        }
        return responseTypeInfo;
    }

    private static void processProperties(ResponseTypeInfo resultData, String prefix, Class<?> type) {
        ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapper(type);
        for (Field field : beanWrapper.getFields()) {
            ApiProperty apiProperty = field.getAnnotation(ApiProperty.class);
            if (apiProperty != null) {
                if (apiProperty.model()) {
                    if (StringUtils.isNotBlank(prefix)) {
                        prefix += ".";
                    }
                    processProperties(resultData, StringUtils.trimToEmpty(prefix) + StringUtils.defaultIfBlank(apiProperty.name(), field.getName()), Void.class.equals(apiProperty.modelClass()) ? field.getType() : apiProperty.modelClass());
                } else {
                    resultData.addProperty(PropertyInfo.create(apiProperty, prefix, field));
                }
            }
        }
    }

    /**
     * 响应数据类型名称
     */
    private String name;

    /**
     * 响应数据类型描述
     */
    private String description;

    /**
     * 响应数据类型属性
     */
    private final List<PropertyInfo> properties = new ArrayList<>();

    public ResponseTypeInfo(PropertyInfo... properties) {
        if (ArrayUtils.isNotEmpty(properties)) {
            this.properties.addAll(Arrays.asList(properties));
        }
    }

    public String getName() {
        return name;
    }

    public ResponseTypeInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ResponseTypeInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<PropertyInfo> getProperties() {
        return properties;
    }

    public ResponseTypeInfo addProperties(List<PropertyInfo> properties) {
        if (properties != null) {
            properties.forEach(this::addProperty);
        }
        return this;
    }

    public ResponseTypeInfo addProperty(PropertyInfo property) {
        if (property != null && StringUtils.isNotBlank(property.getName())) {
            if (!this.properties.contains(property)) {
                this.properties.add(property);
            }
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return name.equals(((ResponseTypeInfo) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return String.format("ResponseTypeInfo{name='%s', description='%s', properties=%s}", name, description, properties);
    }
}