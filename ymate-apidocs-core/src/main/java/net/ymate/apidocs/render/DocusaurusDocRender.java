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
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.ymate.apidocs.AbstractMarkdown;
import net.ymate.apidocs.AbstractMultiDocRender;
import net.ymate.apidocs.Docs;
import net.ymate.apidocs.base.ApiInfo;
import net.ymate.apidocs.base.DocInfo;
import net.ymate.apidocs.base.GroupInfo;
import net.ymate.platform.commons.markdown.Link;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.ParagraphList;
import net.ymate.platform.core.persistence.base.EntityMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/11/1 8:05 下午
 * @since 2.0.0
 */
public class DocusaurusDocRender extends AbstractMultiDocRender {

    private static final Log LOG = LogFactory.getLog(DocusaurusDocRender.class);

    public DocusaurusDocRender(DocInfo docInfo, File outputDir, boolean overwrite) {
        super(docInfo, outputDir, overwrite);
    }

    private void doWriteCategoryJson(String path, String label, int order) throws IOException {
        String bookJsonFilePath = String.format("%s/_category_.json", path);
        File categoryJsonFile = Docs.checkTargetFileAndGet(getOutputDir(), bookJsonFilePath, isOverwrite());
        if (categoryJsonFile != null) {
            JSONObject categoryJson;
            if (categoryJsonFile.exists()) {
                categoryJson = JSON.parseObject(IOUtils.toString(new FileInputStream(categoryJsonFile), StandardCharsets.UTF_8), Feature.OrderedField);
            } else {
                categoryJson = new JSONObject(true);
            }
            categoryJson.put("label", label);
            categoryJson.put("position", order);
            try (OutputStream outputStream = new FileOutputStream(categoryJsonFile)) {
                IOUtils.write(categoryJson.toString(SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat), outputStream, "UTF-8");
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("Output file: %s", categoryJsonFile));
                }
            }
        }
    }

    private void doWriteCategoryJson() throws IOException {
        DocInfo docInfo = getDocInfo();
        doWriteCategoryJson(docInfo.getId(), docInfo.getTitle(), docInfo.getOrder());
    }

    private void doWriteContent() throws IOException {
        DocInfo docInfo = getDocInfo();
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        String title = AbstractMarkdown.i18nText(getDocInfo().getOwner(), "doc.overview", "Overview");
        markdownBuilder.append("---\ntitle: ").append(title).append("\nsidebar_position: 1\n---\n").p().append(doReadmeContentBuilder());
        //
        ParagraphList overviewList = ParagraphList.create()
                .addItem(doServersPartBuilder(MarkdownBuilder.create().append("---\nsidebar_position: 2\n---\n\n\n"), true).toMarkdown())
                .addItem(doAuthorizationPartBuilder(MarkdownBuilder.create().append("---\nsidebar_position: 3\n---\n\n\n"), true).toMarkdown())
                .addItem(doSecurityPartBuilder(MarkdownBuilder.create().append("---\nsidebar_position: 4\n---\n\n\n"), true).toMarkdown())
                .addItem(doGlobalRequestParamsPartBuilder(MarkdownBuilder.create().append("---\nsidebar_position: 5\n---\n\n\n"), true).toMarkdown())
                .addItem(doGlobalRequestHeadersPartBuilder(MarkdownBuilder.create().append("---\nsidebar_position: 6\n---\n\n\n"), true).toMarkdown())
                .addItem(doGlobalResponseHeadersPartBuilder(MarkdownBuilder.create().append("---\nsidebar_position: 7\n---\n\n\n"), true).toMarkdown())
                .addItem(doChangelogPartBuilder(MarkdownBuilder.create().append("---\nsidebar_position: 8\n---\n\n\n"), true).toMarkdown())
                .addItem(doExtensionsPartBuilder(MarkdownBuilder.create().append("---\nsidebar_position: 9\n---\n\n\n"), true).toMarkdown());
        String aboutContent = overviewList.toMarkdown();
        if (StringUtils.isNotBlank(aboutContent)) {
            markdownBuilder.title(AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.about", "About"), 2).p()
                    .append(aboutContent).p();
        }
        String apisTitle = AbstractMarkdown.i18nText(docInfo.getOwner(), "doc.apis", "Apis");
        markdownBuilder.title(apisTitle, 2).p();
        String apisParentPath = String.format("%s/apis", docInfo.getId());
        doWriteCategoryJson(apisParentPath, apisTitle, 10);
        //
        AtomicInteger idx = new AtomicInteger(1);
        if (!docInfo.getApis().isEmpty()) {
            ParagraphList apiList = ParagraphList.create();
            List<GroupInfo> groups = docInfo.getGroups();
            if (!groups.isEmpty()) {
                for (GroupInfo groupInfo : groups) {
                    List<ApiInfo> apiInfos = docInfo.getApis(groupInfo.getName());
                    if (!apiInfos.isEmpty()) {
                        doWriteCategoryJson(String.format("%s/%s", apisParentPath, groupInfo.getName()), groupInfo.getName(), idx.getAndAdd(1));
                        apiList.addItem(groupInfo.getName());
                        ParagraphList subApiList = ParagraphList.create();
                        doAppendApiActionList(apiInfos, subApiList, String.format("apis/%s", groupInfo.getName()), new AtomicInteger(1));
                        apiList.addBody(subApiList);
                    }
                }
            }
            doAppendApiActionList(docInfo.getApis(null), apiList, StringUtils.EMPTY, idx);
            //
            markdownBuilder.append(apiList.toMarkdown()).p();
        }
        markdownBuilder.append(doAppendixPartBuilder(MarkdownBuilder.create().append("---\nsidebar_position: ").append(String.valueOf(idx.get())).append("\n---\n\n\n"), true));
        doWriteFileContent("overview", markdownBuilder.toMarkdown());
    }

    private void doAppendApiActionList(List<ApiInfo> apiInfos, ParagraphList parent, String parentPath, AtomicInteger idx) throws IOException {
        if (!apiInfos.isEmpty()) {
            for (ApiInfo apiInfo : apiInfos) {
                String apiFilePath = String.format("api-%s", RegExUtils.replaceAll(EntityMeta.fieldNameToPropertyName(StringUtils.substringAfterLast(apiInfo.getId(), "."), 0), "_", "-"));
                MarkdownBuilder markdownBuilder = MarkdownBuilder.create()
                        .append("---\nsidebar_position: ")
                        .append(String.valueOf(idx.getAndAdd(1)))
                        .append("\n---\n\n\n")
                        .append(apiInfo);
                if (StringUtils.isNotBlank(parentPath)) {
                    parent.addSubItem(Link.create(apiInfo.getName(), String.format("%s/%s", parentPath, apiFilePath)).toMarkdown());
                    doWriteFileContent(String.format("%s/%s", parentPath, String.format("%s.md", apiFilePath)), markdownBuilder.toMarkdown());
                } else {
                    parent.addItem(Link.create(apiInfo.getName(), String.format("apis/%s", apiFilePath)).toMarkdown());
                    doWriteFileContent(String.format("apis/%s.md", apiFilePath), markdownBuilder.toMarkdown());
                }
            }
        }
    }

    @Override
    public String render() throws IOException {
        doWriteCategoryJson();
        doWriteContent();
        return "SUCCEED";
    }
}
