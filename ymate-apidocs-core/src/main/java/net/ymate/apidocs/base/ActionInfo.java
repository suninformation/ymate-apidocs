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

import net.ymate.apidocs.annotation.*;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Text;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.base.Type;
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
public class ActionInfo implements IMarkdown {

    public static String processRequestMapping(String root, String mapping) {
        StringBuilder mappingBuilder = new StringBuilder(doCheckMappingSeparator(root));
        if (StringUtils.isNotBlank(mapping)) {
            mappingBuilder.append(doCheckMappingSeparator(mapping));
        }
        return mappingBuilder.toString();
    }

    public static ActionInfo create(ApiInfo owner, String name, String mapping, String displayName) {
        return new ActionInfo(owner, name, mapping, displayName);
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

    public static ActionInfo create(ApiInfo owner, Method method) {
        if (method != null) {
            ApiAction apiAction = method.getAnnotation(ApiAction.class);
            if (apiAction != null && !apiAction.hidden()) {
                String mapping = apiAction.mapping();
                if (StringUtils.isBlank(mapping)) {
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    if (requestMapping != null) {
                        RequestMapping rootRequestMapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
                        mapping = processRequestMapping(rootRequestMapping != null ? rootRequestMapping.value() : null, requestMapping.value());
                    } else {
                        mapping = doCheckMappingSeparator(method.getName());
                    }
                } else {
                    Api api = method.getDeclaringClass().getAnnotation(Api.class);
                    mapping = api != null ? processRequestMapping(api.mapping(), mapping) : processRequestMapping(null, mapping);
                }
                List<String> httpMethods = new ArrayList<>();
                if (ArrayUtils.isEmpty(apiAction.httpMethod())) {
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    if (requestMapping != null) {
                        Arrays.stream(requestMapping.method()).filter(httpMethod -> !httpMethods.contains(httpMethod.name())).forEachOrdered(httpMethod -> httpMethods.add(httpMethod.name()));
                    }
                    requestMapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
                    if (requestMapping != null) {
                        Arrays.stream(requestMapping.method()).filter(httpMethod -> !httpMethods.contains(httpMethod.name())).forEachOrdered(httpMethod -> httpMethods.add(httpMethod.name()));
                    }
                    if (httpMethods.isEmpty()) {
                        httpMethods.add(Type.HttpMethod.GET.name());
                    }
                } else {
                    httpMethods.addAll(Arrays.asList(apiAction.httpMethod()));
                }
                ActionInfo actionInfo = new ActionInfo(owner, method.getName(), mapping, apiAction.value())
                        .setDescription(apiAction.description())
                        .setGroup(apiAction.group())
                        .setHttpStatus(apiAction.httpStatus())
                        .setOrder(apiAction.order())
                        .setDeprecated(owner.isDeprecated() || method.isAnnotationPresent(Deprecated.class))
                        .setRequestType(apiAction.requestType())
                        .setSecurity(SecurityInfo.create(method.getAnnotation(ApiSecurity.class), owner.getSecurity()))
                        .addNotes(Arrays.asList(apiAction.notes()))
                        .addMethods(httpMethods)
                        .addScopes(Arrays.asList(apiAction.scopes()))
                        .addChangeLogs(ChangeLogInfo.create(method.getAnnotation(ApiChangeLogs.class)))
                        .addChangeLog(ChangeLogInfo.create(method.getAnnotation(ApiChangeLog.class)))
                        .addExtensions(ExtensionInfo.create(method.getAnnotation(ApiExtensions.class)))
                        .addExtension(ExtensionInfo.create(method.getAnnotation(ApiExtension.class)))
                        .addExamples(ExampleInfo.create(method.getAnnotation(ApiExamples.class)))
                        .addExample(ExampleInfo.create(method.getAnnotation(ApiExample.class)))
                        .addParams(ParamInfo.create(method.getAnnotation(ApiParams.class)))
                        .addParam(ParamInfo.create(method.getAnnotation(ApiParam.class)));
                //
                String[] paramNames = ClassUtils.getMethodParamNames(method);
                Parameter[] parameters = method.getParameters();
                for (int idx = 0; idx < parameters.length; idx++) {
                    String paramName = parameters[idx].getName();
                    if (paramNames.length > idx) {
                        paramName = paramNames[idx];
                    }
                    ParamInfo paramInfo = ParamInfo.create(parameters[idx], paramName);
                    if (paramInfo != null) {
                        if (paramInfo.isModel()) {
                            ClassUtils.wrapper(parameters[idx].getType()).getFields().forEach(field -> actionInfo.addParam(ParamInfo.create(field)));
                        } else {
                            actionInfo.addParam(paramInfo);
                        }
                    }
                }
                //
                ApiRequestHeaders apiRequestHeaders = method.getAnnotation(ApiRequestHeaders.class);
                if (apiRequestHeaders != null) {
                    actionInfo.addRequestHeaders(HeaderInfo.create(apiRequestHeaders.value()));
                }
                //
                ApiResponseHeaders apiResponseHeaders = method.getAnnotation(ApiResponseHeaders.class);
                if (apiResponseHeaders != null) {
                    actionInfo.addResponseHeaders(HeaderInfo.create(apiResponseHeaders.value()));
                }
                //
                ApiResponses apiResponses = method.getAnnotation(ApiResponses.class);
                if (apiResponses != null) {
                    actionInfo.setResponseType(ResponseTypeInfo.create(apiResponses));
                    Arrays.stream(apiResponses.value()).map(ResponseInfo::create).forEachOrdered(actionInfo::addResponse);
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

    private ApiInfo owner;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 请求URL地址映射
     */
    private String mapping;

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

    public ActionInfo(ApiInfo owner, String name, String mapping, String displayName) {
        if (owner == null) {
            throw new NullArgumentException("owner");
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
        this.owner = owner;
        this.name = name;
        this.mapping = mapping;
        this.displayName = displayName;
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
        if (StringUtils.isNotBlank(scope) && owner.getOwner().getAuthorization() != null) {
            if (!scopes.contains(scope) && !owner.getScopes().contains(scope)) {
                if (!owner.getScopes().contains(scope)) {
                    if (owner.getOwner().getAuthorization().getScopeNames().contains(scope)) {
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

    public List<HeaderInfo> getRequestHeaders() {
        return requestHeaders;
    }

    public ActionInfo addRequestHeaders(List<HeaderInfo> requestHeaders) {
        if (requestHeaders != null) {
            this.requestHeaders.addAll(requestHeaders);
        }
        return this;
    }

    public ActionInfo addRequestHeader(HeaderInfo requestHeader) {
        if (requestHeader != null) {
            this.requestHeaders.add(requestHeader);
        }
        return this;
    }

    public List<HeaderInfo> getResponseHeaders() {
        return responseHeaders;
    }

    public ActionInfo addResponseHeaders(List<HeaderInfo> responseHeaders) {
        if (responseHeaders != null) {
            this.responseHeaders.addAll(responseHeaders);
        }
        return this;
    }

    public ActionInfo addResponseHeader(HeaderInfo responseHeader) {
        if (responseHeader != null) {
            this.responseHeaders.add(responseHeader);
        }
        return this;
    }

    public List<ParamInfo> getParams() {
        return params;
    }

    public ActionInfo addParams(List<ParamInfo> params) {
        if (params != null) {
            this.params.addAll(params);
        }
        return this;
    }

    public ActionInfo addParam(ParamInfo param) {
        if (param != null) {
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
        if (response != null) {
            this.responses.add(response);
            this.responses.sort(Comparator.comparing(ResponseInfo::getCode));
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
            this.owner.getOwner().addAuthor(changeLog.getAuthor());
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
                .title(Text.create(String.format("%s %s", displayName, mapping), deprecated ? Text.Style.STRIKEOUT : null), 5);
        if (StringUtils.isNotBlank(description)) {
            markdownBuilder.p().text(description, deprecated ? Text.Style.STRIKEOUT : null);
        }
        if (StringUtils.isNotBlank(group)) {
            markdownBuilder.p().title("Group", 6).p().code(group);
        }
        if (!notes.isEmpty()) {
            markdownBuilder.p().title("Notes", 6).p();
            Iterator<String> notesIt = notes.iterator();
            while (notesIt.hasNext()) {
                markdownBuilder.quote(notesIt.next());
                if (notesIt.hasNext()) {
                    markdownBuilder.br();
                }
            }
        }
        if (!changeLogs.isEmpty()) {
            markdownBuilder.p().title("Changelog", 6).p().append(ChangeLogInfo.toMarkdown(changeLogs));
        }
        if (!scopes.isEmpty()) {
            markdownBuilder.p().title("Authorization", 6).p().text("Scopes:", Text.Style.BOLD).space();
            scopes.forEach((scope) -> markdownBuilder.code(scope).space());
        }
        if (security != null) {
            String securityMarkdown = security.toMarkdown();
            if (StringUtils.isNotBlank(securityMarkdown)) {
                markdownBuilder.p().title("Security", 6).append(securityMarkdown);
            }
        }
        if (StringUtils.isNotBlank(requestType)) {
            markdownBuilder.p().title("Request type", 6).p().code(requestType);
        }
        if (!methods.isEmpty()) {
            markdownBuilder.p().title("Request methods", 6).p();
            methods.forEach((method) -> markdownBuilder.code(method.toUpperCase()).space());
        }
        if (!requestHeaders.isEmpty()) {
            markdownBuilder.p().title("Request headers", 6).p().append(HeaderInfo.toMarkdown(requestHeaders));
        }
        if (!params.isEmpty()) {
            markdownBuilder.p().title("Request parameters", 6).p().append(ParamInfo.toMarkdown(params));
        }
        if (!responseHeaders.isEmpty()) {
            markdownBuilder.p().title("Response headers", 6).p().append(HeaderInfo.toMarkdown(responseHeaders));
        }
        if (responseType != null) {
            markdownBuilder.p().title("Response type", 6).p().text(responseType.getName());
            if (StringUtils.isNotBlank(responseType.getDescription())) {
                markdownBuilder.p().append(responseType.getDescription());
            }
            markdownBuilder.p().append(PropertyInfo.toMarkdownTable(responseType.getProperties()));
        }
        if (!responses.isEmpty()) {
            markdownBuilder.p().title("Response codes", 6).p().append(ResponseInfo.toMarkdown(responses));
        }
        if (!extensions.isEmpty()) {
            markdownBuilder.p().title("Extensions", 6).p().append(ExtensionInfo.toMarkdown(extensions));
        }
        if (!examples.isEmpty()) {
            markdownBuilder.p().title("Examples", 6).p().append(ExampleInfo.toMarkdown(examples));
        }
        return markdownBuilder.toMarkdown();
    }

    @Override
    public String toString() {
        return String.format("ActionInfo{name='%s', displayName='%s', mapping='%s', description='%s', notes=%s, httpStatus=%d, security=%s, scopes=%s, group='%s', order=%d, deprecated=%s, methods=%s, requestHeaders=%s, responseHeaders=%s, params=%s, requestType='%s', responseType=%s, responses=%s, changeLogs=%s, examples=%s, extensions=%s}", name, displayName, mapping, description, notes, httpStatus, security, scopes, group, order, deprecated, methods, requestHeaders, responseHeaders, params, requestType, responseType, responses, changeLogs, examples, extensions);
    }
}