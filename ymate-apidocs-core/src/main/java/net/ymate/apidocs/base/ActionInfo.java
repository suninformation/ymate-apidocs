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

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.ymate.apidocs.AbstractMarkdown;
import net.ymate.apidocs.IDocs;
import net.ymate.apidocs.annotation.*;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Text;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.impl.DefaultResultSet;
import net.ymate.platform.webmvc.RequestMeta;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.util.WebResult;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 描述一个API接口方法
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/08 16:49
 */
public class ActionInfo extends AbstractMarkdown {

    public static String processRequestMapping(String root, String mapping) {
        StringBuilder mappingBuilder = new StringBuilder(doCheckMappingSeparator(root));
        if (StringUtils.isNotBlank(mapping)) {
            mappingBuilder.append(doCheckMappingSeparator(mapping));
        }
        return mappingBuilder.toString();
    }

    public static ActionInfo create(IDocs owner, ApiInfo apiInfo, String name, String mapping, String displayName) {
        return new ActionInfo(owner, apiInfo, name, mapping, displayName);
    }

    private static String doCheckMappingSeparator(String requestMapping) {
        if (StringUtils.isBlank(requestMapping)) {
            return StringUtils.EMPTY;
        }
        if (!requestMapping.startsWith(Type.Const.PATH_SEPARATOR)) {
            requestMapping = Type.Const.PATH_SEPARATOR.concat(requestMapping);
        }
        if (requestMapping.endsWith(Type.Const.PATH_SEPARATOR)) {
            requestMapping = requestMapping.substring(0, requestMapping.length() - 1);
        }
        return requestMapping;
    }

    private static RequestMeta doBuildRequestMeta(Method method) {
        RequestMeta requestMeta = null;
        try {
            requestMeta = new RequestMeta(null, method.getDeclaringClass(), method);
        } catch (Exception ignored) {
        }
        return requestMeta;
    }

