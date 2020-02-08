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
package net.ymate.apidocs;

import net.ymate.apidocs.annotation.*;
import net.ymate.apidocs.base.*;
import net.ymate.apidocs.handle.DocsHandler;
import net.ymate.apidocs.impl.DefaultDocsConfig;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/01 00:34
 */
public class Docs implements IModule, IDocs {

    private static final Log LOG = LogFactory.getLog(Docs.class);

    private static volatile IDocs instance;

    private IApplication owner;

    private IDocsConfig config;

    private boolean initialized;

    private final Map<String, DocInfo> docInfoMap = new ConcurrentHashMap<>();

    public static IDocs get() {
        IDocs inst = instance;
        if (inst == null) {
            synchronized (Docs.class) {
                inst = instance;
                if (inst == null) {
                    instance = inst = YMP.get().getModuleManager().getModule(Docs.class);
                }
            }
        }
        return inst;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            //
            YMP.showModuleVersion("ymate-apidocs-core", this);
            //
            this.owner = owner;
            if (config == null) {
                IModuleConfigurer moduleConfigurer = owner.getConfigurer().getModuleConfigurer(MODULE_NAME);
                config = moduleConfigurer == null ? DefaultDocsConfig.defaultConfig() : DefaultDocsConfig.create(moduleConfigurer);
            }
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            if (config.isEnabled()) {
                IBeanLoadFactory beanLoaderFactory = YMP.getBeanLoadFactory();
                if (beanLoaderFactory != null) {
                    IBeanLoader beanLoader = beanLoaderFactory.getBeanLoader();
                    if (beanLoader != null) {
                        beanLoader.registerHandler(Api.class, new DocsHandler(this));
                    }
                }
            }
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            if (config.isEnabled()) {
                docInfoMap.clear();
            }
            //
            config = null;
            owner = null;
        }
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public IDocsConfig getConfig() {
        return config;
    }

    @Override
    public Package apisPackageLookup(Class<? extends Api> targetClass) {
        Package packageObj = targetClass.getPackage();
        do {
            if (packageObj == null || packageObj.isAnnotationPresent(Apis.class)) {
                break;
            }
        } while ((packageObj = targetClass.getSuperclass().getPackage()) != null);
        return packageObj == null || !packageObj.isAnnotationPresent(Apis.class) ? Docs.class.getPackage() : packageObj;
    }

    @Override
    public void registerApi(Class<? extends Api> targetClass) throws Exception {
        if (config.isEnabled()) {
            Package apisPackage = apisPackageLookup(targetClass);
            //
            Apis apisAnn = apisPackage.getAnnotation(Apis.class);
            String docsId = String.format("%s_%s", apisPackage.getName(), apisAnn.version());
            DocInfo docInfo = ReentrantLockHelper.putIfAbsentAsync(docInfoMap, docsId, () -> DocInfo.create(docsId, apisAnn.title(), apisAnn.version())
                    .setDescription(apisAnn.description())
                    .setLicense(LicenseInfo.create(apisPackage.getAnnotation(ApiLicense.class)))
                    .setAuthorization(AuthorizationInfo.create(apisPackage.getAnnotation(ApiAuthorization.class)))
                    .setSecurity(SecurityInfo.create(apisPackage.getAnnotation(ApiSecurity.class), null))
                    .addAuthors(AuthorInfo.create(apisPackage.getAnnotation(ApiAuthors.class)))
                    .addAuthor(AuthorInfo.create(apisPackage.getAnnotation(ApiAuthor.class)))
                    .addChangeLogs(ChangeLogInfo.create(apisPackage.getAnnotation(ApiChangeLogs.class)))
                    .addChangeLog(ChangeLogInfo.create(apisPackage.getAnnotation(ApiChangeLog.class)))
                    .addExtensions(ExtensionInfo.create(apisPackage.getAnnotation(ApiExtensions.class)))
                    .addExtension(ExtensionInfo.create(apisPackage.getAnnotation(ApiExtension.class)))
                    .addGroups(GroupInfo.create(apisPackage.getAnnotation(ApiGroups.class)))
                    .addGroup(GroupInfo.create(apisPackage.getAnnotation(ApiGroup.class)))
                    .addServers(ServerInfo.create(apisPackage.getAnnotation(ApiServers.class)))
                    .addServer(ServerInfo.create(apisPackage.getAnnotation(ApiServer.class))));
            docInfo.addApi(ApiInfo.create(docInfo, targetClass));
        }
    }

    @Override
    public Map<String, DocInfo> getDocs() {
        return Collections.unmodifiableMap(docInfoMap);
    }
}
