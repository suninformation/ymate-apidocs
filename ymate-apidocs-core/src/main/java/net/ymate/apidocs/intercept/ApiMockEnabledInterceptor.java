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
package net.ymate.apidocs.intercept;

import net.ymate.apidocs.Docs;
import net.ymate.apidocs.annotation.Api;
import net.ymate.apidocs.annotation.ApiGenerateResponseExample;
import net.ymate.apidocs.annotation.ApiResponses;
import net.ymate.apidocs.annotation.Apis;
import net.ymate.apidocs.base.ResponseTypeInfo;
import net.ymate.platform.core.beans.intercept.AbstractInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptContext;
import net.ymate.platform.core.beans.intercept.InterceptException;
import net.ymate.platform.core.persistence.impl.DefaultResultSet;
import net.ymate.platform.webmvc.IWebResult;
import net.ymate.platform.webmvc.util.WebResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/20 上午 09:48
 */
public final class ApiMockEnabledInterceptor extends AbstractInterceptor {

    private static final Log LOG = LogFactory.getLog(ApiMockEnabledInterceptor.class);

    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        return null;
    }

    @Override
    protected Object after(InterceptContext context) throws InterceptException {
        if (context.getResultObject() == null) {
            ApiResponses apiResponses = context.getTargetMethod().getAnnotation(ApiResponses.class);
            if (apiResponses != null) {
                ApiGenerateResponseExample apiGenerateResponseExample = context.getTargetMethod().getAnnotation(ApiGenerateResponseExample.class);
                if (apiGenerateResponseExample != null && !Void.class.equals(apiResponses.type())) {
                    try {
                        Object instance = ResponseTypeInfo.create(apiResponses.type());
                        if (instance != null) {
                            if (apiGenerateResponseExample.paging()) {
                                instance = new DefaultResultSet<>(Collections.singletonList(instance), 1, 20, 1);
                            } else if (apiResponses.multiple() || apiResponses.type().isArray()) {
                                instance = Collections.singletonList(instance);
                            }
                            ApiMockEnabled apiMockEnabledAnn = findInterceptAnnotation(context, ApiMockEnabled.class);
                            if (apiMockEnabledAnn.useWebResult()) {
                                IWebResult<?> result = WebResult.builder().succeed().data(instance).build().keepNullValue();
                                Api apiAnn = context.getTargetClass().getAnnotation(Api.class);
                                if (apiAnn != null) {
                                    @SuppressWarnings("unchecked")
                                    Package apisPackage = context.getOwner().getModuleManager().getModule(Docs.class).apisPackageLookup((Class<? extends Api>) context.getTargetClass());
                                    Apis apisAnn = apisPackage.getAnnotation(Apis.class);
                                    if (apisAnn.snakeCase()) {
                                        result.snakeCase();
                                    }
                                }
                                instance = result;
                            }
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Results data mocked: YES");
                            }
                            return instance;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return null;
    }
}