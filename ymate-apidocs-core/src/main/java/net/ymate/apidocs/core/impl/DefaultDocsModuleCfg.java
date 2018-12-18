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
package net.ymate.apidocs.core.impl;

import net.ymate.apidocs.core.IDocs;
import net.ymate.apidocs.core.IDocsModuleCfg;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;

import java.util.Collections;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 下午 12:07
 * @version 1.0
 */
public class DefaultDocsModuleCfg implements IDocsModuleCfg {

    private String __title;

    private String __brand;

    private String __description;

    private boolean __disabled;

    private Map<String, String> __params;

    public DefaultDocsModuleCfg(YMP owner) {
        IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(IDocs.MODULE_NAME));
        //
        __title = _moduleCfg.getString("title", "ApiDocs");
        __brand = _moduleCfg.getString("brand", __title);
        __description = _moduleCfg.getString("description", "A simple development tool for document generation.");
        __disabled = _moduleCfg.getBoolean("disabled");
        //
        __params = _moduleCfg.getMap("params.");
    }

    @Override
    public String getTitle() {
        return __title;
    }

    @Override
    public String getBrand() {
        return __brand;
    }

    @Override
    public String getDescription() {
        return __description;
    }

    @Override
    public boolean isDisabled() {
        return __disabled;
    }

    @Override
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(__params);
    }
}