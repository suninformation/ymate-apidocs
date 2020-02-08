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
package net.ymate.apidocs.handle;

import net.ymate.apidocs.IDocs;
import net.ymate.apidocs.annotation.Api;
import net.ymate.platform.core.beans.IBeanHandler;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 12:27
 */
public class DocsHandler implements IBeanHandler {

    private final IDocs owner;

    public DocsHandler(IDocs owner) {
        this.owner = owner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(Class<?> targetClass) throws Exception {
        if (targetClass.isAnnotationPresent(Api.class)) {
            owner.registerApi((Class<? extends Api>) targetClass);
        }
        return null;
    }
}
