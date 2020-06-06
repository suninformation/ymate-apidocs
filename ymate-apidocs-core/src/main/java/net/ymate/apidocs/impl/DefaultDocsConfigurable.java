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
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurable;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/07 15:21
 */
public final class DefaultDocsConfigurable extends DefaultModuleConfigurable {

    public static Builder builder() {
        return new Builder();
    }

    private DefaultDocsConfigurable() {
        super(IDocs.MODULE_NAME);
    }

    public static final class Builder {

        private final DefaultDocsConfigurable configurable = new DefaultDocsConfigurable();

        private Builder() {
        }

        public Builder enabled(boolean enabled) {
            configurable.addConfig(IDocsConfig.ENABLED, String.valueOf(enabled));
            return this;
        }

        public Builder i18nResourceName(String i18nResourceName) {
            configurable.addConfig(IDocsConfig.I18N_RESOURCE_NAME, i18nResourceName);
            return this;
        }

        public Builder ignoredRequestMethods(String ignoredRequestMethods) {
            configurable.addConfig(IDocsConfig.IGNORED_REQUEST_METHODS, StringUtils.trimToEmpty(ignoredRequestMethods));
            return this;
        }

        public IModuleConfigurer build() {
            return configurable.toModuleConfigurer();
        }
    }
}
