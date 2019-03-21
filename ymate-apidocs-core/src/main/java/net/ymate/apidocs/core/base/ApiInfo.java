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

import net.ymate.apidocs.annotation.*;
import net.ymate.apidocs.core.IMarkdown;
import net.ymate.platform.core.i18n.I18N;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 描述一个API接口
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/15 下午4:58
 * @version 1.0
 */
public class ApiInfo implements IMarkdown, Serializable {

    public static ApiInfo create(Class<?> clazz, String name) {
        return new ApiInfo(clazz, name);
    }

    public static ApiInfo create(Class<? extends Api> targetClass) {
        if (targetClass != null) {
            Api _api = targetClass.getAnnotation(Api.class);
            if (!_api.hidden()) {
                ApiInfo _apiInfo = new ApiInfo(targetClass, _api.value())
                        .setGroup(_api.group())
                        .setDescription(_api.description());
                for (ApiGroup _group : _api.groups()) {
                    _apiInfo.addGroup(GroupInfo.create(_group.name()).setDescription(_group.description()));
                }
                for (ApiParam _param : _api.params()) {
                    _apiInfo.addParam(ParamInfo.create(_param));
                }
                for (ApiChangelog _changelog : _api.changelog()) {
                    _apiInfo.addChangelog(ChangelogInfo.create(_changelog));
                }
                if (targetClass.isAnnotationPresent(ApiSecurity.class)) {
                    _apiInfo.setSecurity(SecurityInfo.create(targetClass.getAnnotation(ApiSecurity.class)));
                }
                if (targetClass.isAnnotationPresent(ApiAuthorization.class)) {
                    ApiAuthorization _authorization = targetClass.getAnnotation(ApiAuthorization.class);
                    _apiInfo.setAuthType(_authorization.type());
                    for (ApiScope _scope : _authorization.scopes()) {
                        _apiInfo.addScope(ScopeInfo.create(_scope.name(), _scope.description()));
                    }
                }
                if (targetClass.isAnnotationPresent(ApiResponses.class)) {
                    ApiResponses _responses = targetClass.getAnnotation(ApiResponses.class);
                    if (!Void.class.equals(_responses.type())) {
                        _apiInfo.setResponseType(ResponseTypeInfo.create(_responses));
                    }
                    for (ApiResponse _response : _responses.value()) {
                        _apiInfo.addResponse(ResponseInfo.create(_response));
                    }
                }
                for (Method _method : targetClass.getDeclaredMethods()) {
                    if (_method.isAnnotationPresent(ApiAction.class)) {
                        _apiInfo.addAction(ActionInfo.create(_method));
                    }
                }
                return _apiInfo;
            }
        }
        return null;
    }

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
     * 描述信息
     */
    private String description;

    private String linkUrl;

    /**
     * 接口方法分组定义
     */
    private List<GroupInfo> groups;

    /**
     * 接口全局参数定义
     */
    private List<ParamInfo> params;

    /**
     * 接口全局响应数据类型
     */
    private ResponseTypeInfo responseType;

    /**
     * 接口全局响应信息集合
     */
    private List<ResponseInfo> responses;

    /**
     * 接口全局访问权限
     */
    private SecurityInfo security;

    /**
     * 授权类型，如：OAuth2等
     */
    private String authType;

    /**
     * 授权范围
     */
    private List<ScopeInfo> scopes;

    /**
     * 接口方法集合
     */
    private List<ActionInfo> actions;

    /**
     * 接口变更记录
     */
    private List<ChangelogInfo> changelogs;

