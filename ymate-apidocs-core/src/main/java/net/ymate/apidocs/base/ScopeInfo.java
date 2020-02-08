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
 * 描述一个授权范围
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/09 22:42
 */
public class ScopeInfo extends AbstractBaseInfo<ScopeInfo> {

    public static ScopeInfo create(String name) {
        return new ScopeInfo(name);
    }

    public static ScopeInfo create(String name, String description) {
        return new ScopeInfo(name).setDescription(description);
    }

    public ScopeInfo(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return String.format("ScopeInfo{name='%s', description='%s'}", getName(), getDescription());
    }
}
