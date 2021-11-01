/*
 * Copyright 2007-2021 the original author or authors.
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
/*
 * Copyright (c) 2007-2021, the original author or authors. All rights reserved.
 *
 * This program licensed under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package net.ymate.apidocs;

import net.ymate.apidocs.base.*;
import net.ymate.platform.commons.DateTimeHelper;
import net.ymate.platform.commons.markdown.Link;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.ParagraphList;
import net.ymate.platform.commons.markdown.Text;
import net.ymate.platform.commons.util.DateTimeUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/11/1 11:32 下午
 * @since 1.0.0
 */
public abstract class AbstractMultiDocRender extends AbstractDocRender {

    private static final Log LOG = LogFactory.getLog(AbstractMultiDocRender.class);

    private final File outputDir;

    private final boolean overwrite;

    public AbstractMultiDocRender(DocInfo docInfo, File outputDir, boolean overwrite) {
        super(docInfo);
        if (outputDir == null) {
            throw new NullArgumentException("outputDir");
        }
        if (!outputDir.exists() && outputDir.mkdirs() && LOG.isInfoEnabled()) {
            LOG.info(String.format("Create a directory for %s.", outputDir));
        } else if (!outputDir.isDirectory() || !outputDir.canWrite()) {
            throw new IllegalArgumentException(String.format("Invalid argument outputDir: %s", outputDir));
        }
        this.outputDir = outputDir;
        this.overwrite = overwrite;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    protected void doWriteFileContent(String fileName, String content) throws IOException {
        String filePath = String.format("%s/%s", getDocInfo().getId(), fileName);
        if (!StringUtils.endsWithIgnoreCase(filePath, ".md")) {
            filePath += ".md";
        }
        File targetFile = Docs.checkTargetFileAndGet(outputDir, filePath, overwrite);
        if (targetFile != null) {
            try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                IOUtils.write(content, outputStream, "UTF-8");
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("Output file: %s", targetFile));
                }
            }
        }
    }

    protected MarkdownBuilder doReadmeContentBuilder() {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create().title(docInfo.getTitle()).p();
        if (StringUtils.isNotBlank(docInfo.getDescription())) {
            markdownBuilder.text(docInfo.getDescription()).p();
        }
        markdownBuilder.text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.version", "Version"), Text.Style.BOLD).p().text(docInfo.getVersion()).p();
        if (docInfo.getLicense() != null) {
            markdownBuilder.text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.license", "License"), Text.Style.BOLD).p().text(docInfo.getLicense()).p();
        }
        if (docInfo.getAuthors().isEmpty()) {
            docInfo.addAuthor(AuthorInfo.create("YMATE-APIDocs").setUrl("https://www.ymate.net/"));
        }
        markdownBuilder.text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.authors", "Authors"), Text.Style.BOLD).p().text(AuthorInfo.toMarkdown(docInfo.getAuthors())).p();
        return markdownBuilder;
    }

    protected String doBuildLink(String url, boolean withoutExt) {
        if (withoutExt) {
            return url;
        }
        return String.format("%s.md", url);
    }

    protected MarkdownBuilder doServersPartBuilder(MarkdownBuilder header, boolean withoutExt) throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!docInfo.getServers().isEmpty()) {
            String textServers = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.servers", "Servers");
            markdownBuilder.append(Link.create(textServers, doBuildLink("servers", withoutExt)));
            doWriteFileContent("servers", MarkdownBuilder.create().append(header).title(textServers, 1).p().append(ServerInfo.toMarkdown(docInfo.getOwner(), docInfo.getServers())).toMarkdown());
        }
        return markdownBuilder;
    }

    protected MarkdownBuilder doAuthorizationPartBuilder(MarkdownBuilder header, boolean withoutExt) throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (docInfo.getAuthorization() != null) {
            String textAuthorization = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.authorization", "Authorization");
            markdownBuilder.append(Link.create(textAuthorization, doBuildLink("authorization", withoutExt)));
            doWriteFileContent("authorization", MarkdownBuilder.create().append(header).title(textAuthorization, 1).p().append(docInfo.getAuthorization()).toMarkdown());
        }
        return markdownBuilder;
    }

    protected MarkdownBuilder doSecurityPartBuilder(MarkdownBuilder header, boolean withoutExt) throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (docInfo.getSecurity() != null) {
            String textSecurityContext = docInfo.getSecurity().toMarkdown();
            if (StringUtils.isNotBlank(textSecurityContext)) {
                String textSecurity = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.security", "Security");
                markdownBuilder.append(Link.create(textSecurity, doBuildLink("security", withoutExt)));
                doWriteFileContent("security", MarkdownBuilder.create().append(header).title(textSecurity, 1).p().append(textSecurityContext).toMarkdown());
            }
        }
        return markdownBuilder;
    }

    protected MarkdownBuilder doGlobalRequestParamsPartBuilder(MarkdownBuilder header, boolean withoutExt) throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!docInfo.getParams().isEmpty()) {
            String textRequestParams = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.request_parameters", "Global request parameters");
            markdownBuilder.append(Link.create(textRequestParams, doBuildLink("global-request-parameters", withoutExt)));
            doWriteFileContent("global-request-parameters", MarkdownBuilder.create().append(header).title(textRequestParams, 1).p().append(ParamInfo.toMarkdown(docInfo.getOwner(), docInfo.getParams())).toMarkdown());
        }
        return markdownBuilder;
    }

    protected MarkdownBuilder doGlobalRequestHeadersPartBuilder(MarkdownBuilder header, boolean withoutExt) throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!docInfo.getRequestHeaders().isEmpty()) {
            String textRequestHeaders = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.request_headers", "Global request headers");
            markdownBuilder.append(Link.create(textRequestHeaders, doBuildLink("global-request-headers", withoutExt)));
            doWriteFileContent("global-request-headers", MarkdownBuilder.create().append(header).title(textRequestHeaders, 1).p().append(HeaderInfo.toMarkdown(docInfo.getOwner(), docInfo.getRequestHeaders())).toMarkdown());
        }
        return markdownBuilder;
    }

    protected MarkdownBuilder doGlobalResponseHeadersPartBuilder(MarkdownBuilder header, boolean withoutExt) throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!docInfo.getResponseHeaders().isEmpty()) {
            String textResponseHeaders = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_headers", "Global response headers");
            markdownBuilder.append(Link.create(textResponseHeaders, doBuildLink("global-response-headers", withoutExt)));
            doWriteFileContent("global-response-headers", MarkdownBuilder.create().append(header).title(textResponseHeaders, 1).p().append(HeaderInfo.toMarkdown(docInfo.getOwner(), docInfo.getResponseHeaders())).toMarkdown());
        }
        return markdownBuilder;
    }

    protected MarkdownBuilder doChangelogPartBuilder(MarkdownBuilder header, boolean withoutExt) throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!docInfo.getChangeLogs().isEmpty()) {
            String textChangelog = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.changelog", "Changelog");
            markdownBuilder.append(Link.create(textChangelog, doBuildLink("changelog", withoutExt)));
            doWriteFileContent("changelog", MarkdownBuilder.create().append(header).title(textChangelog, 1).p().append(ChangeLogInfo.toMarkdown(docInfo.getOwner(), docInfo.getChangeLogs())).toMarkdown());
        }
        return markdownBuilder;
    }

    protected MarkdownBuilder doExtensionsPartBuilder(MarkdownBuilder header, boolean withoutExt) throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!docInfo.getExtensions().isEmpty()) {
            String textExtensions = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.extensions", "Extensions");
            markdownBuilder.append(Link.create(textExtensions, doBuildLink("extensions", withoutExt)));
            doWriteFileContent("extensions", MarkdownBuilder.create().append(header).title(textExtensions, 1).p().append(ExtensionInfo.toMarkdown(docInfo.getExtensions())).toMarkdown());
        }
        return markdownBuilder;
    }

    protected MarkdownBuilder doAppendixPartBuilder(MarkdownBuilder header, boolean withoutExt) throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!docInfo.getResponses().isEmpty() || !docInfo.getResponseTypes().isEmpty() || !docInfo.getResponseExamples().isEmpty() || !docInfo.getResponseProperties().isEmpty()) {
            String appendixTitle = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.appendix", "Appendix");
            markdownBuilder.title(appendixTitle, 2).p();
            MarkdownBuilder appendixMarkdownBuilder = MarkdownBuilder.create().append(header).title(appendixTitle).p();
            ParagraphList appendixList = ParagraphList.create();
            if (!docInfo.getResponseProperties().isEmpty()) {
                String textResponseStructure = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_structure", "Response structure");
                appendixList.addItem(Link.create(textResponseStructure, String.format("%s#%s", doBuildLink("appendix", withoutExt), RegExUtils.replaceAll(StringUtils.lowerCase(textResponseStructure), StringUtils.SPACE, "-"))).toMarkdown());
                appendixMarkdownBuilder.title(textResponseStructure, 2).p();
                if (!docInfo.getResponseExamples().isEmpty()) {
                    appendixMarkdownBuilder.text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_structure_examples", "Response examples")).p()
                            .append(ExampleInfo.toMarkdown(docInfo.getResponseExamples())).p().text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_structure_properties", "Response properties")).p();
                }
                appendixMarkdownBuilder.append(PropertyInfo.toMarkdownTable(docInfo.getOwner(), docInfo.getResponseProperties())).p();
            }
            if (!docInfo.getResponses().isEmpty()) {
                String textResponseCodes = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_codes", "Response codes");
                appendixList.addItem(Link.create(textResponseCodes, String.format("%s#%s", doBuildLink("appendix", withoutExt), RegExUtils.replaceAll(StringUtils.lowerCase(textResponseCodes), StringUtils.SPACE, "-"))).toMarkdown());
                appendixMarkdownBuilder.title(textResponseCodes, 2).p();
                List<ResponseInfo> sorted = new ArrayList<>(docInfo.getResponses().values());
                sorted.sort((o1, o2) -> Integer.valueOf(o2.getCode()).compareTo(Integer.valueOf(o1.getCode())));
                appendixMarkdownBuilder.append(ResponseInfo.toMarkdown(docInfo.getOwner(), sorted)).p();
            }
            if (!docInfo.getResponseTypes().isEmpty()) {
                String textResponseTypes = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.response_types", "Response types");
                appendixList.addItem(Link.create(textResponseTypes, String.format("%s#%s", doBuildLink("appendix", withoutExt), RegExUtils.replaceAll(StringUtils.lowerCase(textResponseTypes), StringUtils.SPACE, "-"))).toMarkdown());
                appendixMarkdownBuilder.title(textResponseTypes, 2).p();
                String[] keys = docInfo.getResponseTypes().keySet().toArray(new String[0]);
                Arrays.sort(keys);
                Arrays.stream(keys).forEachOrdered(key -> {
                    ResponseTypeInfo responseType = docInfo.getResponseTypes().get(key);
                    appendixList.addSubItem(Link.create(responseType.getName(), String.format("%s#%s", doBuildLink("appendix", withoutExt), RegExUtils.replaceAll(StringUtils.lowerCase(responseType.getName()), StringUtils.SPACE, "-"))).toMarkdown());
                    appendixMarkdownBuilder.title(responseType.getName(), 3).p();
                    if (StringUtils.isNotBlank(responseType.getDescription())) {
                        appendixMarkdownBuilder.append(responseType.getDescription()).p();
                    }
                    appendixMarkdownBuilder.append(PropertyInfo.toMarkdownTable(docInfo.getOwner(), responseType.getProperties())).p();
                });
            }
            doWriteFileContent("appendix", appendixMarkdownBuilder.toMarkdown());
            markdownBuilder.append(appendixList);
        }
        return markdownBuilder;
    }

    protected MarkdownBuilder doFooterContentBuilder() {
        DocInfo docInfo = getDocInfo();
        return MarkdownBuilder.create().p(5).hr()
                .quote(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.footer", "This document is built with YMATE-APIDocs. Please visit [https://ymate.net/](https://ymate.net/) for more information.")).br()
                .quote(MarkdownBuilder.create().text(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.create_time", "Create time: "), Text.Style.BOLD).space().text(DateTimeHelper.now().toString(DateTimeUtils.YYYY_MM_DD_HH_MM), Text.Style.ITALIC));
    }

    @Override
    public void render(OutputStream output) throws IOException {
        throw new UnsupportedOperationException();
    }
}
