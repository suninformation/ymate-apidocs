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
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/10/04 12:26
 */
public class ResponseTypeInfo implements Serializable {

    public static ResponseTypeInfo create(ApiResponses responses) {
        ResponseTypeInfo responseTypeInfo = new ResponseTypeInfo()
                .setName(responses.name())
                .setMultiple(responses.multiple())
                .setDescription(responses.description());
        Arrays.stream(responses.properties()).map(PropertyInfo::create).forEachOrdered(responseTypeInfo::addProperty);
        if (!Void.class.equals(responses.type())) {
            Class<?> responseType = responses.type().isArray() ? ClassUtils.getArrayClassType(responses.type()) : responses.type();
            responseTypeInfo.setName(StringUtils.defaultIfBlank(responseTypeInfo.getName(), responseType.getSimpleName()));
            responseTypeInfo.setMultiple(responses.multiple() || responses.type().isArray());
            responseTypeInfo.addProperties(PropertyInfo.create(null, responseType));
        }
        return responseTypeInfo;
    }

    public static Object create(Class<?> targetClass) throws Exception {
        ClassUtils.BeanWrapper<?> wrapper = ClassUtils.wrapper(targetClass.isArray() ? ClassUtils.getArrayClassType(targetClass).newInstance() : targetClass.newInstance());
        wrapper.getFields().forEach(field -> {
            ApiProperty apiProperty = field.getAnnotation(ApiProperty.class);
            if (apiProperty != null && !field.getType().equals(Map.class)) {
                try {
                    if (field.getType().equals(List.class) && !Void.class.equals(apiProperty.valueClass())) {
                        wrapper.setValue(field, Collections.singletonList(create(apiProperty.valueClass())));
                    } else if (field.getType().equals(Set.class) && !Void.class.equals(apiProperty.valueClass())) {
                        wrapper.setValue(field, Collections.singleton(create(apiProperty.valueClass())));
                    } else if (field.getType().isArray() && !Void.class.equals(apiProperty.valueClass())) {
                        wrapper.setValue(field, Collections.singletonList(create(apiProperty.valueClass())).toArray());
                    } else if (!Void.class.equals(apiProperty.valueClass())) {
                        wrapper.setValue(field, create(apiProperty.valueClass()));
                    } else if (StringUtils.isNotBlank(apiProperty.demoValue())) {
                        wrapper.setValue(field, apiProperty.demoValue());
                    }
                } catch (Exception ignored) {
                }
            }
        });
        return wrapper.getTargetObject();
    }

    /**
     * 响应数据类型名称
     */
    private String name;

    /**
     * 是否为数组集合
     */
    private boolean multiple;

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

    public boolean isMultiple() {
        return multiple;
    }

    public ResponseTypeInfo setMultiple(boolean multiple) {
        this.multiple = multiple;
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
        return String.format("ResponseTypeInfo{name='%s', multiple=%s, description='%s', properties=%s}", name, multiple, description, properties);
    }
}
