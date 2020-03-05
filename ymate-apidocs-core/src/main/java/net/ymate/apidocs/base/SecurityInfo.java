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
import net.ymate.apidocs.annotation.ApiSecurity;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 描述一个接口访问权限
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/09 22:21
 */
public class SecurityInfo extends AbstractMarkdown {

    public static SecurityInfo create(IDocs owner) {
        return new SecurityInfo(owner);
    }

    public static SecurityInfo create(IDocs owner, ApiSecurity security, SecurityInfo parent) {
        if (security != null) {
            SecurityInfo securityInfo = new SecurityInfo(owner, parent)
                    .setDescription(security.description())
                    .setLogicalType(security.logicalType());
            Arrays.stream(security.roles()).map(apiRole -> RoleInfo.create(apiRole.value()).setDescription(apiRole.description())).forEachOrdered(securityInfo::addRole);
            Arrays.stream(security.value()).map(apiPermission -> PermissionInfo.create(apiPermission.value()).setDescription(apiPermission.description())).forEachOrdered(securityInfo::addPermission);
            return securityInfo;
        }
        return null;
    }

    private SecurityInfo parent;

    /**
     * 角色集合
     */
    private final List<RoleInfo> roles = new ArrayList<>();

    /**
     * 权限码集合
     */
    private final List<PermissionInfo> permissions = new ArrayList<>();

    /**
     * 逻辑类型
     */
    private ApiSecurity.LogicalType logicalType;

    /**
     * 描述
     */
    private String description;

    public SecurityInfo(IDocs owner) {
        super(owner);
    }

    public SecurityInfo(IDocs owner, SecurityInfo parent) {
        this(owner);
        this.parent = parent;
    }

    @JSONField(serialize = false)
    public SecurityInfo getParent() {
        return parent;
    }

    public List<RoleInfo> getRoles() {
        return roles;
    }

    public boolean hasRole(RoleInfo role) {
        if (this.roles.contains(role)) {
            return true;
        } else if (parent != null) {
            return parent.hasRole(role);
        }
        return false;
    }

    public SecurityInfo addRoles(List<RoleInfo> roles) {
        if (roles != null) {
            roles.forEach(this::addRole);
        }
        return this;
    }

    public SecurityInfo addRole(RoleInfo role) {
        if (role != null && !hasRole(role)) {
            this.roles.add(role);
        }
        return this;
    }

    public SecurityInfo addRole(String name, String description) {
        return addRole(RoleInfo.create(name).setDescription(description));
    }

    public List<PermissionInfo> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(PermissionInfo permission) {
        if (this.permissions.contains(permission)) {
            return true;
        } else if (parent != null) {
            return parent.hasPermission(permission);
        }
        return false;
    }

    public SecurityInfo setPermissions(List<PermissionInfo> permissions) {
        if (permissions != null) {
            permissions.forEach(this::addPermission);
        }
        return this;
    }

    public SecurityInfo addPermission(PermissionInfo permission) {
        if (permission != null && !hasPermission(permission)) {
            this.permissions.add(permission);
        }
        return this;
    }

    public SecurityInfo addPermission(String name, String description) {
        return addPermission(PermissionInfo.create(name).setDescription(description));
    }

    public ApiSecurity.LogicalType getLogicalType() {
        return logicalType;
    }

    public SecurityInfo setLogicalType(ApiSecurity.LogicalType logicalType) {
        this.logicalType = logicalType;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SecurityInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toMarkdown() {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (StringUtils.isNotBlank(description) || !roles.isEmpty() || !permissions.isEmpty()) {
            if (StringUtils.isNotBlank(description)) {
                markdownBuilder.p().append(description);
            }
            if (ApiSecurity.LogicalType.AND.equals(logicalType)) {
                markdownBuilder.p().append(i18nText("security.logical_type", "Logical type: ")).append("AND");
            }
            if (!roles.isEmpty()) {
                markdownBuilder.p().text(i18nText("security.roles", "Roles: "), Text.Style.BOLD).p().append(RoleInfo.toMarkdown(getOwner(), roles));
            }
            if (!permissions.isEmpty()) {
                markdownBuilder.p().text(i18nText("security.permissions", "Permissions: "), Text.Style.BOLD).p().append(PermissionInfo.toMarkdown(getOwner(), permissions));
            }
        }
        return markdownBuilder.toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
