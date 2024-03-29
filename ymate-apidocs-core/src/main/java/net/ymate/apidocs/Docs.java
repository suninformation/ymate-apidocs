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
import net.ymate.apidocs.intercept.ApiMockEnabled;
import net.ymate.apidocs.intercept.ApiMockEnabledInterceptor;
import net.ymate.apidocs.render.*;
import net.ymate.platform.commons.DateTimeHelper;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.markdown.Link;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.ParagraphList;
import net.ymate.platform.commons.markdown.Text;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.core.*;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.intercept.InterceptSettings;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import net.ymate.platform.core.persistence.impl.DefaultResultSet;
import net.ymate.platform.core.support.ErrorCode;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.util.WebErrorCode;
import net.ymate.platform.webmvc.util.WebResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/01 00:34
 */
public final class Docs implements IModule, IDocs {

    private static final Log LOG = LogFactory.getLog(Docs.class);

    public static File checkTargetFileAndGet(File outputDir, String filePath, boolean overwrite) {
        File targetFile = new File(outputDir, filePath);
        boolean notSkipped = !targetFile.exists() || targetFile.exists() && overwrite;
        if (notSkipped) {
            File parentFile = targetFile.getParentFile();
            if (parentFile.exists() || parentFile.mkdirs()) {
                return targetFile;
            }
        } else if (LOG.isWarnEnabled()) {
            LOG.warn(String.format("Skip existing file %s.", targetFile));
        }
        return null;
    }

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
            YMP.showVersion("Initializing ymate-apidocs-core-${version}", new Version(2, 0, 1, Docs.class, Version.VersionType.Release));
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
            if (config.isEnabled()) {
                InterceptSettings interceptSettings = owner.getInterceptSettings();
                interceptSettings.registerInterceptAnnotation(ApiMockEnabled.class, ApiMockEnabledInterceptor.class);
                //
                if (configurer != null) {
                    IBeanLoadFactory beanLoaderFactory = configurer.getBeanLoadFactory();
                    if (beanLoaderFactory != null) {
                        IBeanLoader beanLoader = beanLoaderFactory.getBeanLoader();
                        if (beanLoader != null) {
                            beanLoader.registerHandler(Api.class, new DocsHandler(this));
                        }
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
                        .setSnakeCase(apisAnn.snakeCase())
                        .setOrder(apisAnn.order())
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
                        .addResponseType(ResponseTypeInfo.create(apisPackage.getAnnotation(ApiResponseType.class), apisAnn.snakeCase()))
                        .addRequestHeaders(HeaderInfo.create(apisPackage.getAnnotation(ApiRequestHeaders.class)))
                        .addResponseHeaders(HeaderInfo.create(apisPackage.getAnnotation(ApiResponseHeaders.class)));
                //
                ApiDefaultResponses defaultResponses = apisPackage.getAnnotation(ApiDefaultResponses.class);
                if (defaultResponses != null) {
                    if (ArrayUtils.isNotEmpty(defaultResponses.examples())) {
                        docInfo.addResponseExamples(ExampleInfo.create(defaultResponses.examples()));
                    } else {
                        ErrorCode errorCode = WebErrorCode.invalidParamsValidation();
                        docInfo.addResponseExample(ExampleInfo.create(WebResult.builder()
                                        .code(errorCode.code())
                                        .msg(errorCode.message())
                                        .data(Collections.singletonMap("username", "username is required."))
                                        .build()
                                        .toJsonObject()
                                        .toString(true, true, apisAnn.snakeCase()))
                                .setType("json")
                                .setName(AbstractMarkdown.i18nText(this, "response.example_standard", "Standard")));
                        docInfo.addResponseExample(ExampleInfo.create(WebResult.builder()
                                        .succeed()
                                        .data(new DefaultResultSet<>(Collections.emptyList(), 1, 20, 1))
                                        .build()
                                        .toJsonObject()
                                        .toString(true, true, apisAnn.snakeCase()))
                                .setType("json")
                                .setName(AbstractMarkdown.i18nText(this, "response.example_pagination", "Pagination")));
                    }
                    //
                    if (Serializable.class.equals(defaultResponses.standardType())) {
                        docInfo.addResponseProperty(PropertyInfo.create().setName(StringUtils.defaultIfBlank(defaultResponses.codeParamName(), Type.Const.PARAM_RET)).setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.code", "Response code.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName(StringUtils.defaultIfBlank(defaultResponses.msgParamName(), Type.Const.PARAM_MSG)).setValue(String.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.message", "Message.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName(StringUtils.defaultIfBlank(defaultResponses.dataParamName(), Type.Const.PARAM_DATA)).setValue(Object.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.data", "Business data content.")));
                    } else {
                        docInfo.addResponseProperties(PropertyInfo.create(null, defaultResponses.standardType(), apisAnn.snakeCase()));
                    }
                    if (Serializable.class.equals(defaultResponses.pagingType())) {
                        docInfo.addResponseProperty(PropertyInfo.create().setName(apisAnn.snakeCase() ? "page_count" : "pageCount").setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.page_count", "Paging param: total pages.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName(apisAnn.snakeCase() ? "page_number" : "pageNumber").setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.page_number", "Paging param: Current query page number.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName(apisAnn.snakeCase() ? "page_size" : "pageSize").setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.page_size", "Paging param: records per page.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName("paginated").setValue(Boolean.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.paginated", "Paging param: Pagination query or not.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName(apisAnn.snakeCase() ? "record_count" : "recordCount").setValue(Integer.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.record_count", "Paging param: total records.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName(apisAnn.snakeCase() ? "result_data" : "resultData").setValue(Object[].class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.result_data", "Paging param: the result set data object.")));
                        docInfo.addResponseProperty(PropertyInfo.create().setName(apisAnn.snakeCase() ? "results_available" : "resultsAvailable").setValue(Boolean.class.getSimpleName()).setDescription(AbstractMarkdown.i18nText(this, "response.results_available", "Paging param: whether result set is empty.")));
                    } else {
                        docInfo.addResponseProperties(PropertyInfo.create(StringUtils.defaultIfBlank(defaultResponses.dataParamName(), Type.Const.PARAM_DATA), defaultResponses.pagingType(), apisAnn.snakeCase()));
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
                        docInfo.addResponseType(ResponseTypeInfo.create(apiResponses, apisAnn.snakeCase()));
                    }
                    Arrays.stream(apiResponses.value()).map(ResponseInfo::create).forEachOrdered(docInfo::addResponse);
                }
                //
                ApiResponseTypes apiResponseTypes = apisPackage.getAnnotation(ApiResponseTypes.class);
                if (apiResponseTypes != null) {
                    Arrays.stream(apiResponseTypes.value()).map(responseType -> ResponseTypeInfo.create(responseType, apisAnn.snakeCase())).forEachOrdered(docInfo::addResponseType);
                }
                //
                return docInfo;
            });
            doc.addApi(ApiInfo.create(this, doc, targetClass));
        }
    }

    @Override
    public Map<String, DocInfo> getDocInfoMap() {
        return Collections.unmodifiableMap(docInfoMap);
    }

    @Override
    public List<DocInfo> getDocs() {
        List<DocInfo> docs = new ArrayList<>(docInfoMap.values());
        docs.sort(Comparator.comparingInt(DocInfo::getOrder));
        return Collections.unmodifiableList(docs);
    }

    @Override
    public void writeToDocusaurus(File outputDir, boolean overwrite) throws IOException {
        List<DocInfo> docInfos = getDocs();
        if (!docInfos.isEmpty()) {
            if (!outputDir.exists() && outputDir.mkdirs() && LOG.isInfoEnabled()) {
                LOG.info(String.format("Create a directory for %s.", outputDir));
            }
            FileUtils.unpackJarFile("docusaurus", outputDir);
            ParagraphList paragraphList = ParagraphList.create();
            int idx = 1;
            for (DocInfo docInfo : docInfos) {
                docInfo.setOrder(idx++);
                paragraphList.addItem(Link.create(String.format("%s %s", docInfo.getTitle(), docInfo.getVersion()), String.format("%s/overview", docInfo.getId())).toMarkdown());
                if (StringUtils.isNotBlank(docInfo.getDescription())) {
                    paragraphList.addBody(MarkdownBuilder.create().tab().quote(docInfo.getDescription()));
                }
                new DocusaurusDocRender(docInfo, new File(outputDir, "docs"), overwrite).render();
            }
            MarkdownBuilder markdownBuilder = MarkdownBuilder.create()
                    .append("---\nslug: /\nsidebar_position: 1\n---\n\n")
                    .title(AbstractMarkdown.i18nText(this, "doc.about", "About")).p()
                    .append(paragraphList).p().hr()
                    .append(":::tip\n")
                    .append(AbstractMarkdown.i18nText(this, "doc.footer", "This document is built with YMATE-APIDocs. Please visit [https://ymate.net/](https://ymate.net/) for more information.")).p()
                    .append(MarkdownBuilder.create().text(AbstractMarkdown.i18nText(this, "doc.create_time", "Create time: "), Text.Style.BOLD).space().text(DateTimeHelper.now().toString(DateTimeUtils.YYYY_MM_DD_HH_MM), Text.Style.ITALIC)).br()
                    .append(":::");
            File targetFile = Docs.checkTargetFileAndGet(outputDir, "docs/intro.md", overwrite);
            if (targetFile != null) {
                try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {
                    IOUtils.write(markdownBuilder.toMarkdown(), outputStream, "UTF-8");
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("Output file: %s", targetFile));
                    }
                }
            }
        }
    }

    @Override
    public void writeToGitbook(File outputDir, boolean overwrite) throws IOException {
        for (DocInfo docInfo : getDocs()) {
            new GitbookDocRender(docInfo, outputDir, overwrite).render();
        }
    }

    @Override
    public void writeToPostman(File outputDir, boolean overwrite) throws IOException {
        for (DocInfo docInfo : getDocs()) {
            File targetFile = checkTargetFileAndGet(outputDir, String.format("postman_collection_%s.json", docInfo.getId()), overwrite);
            if (targetFile != null) {
                try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {
                    new PostmanDocRender(docInfo).render(outputStream);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("Output file: %s", targetFile));
                    }
                }
            }
        }
    }

    @Override
    public void writeToMarkdown(File outputDir, boolean overwrite) throws IOException {
        for (DocInfo docInfo : getDocs()) {
            File targetFile = checkTargetFileAndGet(outputDir, String.format("%s.md", docInfo.getId()), overwrite);
            if (targetFile != null) {
                try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {
                    new MarkdownDocRender(docInfo).render(outputStream);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("Output file: %s", targetFile));
                    }
                }
            }
        }
    }

    public void writeToJson(File outputDir, boolean overwrite) throws IOException {
        for (DocInfo docInfo : getDocs()) {
            File targetFile = checkTargetFileAndGet(outputDir, String.format("%s.json", docInfo.getId()), overwrite);
            if (targetFile != null) {
                try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {
                    new JsonDocRender(docInfo).render(outputStream);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("Output file: %s", targetFile));
                    }
                }
            }
        }
    }
}
