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

import net.ymate.apidocs.annotation.ApiProperty;
import net.ymate.apidocs.annotation.ApiResponses;
import net.ymate.apidocs.core.IMarkdown;
import net.ymate.platform.core.i18n.I18N;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/10/4 下午12:26
 * @version 1.0
 */
public class ResponseTypeInfo implements IMarkdown, Serializable {

    public static ResponseTypeInfo create(ApiResponses responses) {
        ResponseTypeInfo _responseTypeInfo = new ResponseTypeInfo().setName(responses.name()).setDescription(responses.description());
        ClassUtils.BeanWrapper<?> _wrapper = ClassUtils.wrapper(responses.type());
        for (Field _field : _wrapper.getFields()) {
            ApiProperty _property = _field.getAnnotation(ApiProperty.class);
            if (_property != null) {
                _responseTypeInfo.addProperty(PropertyInfo.create(_property, _field));
            }
        }
        return _responseTypeInfo;
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
    private List<PropertyInfo> properties;

    public ResponseTypeInfo(PropertyInfo... properties) {
        this.properties = new ArrayList<PropertyInfo>();
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

    public ResponseTypeInfo setProperties(List<PropertyInfo> properties) {
        if (properties != null) {
            this.properties.addAll(properties);
        }
        return this;
    }

    public ResponseTypeInfo addProperty(PropertyInfo property) {
        if (property != null) {
            this.properties.add(property);
        }
        return this;
    }

    @Override
    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        if (StringUtils.isNotBlank(name)) {
            md.append("> **").append(name).append("**").append("\n");
        }
        if (StringUtils.isNotBlank(description)) {
            if (md.length() > 0) {
                md.append("\n");
            }
            md.append(description).append("\n");
        }
        if (properties != null && !properties.isEmpty()) {
            md.append("\n|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_param_name", "Parameter name")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_type", "Type")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_description", "Description")).append("|\n");
            md.append("|---|---|---|\n");
            for (PropertyInfo property : properties) {
                md.append(property.useTable().toMarkdown()).append("\n");
            }
        }
        return md.toString();
    }
}
