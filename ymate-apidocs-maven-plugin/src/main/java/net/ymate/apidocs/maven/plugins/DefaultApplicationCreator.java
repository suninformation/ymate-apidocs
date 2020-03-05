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

import net.ymate.apidocs.impl.DefaultDocsConfigurable;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.commons.util.ResourceUtils;
import net.ymate.platform.core.*;
import net.ymate.platform.core.i18n.II18nEventHandler;
import net.ymate.platform.core.impl.DefaultApplicationConfigureParser;
import net.ymate.platform.validation.IValidation;
import net.ymate.platform.webmvc.IWebMvc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/08 17:03
 */
public class DefaultApplicationCreator implements IApplicationCreator {

    @Override
    public IApplication create(Class<?> mainClass, String[] args, IApplicationInitializer... applicationInitializers) throws Exception {
        return new Application(ApplicationConfigureBuilder.builder(DefaultApplicationConfigureParser.defaultEmpty())
                .runEnv(IApplication.Environment.DEV)
                .i18nEventHandler(new II18nEventHandler() {

                    @Override
                    public Locale onLocale() {
                        return null;
                    }

                    @Override
                    public void onChanged(Locale locale) {
                    }

                    @Override
                    public InputStream onLoad(String resourceName) throws IOException {
                        return ResourceUtils.getResourceAsStream("META-INF/" + resourceName, getClass());
                    }
                })
                .excludedModules(IWebMvc.MODULE_NAME, ICaches.MODULE_NAME, IValidation.MODULE_NAME)
                .addModuleConfigurers(DefaultDocsConfigurable.builder().build()).build());
    }
}
