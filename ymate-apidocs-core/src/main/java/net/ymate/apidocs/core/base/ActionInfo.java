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
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 描述一个API接口方法
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/8 下午4:49
 * @version 1.0
 */
public class ActionInfo implements IMarkdown, Serializable {

    public static ActionInfo create(String name, String mapping, String description) {
        return new ActionInfo(name, mapping, description);
    }

    private static ParamInfo __parseParamInfo(ApiParam param, String paramName, String paramType, boolean isArray) {
        if (!param.hidden()) {
            String _paramName = StringUtils.defaultIfBlank(param.name(), paramName);
            if (StringUtils.isBlank(_paramName)) {
                throw new NullArgumentException("paramName");
            }
            String _paramType = StringUtils.defaultIfBlank(param.type(), paramType);
            if (StringUtils.isBlank(_paramType)) {
                throw new NullArgumentException("paramType");
            }
            ParamInfo _paramInfo = new ParamInfo(_paramName)
                    .setDescription(param.value())
                    .setDefaultValue(param.defaultValue())
                    .setAllowValues(param.allowValues())
                    .setModel(param.model())
                    .setMultiple(param.multiple() || isArray)
                    .setRequired(param.required())
                    .setType(_paramType);
            for (ApiExample example : param.examples()) {
                _paramInfo.addExample(ExampleInfo.create(example));
            }
            return _paramInfo;
        }
        return null;
    }

