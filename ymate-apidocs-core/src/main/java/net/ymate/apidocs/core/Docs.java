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
package net.ymate.apidocs.core;

import net.ymate.apidocs.annotation.Api;
import net.ymate.apidocs.annotation.ApiAuthorization;
import net.ymate.apidocs.annotation.ApiSecurity;
import net.ymate.apidocs.annotation.Apis;
import net.ymate.apidocs.core.base.ApiInfo;
import net.ymate.apidocs.core.base.DocsInfo;
import net.ymate.apidocs.core.handle.DocsHandler;
import net.ymate.apidocs.core.impl.DefaultDocsModuleCfg;
import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 下午 12:07
 * @version 1.0
 */
@Module
public class Docs implements IModule, IDocs {

    private static final Log _LOG = LogFactory.getLog(Docs.class);

    public static final Version VERSION = new Version(1, 0, 0, Docs.class.getPackage().getImplementationVersion(), Version.VersionType.Alpha);

    private static volatile IDocs __instance;

    private YMP __owner;

    private IDocsModuleCfg __moduleCfg;

    private boolean __inited;

    private static final Map<String, DocsInfo> __docsMap = new ConcurrentHashMap<String, DocsInfo>();

    public static IDocs get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(Docs.class);
                }
            }
        }
        return __instance;
    }

    @Override
    public String getName() {
        return IDocs.MODULE_NAME;
    }

    @Override
    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-apidocs-core-" + VERSION);
            //
            __owner = owner;
            __moduleCfg = new DefaultDocsModuleCfg(owner);
            if (!__moduleCfg.isDisabled()) {
                __owner.registerHandler(Api.class, new DocsHandler(this));
            }
            //
            __inited = true;
        }
    }

    @Override
    public boolean isInited() {
        return __inited;
    }

    @Override
    public void registerApi(Class<? extends Api> targetClass) {
        if (!__moduleCfg.isDisabled()) {
            String _packageName = targetClass.getPackage().getName();
            int _count = StringUtils.split(_packageName, '.').length;
            Apis _apis = null;
            ApiSecurity _security = null;
            ApiAuthorization _authorization = null;
            do {
                _count--;
                Package _package = Package.getPackage(_packageName);
                if (_package == null) {
                    break;
                }
                _apis = _package.getAnnotation(Apis.class);
                if (_apis == null) {
                    _packageName = StringUtils.substringBeforeLast(_packageName, ".");
                } else {
                    _security = _package.getAnnotation(ApiSecurity.class);
                    _authorization = _package.getAnnotation(ApiAuthorization.class);
                }
            } while (_apis == null && _count > 0);
            //
            if (_apis == null) {
                _apis = Docs.class.getPackage().getAnnotation(Apis.class);
                _packageName = Docs.class.getPackage().getName();
            }
            //
            String _docsId = _packageName.concat("_").concat(_apis.version());
            DocsInfo _docsInfo = __docsMap.get(_docsId);
            if (_docsInfo == null) {
                _docsInfo = DocsInfo.create(_docsId, _apis, _security, _authorization);
                __docsMap.put(_docsId, _docsInfo);
            }
            _docsInfo.addApi(ApiInfo.create(targetClass));
        }
    }

    @Override
    public Map<String, DocsInfo> getDocsMap() {
        return Collections.unmodifiableMap(__docsMap);
    }

    @Override
    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            __moduleCfg = null;
            __owner = null;
        }
    }

    @Override
    public YMP getOwner() {
        return __owner;
    }

    @Override
    public IDocsModuleCfg getModuleCfg() {
        return __moduleCfg;
    }
}
