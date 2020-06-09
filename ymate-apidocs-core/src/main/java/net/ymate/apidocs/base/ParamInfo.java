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

import net.ymate.apidocs.AbstractMarkdown;
import net.ymate.apidocs.IDocs;
import net.ymate.apidocs.annotation.ApiExample;
import net.ymate.apidocs.annotation.ApiExamples;
import net.ymate.apidocs.annotation.ApiParam;
import net.ymate.apidocs.annotation.ApiParams;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Table;
import net.ymate.platform.commons.markdown.Text;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.validation.annotation.VField;
import net.ymate.platform.validation.validate.IDataRangeValuesProvider;
import net.ymate.platform.validation.validate.VDataRange;
import net.ymate.platform.validation.validate.VRequired;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.annotation.ModelBind;
import net.ymate.platform.webmvc.annotation.PathVariable;
import net.ymate.platform.webmvc.annotation.RequestParam;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述一个接口方法参数
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 17:03
 */
public class ParamInfo extends AbstractMarkdown {

    public static ParamInfo create(IDocs owner, String name, String type) {
        return new ParamInfo(owner, name, type);
    }

    public static ParamInfo create(IDocs owner, Field field) {
        return create(owner, field, field.getName(), field.getType());
    }

    public static ParamInfo create(IDocs owner, Parameter parameter, String defaultParamName) {
        return create(owner, parameter, StringUtils.defaultIfBlank(defaultParamName, parameter.getName()), parameter.getType());
    }

    public static List<ParamInfo> create(IDocs owner, ApiParams apiParams) {
        if (apiParams != null) {
            return create(owner, apiParams.value());
        }
        return Collections.emptyList();
    }

    public static List<ParamInfo> create(IDocs owner, ApiParam[] apiParams) {
        if (apiParams != null) {
            return Arrays.stream(apiParams).map((apiParam) -> ParamInfo.create(owner, apiParam)).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static ParamInfo create(IDocs owner, AnnotatedElement annotatedElement, String defaultParamName, Class<?> paramType) {
        if (annotatedElement != null) {
            ParamInfo paramInfo = doCreate(owner, annotatedElement, defaultParamName, paramType);
            if (paramInfo != null) {
                ApiExample apiExample = annotatedElement.getAnnotation(ApiExample.class);
                if (apiExample != null) {
                    paramInfo.addExample(ExampleInfo.create(apiExample));
                }
                ApiExamples apiExamples = annotatedElement.getAnnotation(ApiExamples.class);
                if (apiExamples != null) {
                    Arrays.stream(apiExamples.value()).map(ExampleInfo::create).forEachOrdered(paramInfo::addExample);
                }
                return paramInfo;
            }
        }
        return null;
    }

    private static ParamInfo doCreate(IDocs owner, AnnotatedElement annotatedElement, String defaultParamName, Class<?> paramType) {
        ApiParam apiParam = annotatedElement.getAnnotation(ApiParam.class);
        if (apiParam != null) {
            paramType = paramType != null ? paramType : apiParam.type();
            //
            RequestParam requestParam = annotatedElement.getAnnotation(RequestParam.class);
            PathVariable pathVariable = annotatedElement.getAnnotation(PathVariable.class);
            String paramName = null;
            if (requestParam != null) {
                paramName = StringUtils.defaultIfBlank(requestParam.value(), apiParam.value());
            } else if (pathVariable != null) {
                paramName = StringUtils.defaultIfBlank(pathVariable.value(), apiParam.value());
            }
            if (StringUtils.isBlank(paramName)) {
                paramName = defaultParamName;
            }
            boolean required = apiParam.required() || annotatedElement.isAnnotationPresent(VRequired.class) || apiParam.pathVariable() || pathVariable != null;
            if (!apiParam.hidden() && StringUtils.isNotBlank(paramName)) {
                String description = apiParam.description();
                if (StringUtils.isBlank(description) && annotatedElement.isAnnotationPresent(VField.class)) {
                    description = annotatedElement.getAnnotation(VField.class).name();
                }
                ParamInfo paramInfo = new ParamInfo(owner, paramName, paramType.getSimpleName())
                        .setDefaultValue(StringUtils.defaultIfBlank(apiParam.defaultValue(), requestParam != null ? StringUtils.defaultIfBlank(requestParam.defaultValue(), apiParam.defaultValue()) : apiParam.defaultValue()))
                        .setDemoValue(apiParam.demoValue())
                        .addAllowValues(Arrays.asList(apiParam.allowValues()))
                        .setModel(apiParam.model() || annotatedElement.isAnnotationPresent(ModelBind.class))
                        .setMultiple(apiParam.multiple() || paramType.isArray())
                        .setMultipart(apiParam.multipart() || paramType.isArray() ? ClassUtils.getArrayClassType(paramType).equals(IUploadFileWrapper.class) : paramType.equals(IUploadFileWrapper.class))
                        .setPathVariable(apiParam.pathVariable() || pathVariable != null)
                        .setRequired(required)
                        .setDescription(description)
                        .addExample(StringUtils.isNotBlank(apiParam.example()) ? ExampleInfo.create(apiParam.example()) : null)
                        .addExamples(ExampleInfo.create(apiParam.examples()));
                if (ArrayUtils.isEmpty(apiParam.allowValues())) {
                    VDataRange dataRange = annotatedElement.getAnnotation(VDataRange.class);
                    if (dataRange != null) {
                        if (!IDataRangeValuesProvider.class.equals(dataRange.providerClass())) {
                            paramInfo.addAllowValues(ClassUtils.impl(dataRange.providerClass(), IDataRangeValuesProvider.class).values());
                        } else {
                            paramInfo.addAllowValues(Arrays.asList(dataRange.value()));
                        }
                    }
                }
                return paramInfo;
            }
        }
        return null;
    }

    public static ParamInfo create(IDocs owner, ApiParam apiParam) {
        if (apiParam != null) {
            if (!apiParam.hidden() && StringUtils.isNotBlank(apiParam.value())) {
                return new ParamInfo(owner, apiParam.value(), apiParam.type().getSimpleName())
                        .setDefaultValue(apiParam.defaultValue())
                        .setDemoValue(apiParam.demoValue())
                        .addAllowValues(Arrays.asList(apiParam.allowValues()))
                        .setModel(apiParam.model())
                        .setMultiple(apiParam.multiple() || apiParam.type().isArray())
                        .setMultipart(apiParam.multipart() || apiParam.type().isArray() ? ClassUtils.getArrayClassType(apiParam.type()).equals(IUploadFileWrapper.class) : apiParam.type().equals(IUploadFileWrapper.class))
                        .setPathVariable(apiParam.pathVariable())
                        .setRequired(apiParam.required())
                        .setDescription(apiParam.description())
                        .addExample(StringUtils.isNotBlank(apiParam.example()) ? ExampleInfo.create(apiParam.example()) : null)
                        .addExamples(ExampleInfo.create(apiParam.examples()));
            }
        }
        return null;
    }

    public static String toMarkdown(IDocs owner, List<ParamInfo> params) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!params.isEmpty()) {
            Table table = Table.create()
                    .addHeader(AbstractMarkdown.i18nText(owner, "param.name", "Name"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "param.type", "Type"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "param.required", "Required"), Table.Align.CENTER)
                    .addHeader(AbstractMarkdown.i18nText(owner, "param.default", "Default"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "param.description", "Description"), Table.Align.LEFT);

            markdownBuilder.append(table);
            Iterator<ParamInfo> paramIt = params.iterator();
            while (paramIt.hasNext()) {
                ParamInfo param = paramIt.next();
                markdownBuilder.append(param.toMarkdown());
                if (!param.getExamples().isEmpty()) {
                    markdownBuilder.p().text(AbstractMarkdown.i18nText(owner, "param.examples", "Parameter examples"), Text.Style.BOLD).p().append(ExampleInfo.toMarkdown(param.getExamples()));
                    if (paramIt.hasNext()) {
                        markdownBuilder.p().append(table);
                    }
                }
            }
        }
        return markdownBuilder.toMarkdown();
    }

