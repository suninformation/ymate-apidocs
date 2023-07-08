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
package net.ymate.apidocs.render;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.ymate.apidocs.AbstractMarkdown;
import net.ymate.apidocs.AbstractMultiDocRender;
import net.ymate.apidocs.Docs;
import net.ymate.apidocs.base.ApiInfo;
import net.ymate.apidocs.base.AuthorInfo;
import net.ymate.apidocs.base.DocInfo;
import net.ymate.apidocs.base.GroupInfo;
import net.ymate.platform.commons.markdown.Link;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.ParagraphList;
import net.ymate.platform.commons.util.ClassUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/7/2 10:24 下午
 */
public class GitbookDocRender extends AbstractMultiDocRender {

    private static final Log LOG = LogFactory.getLog(GitbookDocRender.class);

    public GitbookDocRender(DocInfo docInfo, File outputDir, boolean overwrite) {
        super(docInfo, outputDir, overwrite);
    }

    private void doWriteGitbookJson() throws IOException {
        DocInfo docInfo = getDocInfo();
        String bookJsonFilePath = String.format("%s/book.json", docInfo.getId());
        File bookJsonFile = new File(getOutputDir(), bookJsonFilePath);
        JSONObject bookJson;
        boolean isNew = false;
        if (bookJsonFile.exists()) {
            bookJson = JSON.parseObject(IOUtils.toString(Files.newInputStream(bookJsonFile.toPath()), StandardCharsets.UTF_8), Feature.OrderedField);
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
        if (bookJsonFile.getParentFile().mkdirs() && LOG.isInfoEnabled()) {
            LOG.info(String.format("Create a directory for %s.", bookJsonFile.getParentFile()));
        }
        try (OutputStream outputStream = Files.newOutputStream(bookJsonFile.toPath())) {
            IOUtils.write(bookJson.toString(SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat), outputStream, "UTF-8");
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Output file: %s", bookJsonFile));
            }
        }
    }

    private void doWriteGitbookSummary() throws IOException {
        DocInfo docInfo = getDocInfo();
        File summaryFile = Docs.checkTargetFileAndGet(getOutputDir(), String.format("%s/SUMMARY.md", docInfo.getId()), isOverwrite());
        if (summaryFile != null) {
            MarkdownBuilder emptyHeader = MarkdownBuilder.create();
            MarkdownBuilder markdownBuilder = MarkdownBuilder.create()
                    .title("Summary").p()
                    .title(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.about", "About"), 2).p();
            ParagraphList overviewList = ParagraphList.create()
                    .addItem(Link.create(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.overview", "Overview"), "README.md").toMarkdown())
                    .addItem(doServersPartBuilder(emptyHeader, false).toMarkdown())
                    .addItem(doAuthorizationPartBuilder(emptyHeader, false).toMarkdown())
                    .addItem(doSecurityPartBuilder(emptyHeader, false).toMarkdown())
                    .addItem(doGlobalRequestParamsPartBuilder(emptyHeader, false).toMarkdown())
                    .addItem(doGlobalRequestHeadersPartBuilder(emptyHeader, false).toMarkdown())
                    .addItem(doGlobalResponseHeadersPartBuilder(emptyHeader, false).toMarkdown())
                    .addItem(doChangelogPartBuilder(emptyHeader, false).toMarkdown())
                    .addItem(doExtensionsPartBuilder(emptyHeader, false).toMarkdown());
            markdownBuilder.append(overviewList.toMarkdown()).p().title(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.apis", "Apis"), 2).p();
            //
            ParagraphList apiList = ParagraphList.create();
            doAppendApiActionList(docInfo.getApis(null), apiList);
            markdownBuilder.append(apiList.toMarkdown()).p();
            //
            List<GroupInfo> groups = docInfo.getGroups();
            if (!groups.isEmpty()) {
                for (GroupInfo groupInfo : groups) {
                    List<ApiInfo> apiInfos = docInfo.getApis(groupInfo.getName());
                    if (!apiInfos.isEmpty()) {
                        markdownBuilder.title(groupInfo.getName(), 3).p();
                        apiList = ParagraphList.create();
                        doAppendApiActionList(apiInfos, apiList);
                        markdownBuilder.append(apiList.toMarkdown()).p();
                    }
                }
            }
            //
            markdownBuilder.append(doAppendixPartBuilder(emptyHeader, false));
            try (OutputStream outputStream = Files.newOutputStream(summaryFile.toPath())) {
                IOUtils.write(markdownBuilder.toMarkdown(), outputStream, "UTF-8");
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("Output file: %s", summaryFile));
                }
            }
        }
    }

    private void doAppendApiActionList(List<ApiInfo> apiInfos, ParagraphList parent) throws IOException {
        if (!apiInfos.isEmpty()) {
            for (ApiInfo apiInfo : apiInfos) {
                String apiFilePath = String.format("api-%s.md", RegExUtils.replaceAll(ClassUtils.fieldNameToPropertyName(StringUtils.substringAfterLast(apiInfo.getId(), "."), 0), "_", "-"));
                parent.addItem(Link.create(apiInfo.getName(), apiFilePath).toMarkdown());
                doWriteFileContent(apiFilePath, apiInfo.toMarkdown());
//                ParagraphList apiSubList = ParagraphList.create();
//                if (!apiInfo.getActions().isEmpty()) {
//                    Set<String> actionGroupNames = apiInfo.getGroupNames();
//                    if (!actionGroupNames.isEmpty()) {
//                        for (String actionGroupName : actionGroupNames) {
//                            List<ActionInfo> actionInfos = apiInfo.getActions(actionGroupName);
//                            if (!actionInfos.isEmpty()) {
//                                actionInfos.forEach(actionInfo -> apiSubList.addSubItem(Link.create(actionInfo.getDisplayName(), String.format("%s#%s", apiFilePath, RegExUtils.replaceAll(StringUtils.lowerCase(actionInfo.getDisplayName()), StringUtils.SPACE, "-"))).toMarkdown()));
//                            }
//                        }
//                    } else {
//                        List<ActionInfo> actionInfos = apiInfo.getActions();
//                        if (!actionInfos.isEmpty()) {
//                            actionInfos.forEach(actionInfo -> apiSubList.addSubItem(Link.create(actionInfo.getDisplayName(), String.format("%s#%s", apiFilePath, RegExUtils.replaceAll(StringUtils.lowerCase(actionInfo.getDisplayName()), StringUtils.SPACE, "-"))).toMarkdown()).toMarkdown());
//                        }
//                    }
//                }
//                parent.addBody(apiSubList);
            }
        }
    }

    @Override
    public String render() throws IOException {
        doWriteGitbookJson();
        doWriteFileContent("README", doReadmeContentBuilder().append(doFooterContentBuilder()).toMarkdown());
        doWriteGitbookSummary();
        return "SUCCEED";
    }
}
