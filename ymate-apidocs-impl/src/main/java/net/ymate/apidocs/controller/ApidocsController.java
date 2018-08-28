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
package net.ymate.apidocs.controller;

import net.ymate.apidocs.NavItem;
import net.ymate.apidocs.core.Docs;
import net.ymate.apidocs.core.IDocs;
import net.ymate.apidocs.core.base.DocsInfo;
import net.ymate.apidocs.intercept.ApidocsStatusInterceptor;
import net.ymate.platform.core.beans.annotation.Before;
import net.ymate.platform.core.i18n.I18N;
import net.ymate.platform.core.util.DateTimeUtils;
import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.annotation.RequestParam;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import net.ymate.platform.webmvc.view.impl.BinaryView;
import net.ymate.platform.webmvc.view.impl.HttpStatusView;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/5/11 上午1:27
 * @version 1.0
 */
@Controller
@RequestMapping("/apidocs")
@Before(ApidocsStatusInterceptor.class)
public class ApidocsController {

    @RequestMapping("/")
    public IView index() throws Exception {
        List<DocsInfo> docs = new ArrayList<DocsInfo>(Docs.get().getDocsMap().values());
        return View.jspView(IDocs.MODULE_NAME + "/index").addAttribute("_docs", docs);
    }

    @RequestMapping("/content")
    public IView content(@RequestParam String doc) throws Exception {
        Map<String, DocsInfo> docs = Docs.get().getDocsMap();
        if (StringUtils.isBlank(doc) || !docs.keySet().contains(doc)) {
            return View.httpStatusView(404);
        }
        DocsInfo docsInfo = docs.get(doc);
        List<NavItem> navs = new ArrayList<NavItem>();
        NavItem overview = new NavItem(I18N.formatMessage("apidocs-messages", "apidocs.content.overview", "Overview"), "overview")
                .addSubItem(new NavItem(I18N.formatMessage("apidocs-messages", "apidocs.content.basic", "Basic"), "basic"));
        if (!docsInfo.getChangelogs().isEmpty()) {
            overview.addSubItem(new NavItem(I18N.formatMessage("apidocs-messages", "apidocs.content.changelog", "Changelog"), "changelog"));
        }
        if (!docsInfo.getExtensions().isEmpty()) {
            overview.addSubItem(new NavItem(I18N.formatMessage("apidocs-messages", "apidocs.content.extensions", "Extensions"), "extensions"));
        }
        navs.add(overview);
        navs.addAll(NavItem.create(docsInfo.getApis()));
        //
        return View.jspView(IDocs.MODULE_NAME + "/content")
                .addAttribute("_docInfo", docsInfo)
                .addAttribute("_navs", navs);
    }

    @RequestMapping("/download")
    public IView download(@RequestParam String doc, @RequestParam(defaultValue = "doc") String type) throws Exception {
        Map<String, DocsInfo> docs = Docs.get().getDocsMap();
        if (StringUtils.isBlank(doc) || !docs.keySet().contains(doc)) {
            return View.httpStatusView(404);
        }
        DocsInfo docsInfo = docs.get(doc);
        String attachName = docsInfo.getTitle() + "_" + docsInfo.getVersion() + "_" + DateTimeUtils.formatTime(System.currentTimeMillis(), "_yyyyMMdd_HHmm_ss");
        if (StringUtils.equalsIgnoreCase(type, "markdown")) {
            return new BinaryView(docsInfo.toMarkdown().getBytes(WebContext.getRequest().getCharacterEncoding())).useAttachment(attachName + ".md");
        } else if (StringUtils.equalsIgnoreCase(type, "doc")) {
            File tmpFile = File.createTempFile(IDocs.MODULE_NAME + "_", ".doc");
            View.jspView(IDocs.MODULE_NAME + "/content_download").addAttribute("_docInfo", docsInfo).render(new FileOutputStream(tmpFile));
            return View.binaryView(tmpFile).useAttachment(attachName + ".doc");
        }
        return HttpStatusView.BAD_REQUEST;
    }
}
