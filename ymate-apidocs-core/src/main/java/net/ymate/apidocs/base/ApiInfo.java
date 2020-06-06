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
import net.ymate.apidocs.AbstractMarkdown;
import net.ymate.apidocs.IDocs;
import net.ymate.apidocs.annotation.*;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Text;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述一个API接口
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 16:58
 */
public class ApiInfo extends AbstractMarkdown {

    public static ApiInfo create(IDocs owner, DocInfo docInfo, String id, String name) {
        return new ApiInfo(owner, docInfo, id, name);
    }

    public static ApiInfo create(IDocs owner, DocInfo docInfo, Class<?> targetClass) {
        if (targetClass != null) {
            Api api = targetClass.getAnnotation(Api.class);
            if (api != null && !api.hidden()) {
                ApiInfo apiInfo = new ApiInfo(owner, docInfo, targetClass.getName(), api.value())
                        .setGroup(api.group())
                        .setDescription(api.description())
                        .setOrder(api.order())
                        .setDeprecated(targetClass.isAnnotationPresent(Deprecated.class))
                        .setSecurity(SecurityInfo.create(owner, targetClass.getAnnotation(ApiSecurity.class), docInfo.getSecurity()))
                        .addScopes(Arrays.asList(api.scopes()))
                        .addParams(ParamInfo.create(owner, targetClass.getAnnotation(ApiParams.class)))
                        .addParam(ParamInfo.create(owner, targetClass.getAnnotation(ApiParam.class)))
                        .addGroups(GroupInfo.create(targetClass.getAnnotation(ApiGroups.class)))
                        .addGroup(GroupInfo.create(targetClass.getAnnotation(ApiGroup.class)))
                        .addChangeLogs(ChangeLogInfo.create(targetClass.getAnnotation(ApiChangeLogs.class)))
                        .addChangeLog(ChangeLogInfo.create(targetClass.getAnnotation(ApiChangeLog.class)))
                        .addExtensions(ExtensionInfo.create(targetClass.getAnnotation(ApiExtensions.class)))
                        .addExtension(ExtensionInfo.create(targetClass.getAnnotation(ApiExtension.class)))
                        .addResponse(ResponseInfo.create(targetClass.getAnnotation(ApiResponse.class)))
                        .addRequestHeaders(HeaderInfo.create(targetClass.getAnnotation(ApiRequestHeaders.class)))
                        .addResponseHeaders(HeaderInfo.create(targetClass.getAnnotation(ApiResponseHeaders.class)));
                //
                ApiResponses apiResponses = targetClass.getAnnotation(ApiResponses.class);
                if (apiResponses != null) {
                    if (!Void.class.equals(apiResponses.type())) {
                        apiInfo.setResponseType(ResponseTypeInfo.create(apiResponses));
                    }
                    Arrays.stream(apiResponses.value()).map(ResponseInfo::create).forEachOrdered(apiInfo::addResponse);
                }
                ApiResponseTypes apiResponseTypes = targetClass.getAnnotation(ApiResponseTypes.class);
                if (apiResponseTypes != null) {
                    Arrays.stream(apiResponseTypes.value()).map(ResponseTypeInfo::create).forEachOrdered(docInfo::addResponseType);
                } else {
                    ApiResponseType apiResponseType = targetClass.getAnnotation(ApiResponseType.class);
                    if (apiResponseType != null) {
                        docInfo.addResponseType(ResponseTypeInfo.create(apiResponseType));
                    }
                }
                Arrays.stream(targetClass.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(ApiAction.class) && !Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()))
                        .map((method) -> ActionInfo.create(owner, apiInfo, method))
                        .forEachOrdered(apiInfo::addAction);
                return apiInfo;
            }
        }
        return null;
    }

    public static String toMarkdown(List<ApiInfo> apis) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!apis.isEmpty()) {
            apis.forEach(markdownBuilder::append);
        }
        return markdownBuilder.toMarkdown();
    }

    private final DocInfo docInfo;

    private final String id;

    /**
     * 接口名称
     */
    private final String name;

    /**
     * 接口所属分组名称
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
     * 描述信息
     */
    private String description;

    /**
     * 接口方法分组定义
     */
    private final List<GroupInfo> groups = new ArrayList<>();

    /**
     * 接口全局HTTP请求头信息集合
     */
    private final List<HeaderInfo> requestHeaders = new ArrayList<>();

    /**
     * 接口全局HTTP响应头信息集合
     */
    private final List<HeaderInfo> responseHeaders = new ArrayList<>();

    /**
     * 接口全局参数定义
     */
    private final List<ParamInfo> params = new ArrayList<>();

    /**
     * 接口全局响应数据类型
     */
    private ResponseTypeInfo responseType;

    /**
     * 接口全局响应信息集合
     */
    private final List<ResponseInfo> responses = new ArrayList<>();

    /**
     * 接口全局访问权限
     */
    private SecurityInfo security;

    /**
     * 接口全局授权范围集合
     */
    private final List<String> scopes = new ArrayList<>();

    /**
     * 接口方法集合
     */
    private final List<ActionInfo> actions = new ArrayList<>();

    /**
     * 分组接口方法集合
     */
    private final Map<String, List<ActionInfo>> groupActions = new ConcurrentHashMap<>();

    /**
     * 接口变更记录
     */
    private final List<ChangeLogInfo> changeLogs = new ArrayList<>();

    /**
     * 扩展信息
     */
    private final List<ExtensionInfo> extensions = new ArrayList<>();

    public ApiInfo(IDocs owner, DocInfo docInfo, String id, String name) {
        super(owner);
        if (docInfo == null) {
            throw new NullArgumentException("docInfo");
        }
        if (StringUtils.isBlank(id)) {
            throw new NullArgumentException("id");
        }
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.docInfo = docInfo;
        this.id = id;
        this.name = name;
    }

    @JSONField(serialize = false)
    public DocInfo getDocInfo() {
        return docInfo;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public ApiInfo setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApiInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

    public ApiInfo addGroups(List<GroupInfo> groups) {
        if (groups != null) {
            this.groups.addAll(groups);
        }
        return this;
    }

    public ApiInfo addGroup(GroupInfo group) {
        if (group != null) {
            this.groups.add(group);
        }
        return this;
    }

    @JSONField(serialize = false)
    public Set<String> getGroupNames() {
        Set<String> groupNames = new LinkedHashSet<>();
        for (GroupInfo group : groups) {
            groupNames.add(group.getName());
        }
        return groupNames;
    }

    public int getOrder() {
        return order;
    }

    public ApiInfo setOrder(int order) {
        this.order = order;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public ApiInfo setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    public boolean hasRequestHeader(HeaderInfo header) {
        return requestHeaders.contains(header) || docInfo.hasRequestHeader(header);
    }

    public List<HeaderInfo> getRequestHeaders() {
        return requestHeaders;
    }

    public ApiInfo addRequestHeaders(List<HeaderInfo> requestHeaders) {
        if (requestHeaders != null) {
            requestHeaders.forEach(this::addRequestHeader);
        }
        return this;
    }

    public ApiInfo addRequestHeader(HeaderInfo requestHeader) {
        if (requestHeader != null && !hasRequestHeader(requestHeader)) {
            this.requestHeaders.add(requestHeader);
        }
        return this;
    }

    public boolean hasResponseHeader(HeaderInfo header) {
        return responseHeaders.contains(header) || docInfo.hasResponseHeader(header);
    }

    public List<HeaderInfo> getResponseHeaders() {
        return responseHeaders;
    }

    public ApiInfo addResponseHeaders(List<HeaderInfo> responseHeaders) {
        if (responseHeaders != null) {
            responseHeaders.forEach(this::addResponseHeader);
        }
        return this;
    }

    public ApiInfo addResponseHeader(HeaderInfo responseHeader) {
        if (responseHeader != null && !hasResponseHeader(responseHeader)) {
            this.responseHeaders.add(responseHeader);
        }
        return this;
    }

    public boolean hasParam(ParamInfo param) {
        return params.contains(param) || docInfo.hasParam(param);
    }

    public List<ParamInfo> getParams() {
        return params;
    }

    public ApiInfo addParams(List<ParamInfo> params) {
        if (params != null) {
            params.forEach(this::addParam);
        }
        return this;
    }

    public ApiInfo addParam(ParamInfo param) {
        if (param != null && !hasParam(param)) {
            this.params.add(param);
        }
        return this;
    }

    public ResponseTypeInfo getResponseType() {
        return responseType;
    }

    public ApiInfo setResponseType(ResponseTypeInfo responseType) {
        this.responseType = responseType;
        return this;
    }

    public boolean hasResponse(ResponseInfo response) {
        return responses.contains(response);
    }

    public List<ResponseInfo> getResponses() {
        return responses;
    }

    public ApiInfo addResponses(List<ResponseInfo> responses) {
        if (responses != null) {
            responses.forEach(this::addResponse);
        }
        return this;
    }

    public ApiInfo addResponse(ResponseInfo response) {
        if (response != null && !hasResponse(response)) {
            this.responses.add(response);
        }
        return this;
    }

    public SecurityInfo getSecurity() {
        return security;
    }

    public ApiInfo setSecurity(SecurityInfo security) {
        this.security = security;
        return this;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public ApiInfo addScopes(List<String> scopes) {
        if (scopes != null) {
            scopes.forEach(this::addScope);
        }
        return this;
    }

    public ApiInfo addScope(String scope) {
        if (StringUtils.isNotBlank(scope) && docInfo.getAuthorization() != null) {
            if (!scopes.contains(scope)) {
                if (docInfo.getAuthorization().getScopeNames().contains(scope)) {
                    scopes.add(scope);
                } else {
                    throw new IllegalArgumentException(String.format("Scope %s does not exist.", scope));
                }
            }
        }
        return this;
    }

    public List<ActionInfo> getActions() {
        return actions;
    }

    public List<ActionInfo> getActions(String group) {
        if (groupActions.containsKey(group)) {
            return groupActions.get(group);
        }
        return Collections.emptyList();
    }

    public ApiInfo addActions(List<ActionInfo> actions) {
        if (actions != null) {
            actions.forEach(this::addAction);
        }
        return this;
    }

    public ApiInfo addAction(ActionInfo action) {
        if (action != null) {
            if (StringUtils.isNotBlank(action.getGroup())) {
                if (!getGroupNames().contains(action.getGroup())) {
                    throw new IllegalArgumentException(String.format("Group %s does not exist.", action.getGroup()));
                } else {
                    try {
                        List<ActionInfo> currGroupActions = ReentrantLockHelper.putIfAbsentAsync(groupActions, action.getGroup(), ArrayList::new);
                        currGroupActions.add(action);
                        currGroupActions.sort(Comparator.comparingInt(ActionInfo::getOrder));
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }
            }
            this.getDocInfo().addResponses(action.getResponses());
            this.getDocInfo().addResponseType(action.getResponseType());
            this.actions.add(action);
            this.actions.sort(Comparator.comparingInt(ActionInfo::getOrder));
        }
        return this;
    }

    public List<ChangeLogInfo> getChangeLogs() {
        return changeLogs;
    }

    public ApiInfo addChangeLogs(List<ChangeLogInfo> changeLogs) {
        if (changeLogs != null) {
            changeLogs.forEach(this::addChangeLog);
        }
        return this;
    }

    public ApiInfo addChangeLog(ChangeLogInfo changeLog) {
        if (changeLog != null) {
            this.docInfo.addAuthor(changeLog.getAuthor());
            this.changeLogs.add(changeLog);
        }
        return this;
    }

    public List<ExtensionInfo> getExtensions() {
        return extensions;
    }

    public ApiInfo addExtensions(List<ExtensionInfo> extensions) {
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
        return this;
    }

    public ApiInfo addExtension(ExtensionInfo extension) {
        if (extension != null) {
            this.extensions.add(extension);
        }
        return this;
    }

    @Override
    public String toMarkdown() {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create().title(Text.create(name, deprecated ? Text.Style.STRIKEOUT : null), 3);
        if (StringUtils.isNotBlank(description)) {
            markdownBuilder.p().text(description, deprecated ? Text.Style.STRIKEOUT : null);
        }
        if (StringUtils.isNotBlank(group)) {
            markdownBuilder.p().title(i18nText("api.group", "Group"), 4).p().code(group);
        }
        if (!changeLogs.isEmpty()) {
            markdownBuilder.p().title(i18nText("api.changelog", "Changelog"), 4).p().append(ChangeLogInfo.toMarkdown(getOwner(), changeLogs));
        }
        if (!scopes.isEmpty()) {
            markdownBuilder.p().title(i18nText("api.authorization", "Authorization"), 4).p().text(i18nText("api.scopes", "Scopes: "), Text.Style.BOLD).space();
            scopes.forEach((scope) -> markdownBuilder.code(scope).space());
        }
        if (security != null) {
            String securityMarkdown = security.toMarkdown();
            if (StringUtils.isNotBlank(securityMarkdown)) {
                markdownBuilder.p().title(i18nText("api.security", "Security"), 4).append(securityMarkdown);
            }
        }
        if (!params.isEmpty()) {
            markdownBuilder.p().title(i18nText("api.request_parameters", "Request parameters"), 4).p().append(ParamInfo.toMarkdown(getOwner(), params));
        }
        if (!requestHeaders.isEmpty()) {
            markdownBuilder.p().title(i18nText("api.request_headers", "Request headers"), 4).p().append(HeaderInfo.toMarkdown(getOwner(), requestHeaders));
        }
        if (!responseHeaders.isEmpty()) {
            markdownBuilder.p().title(i18nText("api.response_headers", "Response headers"), 4).p().append(HeaderInfo.toMarkdown(getOwner(), responseHeaders));
        }
        if (!responses.isEmpty()) {
            responses.sort((o1, o2) -> Integer.valueOf(o2.getCode()).compareTo(Integer.valueOf(o1.getCode())));
            markdownBuilder.p().title(i18nText("api.response_codes", "Response codes"), 4).p().append(ResponseInfo.toMarkdown(getOwner(), responses));
        }
        if (!extensions.isEmpty()) {
            markdownBuilder.p().title(i18nText("api.extensions", "Extensions"), 4).p().append(ExtensionInfo.toMarkdown(extensions));
        }
        if (!actions.isEmpty()) {
            markdownBuilder.p(2).title(i18nText("api.actions", "Actions"), 4).p();
            if (!groupActions.isEmpty()) {
                groups.stream().map(group -> getActions(group.getName()))
                        .filter(actionInfos -> !actionInfos.isEmpty())
                        .map(ActionInfo::toMarkdown).forEachOrdered(markdownBuilder::append);
            } else {
                markdownBuilder.append(ActionInfo.toMarkdown(actions));
            }
        }
        return markdownBuilder.p().toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
