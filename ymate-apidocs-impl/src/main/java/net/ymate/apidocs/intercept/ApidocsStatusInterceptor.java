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
package net.ymate.apidocs.intercept;

import net.ymate.apidocs.core.Docs;
import net.ymate.apidocs.core.IDocsModuleCfg;
import net.ymate.platform.core.beans.intercept.AbstractInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptContext;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.View;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/5/11 上午3:12
 * @version 1.0
 */
public class ApidocsStatusInterceptor extends AbstractInterceptor {

    @Override
    protected Object __before(InterceptContext context) throws Exception {
        IDocsModuleCfg _moduleCfg = Docs.get().getModuleCfg();
        if (_moduleCfg.isDisabled()) {
            return View.httpStatusView(403);
        }
        WebContext.getRequest().setAttribute("__title", _moduleCfg.getTitle());
        WebContext.getRequest().setAttribute("__brand", _moduleCfg.getBrand());
        WebContext.getRequest().setAttribute("__description", _moduleCfg.getDescription());
        WebContext.getRequest().setAttribute("__extendParams", _moduleCfg.getParams());
        return null;
    }

    @Override
    protected Object __after(InterceptContext context) throws Exception {
        return null;
    }
}
