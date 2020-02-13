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

/**
 * 描述一个权限
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/09 22:39
 */
public class PermissionInfo extends AbstractBaseInfo<PermissionInfo> {

    public static PermissionInfo create(String name) {
        return new PermissionInfo(name);
    }

    public PermissionInfo(String name) {
        super(name);
    }
}