    public ApiInfo(Class<?> clazz, String name) {
        if (clazz == null) {
            throw new NullArgumentException("clazz");
        }
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.id = clazz.getName();
        this.name = name;
        this.groups = new ArrayList<GroupInfo>();
        this.params = new ArrayList<ParamInfo>();
        this.responses = new ArrayList<ResponseInfo>();
        this.scopes = new ArrayList<ScopeInfo>();
        this.actions = new ArrayList<ActionInfo>();
        this.changelogs = new ArrayList<ChangelogInfo>();
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

    public String getLinkUrl() {
        return StringUtils.replace(StringUtils.defaultIfBlank(linkUrl, name), " ", "-");
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

    public ApiInfo setGroups(List<GroupInfo> groups) {
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

    public List<ParamInfo> getParams() {
        return params;
    }

    public ApiInfo setParams(List<ParamInfo> params) {
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

    public ApiInfo setResponses(List<ResponseInfo> responses) {
        if (responses != null) {
            this.responses = responses;
        }
        return this;
    }

    public ApiInfo addResponse(ResponseInfo response) {
        if (response != null) {
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

    public List<ScopeInfo> getScopes() {
        return scopes;
    }

    public ApiInfo setScopes(List<ScopeInfo> scopes) {
        if (scopes != null) {
            this.scopes.addAll(scopes);
        }
        return this;
    }

    public ApiInfo addScope(ScopeInfo scope) {
        if (scope != null) {
            this.scopes.add(scope);
        }
        return this;
    }

    public ApiInfo addScope(String name, String description) {
        return addScope(ScopeInfo.create(name, description));
    }

    public String getAuthType() {
        return authType;
    }

    public ApiInfo setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public List<ActionInfo> getActions() {
        Collections.sort(actions, new Comparator<ActionInfo>() {
            @Override
            public int compare(ActionInfo o1, ActionInfo o2) {
                return StringUtils.trimToEmpty(o2.getName()).compareTo(StringUtils.trimToEmpty(o1.getName()));
            }
        });
        return actions;
    }

    public ApiInfo setActions(List<ActionInfo> actions) {
        if (actions != null) {
            this.actions.addAll(actions);
        }
        return this;
    }

    public ApiInfo addAction(ActionInfo action) {
        if (action != null) {
            this.actions.add(action);
        }
        return this;
    }

    public List<ChangelogInfo> getChangelogs() {
        return changelogs;
    }

    public ApiInfo setChangelogs(List<ChangelogInfo> changelogs) {
        if (changelogs != null) {
            this.changelogs.addAll(changelogs);
        }
        return this;
    }

    public ApiInfo addChangelog(ChangelogInfo changelog) {
        if (changelog != null) {
            this.changelogs.add(changelog);
        }
        return this;
    }

    @Override
    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        md.append("### ").append(name).append("\n\n");
        md.append("> _").append(id).append("_\n\n");
        if (!changelogs.isEmpty()) {
            md.append("\n#### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.changelog", "Changelog")).append("\n\n");
            for (ChangelogInfo changelog : changelogs) {
                md.append(changelog.toMarkdown()).append("\n");
            }
        }
        if (StringUtils.isNotBlank(authType)) {
            md.append("\n#### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.global_authorization_type", "Global authorization type")).append("\n\n");
            md.append(authType).append("\n");
            if (!scopes.isEmpty()) {
                md.append("\n|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.authorization_scope", "Authorization scope")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_description", "Description")).append("|\n");
                md.append("|---|---|\n");
                for (ScopeInfo scope : scopes) {
                    md.append(scope.toMarkdown()).append("\n");
                }
            }
        }
        if (security != null) {
            md.append("\n#### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.global_security", "Global security")).append("\n\n");
            md.append("\n").append(security.toMarkdown()).append("\n");
        }
        if (!params.isEmpty()) {
            md.append("\n#### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.global_parameters", "Global parameters")).append("\n\n");
            //
            StringBuilder _tHeader = new StringBuilder();
            _tHeader.append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_param_name", "Parameter name")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_type", "Type")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_default_value", "Default")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_description", "Description")).append("|\n");
            _tHeader.append("|---|---|---|---|\n");
            //
            boolean _flag = false;
            md.append(_tHeader);
            for (ParamInfo param : params) {
                if (_flag) {
                    md.append(_tHeader);
                    _flag = false;
                }
                md.append(param.toMarkdown()).append("\n");
                if (!param.getExamples().isEmpty()) {
                    for (ExampleInfo _exam : param.getExamples()) {
                        md.append(_exam.toMarkdown()).append("\n");
                    }
                    _flag = true;
                }
            }
        }
        if (responseType != null) {
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.global_response_parameters", "Global response parameters")).append("\n\n");
            md.append(responseType.toMarkdown()).append("\n");
        }
        if (!responses.isEmpty()) {
            md.append("\n#### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.global_responses", "Global responses")).append("\n\n");
            md.append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_response_code", "Code")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_description", "Description")).append("|\n");
            md.append("|---|---|\n");
            for (ResponseInfo response : responses) {
                md.append(response.toMarkdown()).append("\n");
            }
        }
        if (!actions.isEmpty()) {
            md.append("\n");
            for (ActionInfo action : getActions()) {
                md.append(action.toMarkdown()).append("\n");
            }
        }
        return md.toString();
    }
}
