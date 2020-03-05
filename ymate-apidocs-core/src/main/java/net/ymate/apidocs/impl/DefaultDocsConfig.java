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
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/01 00:34
 */
public class DefaultDocsConfig implements IDocsConfig {

    private boolean enabled = true;

    private String i18nResourceName;

    private boolean initialized;

    public static DefaultDocsConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultDocsConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultDocsConfig(moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultDocsConfig() {
    }

    private DefaultDocsConfig(IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        enabled = configReader.getBoolean(ENABLED, true);
        i18nResourceName = configReader.getString(I18N_RESOURCE_NAME, IDocs.MODULE_NAME.replace('.', '_'));
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

        public DefaultDocsConfig build() {
            return config;
        }
    }
}