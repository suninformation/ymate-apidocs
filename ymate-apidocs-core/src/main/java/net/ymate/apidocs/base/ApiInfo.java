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
import net.ymate.apidocs.annotation.*;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.markdown.IMarkdown;
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
public class ApiInfo implements IMarkdown {

    public static ApiInfo create(DocInfo owner, String id, String name) {
        return new ApiInfo(owner, id, name);
    }

    public static ApiInfo create(DocInfo owner, Class<?> targetClass) {
        if (targetClass != null) {
            Api api = targetClass.getAnnotation(Api.class);
            if (api != null && !api.hidden()) {
                ApiInfo apiInfo = new ApiInfo(owner, targetClass.getName(), api.value())
                        .setGroup(api.group())
                        .setDescription(api.description())
                        .setOrder(api.order())
                        .setDeprecated(targetClass.isAnnotationPresent(Deprecated.class))
                        .setSecurity(SecurityInfo.create(targetClass.getAnnotation(ApiSecurity.class), owner.getSecurity()))
                        .addScopes(Arrays.asList(api.scopes()))
                        .addParams(ParamInfo.create(targetClass.getAnnotation(ApiParams.class)))
                        .addParam(ParamInfo.create(targetClass.getAnnotation(ApiParam.class)))
                        .addGroups(GroupInfo.create(targetClass.getAnnotation(ApiGroups.class)))
                        .addGroup(GroupInfo.create(targetClass.getAnnotation(ApiGroup.class)))
                        .addChangeLogs(ChangeLogInfo.create(targetClass.getAnnotation(ApiChangeLogs.class)))
                        .addChangeLog(ChangeLogInfo.create(targetClass.getAnnotation(ApiChangeLog.class)));
                //
                ApiResponses apiResponses = targetClass.getAnnotation(ApiResponses.class);
                if (apiResponses != null) {
                    if (!Void.class.equals(apiResponses.type())) {
                        apiInfo.setResponseType(ResponseTypeInfo.create(apiResponses));
                    }
                    Arrays.stream(apiResponses.value()).map(ResponseInfo::create).forEachOrdered(apiInfo::addResponse);
                }
                Arrays.stream(targetClass.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(ApiAction.class) && !Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()))
                        .map((method) -> ActionInfo.create(apiInfo, method))
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

    private DocInfo owner;

    private String id;

    /**
     * 接口名称
     */
    private String name;

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
    private List<ResponseInfo> responses = new ArrayList<>();

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

    public ApiInfo(DocInfo owner, String id, String name) {
        if (owner == null) {
            throw new NullArgumentException("owner");
        }
        if (StringUtils.isBlank(id)) {
            throw new NullArgumentException("id");
        }
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.owner = owner;
        this.id = id;
        this.name = name;
    }

    @JSONField(serialize = false)
    public DocInfo getOwner() {
        return owner;
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

    public List<ParamInfo> getParams() {
        return params;
    }

    public ApiInfo addParams(List<ParamInfo> params) {
        if (params != null) {
            this.params.addAll(params);
        }
        return this;
    }

    public ApiInfo addParam(ParamInfo param) {
        if (param != null) {
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
        if (response != null) {
            if (!this.responses.contains(response)) {
                this.responses.add(response);
            }
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
        if (StringUtils.isNotBlank(scope) && owner.getAuthorization() != null) {
            if (!scopes.contains(scope)) {
                if (owner.getAuthorization().getScopeNames().contains(scope)) {
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
            this.getOwner().addResponses(action.getResponses());
            this.getOwner().addResponseType(action.getResponseType());
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
            this.owner.addAuthor(changeLog.getAuthor());
            this.changeLogs.add(changeLog);
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
            markdownBuilder.p().title("Group", 4).p().code(group);
        }
        if (!scopes.isEmpty()) {
            markdownBuilder.p().title("Authorization", 4).p().text("Scopes:", Text.Style.BOLD).space();
            scopes.forEach((scope) -> markdownBuilder.code(scope).space());
        }
        if (security != null) {
            String securityMarkdown = security.toMarkdown();
            if (StringUtils.isNotBlank(securityMarkdown)) {
                markdownBuilder.p().title("Security", 4).append(securityMarkdown);
            }
        }
        if (!changeLogs.isEmpty()) {
            markdownBuilder.p().title("Changelog", 4).p().append(ChangeLogInfo.toMarkdown(changeLogs));
        }
        if (!actions.isEmpty()) {
            markdownBuilder.p(2).title("Actions", 4).p();
            if (!groupActions.isEmpty()) {
                groups.stream().map(group -> getActions(group.getName()))
                        .filter(actionInfos -> !actionInfos.isEmpty())
                        .map(ActionInfo::toMarkdown).forEachOrdered(markdownBuilder::append);
            } else {
                markdownBuilder.append(ActionInfo.toMarkdown(actions));
            }
        }
        return markdownBuilder.toMarkdown();
    }

    @Override
    public String toString() {
        return String.format("ApiInfo{id='%s', name='%s', group='%s', order=%d, deprecated=%s, description='%s', groups=%s, params=%s, responseType=%s, responses=%s, security=%s, scopes=%s, actions=%s, changeLogs=%s}", id, name, group, order, deprecated, description, groups, params, responseType, responses, security, scopes, actions, changeLogs);
    }
}
