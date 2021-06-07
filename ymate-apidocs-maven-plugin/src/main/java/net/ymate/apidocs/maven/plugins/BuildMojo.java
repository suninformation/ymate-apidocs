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
package net.ymate.apidocs.maven.plugins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.ymate.apidocs.AbstractMarkdown;
import net.ymate.apidocs.Docs;
import net.ymate.apidocs.IDocs;
import net.ymate.apidocs.annotation.Api;
import net.ymate.apidocs.base.*;
import net.ymate.apidocs.render.JsonDocRender;
import net.ymate.apidocs.render.MarkdownDocRender;
import net.ymate.apidocs.render.PostmanDocRender;
import net.ymate.platform.commons.DateTimeHelper;
import net.ymate.platform.commons.markdown.Link;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.ParagraphList;
import net.ymate.platform.commons.markdown.Text;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.impl.DefaultBeanLoader;
import net.ymate.platform.core.persistence.base.EntityMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 基于当前工程或指定的JSON格式的ApiDocs配置构建文档，支持html、json或markdown等文件格式输出
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/02 22:09
 */
@Mojo(name = "build", requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
@Execute(phase = LifecyclePhase.COMPILE)
public class BuildMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(property = "packageNames", defaultValue = "${project.groupId}")
    private String[] packageNames;

    /**
     * 输出格式: html|gitbook|postman|json|markdown, 默认值: markdown
     */
    @Parameter(property = "format", defaultValue = "markdown")
    private String format;

    @Parameter(property = "outputDir", defaultValue = "${basedir}")
    private String outputDir;

    /**
     * 自定义语言
     */
    @Parameter(property = "language")
    private String language;

    /**
     * 是否覆盖已存在的文件
     */
    @Parameter(property = "overwrite")
    private boolean overwrite;

    /**
     * 忽略的请求方法名称集合
     */
    @Parameter(property = "ignoredRequestMethods")
    private String[] ignoredRequestMethods;

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (IApplication application = YMP.run()) {
            if (StringUtils.isNotBlank(language)) {
                application.getI18n().current(LocaleUtils.toLocale(language));
            }
            List<URL> urls = new ArrayList<>();
            urls.add(new File(mavenProject.getBuild().getOutputDirectory()).toURI().toURL());
            for (Artifact dependency : mavenProject.getArtifacts()) {
                urls.add(dependency.getFile().toURI().toURL());
            }
            IBeanLoader beanLoader = new DefaultBeanLoader();
            beanLoader.setClassLoader(new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader()));
            beanLoader.registerPackageNames(Arrays.asList(packageNames));
            //
            getLog().info(String.format("packageNames: %s", beanLoader.getPackageNames()));
            getLog().info(String.format("outputDir: %s", outputDir));
            //
            IDocs docs = application.getModuleManager().getModule(Docs.class);
            if (!ArrayUtils.isEmpty(ignoredRequestMethods)) {
                Set<String> ignoredRequestMethodSet = docs.getConfig().getIgnoredRequestMethods();
                Arrays.stream(ignoredRequestMethods).filter(StringUtils::isNotBlank).forEach(methodName -> ignoredRequestMethodSet.add(methodName.toUpperCase()));
            }
            for (Class<?> clazz : beanLoader.load()) {
                if (clazz.isAnnotationPresent(Api.class)) {
                    getLog().info(String.format("Scanned to: %s", clazz.getName()));
                    docs.registerApi((Class<? extends Api>) clazz);
                }
            }
            if (docs.getDocs().isEmpty()) {
                getLog().warn("No documents found.");
            } else {
                switch (StringUtils.lowerCase(format)) {
                    case "html":
                    case "gitbook":
                        writeToGitBook(docs);
                        break;
                    case "postman":
                        writeToPostman(docs);
                        break;
                    case "json":
                        writeToJson(docs);
                        break;
                    case "markdown":
                        writeToMarkdown(docs);
                        break;
                    default:
                        getLog().warn(String.format("Output in %s format is not supported.", format));
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }

    private File getCheckedTargetFile(String filePath) {
        File targetFile = new File(outputDir, filePath);
        boolean notSkipped = !targetFile.exists() || targetFile.exists() && overwrite;
        if (notSkipped) {
            File parentFile = targetFile.getParentFile();
            if (parentFile.exists() || parentFile.mkdirs()) {
                return targetFile;
            }
        } else {
            getLog().warn("Skip existing file " + targetFile);
        }
        return null;
    }

    private void writeToGitBook(IDocs docs) throws IOException {
        for (DocInfo docInfo : docs.getDocs().values()) {
            doWriteGitBookJson(docInfo);
            doWriteGitBookSummary(docInfo);
        }
    }

    private void doWriteGitBookFileContent(DocInfo docInfo, String fileName, String content) throws IOException {
        String filePath = String.format("docs/%s/%s", docInfo.getId(), fileName);
        if (!StringUtils.endsWithIgnoreCase(filePath, ".md")) {
            filePath += ".md";
        }
        File targetFile = getCheckedTargetFile(filePath);
        if (targetFile != null) {
            try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                IOUtils.write(content, outputStream, "UTF-8");
                this.getLog().info("Output file: " + targetFile);
            }
        }
    }

    private void doWriteGitBookJson(DocInfo docInfo) throws IOException {
        String bookJsonFilePath = String.format("docs/%s/book.json", docInfo.getId());
        File bookJsonFile = new File(outputDir, bookJsonFilePath);
        JSONObject bookJson;
        boolean isNew = false;
        if (bookJsonFile.exists()) {
            bookJson = JSON.parseObject(IOUtils.toString(new FileInputStream(bookJsonFile), StandardCharsets.UTF_8), Feature.OrderedField);
        } else {
            bookJson = new JSONObject(true);
            isNew = true;
        }
        bookJson.put("title", docInfo.getTitle());
        StringBuilder authorBuilder = new StringBuilder();
        if (docInfo.getAuthors().isEmpty()) {
            authorBuilder.append("YMP-ApiDocs");
        } else {
            AuthorInfo authorInfo = docInfo.getAuthors().get(0);
            authorBuilder.append(authorInfo.getName());
            if (StringUtils.isNotBlank(authorInfo.getUrl())) {
                authorBuilder.append(" (").append(authorInfo.getUrl()).append(")");
            } else if (StringUtils.isNotBlank(authorInfo.getEmail())) {
                authorBuilder.append(" (").append(authorInfo.getEmail()).append(")");
            }
        }
        bookJson.put("author", authorBuilder.toString());
        bookJson.put("description", docInfo.getDescription());
        if (isNew) {
            String lang = docInfo.getOwner().getOwner().getI18n().current().getLanguage();
            if (StringUtils.equals(lang, "zh")) {
                bookJson.put("language", "zh-hans");
            }
            bookJson.put("gitbook", "3.2.3");
            bookJson.put("styles", new JSONObject());
            bookJson.put("links", new JSONObject());
            bookJson.put("structure", new JSONObject());
            bookJson.put("plugins", new JSONArray());
            bookJson.put("pluginsConfig", new JSONObject());
        }
        bookJsonFile.getParentFile().mkdirs();
        try (OutputStream outputStream = new FileOutputStream(bookJsonFile)) {
            IOUtils.write(bookJson.toString(SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat), outputStream, "UTF-8");
            this.getLog().info("Output file: " + bookJsonFile);
        }
    }

    private void doBuildGitBookReadme(DocInfo docInfo) throws IOException {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create().title(docInfo.getTitle()).p();
        if (StringUtils.isNotBlank(docInfo.getDescription())) {
            markdownBuilder.text(docInfo.getDescription()).p();
        }
        markdownBuilder.text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.version", "Version"), Text.Style.BOLD).p().text(docInfo.getVersion()).p();
        if (docInfo.getLicense() != null) {
            markdownBuilder.text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.license", "License"), Text.Style.BOLD).p().text(docInfo.getLicense()).p();
        }
        if (docInfo.getAuthors().isEmpty()) {
            docInfo.addAuthor(AuthorInfo.create("YMP-ApiDocs").setUrl("https://www.ymate.net/"));
        }
        markdownBuilder.text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.authors", "Authors"), Text.Style.BOLD).p().text(AuthorInfo.toMarkdown(docInfo.getAuthors())).p();
        //
        markdownBuilder.p(5).hr()
                .quote(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.footer", "This document is built and generated based on the `YMP-ApiDocs`. Please visit [https://ymate.net/](https://ymate.net/) for more information.")).br()
                .quote(MarkdownBuilder.create().text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.create_time", "Create time: "), Text.Style.BOLD).space().text(DateTimeHelper.now().toString(DateTimeUtils.YYYY_MM_DD_HH_MM), Text.Style.ITALIC));
        //
        doWriteGitBookFileContent(docInfo, "README", markdownBuilder.toMarkdown());
    }

    private void doWriteGitBookSummary(DocInfo docInfo) throws IOException {
        File summaryFile = getCheckedTargetFile(String.format("docs/%s/SUMMARY.md", docInfo.getId()));
        if (summaryFile != null) {
            MarkdownBuilder markdownBuilder = MarkdownBuilder.create()
                    .title("Summary").p()
                    .title(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.about", "About"), 3).p();
            //
            ParagraphList overviewList = ParagraphList.create()
                    .addItem(Link.create(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.overview", "Overview"), "README.md").toMarkdown());
            doBuildGitBookReadme(docInfo);
            //
            if (!docInfo.getServers().isEmpty()) {
                String textServers = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.servers", "Servers");
                overviewList.addItem(Link.create(textServers, "servers.md").toMarkdown());
                doWriteGitBookFileContent(docInfo, "servers", MarkdownBuilder.create().title(textServers, 1).p().append(ServerInfo.toMarkdown(docInfo.getOwner(), docInfo.getServers())).toMarkdown());
            }
            if (docInfo.getAuthorization() != null) {
                String textAuthorization = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.authorization", "Authorization");
                overviewList.addItem(Link.create(textAuthorization, "authorization.md").toMarkdown());
                doWriteGitBookFileContent(docInfo, "authorization", MarkdownBuilder.create().title(textAuthorization, 1).p().append(docInfo.getAuthorization()).toMarkdown());
            }
            if (docInfo.getSecurity() != null) {
                String textSecurityContext = docInfo.getSecurity().toMarkdown();
                if (StringUtils.isNotBlank(textSecurityContext)) {
                    String textSecurity = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.security", "Security");
                    overviewList.addItem(Link.create(textSecurity, "security.md").toMarkdown());
                    doWriteGitBookFileContent(docInfo, "security", MarkdownBuilder.create().title(textSecurity, 1).p().append(textSecurityContext).toMarkdown());
                }
            }
            if (!docInfo.getParams().isEmpty()) {
                String textRequestParams = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.request_parameters", "Global request parameters");
                overviewList.addItem(Link.create(textRequestParams, "global-request-parameters.md").toMarkdown());
                doWriteGitBookFileContent(docInfo, "global-request-parameters", MarkdownBuilder.create().title(textRequestParams, 1).p().append(ParamInfo.toMarkdown(docInfo.getOwner(), docInfo.getParams())).toMarkdown());
            }
            if (!docInfo.getRequestHeaders().isEmpty()) {
                String textRequestHeaders = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.request_headers", "Global request headers");
                overviewList.addItem(Link.create(textRequestHeaders, "global-request-headers.md").toMarkdown());
                doWriteGitBookFileContent(docInfo, "global-request-headers", MarkdownBuilder.create().title(textRequestHeaders, 1).p().append(HeaderInfo.toMarkdown(docInfo.getOwner(), docInfo.getRequestHeaders())).toMarkdown());
            }
            if (!docInfo.getResponseHeaders().isEmpty()) {
                String textResponseHeaders = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_headers", "Global response headers");
                overviewList.addItem(Link.create(textResponseHeaders, "global-response-headers.md").toMarkdown());
                doWriteGitBookFileContent(docInfo, "global-response-headers", MarkdownBuilder.create().title(textResponseHeaders, 1).p().append(HeaderInfo.toMarkdown(docInfo.getOwner(), docInfo.getResponseHeaders())).toMarkdown());
            }
            if (!docInfo.getChangeLogs().isEmpty()) {
                String textChangelog = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.changelog", "Changelog");
                overviewList.addItem(Link.create(textChangelog, "changelog.md").toMarkdown());
                doWriteGitBookFileContent(docInfo, "changelog", MarkdownBuilder.create().title(textChangelog, 1).p().append(ChangeLogInfo.toMarkdown(docInfo.getOwner(), docInfo.getChangeLogs())).toMarkdown());
            }
            if (!docInfo.getExtensions().isEmpty()) {
                String textExtensions = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.extensions", "Extensions");
                overviewList.addItem(Link.create(textExtensions, "extensions.md").toMarkdown());
                doWriteGitBookFileContent(docInfo, "extensions", MarkdownBuilder.create().title(textExtensions, 1).p().append(ExtensionInfo.toMarkdown(docInfo.getExtensions())).toMarkdown());
            }
            markdownBuilder.append(overviewList.toMarkdown()).p().title(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.apis", "Apis"), 3).p();
            if (!docInfo.getApis().isEmpty()) {
                ParagraphList apiList = ParagraphList.create();
                Set<String> groupNames = docInfo.getGroupNames();
                if (!groupNames.isEmpty()) {
                    for (String groupName : groupNames) {
                        apiList.addItem(groupName);
                        doAppendApiActionList(docInfo.getApis(groupName), apiList);
                    }
                } else {
                    doAppendApiActionList(docInfo.getApis(), apiList);
                }
                markdownBuilder.append(apiList.toMarkdown()).p();
            }
            //
            if (!docInfo.getResponses().isEmpty() || !docInfo.getResponseTypes().isEmpty() || !docInfo.getResponseExamples().isEmpty() || !docInfo.getResponseProperties().isEmpty()) {
                String appendixTitle = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.appendix", "Appendix");
                markdownBuilder.title(appendixTitle, 3).p();
                MarkdownBuilder appendixMarkdownBuilder = MarkdownBuilder.create().title(appendixTitle).p();
                ParagraphList appendixList = ParagraphList.create();
                if (!docInfo.getResponseProperties().isEmpty()) {
                    String textResponseStructure = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_structure", "Response structure");
                    appendixList.addItem(Link.create(textResponseStructure, String.format("appendix.md#%s", RegExUtils.replaceAll(StringUtils.lowerCase(textResponseStructure), StringUtils.SPACE, "-"))).toMarkdown());
                    appendixMarkdownBuilder.title(textResponseStructure, 2).p();
                    if (!docInfo.getResponseExamples().isEmpty()) {
                        appendixMarkdownBuilder.text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_structure_examples", "Response examples")).p()
                                .append(ExampleInfo.toMarkdown(docInfo.getResponseExamples())).p().text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_structure_properties", "Response properties")).p();
                    }
                    appendixMarkdownBuilder.append(PropertyInfo.toMarkdownTable(docInfo.getOwner(), docInfo.getResponseProperties())).p();
                }
                if (!docInfo.getResponses().isEmpty()) {
                    String textResponseCodes = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_codes", "Response codes");
                    appendixList.addItem(Link.create(textResponseCodes, String.format("appendix.md#%s", RegExUtils.replaceAll(StringUtils.lowerCase(textResponseCodes), StringUtils.SPACE, "-"))).toMarkdown());
                    appendixMarkdownBuilder.title(textResponseCodes, 2).p();
                    List<ResponseInfo> sorted = new ArrayList<>(docInfo.getResponses().values());
                    sorted.sort((o1, o2) -> Integer.valueOf(o2.getCode()).compareTo(Integer.valueOf(o1.getCode())));
                    appendixMarkdownBuilder.append(ResponseInfo.toMarkdown(docInfo.getOwner(), sorted));
                }
                if (!docInfo.getResponseTypes().isEmpty()) {
                    String textResponseTypes = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_types", "Response types");
                    appendixList.addItem(Link.create(textResponseTypes, String.format("appendix.md#%s", RegExUtils.replaceAll(StringUtils.lowerCase(textResponseTypes), StringUtils.SPACE, "-"))).toMarkdown());
                    appendixMarkdownBuilder.title(textResponseTypes, 2).p();
                    String[] keys = docInfo.getResponseTypes().keySet().toArray(new String[0]);
                    Arrays.sort(keys);
                    Arrays.stream(keys).forEachOrdered(key -> {
                        ResponseTypeInfo responseType = docInfo.getResponseTypes().get(key);
                        appendixList.addSubItem(Link.create(responseType.getName(), String.format("appendix.md#%s", RegExUtils.replaceAll(StringUtils.lowerCase(responseType.getName()), StringUtils.SPACE, "-"))).toMarkdown());
                        appendixMarkdownBuilder.title(responseType.getName(), 3).p();
                        if (StringUtils.isNotBlank(responseType.getDescription())) {
                            appendixMarkdownBuilder.append(responseType.getDescription()).p();
                        }
                        appendixMarkdownBuilder.append(PropertyInfo.toMarkdownTable(docInfo.getOwner(), responseType.getProperties())).p();
                    });
                }
                doWriteGitBookFileContent(docInfo, "appendix", appendixMarkdownBuilder.toMarkdown());
                markdownBuilder.append(appendixList.toMarkdown()).p();
            }
            //
            try (OutputStream outputStream = new FileOutputStream(summaryFile)) {
                IOUtils.write(markdownBuilder.toMarkdown(), outputStream, "UTF-8");
                this.getLog().info("Output file: " + summaryFile);
            }
        }
    }

    private void doAppendApiActionList(List<ApiInfo> apiInfos, ParagraphList parent) throws IOException {
        if (!apiInfos.isEmpty()) {
            for (ApiInfo apiInfo : apiInfos) {
                ParagraphList apiSubList = ParagraphList.create();
                String apiFilePath = String.format("apis-%s.md", RegExUtils.replaceAll(EntityMeta.fieldNameToPropertyName(StringUtils.substringAfterLast(apiInfo.getId(), "."), 0), "_", "-"));
                apiSubList.addItem(Link.create(apiInfo.getName(), apiFilePath).toMarkdown());
                doWriteGitBookFileContent(apiInfo.getDocInfo(), apiFilePath, apiInfo.toMarkdown());
                if (!apiInfo.getActions().isEmpty()) {
                    Set<String> actionGroupNames = apiInfo.getGroupNames();
                    if (!actionGroupNames.isEmpty()) {
                        for (String actionGroupName : actionGroupNames) {
                            List<ActionInfo> actionInfos = apiInfo.getActions(actionGroupName);
                            if (!actionInfos.isEmpty()) {
                                actionInfos.forEach(actionInfo -> apiSubList.addSubItem(Link.create(actionInfo.getDisplayName(), String.format("%s#%s", apiFilePath, RegExUtils.replaceAll(StringUtils.lowerCase(actionInfo.getDisplayName()), StringUtils.SPACE, "-"))).toMarkdown()));
                            }
                        }
                    } else {
                        List<ActionInfo> actionInfos = apiInfo.getActions();
                        if (!actionInfos.isEmpty()) {
                            actionInfos.forEach(actionInfo -> apiSubList.addSubItem(Link.create(actionInfo.getDisplayName(), String.format("%s#%s", apiFilePath, RegExUtils.replaceAll(StringUtils.lowerCase(actionInfo.getDisplayName()), StringUtils.SPACE, "-"))).toMarkdown()).toMarkdown());
                        }
                    }
                }
                parent.addBody(apiSubList);
            }
        }
    }

    private void writeToPostman(IDocs docs) throws IOException {
        for (DocInfo docInfo : docs.getDocs().values()) {
            File targetFile = getCheckedTargetFile(String.format("docs/postman_collection_%s.json", docInfo.getId()));
            if (targetFile != null) {
                try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                    new PostmanDocRender(docInfo).render(outputStream);
                    this.getLog().info("Output file: " + targetFile);
                }
            }
        }
    }

    private void writeToMarkdown(IDocs docs) throws IOException {
        for (DocInfo docInfo : docs.getDocs().values()) {
            File targetFile = getCheckedTargetFile(String.format("docs/%s.md", docInfo.getId()));
            if (targetFile != null) {
                try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                    new MarkdownDocRender(docInfo).render(outputStream);
                    this.getLog().info("Output file: " + targetFile);
                }
            }
        }
    }

    private void writeToJson(IDocs docs) throws IOException {
        for (DocInfo docInfo : docs.getDocs().values()) {
            File targetFile = getCheckedTargetFile(String.format("docs/%s.json", docInfo.getId()));
            if (targetFile != null) {
                try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                    new JsonDocRender(docInfo).render(outputStream);
                    this.getLog().info("Output file: " + targetFile);
                }
            }
        }
    }
}
