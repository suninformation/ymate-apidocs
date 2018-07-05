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
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述一个文档（包含若干API接口）
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/15 下午5:00
 * @version 1.0
 */
public class DocsInfo implements Serializable {

    public static DocsInfo create(String id, String title, String version) {
        return new DocsInfo(id, title, version);
    }

    public static DocsInfo create(String id, Apis apis, ApiSecurity security, ApiAuthorization authorization) {
        DocsInfo _docsInfo = new DocsInfo(id, apis.title(), apis.version())
                .setDescription(apis.description())
                .setContact(AuthorInfo.create(apis.contact()))
                .setLicense(LicenseInfo.create(apis.license()));
        if (security != null) {
            _docsInfo.setSecurity(SecurityInfo.create(security));
        }
        if (authorization != null) {
            _docsInfo.setAuthType(authorization.type());
            for (ApiScope _scope : authorization.scopes()) {
                _docsInfo.addScope(ScopeInfo.create(_scope.name(), _scope.description()));
            }
        }
        for (ApiGroup _group : apis.groups()) {
            _docsInfo.addGroup(GroupInfo.create(_group.name()).setDescription(_group.description()));
        }
        for (ApiChangelog _changelog : apis.changelog()) {
            _docsInfo.addChangelog(ChangelogInfo.create(_changelog));
        }
        for (ApiExtension _extension : apis.extensions()) {
            _docsInfo.addExtensions(ExtensionInfo.create(_extension));
        }
        return _docsInfo;
    }

    private String id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 版本信息
     */
    private String version;

    /**
     * 文档描述
     */
    private String description;

    /**
     * 作者信息
     */
    private AuthorInfo contact;

    /**
     * 协议信息
     */
    private LicenseInfo license;

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
     * API接口信息
     */
    private List<ApiInfo> apis;

    /**
     * 接口方法分组定义
     */
    private List<GroupInfo> groups;

    /**
     * 接口变更记录
     */
    private List<ChangelogInfo> changelogs;

    /**
     * 扩展信息
     */
    private List<ExtensionInfo> extensions;

    public DocsInfo(String id, String title, String version) {
        if (StringUtils.isBlank(id)) {
            throw new NullArgumentException("id");
        }
        if (StringUtils.isBlank(title)) {
            throw new NullArgumentException("title");
        }
        if (StringUtils.isBlank(version)) {
            throw new NullArgumentException("version");
        }
        this.id = id;
        this.title = title;
        this.version = version;
        this.scopes = new ArrayList<ScopeInfo>();
        this.apis = new ArrayList<ApiInfo>();
        this.groups = new ArrayList<GroupInfo>();
        this.changelogs = new ArrayList<ChangelogInfo>();
        this.extensions = new ArrayList<ExtensionInfo>();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public DocsInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public AuthorInfo getContact() {
        return contact;
    }

    public DocsInfo setContact(AuthorInfo contact) {
        this.contact = contact;
        return this;
    }

    public LicenseInfo getLicense() {
        return license;
    }

    public DocsInfo setLicense(LicenseInfo license) {
        this.license = license;
        return this;
    }

    public SecurityInfo getSecurity() {
        return security;
    }

    public DocsInfo setSecurity(SecurityInfo security) {
        this.security = security;
        return this;
    }

    public String getAuthType() {
        return authType;
    }

    public DocsInfo setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public List<ScopeInfo> getScopes() {
        return scopes;
    }

    public DocsInfo setScopes(List<ScopeInfo> scopes) {
        if (scopes != null) {
            this.scopes.addAll(scopes);
        }
        return this;
    }

    public DocsInfo addScope(ScopeInfo scope) {
        if (scope != null) {
            this.scopes.add(scope);
        }
        return this;
    }

    public DocsInfo addScope(String name, String description) {
        return addScope(ScopeInfo.create(name, description));
    }

    public List<ApiInfo> getApis() {
        return apis;
    }

    public DocsInfo setApis(List<ApiInfo> apis) {
        if (apis != null) {
            this.apis.addAll(apis);
        }
        this.apis = apis;
        return this;
    }

    public DocsInfo addApi(ApiInfo api) {
        if (api != null) {
            this.apis.add(api);
        }
        return this;
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

    public DocsInfo setGroups(List<GroupInfo> groups) {
        if (groups != null) {
            this.groups.addAll(groups);
        }
        return this;
    }

    public DocsInfo addGroup(GroupInfo group) {
        if (group != null) {
            this.groups.add(group);
        }
        return this;
    }

    public List<ChangelogInfo> getChangelogs() {
        return changelogs;
    }

    public DocsInfo setChangelogs(List<ChangelogInfo> changelogs) {
        if (changelogs != null) {
            this.changelogs.addAll(changelogs);
        }
        return this;
    }

    public DocsInfo addChangelog(ChangelogInfo changelog) {
        if (changelog != null) {
            this.changelogs.add(changelog);
        }
        return this;
    }

    public List<ExtensionInfo> getExtensions() {
        return extensions;
    }

    public DocsInfo setExtensions(List<ExtensionInfo> extensions) {
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
        return this;
    }

    public DocsInfo addExtensions(ExtensionInfo extension) {
        if (extension != null) {
            this.extensions.add(extension);
        }
        return this;
    }
}