    public static ActionInfo create(Method method) {
        if (method != null) {
            ApiAction _action = method.getAnnotation(ApiAction.class);
            if (_action != null && !_action.hidden()) {
                ActionInfo _actionInfo = new ActionInfo(method.getName(), _action.mapping(), _action.value())
                        .setDescription(_action.description())
                        .setNotes(_action.notes())
                        .setGroups(Arrays.asList(_action.groups()))
                        .setHttpStatus(_action.httpStatus())
                        .setMethods(Arrays.asList(_action.httpMethod()));
                //
                String[] _paramNames = ClassUtils.getMethodParamNames(method);
                if (!ArrayUtils.isEmpty(_paramNames)) {
                    Class<?>[] _paramTypes = method.getParameterTypes();
                    Annotation[][] _paramAnnotations = method.getParameterAnnotations();
                    for (int _idx = 0; _idx < _paramNames.length; _idx++) {
                        String _paramName = _paramNames[_idx];
                        for (Annotation _anno : _paramAnnotations[_idx]) {
                            if (_anno instanceof ApiParam) {
                                if (((ApiParam) _anno).model()) {
                                    for (Field _field : ClassUtils.wrapper(_paramTypes[_idx]).getFields()) {
                                        ApiParam _fieldApiParam = _field.getAnnotation(ApiParam.class);
                                        if (_fieldApiParam != null) {
                                            ParamInfo _paramInfo = __parseParamInfo(_fieldApiParam, _field.getName(), _field.getType().getSimpleName(), _field.getType().isArray());
                                            if (_paramInfo != null) {
                                                _actionInfo.addParam(_paramInfo);
                                            }
                                        }
                                    }
                                } else {
                                    ParamInfo _paramInfo = __parseParamInfo((ApiParam) _anno, _paramName, _paramTypes[_idx].getSimpleName(), _paramTypes[_idx].isArray());
                                    if (_paramInfo != null) {
                                        _actionInfo.addParam(_paramInfo);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                //
                for (ApiParam _param : _action.params()) {
                    ParamInfo _paramInfo = __parseParamInfo(_param, null, null, false);
                    if (_paramInfo != null) {
                        _actionInfo.addParam(_paramInfo);
                    }
                }
                //
                for (ApiHeader _header : _action.headers()) {
                    _actionInfo.addHeader(HeaderInfo.create(_header));
                }
                for (ApiChangelog _changelog : _action.changelog()) {
                    _actionInfo.addChangelog(ChangelogInfo.create(_changelog));
                }
                for (ApiExtension _extension : _action.extensions()) {
                    _actionInfo.addExtensions(ExtensionInfo.create(_extension));
                }
                for (ApiExample example : _action.examples()) {
                    _actionInfo.addExample(ExampleInfo.create(example));
                }
                //
                if (method.isAnnotationPresent(ApiSecurity.class)) {
                    _actionInfo.setSecurity(SecurityInfo.create(method.getAnnotation(ApiSecurity.class)));
                }
                if (method.isAnnotationPresent(ApiAuthorization.class)) {
                    ApiAuthorization _authorization = method.getAnnotation(ApiAuthorization.class);
                    _actionInfo.setAuthType(_authorization.type());
                    for (ApiScope _scope : _authorization.scopes()) {
                        _actionInfo.addScope(_scope.name(), _scope.description());
                    }
                }
                //
                if (method.isAnnotationPresent(ApiResponses.class)) {
                    ApiResponses _responses = method.getAnnotation(ApiResponses.class);
                    if (!Void.class.equals(_responses.type())) {
                        _actionInfo.setResponseType(ResponseTypeInfo.create(_responses));
                    }
                    for (ApiResponse _response : _responses.value()) {
                        _actionInfo.addResponse(ResponseInfo.create(_response));
                    }
                }
                //
                return _actionInfo;
            }
        }
        return null;
    }

    /**
     * 接口名称
     */
    private String name;

    /**
     * 显示名称
     */
    private String dispName;

    /**
     * 请求URL地址映射
     */
    private String mapping;

    /**
     * 接口方法描述
     */
    private String description;

    /**
     * 文档锚点URL路径名
     */
    private String linkUrl;

    /**
     * 接口方法提示内容
     */
    private String notes;

    /**
     * HTTP请求响应状态值
     */
    private int httpStatus;

    /**
     * 接口方法访问权限
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
     * 接口方法所属分组
     */
    private List<String> groups;

    /**
     * HTTP请求方法，如：GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE等
     */
    private List<String> methods;

    /**
     * HTTP请求头信息集合
     */
    private List<HeaderInfo> headers;

    /**
     * 接口方法参数定义
     */
    private List<ParamInfo> params;

    /**
     * 接口方法响应数据类型
     */
    private ResponseTypeInfo responseType;

    /**
     * 接口方法响应信息集合
     */
    private List<ResponseInfo> responses;

    /**
     * 接口方法变更记录
     */
    private List<ChangelogInfo> changelogs;

    /**
     * 接口示例
     */
    private List<ExampleInfo> examples;

    /**
     * 扩展信息
     */
    private List<ExtensionInfo> extensions;

    public ActionInfo(String name, String mapping, String dispName) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        if (StringUtils.isBlank(mapping)) {
            throw new NullArgumentException("mapping");
        }
        if (StringUtils.isBlank(dispName)) {
            throw new NullArgumentException("dispName");
        }
        this.name = name;
        this.mapping = mapping;
        this.dispName = dispName;
        this.scopes = new ArrayList<ScopeInfo>();
        this.groups = new ArrayList<String>();
        this.methods = new ArrayList<String>();
        this.headers = new ArrayList<HeaderInfo>();
        this.params = new ArrayList<ParamInfo>();
        this.responses = new ArrayList<ResponseInfo>();
        this.changelogs = new ArrayList<ChangelogInfo>();
        this.examples = new ArrayList<ExampleInfo>();
        this.extensions = new ArrayList<ExtensionInfo>();
    }

    public String getName() {
        return name;
    }

    public String getMapping() {
        return mapping;
    }

    public String getDispName() {
        return dispName;
    }

    public String getDescription() {
        return description;
    }

    public ActionInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLinkUrl() {
        return StringUtils.replace(StringUtils.defaultIfBlank(linkUrl, name), " ", "-");
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getNotes() {
        return notes;
    }

    public ActionInfo setNotes(String notes) {
        this.notes = notes;
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

    public List<ScopeInfo> getScopes() {
        return scopes;
    }

    public ActionInfo setScopes(List<ScopeInfo> scopes) {
        if (scopes != null) {
            this.scopes.addAll(scopes);
        }
        return this;
    }

    public ActionInfo addScope(ScopeInfo scope) {
        if (scope != null) {
            this.scopes.add(scope);
        }
        return this;
    }

    public ActionInfo addScope(String name, String description) {
        return addScope(ScopeInfo.create(name, description));
    }

    public String getAuthType() {
        return authType;
    }

    public ActionInfo setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public List<String> getGroups() {
        return groups;
    }

    public ActionInfo setGroups(List<String> groups) {
        if (groups != null) {
            this.groups.addAll(groups);
        }
        return this;
    }

    public ActionInfo addGroup(String group) {
        if (StringUtils.isNotBlank(group)) {
            this.groups.add(group);
        }
        return this;
    }

    public List<String> getMethods() {
        return methods;
    }

    public ActionInfo setMethods(List<String> methods) {
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

    public List<HeaderInfo> getHeaders() {
        return headers;
    }

    public ActionInfo setHeaders(List<HeaderInfo> headers) {
        if (headers != null) {
            this.headers.addAll(headers);
        }
        return this;
    }

    public ActionInfo addHeader(HeaderInfo header) {
        if (header != null) {
            this.headers.add(header);
        }
        return this;
    }

    public List<ParamInfo> getParams() {
        return params;
    }

    public ActionInfo setParams(List<ParamInfo> params) {
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

    public ActionInfo setResponses(List<ResponseInfo> responses) {
        if (responses != null) {
            this.responses.addAll(responses);
        }
        return this;
    }

    public ActionInfo addResponse(ResponseInfo response) {
        if (response != null) {
            this.responses.add(response);
        }
        return this;
    }

    public List<ChangelogInfo> getChangelogs() {
        return changelogs;
    }

    public ActionInfo setChangelogs(List<ChangelogInfo> changelogs) {
        if (changelogs != null) {
            this.changelogs.addAll(changelogs);
        }
        return this;
    }

    public ActionInfo addChangelog(ChangelogInfo changelog) {
        if (changelog != null) {
            this.changelogs.add(changelog);
        }
        return this;
    }

    public List<ExampleInfo> getExamples() {
        return examples;
    }

    public ActionInfo setExamples(List<ExampleInfo> examples) {
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

    public ActionInfo setExtensions(List<ExtensionInfo> extensions) {
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
        return this;
    }

    public ActionInfo addExtensions(ExtensionInfo extension) {
        if (extension != null) {
            this.extensions.add(extension);
        }
        return this;
    }

    @Override
    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        md.append("#### ").append(dispName).append(" (_").append(mapping).append("_)\n\n");
        md.append(description).append("\n");
        if (StringUtils.isNotBlank(notes)) {
            md.append("\n> _**Notes:**_ ").append(notes).append("\n");
        }
        if (!changelogs.isEmpty()) {
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.changelog", "Changelog")).append("\n\n");
            for (ChangelogInfo changelog : changelogs) {
                md.append(changelog.toMarkdown()).append("\n");
            }
        }
        if (StringUtils.isNotBlank(authType)) {
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.authorization_type", "Authorization type")).append("\n\n");
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
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.security", "Security")).append("\n\n");
            md.append("\n").append(security.toMarkdown()).append("\n");
        }
        if (!methods.isEmpty()) {
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.request_methods", "Request methods")).append("\n\n");
            for (String method : methods) {
                md.append("`").append(method).append("` ");
            }
            md.append("\n");
        }
        if (!headers.isEmpty()) {
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.request_headers", "Request headers")).append("\n\n");
            md.append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_header_name", "Header name")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_type", "Type")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_description", "Description")).append("|\n");
            md.append("|---|---|---|\n");
            for (HeaderInfo header : headers) {
                md.append(header.toMarkdown()).append("\n");
            }
        }
        if (!params.isEmpty()) {
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.parameters", "Parameters")).append("\n\n");
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
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.response_parameters", "Response parameters")).append("\n\n");
            md.append(responseType.toMarkdown()).append("\n");
        }
        if (!responses.isEmpty()) {
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.responses", "Responses")).append("\n\n");
            md.append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_response_code", "Code")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_description", "Description")).append("|\n");
            md.append("|---|---|\n");
            for (ResponseInfo response : responses) {
                md.append(response.toMarkdown()).append("\n");
            }
        }
        if (!extensions.isEmpty()) {
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.extensions", "Extensions")).append("\n\n");
            for (ExtensionInfo extension : extensions) {
                md.append("\n").append(extension.toMarkdown()).append("\n");
            }
        }
        if (!examples.isEmpty()) {
            md.append("\n##### ").append(I18N.formatMessage("apidocs-messages", "apidocs.content.examples", "Examples")).append("\n\n");
            for (ExampleInfo example : examples) {
                md.append("\n").append(example.toMarkdown()).append("\n");
            }
        }
        return md.toString();
    }
}
