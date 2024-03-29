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
import net.ymate.apidocs.annotation.ApiAuthorization;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Text;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 接口授权验证信息
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/10 18:49
 */
public class AuthorizationInfo extends AbstractMarkdown {

    public static AuthorizationInfo create(IDocs owner, String name, String url) {
        return new AuthorizationInfo(owner, name, url);
    }

    public static AuthorizationInfo create(IDocs owner, ApiAuthorization authorization) {
        if (authorization != null && StringUtils.isNotBlank(authorization.value()) && StringUtils.isNotBlank(authorization.url())) {
            AuthorizationInfo authorizationInfo = new AuthorizationInfo(owner, authorization.value(), authorization.url())
                    .setType(authorization.type())
                    .setTokenName(authorization.tokenName())
                    .setTokenStore(authorization.tokenStore().name())
                    .setRequestType(authorization.requestType())
                    .addRequestParams(ParamInfo.create(owner, authorization.requestParams()))
                    .setDescription(authorization.description());
            Arrays.stream(authorization.scopes()).map(scope -> ScopeInfo.create(scope.value(), scope.description())).forEachOrdered(authorizationInfo::addScope);
            return authorizationInfo;
        }
        return null;
    }

    /**
     * 授权类型名称
     */
    private final String name;

    /**
     * 授权服务URl地址
     */
    private final String url;

    /**
     * 授权方式
     */
    private String type;

    /**
     * 令牌名称
     */
    private String tokenName;

    /**
     * 令牌存储方式
     */
    private String tokenStore;

    /**
     * 令牌HTTP请求类型，如：GET, POST等
     */
    private String requestType;

    /**
     * 令牌请求参数集合
     */
    private final List<ParamInfo> requestParams = new ArrayList<>();

    /**
     * 描述
     */
    private String description;

    /**
     * 授权范围
     */
    private final List<ScopeInfo> scopes = new ArrayList<>();

    public AuthorizationInfo(IDocs owner, String name, String url) {
        super(owner);
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        if (StringUtils.isBlank(url)) {
            throw new NullArgumentException("url");
        }
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public AuthorizationInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getTokenName() {
        return tokenName;
    }

    public AuthorizationInfo setTokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    public String getTokenStore() {
        return tokenStore;
    }

    public AuthorizationInfo setTokenStore(String tokenStore) {
        this.tokenStore = tokenStore;
        return this;
    }

    public String getRequestType() {
        return requestType;
    }

    public AuthorizationInfo setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    public List<ParamInfo> getRequestParams() {
        return requestParams;
    }

    public AuthorizationInfo addRequestParams(List<ParamInfo> params) {
        if (params != null) {
            params.forEach(this::addRequestParam);
        }
        return this;
    }

    public AuthorizationInfo addRequestParam(ParamInfo param) {
        if (param != null) {
            if (!this.requestParams.contains(param)) {
                this.requestParams.add(param);
            }
        }
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AuthorizationInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    @JSONField(serialize = false)
    public Set<String> getScopeNames() {
        Set<String> scopeNames = new LinkedHashSet<>();
        for (ScopeInfo scope : scopes) {
            scopeNames.add(scope.getName());
        }
        return scopeNames;
    }

    public List<ScopeInfo> getScopes() {
        return scopes;
    }

    public AuthorizationInfo addScopes(List<ScopeInfo> scopes) {
        if (scopes != null) {
            scopes.forEach(this::addScope);
        }
        return this;
    }

    public AuthorizationInfo addScope(ScopeInfo scope) {
        if (scope != null) {
            if (!this.scopes.contains(scope)) {
                this.scopes.add(scope);
            }
        }
        return this;
    }

    public AuthorizationInfo addScope(String name, String description) {
        return addScope(ScopeInfo.create(name, description));
    }

    @Override
    public String toMarkdown() {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (StringUtils.isNotBlank(description)) {
            markdownBuilder.append(description).p();
        }
        markdownBuilder.append(i18nText("authorization.name", "Name: ")).text(name, Text.Style.BOLD).p().append(i18nText("authorization.url", "URL: ")).link(url, url);
        if (StringUtils.isNotBlank(type)) {
            markdownBuilder.p().append(i18nText("authorization.type", "Type: ")).text(type);
        }
        if (StringUtils.isNotBlank(tokenName)) {
            markdownBuilder.p().append(i18nText("authorization.token_name", "Token name: ")).text(tokenName);
            markdownBuilder.p().append(i18nText("authorization.token_store", "Token store: ")).text(tokenStore);
            markdownBuilder.p().append(i18nText("authorization.request_type", "Request type: ")).text(StringUtils.upperCase(requestType));
            if (!requestParams.isEmpty()) {
                markdownBuilder.p().text(i18nText("authorization.request_parameters", "Request parameters: "), Text.Style.BOLD).p().append(ParamInfo.toMarkdown(getOwner(), requestParams));
            }
        }
        if (!scopes.isEmpty()) {
            markdownBuilder.p().text(i18nText("authorization.scopes", "Scopes: "), Text.Style.BOLD).p().append(ScopeInfo.toMarkdown(getOwner(), scopes));
        }
        return markdownBuilder.toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
