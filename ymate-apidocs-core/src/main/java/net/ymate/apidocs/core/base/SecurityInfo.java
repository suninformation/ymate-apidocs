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

import net.ymate.apidocs.annotation.ApiPermission;
import net.ymate.apidocs.annotation.ApiRole;
import net.ymate.apidocs.annotation.ApiSecurity;
import net.ymate.apidocs.core.IMarkdown;
import net.ymate.platform.core.i18n.I18N;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述一个接口访问权限
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/9 下午10:21
 * @version 1.0
 */
public class SecurityInfo implements IMarkdown, Serializable {

    public static SecurityInfo create() {
        return new SecurityInfo();
    }

    public static SecurityInfo create(ApiSecurity security) {
        if (security != null) {
            SecurityInfo _securityInfo = new SecurityInfo()
                    .setDescription(security.description())
                    .setLogicType(security.logicType().name());
            for (ApiRole _role : security.roles()) {
                _securityInfo.addRole(RoleInfo.create(_role.name(), _role.description()));
            }
            for (ApiPermission _permission : security.value()) {
                _securityInfo.addPermission(PermissionInfo.create(_permission.name(), _permission.description()));
            }
            return _securityInfo;
        }
        return null;
    }

    /**
     * 角色集合
     */
    private List<RoleInfo> roles;

    /**
     * 权限码集合
     */
    private List<PermissionInfo> permissions;

    /**
     * 逻辑类型
     */
    private String logicType;

    /**
     * 描述
     */
    private String description;

    public SecurityInfo() {
        this.roles = new ArrayList<RoleInfo>();
        this.permissions = new ArrayList<PermissionInfo>();
    }

    public List<RoleInfo> getRoles() {
        return roles;
    }

    public SecurityInfo setRoles(List<RoleInfo> roles) {
        if (roles != null) {
            this.roles.addAll(roles);
        }
        return this;
    }

    public SecurityInfo addRole(RoleInfo role) {
        if (role != null) {
            this.roles.add(role);
        }
        return this;
    }

    public SecurityInfo addRole(String name, String description) {
        return addRole(RoleInfo.create(name, description));
    }

    public List<PermissionInfo> getPermissions() {
        return permissions;
    }

    public SecurityInfo setPermissions(List<PermissionInfo> permissions) {
        if (permissions != null) {
            this.permissions.addAll(permissions);
        }
        return this;
    }

    public SecurityInfo addPermission(PermissionInfo permission) {
        if (permission != null) {
            this.permissions.add(permission);
        }
        return this;
    }

    public SecurityInfo addPermission(String name, String description) {
        return addPermission(PermissionInfo.create(name, description));
    }

    public String getLogicType() {
        return logicType;
    }

    public SecurityInfo setLogicType(String logicType) {
        this.logicType = logicType;
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
        StringBuilder md = new StringBuilder();
        md.append(description).append("\n");
        if (!roles.isEmpty()) {
            md.append("\n");
            for (RoleInfo role : roles) {
                md.append("`").append(role.toMarkdown()).append("`").append(" ");
            }
            md.append("\n");
        }
        if (!permissions.isEmpty()) {
            md.append("\n|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.security_permissions", "Permissions")).append("|").append(I18N.formatMessage("apidocs-messages", "apidocs.content.table_field_description", "Description")).append("|\n");
            md.append("|---|---|\n");
            for (PermissionInfo permission : permissions) {
                md.append(permission.toMarkdown()).append("\n");
            }
            md.append("\n");
        }
        return md.toString();
    }
}
