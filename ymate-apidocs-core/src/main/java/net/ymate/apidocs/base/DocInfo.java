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
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Table;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述一个文档（包含若干API接口）
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 17:00
 */
public class DocInfo implements IMarkdown {

    public static DocInfo create(String id, String title, String version) {
        return new DocInfo(id, title, version);
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
    private final List<AuthorInfo> authors = new ArrayList<>();

    /**
     * 协议信息
     */
    private LicenseInfo license;

    /**
     * 接口访问权限信息
     */
    private SecurityInfo security;

    /**
     * 接口授权验证信息
     */
    private AuthorizationInfo authorization;

    /**
     * API接口集合
     */
    private final List<ApiInfo> apis = new ArrayList<>();

    /**
     * 分组API接口集合
     */
    private final Map<String, List<ApiInfo>> groupApis = new ConcurrentHashMap<>();

    /**
     * 接口方法分组定义
     */
    private final List<GroupInfo> groups = new ArrayList<>();

    /**
     * 响应数据类型集合
     */
    private final Map<String, ResponseTypeInfo> responseTypes = new ConcurrentHashMap<>();

    /**
     * 响应码集合
     */
    private final Map<String, ResponseInfo> responses = new ConcurrentHashMap<>();

    /**
     * 接口变更记录
     */
    private final List<ChangeLogInfo> changeLogs = new ArrayList<>();

    /**
     * 扩展信息
     */
    private final List<ExtensionInfo> extensions = new ArrayList<>();

    /**
     * 服务信息
     */
    private final List<ServerInfo> servers = new ArrayList<>();

    public DocInfo(String id, String title, String version) {
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

    public DocInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<AuthorInfo> getAuthors() {
        return authors;
    }

    public DocInfo addAuthors(List<AuthorInfo> authors) {
        if (authors != null) {
            authors.forEach(this::addAuthor);
        }
        return this;
    }

    public DocInfo addAuthor(AuthorInfo author) {
        if (author != null && !this.authors.contains(author)) {
            this.authors.add(author);
        }
        return this;
    }

    public LicenseInfo getLicense() {
        return license;
    }

    public DocInfo setLicense(LicenseInfo license) {
        this.license = license;
        return this;
    }

    public SecurityInfo getSecurity() {
        return security;
    }

    public DocInfo setSecurity(SecurityInfo security) {
        this.security = security;
        return this;
    }

    public AuthorizationInfo getAuthorization() {
        return authorization;
    }

    public DocInfo setAuthorization(AuthorizationInfo authorization) {
        this.authorization = authorization;
        return this;
    }

    public List<ApiInfo> getApis() {
        return apis;
    }

    public List<ApiInfo> getApis(String group) {
        if (groupApis.containsKey(group)) {
            return groupApis.get(group);
        }
        return Collections.emptyList();
    }

    public DocInfo addApis(List<ApiInfo> apis) {
        if (apis != null) {
            apis.forEach(this::addApi);
        }
        return this;
    }

    public DocInfo addApi(ApiInfo api) {
        if (api != null) {
            if (StringUtils.isNotBlank(api.getGroup())) {
                if (!getGroupNames().contains(api.getGroup())) {
                    throw new IllegalArgumentException(String.format("Group %s does not exist.", api.getGroup()));
                } else {
                    try {
                        List<ApiInfo> currGroupApis = ReentrantLockHelper.putIfAbsentAsync(groupApis, api.getGroup(), ArrayList::new);
                        currGroupApis.add(api);
                        currGroupApis.sort(Comparator.comparingInt(ApiInfo::getOrder));
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }
            }
            this.addResponses(api.getResponses());
            this.addResponseType(api.getResponseType());
            this.apis.add(api);
            this.apis.sort(Comparator.comparingInt(ApiInfo::getOrder));
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

    public List<GroupInfo> getGroups() {
        return groups;
    }

    public DocInfo addGroups(List<GroupInfo> groups) {
        if (groups != null) {
            this.groups.addAll(groups);
        }
        return this;
    }

    public DocInfo addGroup(GroupInfo group) {
        if (group != null) {
            this.groups.add(group);
        }
        return this;
    }

    @JSONField(serialize = false)
    public Map<String, ResponseTypeInfo> getResponseTypes() {
        return responseTypes;
    }

    public DocInfo addResponseTypes(List<ResponseTypeInfo> responseTypes) {
        if (responseTypes != null) {
            responseTypes.forEach(this::addResponseType);
        }
        return this;
    }

    public DocInfo addResponseType(ResponseTypeInfo responseType) {
        if (responseType != null && StringUtils.isNotBlank(responseType.getName()) && !responseType.getProperties().isEmpty()) {
            if (!this.responseTypes.containsKey(responseType.getName())) {
                this.responseTypes.put(responseType.getName(), responseType);
            }
        }
        return this;
    }

    @JSONField(serialize = false)
    public Map<String, ResponseInfo> getResponses() {
        return responses;
    }

    public DocInfo addResponses(List<ResponseInfo> responses) {
        if (responses != null) {
            responses.forEach(this::addResponse);
        }
        return this;
    }

    public DocInfo addResponse(ResponseInfo response) {
        if (response != null) {
            if (!this.responses.containsKey(response.getCode())) {
                this.responses.put(response.getCode(), response);
            }
        }
        return this;
    }

    public List<ChangeLogInfo> getChangeLogs() {
        return changeLogs;
    }

    public DocInfo addChangeLogs(List<ChangeLogInfo> changeLogs) {
        if (changeLogs != null) {
            this.changeLogs.addAll(changeLogs);
        }
        return this;
    }

    public DocInfo addChangeLog(ChangeLogInfo changeLogs) {
        if (changeLogs != null) {
            this.changeLogs.add(changeLogs);
        }
        return this;
    }

    public List<ExtensionInfo> getExtensions() {
        return extensions;
    }

    public DocInfo addExtensions(List<ExtensionInfo> extensions) {
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
        return this;
    }

    public DocInfo addExtension(ExtensionInfo extension) {
        if (extension != null) {
            this.extensions.add(extension);
        }
        return this;
    }

    public List<ServerInfo> getServers() {
        return servers;
    }

    public DocInfo addServers(List<ServerInfo> servers) {
        if (servers != null) {
            this.servers.addAll(servers);
        }
        return this;
    }

    public DocInfo addServer(ServerInfo server) {
        if (server != null) {
            this.servers.add(server);
        }
        return this;
    }

    @Override
    public String toMarkdown() {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create().title(title).p();
        if (StringUtils.isNotBlank(description)) {
            markdownBuilder.text(description).p();
        }
        markdownBuilder.append("[TOC]").p();
        Table table = Table.create().addHeader("Version").addHeader(version);
        if (license != null) {
            table.addRow().addColumn("License").addColumn(license);
        }
        if (!authors.isEmpty()) {
            table.addRow().addColumn("Authors").addColumn(AuthorInfo.toMarkdown(authors));
        }
        markdownBuilder.p(2).title("Overview", 2).p().append(table);
        if (!servers.isEmpty()) {
            markdownBuilder.p().title("Servers", 3).p().append(ServerInfo.toMarkdown(servers));
        }
        if (authorization != null) {
            markdownBuilder.p().title("Authorization", 3).p().append(authorization);
        }
        if (security != null) {
            String securityMarkdown = security.toMarkdown();
            if (StringUtils.isNotBlank(securityMarkdown)) {
                markdownBuilder.p().title("Security", 3).append(securityMarkdown);
            }
        }
        if (!changeLogs.isEmpty()) {
            markdownBuilder.p().title("Changelog", 3).p().append(ChangeLogInfo.toMarkdown(changeLogs));
        }
        if (!extensions.isEmpty()) {
            markdownBuilder.p().title("Extensions", 3).p().append(ExtensionInfo.toMarkdown(extensions));
        }
        if (!apis.isEmpty()) {
            markdownBuilder.p(2).title("Apis", 2).p();
            if (!groupApis.isEmpty()) {
                groups.stream().map(group -> getApis(group.getName()))
                        .filter(apiInfos -> !apiInfos.isEmpty())
                        .map(ApiInfo::toMarkdown).forEachOrdered(markdownBuilder::append);
            } else {
                markdownBuilder.append(ApiInfo.toMarkdown(apis));
            }
        }
        if (!responses.isEmpty() || !responseTypes.isEmpty()) {
            markdownBuilder.p(2).title("Appendix", 2).p();
            if (!responseTypes.isEmpty()) {
                markdownBuilder.title("Response types", 3).p();
                for (ResponseTypeInfo responseType : responseTypes.values()) {
                    markdownBuilder.title(responseType.getName(), 4).p();
                    if (StringUtils.isNotBlank(responseType.getDescription())) {
                        markdownBuilder.append(responseType.getDescription()).p();
                    }
                    markdownBuilder.append(PropertyInfo.toMarkdownTable(responseType.getProperties())).p();
                }
            }
            if (!responses.isEmpty()) {
                markdownBuilder.title("Response codes", 3).p();
                List<ResponseInfo> sorted = new ArrayList<>(responses.values());
                sorted.sort(Comparator.comparing(ResponseInfo::getCode));
                markdownBuilder.append(ResponseInfo.toMarkdown(sorted));
            }
        }
        markdownBuilder.p(5).hr()
                .quote("This document is generated based on the `YMP-ApiDocs` module. Please visit [https://ymate.net/](https://ymate.net/) for more information.").br()
                .quote("此文档基于 `YMP ApiDocs` 模块生成，请访问 [https://ymate.net/](https://ymate.net/) 了解更多信息。");
        return markdownBuilder.toMarkdown();
    }
}