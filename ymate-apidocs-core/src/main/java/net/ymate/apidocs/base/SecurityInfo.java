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
import net.ymate.module.security.annotation.LogicType;
import net.ymate.module.security.annotation.Permission;
import net.ymate.module.security.annotation.RoleType;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Text;
import net.ymate.platform.commons.util.ClassUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 描述一个接口访问权限
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/09 22:21
 */
public class SecurityInfo extends AbstractMarkdown {

    public static SecurityInfo create(IDocs owner) {
        return new SecurityInfo(owner);
    }

    public static SecurityInfo create(IDocs owner, Method method, ApiSecurity security, SecurityInfo parent) {
        SecurityInfo securityInfo = null;
        if (security != null) {
            securityInfo = new SecurityInfo(owner, parent)
                    .setDescription(security.description())
                    .setLogicalType(security.logicalType());
            Arrays.stream(security.roles()).forEachOrdered(securityInfo::addRole);
            Arrays.stream(security.value()).forEachOrdered(securityInfo::addPermission);
        }
        if (method != null) {
            securityInfo = parsePermission(owner, method, securityInfo);
        }
        return securityInfo;
    }

    private static LogicType parseLogicType(Permission permissionAnn, Permission classPermissionAnn, Permission packagePermissionAnn) {
        LogicType logicType = LogicType.AND;
        if (!LogicType.INHERIT.equals(permissionAnn.logicType())) {
            logicType = permissionAnn.logicType();
        } else if (classPermissionAnn != null && !LogicType.INHERIT.equals(classPermissionAnn.logicType())) {
            logicType = classPermissionAnn.logicType();
        } else if (packagePermissionAnn != null && !LogicType.INHERIT.equals(packagePermissionAnn.logicType())) {
            logicType = packagePermissionAnn.logicType();
        }
        return logicType;
    }

    private static SecurityInfo parsePermission(IDocs owner, Method targetMethod, SecurityInfo securityInfo) {
        Permission permissionAnn = targetMethod.getAnnotation(Permission.class);
        if (permissionAnn != null) {
            Permission packagePermissionAnn = ClassUtils.getPackageAnnotation(targetMethod.getDeclaringClass(), Permission.class);
            Permission classPermissionAnn = targetMethod.getDeclaringClass().getAnnotation(Permission.class);
            // LogicType
            LogicType logicType = parseLogicType(permissionAnn, classPermissionAnn, packagePermissionAnn);
            // RoleType
            Set<RoleType> roleTypes = new HashSet<>();
            if (ArrayUtils.contains(permissionAnn.roleTypes(), RoleType.ALL)) {
                roleTypes.add(RoleType.INHERIT);
            } else if (ArrayUtils.contains(permissionAnn.roleTypes(), RoleType.INHERIT)) {
                if (classPermissionAnn != null) {
                    if (ArrayUtils.contains(classPermissionAnn.roleTypes(), RoleType.ALL)) {
                        roleTypes.add(RoleType.INHERIT);
                    } else if (ArrayUtils.contains(classPermissionAnn.roleTypes(), RoleType.INHERIT)) {
                        if (packagePermissionAnn != null) {
                            Arrays.stream(packagePermissionAnn.roleTypes())
                                    .filter(roleType -> !RoleType.INHERIT.equals(roleType))
                                    .forEach(roleTypes::add);
                        }
                    } else {
                        roleTypes.addAll(Arrays.asList(classPermissionAnn.roleTypes()));
                    }
                } else if (packagePermissionAnn != null) {
                    if (ArrayUtils.contains(packagePermissionAnn.roleTypes(), RoleType.ALL)) {
                        roleTypes.add(RoleType.INHERIT);
                    } else {
                        Arrays.stream(packagePermissionAnn.roleTypes())
                                .filter(roleType -> !RoleType.INHERIT.equals(roleType))
                                .forEach(roleTypes::add);
                    }
                }
            } else {
                Arrays.stream(permissionAnn.roleTypes())
                        .filter(roleType -> !RoleType.INHERIT.equals(roleType))
                        .forEach(roleTypes::add);
            }
            // Permission
            Set<String> permissions = new HashSet<>(Arrays.asList(permissionAnn.value()));
            if (classPermissionAnn != null) {
                permissions.addAll(Arrays.asList(classPermissionAnn.value()));
            }
            if (packagePermissionAnn != null) {
                permissions.addAll(Arrays.asList(packagePermissionAnn.value()));
            }
            //
            SecurityInfo newSecurityInfo = securityInfo == null ? new SecurityInfo(owner) : securityInfo;
            newSecurityInfo.setLogicalType(logicType.equals(LogicType.OR) ? ApiSecurity.LogicalType.OR : ApiSecurity.LogicalType.AND);
            permissions.forEach(newSecurityInfo::addPermission);
            //
            if (roleTypes.contains(RoleType.INHERIT) || roleTypes.contains(RoleType.ALL)) {
                newSecurityInfo.addRoles(Arrays.asList(RoleType.ADMIN.name(), RoleType.OPERATOR.name(), RoleType.USER.name()));
            } else {
                roleTypes.forEach(roleType -> newSecurityInfo.addRole(roleType.name()));
            }
            return newSecurityInfo;
        }
        return null;
    }

    private SecurityInfo parent;

    /**
     * 角色集合
     */
    private final List<String> roles = new ArrayList<>();

    /**
     * 权限码集合
     */
    private final List<String> permissions = new ArrayList<>();

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

    public List<String> getRoles() {
        return roles;
    }

    public boolean hasRole(String role) {
        if (this.roles.contains(role)) {
            return true;
        } else if (parent != null) {
            return parent.hasRole(role);
        }
        return false;
    }

    public SecurityInfo addRoles(List<String> roles) {
        if (roles != null) {
            roles.forEach(this::addRole);
        }
        return this;
    }

    public SecurityInfo addRole(String role) {
        if (StringUtils.isNotBlank(role) && !hasRole(role)) {
            this.roles.add(role);
        }
        return this;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(String permission) {
        if (this.permissions.contains(permission)) {
            return true;
        } else if (parent != null) {
            return parent.hasPermission(permission);
        }
        return false;
    }

    public SecurityInfo setPermissions(List<String> permissions) {
        if (permissions != null) {
            permissions.forEach(this::addPermission);
        }
        return this;
    }

    public SecurityInfo addPermission(String permission) {
        if (StringUtils.isNotBlank(permission) && !hasPermission(permission)) {
            this.permissions.add(permission);
        }
        return this;
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
            if (!roles.isEmpty()) {
                markdownBuilder.p().text(i18nText("security.roles", "Roles: "), Text.Style.BOLD).p();
                roles.forEach((role) -> markdownBuilder.code(role.toUpperCase()).space());
            }
            if (!permissions.isEmpty()) {
                markdownBuilder.p().text(i18nText("security.permissions", "Permissions: "), Text.Style.BOLD).p();
                permissions.forEach((permission) -> markdownBuilder.code(permission.toLowerCase()).space());
                if (ApiSecurity.LogicalType.OR.equals(logicalType)) {
                    markdownBuilder.p().quote(MarkdownBuilder.create().append(i18nText("security.logical_type", "Logical type: ")).append("OR"));
                }
            }
        }
        return markdownBuilder.toMarkdown();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