    public static ActionInfo create(IDocs owner, ApiInfo apiInfo, Method method) {
        if (method != null) {
            ApiAction apiAction = method.getAnnotation(ApiAction.class);
            if (apiAction != null && !apiAction.hidden()) {
                RequestMeta requestMeta = null;
                String mapping = apiAction.mapping();
                if (StringUtils.isBlank(mapping)) {
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    if (requestMapping != null) {
                        requestMeta = doBuildRequestMeta(method);
                        if (requestMeta != null) {
                            mapping = requestMeta.getMapping();
                        }
                    } else {
                        mapping = doCheckMappingSeparator(method.getName());
                    }
                } else {
                    Api api = method.getDeclaringClass().getAnnotation(Api.class);
                    mapping = api != null ? processRequestMapping(api.mapping(), mapping) : processRequestMapping(null, mapping);
                }
                List<String> httpMethods = new ArrayList<>();
                if (ArrayUtils.isEmpty(apiAction.httpMethod())) {
                    if (requestMeta == null) {
                        requestMeta = doBuildRequestMeta(method);
                    }
                    if (requestMeta != null) {
                        for (Type.HttpMethod httpMethod : requestMeta.getAllowMethods()) {
                            if (!owner.getConfig().getIgnoredRequestMethods().contains(httpMethod.name()) && !httpMethods.contains(httpMethod.name())) {
                                httpMethods.add(httpMethod.name());
                            }
                        }
                    }
                } else {
                    for (String httpMethodName : apiAction.httpMethod()) {
                        if (StringUtils.isNotBlank(httpMethodName) && !owner.getConfig().getIgnoredRequestMethods().contains(httpMethodName.toUpperCase())) {
                            httpMethods.add(httpMethodName);
                        }
                    }
                }
                if (httpMethods.isEmpty()) {
                    httpMethods.add(Type.HttpMethod.GET.name());
                }
                ActionInfo actionInfo = new ActionInfo(owner, apiInfo, method.getName(), mapping, apiAction.value())
                        .setDescription(apiAction.description())
                        .setGroup(apiAction.group())
                        .setHttpStatus(apiAction.httpStatus())
                        .setOrder(apiAction.order())
                        .setDeprecated(apiInfo.isDeprecated() || method.isAnnotationPresent(Deprecated.class))
                        .setRequestType(apiAction.requestType())
                        .setSecurity(SecurityInfo.create(owner, method.getAnnotation(ApiSecurity.class), apiInfo.getSecurity()))
                        .addNotes(Arrays.asList(apiAction.notes()))
                        .addMethods(httpMethods)
                        .addScopes(Arrays.asList(apiAction.scopes()))
                        .addChangeLogs(ChangeLogInfo.create(method.getAnnotation(ApiChangeLogs.class)))
                        .addChangeLog(ChangeLogInfo.create(method.getAnnotation(ApiChangeLog.class)))
                        .addExtensions(ExtensionInfo.create(method.getAnnotation(ApiExtensions.class)))
                        .addExtension(ExtensionInfo.create(method.getAnnotation(ApiExtension.class)))
                        .addExamples(ExampleInfo.create(method.getAnnotation(ApiExamples.class)))
                        .addExample(ExampleInfo.create(method.getAnnotation(ApiExample.class)))
                        .addParams(ParamInfo.create(owner, method.getAnnotation(ApiParams.class)))
                        .addParam(ParamInfo.create(owner, method.getAnnotation(ApiParam.class)))
                        .addResponse(ResponseInfo.create(method.getAnnotation(ApiResponse.class)))
                        .addRequestHeaders(HeaderInfo.create(method.getAnnotation(ApiRequestHeaders.class)))
                        .addResponseHeaders(HeaderInfo.create(method.getAnnotation(ApiResponseHeaders.class)));
                //
                String[] paramNames = ClassUtils.getMethodParamNames(method);
                Parameter[] parameters = method.getParameters();
                for (int idx = 0; idx < parameters.length; idx++) {
                    String paramName = parameters[idx].getName();
                    if (paramNames.length > idx) {
                        paramName = paramNames[idx];
                    }
                    ParamInfo paramInfo = ParamInfo.create(owner, parameters[idx], paramName);
                    if (paramInfo != null) {
                        if (paramInfo.isModel()) {
                            ClassUtils.wrapper(parameters[idx].getType()).getFields().forEach(field -> actionInfo.addParam(ParamInfo.create(owner, field)));
                        } else {
                            actionInfo.addParam(paramInfo);
                        }
                    }
                }
                //
                ApiResponses apiResponses = method.getAnnotation(ApiResponses.class);
                if (apiResponses != null) {
                    ResponseTypeInfo responseTypeInfo = ResponseTypeInfo.create(apiResponses);
                    actionInfo.setResponseType(responseTypeInfo);
                    Arrays.stream(apiResponses.value()).map(ResponseInfo::create).forEachOrdered(actionInfo::addResponse);
                    //
                    ApiGenerateResponseExample apiGenerateResponseExample = method.getAnnotation(ApiGenerateResponseExample.class);
                    if (apiGenerateResponseExample != null && !Void.class.equals(apiResponses.type()) && !responseTypeInfo.getProperties().isEmpty()) {
                        try {
                            Object instance = ResponseTypeInfo.create(apiResponses.type());
                            if (apiGenerateResponseExample.paging()) {
                                instance = new DefaultResultSet<>(Collections.singletonList(instance), 1, 20, 1);
                            } else if (responseTypeInfo.isMultiple()) {
                                instance = Collections.singletonList(instance);
                            }
                            String content = WebResult.succeed().data(instance).toJSONObject().toString(SerializerFeature.PrettyFormat,
                                    SerializerFeature.WriteMapNullValue,
                                    SerializerFeature.WriteNullBooleanAsFalse,
                                    SerializerFeature.WriteNullListAsEmpty,
                                    SerializerFeature.WriteNullNumberAsZero,
                                    SerializerFeature.WriteNullStringAsEmpty,
                                    SerializerFeature.WriteNullNumberAsZero);
                            actionInfo.addExample(ExampleInfo.create(content).setName(apiGenerateResponseExample.name()).setType("json").setDescription(apiGenerateResponseExample.description()));
                        } catch (Exception ignored) {
                        }
                    }
                }
                //
                return actionInfo;
            }
        }
        return null;
    }

