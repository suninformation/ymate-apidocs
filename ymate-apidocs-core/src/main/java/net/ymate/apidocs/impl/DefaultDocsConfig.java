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
package net.ymate.apidocs.impl;

import net.ymate.apidocs.IDocs;
import net.ymate.apidocs.IDocsConfig;
import net.ymate.apidocs.annotation.DocsConf;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/01 00:34
 */
public final class DefaultDocsConfig implements IDocsConfig {

    private boolean enabled = true;

    private String i18nResourceName;

    private final Set<String> ignoredRequestMethods = new HashSet<>();

    private boolean initialized;

    public static DefaultDocsConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultDocsConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultDocsConfig(null, moduleConfigurer);
    }

    public static DefaultDocsConfig create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        return new DefaultDocsConfig(mainClass, moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultDocsConfig() {
    }

    private DefaultDocsConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        DocsConf confAnn = mainClass == null ? null : mainClass.getAnnotation(DocsConf.class);
        //
        enabled = configReader.getBoolean(ENABLED, confAnn == null || confAnn.enabled());
        i18nResourceName = configReader.getString(I18N_RESOURCE_NAME, StringUtils.defaultIfBlank(confAnn != null ? confAnn.i18nResourceName() : null, IDocs.MODULE_NAME.replace('.', '_')));
        String[] ignoredMethodNames = configReader.getArray(IGNORED_REQUEST_METHODS, confAnn != null ? confAnn.ignoredRequestMethods() : new String[0]);
        Arrays.stream(ignoredMethodNames).filter(StringUtils::isNotBlank).map(String::toUpperCase).forEach(ignoredRequestMethods::add);
    }

    @Override
    public void initialize(IDocs owner) throws Exception {
        if (!initialized) {
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (!initialized) {
            this.enabled = enabled;
        }
    }

    @Override
    public String getI18nResourceName() {
        return i18nResourceName;
    }

    @Override
    public Set<String> getIgnoredRequestMethods() {
        return ignoredRequestMethods;
    }

    public void addIgnoredRequestMethod(String ignoredRequestMethod) {
        if (!initialized) {
            if (StringUtils.isNotBlank(ignoredRequestMethod)) {
                this.ignoredRequestMethods.add(ignoredRequestMethod.toUpperCase());
            }
        }
    }

    public void setI18nResourceName(String i18nResourceName) {
        this.i18nResourceName = i18nResourceName;
    }

    public static final class Builder {

        private final DefaultDocsConfig config = new DefaultDocsConfig();

        private Builder() {
        }

        public Builder enabled(boolean enabled) {
            config.setEnabled(enabled);
            return this;
        }

        public Builder i18nResourceName(String i18nResourceName) {
            config.setI18nResourceName(i18nResourceName);
            return this;
        }

        public Builder addIgnoredRequestMethods(String... ignoredRequestMethods) {
            if (ignoredRequestMethods != null && ignoredRequestMethods.length > 0) {
                Arrays.stream(ignoredRequestMethods).forEach(config::addIgnoredRequestMethod);
            }
            return this;
        }

        public DefaultDocsConfig build() {
            return config;
        }
    }
}