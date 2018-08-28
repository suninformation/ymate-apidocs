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

import net.ymate.apidocs.annotation.ApiExample;
import net.ymate.apidocs.annotation.ApiParam;
import net.ymate.apidocs.core.IMarkdown;
import net.ymate.platform.core.i18n.I18N;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述一个接口方法参数
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/15 下午5:03
 * @version 1.0
 */
public class ParamInfo implements IMarkdown, Serializable {

    public static ParamInfo create(String name) {
        return new ParamInfo(name);
    }

    public static ParamInfo create(ApiParam param) {
        if (param != null && !param.hidden() && StringUtils.isNotBlank(param.name())) {
            ParamInfo _paramInfo = new ParamInfo(param.name())
                    .setDefaultValue(param.defaultValue())
                    .setAllowValues(param.allowValues())
                    .setModel(param.model())
                    .setMultiple(param.multiple())
                    .setRequired(param.required())
                    .setType(param.type())
                    .setDescription(param.value());
            for (ApiExample example : param.examples()) {
                _paramInfo.addExample(ExampleInfo.create(example));
            }
            return _paramInfo;
        }
        return null;
    }

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数说明
     */
    private String description;

    /**
     * 参数默认值
     */
    private String defaultValue;

    /**
     * 参数可选值
     */
    private String allowValues;

    /**
     * 参数类型
     */
    private String type;

    /**
     * 参数是否必须
     */
    private boolean required;

    /**
     * 是否为模型对象
     */
    private boolean model;

    /**
     * 是否为数组集合
     */
    private boolean multiple;

    /**
     * 参数示例
     */
    private List<ExampleInfo> examples;

    public ParamInfo(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
        this.examples = new ArrayList<ExampleInfo>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ParamInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public ParamInfo setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getAllowValues() {
        return allowValues;
    }

    public ParamInfo setAllowValues(String allowValues) {
        this.allowValues = allowValues;
        return this;
    }

    public String getType() {
        return type;
    }

    public ParamInfo setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public ParamInfo setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public boolean isModel() {
        return model;
    }

    public ParamInfo setModel(boolean model) {
        this.model = model;
        return this;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public ParamInfo setMultiple(boolean multiple) {
        this.multiple = multiple;
        return this;
    }

    public List<ExampleInfo> getExamples() {
        return examples;
    }

    public ParamInfo setExamples(List<ExampleInfo> examples) {
        if (examples != null) {
            this.examples.addAll(examples);
        }
        return this;
    }

    public ParamInfo addExample(ExampleInfo example) {
        if (example != null) {
            this.examples.add(example);
        }
        return this;
    }

    @Override
    public String toMarkdown() {
        StringBuilder _desc = new StringBuilder();
        boolean flag = false;
        if (StringUtils.isNotBlank(description)) {
            flag = true;
            _desc.append(description);
        }
        if (StringUtils.isNotBlank(allowValues)) {
            if (flag) {
                _desc.append(", ");
            }
            _desc.append(I18N.formatMessage("apidocs-messages", "apidocs.content.param_value_range", "Value Range")).append(": ").append(allowValues);
        }
        return "|`" + name + "`|" + type + "|" + StringUtils.trimToEmpty(defaultValue) + "|" + StringUtils.replaceEach(_desc.toString(), new String[]{"\r\n", "\r", "\n", "\t"}, new String[]{"[\\r][\\n]", "[\\r]", "[\\n]", "[\\t]"}) + "|";
    }
}