    public static String toMarkdown(List<ActionInfo> actions) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!actions.isEmpty()) {
            actions.forEach(markdownBuilder::append);
        }
        return markdownBuilder.toMarkdown();
    }

    private final ApiInfo apiInfo;

    /**
     * 接口名称
     */
    private final String name;

    /**
     * 显示名称
     */
    private final String displayName;

    /**
     * 请求URL地址映射
     */
    private final String mapping;

    /**
     * 接口方法描述
     */
    private String description;

    /**
     * 接口方法提示内容
     */
    private List<String> notes;

    /**
     * HTTP请求响应状态值
     */
    private int httpStatus;

    /**
     * 接口方法访问权限
     */
    private SecurityInfo security;

    /**
     * 接口授权范围集合
     */
    private final List<String> scopes = new ArrayList<>();

    /**
     * 接口方法所属分组
     */
    private String group;

    /**
     * 自定义排序
     *
     * @since 2.0.0
     */
    private int order;

    /**
     * 是否为不被推荐
     *
     * @since 2.0.0
     */
    private boolean deprecated;

    /**
     * HTTP请求方法，如：GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE等
     */
    private final List<String> methods = new ArrayList<>();

    /**
     * HTTP请求头信息集合
     */
    private final List<HeaderInfo> requestHeaders = new ArrayList<>();

    /**
     * HTTP响应头信息集合
     */
    private final List<HeaderInfo> responseHeaders = new ArrayList<>();

    /**
     * 接口方法参数定义
     */
    private final List<ParamInfo> params = new ArrayList<>();

    /**
     * 请求ContentType类型, 可选值: json|xml, 默认空表示标准HTTP请求
     *
     * @since 2.0.0
     */
    private String requestType;

    /**
     * 接口方法响应数据类型
     */
    private ResponseTypeInfo responseType;

    /**
     * 接口方法响应信息集合
     */
    private final List<ResponseInfo> responses = new ArrayList<>();

    /**
     * 接口方法变更记录
     */
    private final List<ChangeLogInfo> changeLogs = new ArrayList<>();

    /**
     * 接口示例
     */
    private final List<ExampleInfo> examples = new ArrayList<>();

    /**
     * 扩展信息
     */
    private final List<ExtensionInfo> extensions = new ArrayList<>();

    public ActionInfo(IDocs owner, ApiInfo apiInfo, String name, String mapping, String displayName) {
        super(owner);
        if (apiInfo == null) {
            throw new NullArgumentException("apiInfo");
        }
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        if (StringUtils.isBlank(mapping)) {
            throw new NullArgumentException("mapping");
        }
        if (StringUtils.isBlank(displayName)) {
            throw new NullArgumentException("displayName");
        }
        this.apiInfo = apiInfo;
        this.name = name;
        this.mapping = mapping;
        this.displayName = displayName;
    }

    @JSONField(serialize = false)
    public ApiInfo getApiInfo() {
        return apiInfo;
    }

    public String getName() {
        return name;
    }

    public String getMapping() {
        return mapping;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public ActionInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<String> getNotes() {
        return notes;
    }

    public ActionInfo addNotes(List<String> notes) {
        if (notes != null) {
            this.notes = notes;
        }
        return this;
    }

    public ActionInfo addNotes(String notes) {
        if (StringUtils.isNotBlank(notes)) {
            this.notes.add(notes);
        }
        return this;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public ActionInfo setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public SecurityInfo getSecurity() {
        return security;
    }

    public ActionInfo setSecurity(SecurityInfo security) {
        this.security = security;
        return this;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public ActionInfo addScopes(List<String> scopes) {
        if (scopes != null) {
            scopes.forEach(this::addScope);
        }
        return this;
    }

    public ActionInfo addScope(String scope) {
        if (StringUtils.isNotBlank(scope) && apiInfo.getDocInfo().getAuthorization() != null) {
            if (!scopes.contains(scope) && !apiInfo.getScopes().contains(scope)) {
                if (!apiInfo.getScopes().contains(scope)) {
                    if (apiInfo.getDocInfo().getAuthorization().getScopeNames().contains(scope)) {
                        scopes.add(scope);
                    } else {
                        throw new IllegalArgumentException(String.format("Scope %s does not exist.", scope));
                    }
                }
            }
        }
        return this;
    }

    public String getGroup() {
        return group;
    }

    public ActionInfo setGroup(String group) {
        this.group = group;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public ActionInfo setOrder(int order) {
        this.order = order;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public ActionInfo setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    public List<String> getMethods() {
        return methods;
    }

    public ActionInfo addMethods(List<String> methods) {
        if (methods != null) {
            this.methods.addAll(methods);
        }
        return this;
    }

    public ActionInfo addMethod(String method) {
        if (StringUtils.isNotBlank(method)) {
            this.methods.add(method);
        }
        return this;
    }

    public boolean hasRequestHeader(HeaderInfo header) {
        return requestHeaders.contains(header) || apiInfo.hasRequestHeader(header);
    }

    public List<HeaderInfo> getRequestHeaders() {
        return requestHeaders;
    }

    public ActionInfo addRequestHeaders(List<HeaderInfo> requestHeaders) {
        if (requestHeaders != null) {
            requestHeaders.forEach(this::addRequestHeader);
        }
        return this;
    }

    public ActionInfo addRequestHeader(HeaderInfo requestHeader) {
        if (requestHeader != null && !hasRequestHeader(requestHeader)) {
            this.requestHeaders.add(requestHeader);
        }
        return this;
    }

    public boolean hasResponseHeader(HeaderInfo header) {
        return responseHeaders.contains(header) || apiInfo.hasResponseHeader(header);
    }

    public List<HeaderInfo> getResponseHeaders() {
        return responseHeaders;
    }

    public ActionInfo addResponseHeaders(List<HeaderInfo> responseHeaders) {
        if (responseHeaders != null) {
            responseHeaders.forEach(this::addResponseHeader);
        }
        return this;
    }

    public ActionInfo addResponseHeader(HeaderInfo responseHeader) {
        if (responseHeader != null && !hasResponseHeader(responseHeader)) {
            this.responseHeaders.add(responseHeader);
        }
        return this;
    }

    public boolean hasParam(ParamInfo param) {
        return params.contains(param) || apiInfo.hasParam(param);
    }

    public List<ParamInfo> getParams() {
        return params;
    }

    public ActionInfo addParams(List<ParamInfo> params) {
        if (params != null) {
            params.forEach(this::addParam);
        }
        return this;
    }

    public ActionInfo addParam(ParamInfo param) {
        if (param != null && !hasParam(param)) {
            this.params.add(param);
        }
        return this;
    }

    public String getRequestType() {
        return requestType;
    }

    public ActionInfo setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    public ResponseTypeInfo getResponseType() {
        return responseType;
    }

    public ActionInfo setResponseType(ResponseTypeInfo responseType) {
        this.responseType = responseType;
        return this;
    }

    public boolean hasResponse(ResponseInfo response) {
        return responses.contains(response) || apiInfo.hasResponse(response);
    }

    public List<ResponseInfo> getResponses() {
        return responses;
    }

    public ActionInfo addResponses(List<ResponseInfo> responses) {
        if (responses != null) {
            this.responses.addAll(responses);
        }
        return this;
    }

    public ActionInfo addResponse(ResponseInfo response) {
        if (response != null && !hasResponse(response)) {
            this.responses.add(response);
        }
        return this;
    }

    public List<ChangeLogInfo> getChangeLogs() {
        return changeLogs;
    }

    public ActionInfo addChangeLogs(List<ChangeLogInfo> changeLogs) {
        if (changeLogs != null) {
            changeLogs.forEach(this::addChangeLog);
        }
        return this;
    }

    public ActionInfo addChangeLog(ChangeLogInfo changeLog) {
        if (changeLog != null) {
            this.apiInfo.getDocInfo().addAuthor(changeLog.getAuthor());
            this.changeLogs.add(changeLog);
        }
        return this;
    }

    public List<ExampleInfo> getExamples() {
        return examples;
    }

    public ActionInfo addExamples(List<ExampleInfo> examples) {
        if (examples != null) {
            this.examples.addAll(examples);
        }
        return this;
    }

    public ActionInfo addExample(ExampleInfo example) {
        if (example != null) {
            this.examples.add(example);
        }
        return this;
    }

    public List<ExtensionInfo> getExtensions() {
        return extensions;
    }

    public ActionInfo addExtensions(List<ExtensionInfo> extensions) {
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
        return this;
    }

    public ActionInfo addExtension(ExtensionInfo extension) {
        if (extension != null) {
            this.extensions.add(extension);
        }
        return this;
    }

    @Override
    public String toMarkdown() {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create()
                .title(Text.create(displayName, deprecated ? Text.Style.STRIKEOUT : null), 5);
        if (StringUtils.isNotBlank(description)) {
            markdownBuilder.p().text(description, deprecated ? Text.Style.STRIKEOUT : null);
        }
        if (StringUtils.isNotBlank(group)) {
            markdownBuilder.p().title(i18nText("action.group", "Group"), 6).p().code(group);
        }
        if (!notes.isEmpty()) {
            markdownBuilder.p().title(i18nText("action.notes", "Notes"), 6).p();
            Iterator<String> notesIt = notes.iterator();
            while (notesIt.hasNext()) {
                markdownBuilder.quote(notesIt.next());
                if (notesIt.hasNext()) {
                    markdownBuilder.br();
                }
            }
        }
        if (!changeLogs.isEmpty()) {
            markdownBuilder.p().title(i18nText("action.changelog", "Changelog"), 6).p().append(ChangeLogInfo.toMarkdown(getOwner(), changeLogs));
        }
        if (!scopes.isEmpty()) {
            markdownBuilder.p().title(i18nText("action.authorization", "Authorization"), 6).p().text(i18nText("action.scopes", "Scopes: "), Text.Style.BOLD).space();
            scopes.forEach((scope) -> markdownBuilder.code(scope).space());
        }
        if (security != null) {
            String securityMarkdown = security.toMarkdown();
            if (StringUtils.isNotBlank(securityMarkdown)) {
                markdownBuilder.p().title(i18nText("action.security", "Security"), 6).append(securityMarkdown);
            }
        }
        markdownBuilder.p().title(i18nText("action.request_mapping", "Request mapping"), 6).p().text(mapping);
        if (StringUtils.isNotBlank(requestType)) {
            markdownBuilder.p().title(i18nText("action.request_type", "Request type"), 6).p().code(requestType);
        }
        if (!methods.isEmpty()) {
            markdownBuilder.p().title(i18nText("action.request_methods", "Request methods"), 6).p();
            methods.forEach((method) -> markdownBuilder.code(method.toUpperCase()).space());
        }
        if (!params.isEmpty()) {
            markdownBuilder.p().title(i18nText("action.request_parameters", "Request parameters"), 6).p().append(ParamInfo.toMarkdown(getOwner(), params));
        }
        if (!requestHeaders.isEmpty()) {
            markdownBuilder.p().title(i18nText("action.request_headers", "Request headers"), 6).p().append(HeaderInfo.toMarkdown(getOwner(), requestHeaders));
        }
        if (!responseHeaders.isEmpty()) {
            markdownBuilder.p().title(i18nText("action.response_headers", "Response headers"), 6).p().append(HeaderInfo.toMarkdown(getOwner(), responseHeaders));
        }
        if (responseType != null) {
            if (StringUtils.isNotBlank(responseType.getName()) || !responseType.getProperties().isEmpty()) {
                markdownBuilder.p().title(i18nText("action.response_type", "Response type"), 6);
                if (StringUtils.isNotBlank(responseType.getName())) {
                    markdownBuilder.p().text(responseType.getName());
                }
                if (StringUtils.isNotBlank(responseType.getDescription())) {
                    markdownBuilder.p().append(responseType.getDescription());
                }
                markdownBuilder.p().append(PropertyInfo.toMarkdownTable(getOwner(), responseType.getProperties()));
            }
        }
        if (!responses.isEmpty()) {
            responses.sort((o1, o2) -> Integer.valueOf(o2.getCode()).compareTo(Integer.valueOf(o1.getCode())));
            markdownBuilder.p().title(i18nText("action.response_codes", "Response codes"), 6).p().append(ResponseInfo.toMarkdown(getOwner(), responses));
        }
        if (!extensions.isEmpty()) {
            markdownBuilder.p().title(i18nText("action.extensions", "Extensions"), 6).p().append(ExtensionInfo.toMarkdown(extensions));
        }
        if (!examples.isEmpty()) {
            markdownBuilder.p().title(i18nText("action.examples", "Examples"), 6).p().append(ExampleInfo.toMarkdown(examples));
        }
        return markdownBuilder.p().toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