    /**
     * 参数名称
     */
    private final String name;

    /**
     * 参数说明
     */
    private String description;

    /**
     * 参数默认值
     */
    private String defaultValue;

    /**
     * 示例值
     *
     * @since 2.0.0
     */
    private String demoValue;

    /**
     * 参数可选值
     */
    private final List<String> allowValues = new ArrayList<>();

    /**
     * 参数类型
     */
    private final String type;

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
     * 是否为文件上传
     *
     * @since 2.0.0
     */
    private boolean multipart;

    /**
     * 是否为路径变量
     *
     * @since 2.0.0
     */
    private boolean pathVariable;

    /**
     * 参数示例
     */
    private final List<ExampleInfo> examples = new ArrayList<>();

    public ParamInfo(IDocs owner, String name, String type) {
        super(owner);
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        if (StringUtils.isBlank(type)) {
            throw new NullArgumentException("type");
        }
        this.name = name;
        this.type = type;
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

    public String getDemoValue() {
        return demoValue;
    }

    public ParamInfo setDemoValue(String demoValue) {
        this.demoValue = demoValue;
        return this;
    }

    public List<String> getAllowValues() {
        return allowValues;
    }

    public ParamInfo addAllowValues(Collection<String> allowValues) {
        if (allowValues != null) {
            allowValues.forEach(this::addAllowValue);
        }
        return this;
    }

    public ParamInfo addAllowValue(String allowValue) {
        if (StringUtils.isNotBlank(allowValue)) {
            if (!this.allowValues.contains(allowValue)) {
                this.allowValues.add(allowValue);
            }
        }
        return this;
    }

    public String getType() {
        return type;
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

    public boolean isMultipart() {
        return multipart;
    }

    public ParamInfo setMultipart(boolean multipart) {
        this.multipart = multipart;
        return this;
    }

    public boolean isPathVariable() {
        return pathVariable;
    }

    public ParamInfo setPathVariable(boolean pathVariable) {
        this.pathVariable = pathVariable;
        return this;
    }

    public List<ExampleInfo> getExamples() {
        return examples;
    }

    public ParamInfo addExamples(List<ExampleInfo> examples) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return name.equals(((ParamInfo) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toMarkdown() {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create().append(PropertyInfo.parseText(description));
        if (!allowValues.isEmpty()) {
            if (markdownBuilder.length() > 0) {
                markdownBuilder.br();
            }
            markdownBuilder.append(i18nText("param.allow_values", "Allow values: ")).append(PropertyInfo.parseText(String.format("{%s}", StringUtils.join(allowValues, '|'))));
        }
        return Table.create().addRow()
                .addColumn(name)
                .addColumn(type)
                .addColumn(required ? "Y" : "N")
                .addColumn(defaultValue)
                .addColumn(markdownBuilder).build().toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
