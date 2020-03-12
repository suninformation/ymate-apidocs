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
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.*;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/01 00:34
 */
public final class Docs implements IModule, IDocs {

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

    public Docs() {
    }

    public Docs(IDocsConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            //
            YMP.showVersion("Initializing ymate-apidocs-core-${version}", new Version(2, 0, 0, Docs.class, Version.VersionType.Alpha));
            //
            this.owner = owner;
            IApplicationConfigureFactory configureFactory = owner.getConfigureFactory();
            IApplicationConfigurer configurer = null;
            if (configureFactory != null) {
                configurer = configureFactory.getConfigurer();
                IModuleConfigurer moduleConfigurer = configurer == null ? null : configurer.getModuleConfigurer(MODULE_NAME);
                if (moduleConfigurer != null) {
                    config = DefaultDocsConfig.create(configureFactory.getMainClass(), moduleConfigurer);
                } else {
                    config = DefaultDocsConfig.create(configureFactory.getMainClass(), DefaultModuleConfigurer.createEmpty(MODULE_NAME));
                }
            }
            if (config == null) {
                config = DefaultDocsConfig.defaultConfig();
            }
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            if (config.isEnabled() && configurer != null) {
                IBeanLoadFactory beanLoaderFactory = configurer.getBeanLoadFactory();
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
        Package packageObj = ClassUtils.getPackage(targetClass, Apis.class);
        return packageObj == null || !packageObj.isAnnotationPresent(Apis.class) ? Docs.class.getPackage() : packageObj;
    }

    @Override
    public void registerApi(Class<? extends Api> targetClass) throws Exception {
        if (config.isEnabled()) {
            Package apisPackage = apisPackageLookup(targetClass);
            //
            Apis apisAnn = apisPackage.getAnnotation(Apis.class);
            String docsId = String.format("%s_%s", apisPackage.getName(), apisAnn.version());
            DocInfo doc = ReentrantLockHelper.putIfAbsentAsync(docInfoMap, docsId, () -> {
                DocInfo docInfo = DocInfo.create(this, docsId, apisAnn.title(), apisAnn.version())
                        .setDescription(apisAnn.description())
                        .setLicense(LicenseInfo.create(apisPackage.getAnnotation(ApiLicense.class)))
                        .setAuthorization(AuthorizationInfo.create(this, apisPackage.getAnnotation(ApiAuthorization.class)))
                        .setSecurity(SecurityInfo.create(this, apisPackage.getAnnotation(ApiSecurity.class), null))
                        .addAuthors(AuthorInfo.create(apisPackage.getAnnotation(ApiAuthors.class)))
                        .addAuthor(AuthorInfo.create(apisPackage.getAnnotation(ApiAuthor.class)))
                        .addChangeLogs(ChangeLogInfo.create(apisPackage.getAnnotation(ApiChangeLogs.class)))
                        .addChangeLog(ChangeLogInfo.create(apisPackage.getAnnotation(ApiChangeLog.class)))
                        .addExtensions(ExtensionInfo.create(apisPackage.getAnnotation(ApiExtensions.class)))
                        .addExtension(ExtensionInfo.create(apisPackage.getAnnotation(ApiExtension.class)))
                        .addGroups(GroupInfo.create(apisPackage.getAnnotation(ApiGroups.class)))
                        .addGroup(GroupInfo.create(apisPackage.getAnnotation(ApiGroup.class)))
                        .addParams(ParamInfo.create(this, apisPackage.getAnnotation(ApiParams.class)))
                        .addParam(ParamInfo.create(this, apisPackage.getAnnotation(ApiParam.class)))
                        .addServers(ServerInfo.create(apisPackage.getAnnotation(ApiServers.class)))
                        .addServer(ServerInfo.create(apisPackage.getAnnotation(ApiServer.class)))
                        .addResponse(ResponseInfo.create(apisPackage.getAnnotation(ApiResponse.class)))
                        .addResponseType(ResponseTypeInfo.create(apisPackage.getAnnotation(ApiResponseType.class)))
                        .addRequestHeaders(HeaderInfo.create(apisPackage.getAnnotation(ApiRequestHeaders.class)))
                        .addResponseHeaders(HeaderInfo.create(apisPackage.getAnnotation(ApiResponseHeaders.class)));
                //
                ApiDefaultResponses defaultResponses = apisPackage.getAnnotation(ApiDefaultResponses.class);
                if (defaultResponses != null) {
                    if (ArrayUtils.isNotEmpty(defaultResponses.examples())) {
                        docInfo.addResponseExamples(ExampleInfo.create(defaultResponses.examples()));
                    } else {
                        docInfo.addResponseExample(ExampleInfo.create("{\n" +
                                "    \"ret\": -1,\n" +
                                "    \"msg\": \"Request parameter validation is invalid.\",\n" +
                                "    \"data\": {\n" +
                                "        \"username\": \"username is required.\",\n" +
                                "        \"password\": \"password is required.\"\n" +
                                "    }\n" +
                                "}").setType("json").setName(AbstractMarkdown.i18nText(this, "response.example_standard", "Standard")));
                        docInfo.addResponseExample(ExampleInfo.create("{\n" +
                                "    \"ret\": 0,\n" +
                                "    \"data\": {\n" +
                                "        \"pageCount\": 1,\n" +
                                "        \"pageNumber\": 1,\n" +
                                "        \"pageSize\": 20,\n" +
                                "        \"paginated\": true,\n" +
                                "        \"recordCount\": 1,\n" +
                                "        \"resultData\": [],\n" +
                                "        \"resultsAvailable\": false\n" +
                                "    }\n" +
                                "}").setType("json").setName(AbstractMarkdown.i18nText(this, "response.example_pagination", "Pagination")));
                    }
                    //
                    if (Serializable.class.equals(defaultResponses.standardType())) {
                        docInfo.addResponseProperty(PropertyInfo.create().setName(Type.Const.PARAM_RET).setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.code", "Response code.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName(Type.Const.PARAM_MSG).setValue(String.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.message", "Message.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName(Type.Const.PARAM_DATA).setValue(Object.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.data", "Business data content.")));
                    } else {
                        docInfo.addResponseProperties(PropertyInfo.create(null, defaultResponses.standardType()));
                    }
                    if (Serializable.class.equals(defaultResponses.pagingType())) {
                        docInfo.addResponseProperty(PropertyInfo.create().setName("pageCount").setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.page_count", "Paging param: total pages.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName("pageNumber").setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.page_number", "Paging param: Current query page number.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName("pageSize").setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.page_size", "Paging param: records per page.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName("paginated").setValue(Boolean.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.paginated", "Paging param: Pagination query or not.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName("recordCount").setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.record_count", "Paging param: total records.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName("resultData").setValue(Object[].class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.result_data", "Paging param: the result set data object.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName("resultsAvailable").setValue(Boolean.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.results_available", "Paging param: whether result set is empty.")));
                    } else {
                        docInfo.addResponseProperties(PropertyInfo.create(Type.Const.PARAM_DATA, defaultResponses.pagingType()));
                    }
                    //
                    docInfo.addResponse(ResponseInfo.create("0", AbstractMarkdown.i18nText(this, "error_code_0", "Request success.")));
                    docInfo.addResponse(ResponseInfo.create("-1", AbstractMarkdown.i18nText(this, "error_code_-1", "Request parameter validation is invalid.")));
                    docInfo.addResponse(ResponseInfo.create("-2", AbstractMarkdown.i18nText(this, "error_code_-2", "The resources was not found or not existed.")));
                    docInfo.addResponse(ResponseInfo.create("-3", AbstractMarkdown.i18nText(this, "error_code_-3", "The request method is unsupported or incorrect.")));
                    docInfo.addResponse(ResponseInfo.create("-4", AbstractMarkdown.i18nText(this, "error_code_-4", "The requested resource is not authorized or privileged.")));
                    docInfo.addResponse(ResponseInfo.create("-5", AbstractMarkdown.i18nText(this, "error_code_-5", "User session invalid or timeout.")));
                    docInfo.addResponse(ResponseInfo.create("-6", AbstractMarkdown.i18nText(this, "error_code_-6", "The requested operation is forbidden.")));
                    docInfo.addResponse(ResponseInfo.create("-7", AbstractMarkdown.i18nText(this, "error_code_-7", "User session is authorized (logged in).")));
                    docInfo.addResponse(ResponseInfo.create("-8", AbstractMarkdown.i18nText(this, "error_code_-8", "The parameter signature is invalid.")));
                    docInfo.addResponse(ResponseInfo.create("-9", AbstractMarkdown.i18nText(this, "error_code_-9", "The size of the uploaded file exceeds the limit.")));
                    docInfo.addResponse(ResponseInfo.create("-10", AbstractMarkdown.i18nText(this, "error_code_-10", "The total size of uploaded files exceeds the limit.")));
                    docInfo.addResponse(ResponseInfo.create("-11", AbstractMarkdown.i18nText(this, "error_code_-11", "The upload file content type is invalid.")));
                    docInfo.addResponse(ResponseInfo.create("-12", AbstractMarkdown.i18nText(this, "error_code_-12", "User session confirmation state invalid.")));
                    docInfo.addResponse(ResponseInfo.create("-13", AbstractMarkdown.i18nText(this, "error_code_-13", "User session has been forced offline.")));
                    docInfo.addResponse(ResponseInfo.create("-20", AbstractMarkdown.i18nText(this, "error_code_-20", "The data version does not match.")));
                    docInfo.addResponse(ResponseInfo.create("-50", AbstractMarkdown.i18nText(this, "error_code_-50", "The system is busy, try again later!")));
                }
                //
                ApiResponses apiResponses = apisPackage.getAnnotation(ApiResponses.class);
                if (apiResponses != null) {
                    if (!Void.class.equals(apiResponses.type())) {
                        docInfo.addResponseType(ResponseTypeInfo.create(apiResponses));
                    }
                    Arrays.stream(apiResponses.value()).map(ResponseInfo::create).forEachOrdered(docInfo::addResponse);
                }
                //
                ApiResponseTypes apiResponseTypes = apisPackage.getAnnotation(ApiResponseTypes.class);
                if (apiResponseTypes != null) {
                    Arrays.stream(apiResponseTypes.value()).map(ResponseTypeInfo::create).forEachOrdered(docInfo::addResponseType);
                }
                //
                return docInfo;
            });
            doc.addApi(ApiInfo.create(this, doc, targetClass));
        }
    }

    @Override
    public Map<String, DocInfo> getDocs() {
        return Collections.unmodifiableMap(docInfoMap);
    }
}
