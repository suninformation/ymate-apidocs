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
import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.annotation.RequestParam;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import org.apache.commons.lang.StringUtils;

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
        List<DocsInfo> _docs = new ArrayList<DocsInfo>(Docs.get().getDocsMap().values());
        return View.jspView(IDocs.MODULE_NAME + "/index").addAttribute("_docs", _docs);
    }

    @RequestMapping("/content")
    public IView content(@RequestParam String doc) throws Exception {
        Map<String, DocsInfo> _docs = Docs.get().getDocsMap();
        if (StringUtils.isBlank(doc) || !_docs.keySet().contains(doc)) {
            return View.httpStatusView(404);
        }
        DocsInfo _docsInfo = _docs.get(doc);
        List<NavItem> _navs = new ArrayList<NavItem>();
        NavItem _overview = new NavItem(I18N.formatMessage("apidocs-messages", "apidocs.content.overview", "Overview"), "overview")
                .addSubItem(new NavItem(I18N.formatMessage("apidocs-messages", "apidocs.content.basic", "Basic"), "basic"));
        if (!_docsInfo.getChangelogs().isEmpty()) {
            _overview.addSubItem(new NavItem(I18N.formatMessage("apidocs-messages", "apidocs.content.changelog", "Changelog"), "changelog"));
        }
        if (!_docsInfo.getExtensions().isEmpty()) {
            _overview.addSubItem(new NavItem(I18N.formatMessage("apidocs-messages", "apidocs.content.extensions", "Extensions"), "extensions"));
        }
        _navs.add(_overview);
        _navs.addAll(NavItem.create(_docsInfo.getApis()));
        //
        return View.jspView(IDocs.MODULE_NAME + "/content")
                .addAttribute("_docInfo", _docsInfo)
                .addAttribute("_navs", _navs);
    }

    @RequestMapping("/download")
    public IView download(@RequestParam String doc) throws Exception {
        Map<String, DocsInfo> _docs = Docs.get().getDocsMap();
        if (StringUtils.isBlank(doc) || !_docs.keySet().contains(doc)) {
            return View.httpStatusView(404);
        }
        DocsInfo _docsInfo = _docs.get(doc);
        return View.jsonView(_docsInfo);
    }
}
