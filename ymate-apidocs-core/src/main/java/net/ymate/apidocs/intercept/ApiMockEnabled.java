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

import net.ymate.platform.core.beans.annotation.InterceptAnnotation;
import net.ymate.platform.core.beans.intercept.IInterceptor;

import java.lang.annotation.*;

/**
 * 开启响应数据模拟（仅当方法返回值为空时）
 *
 * @author 刘镇 (suninformation@163.com) on 2020/03/20 09:15
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterceptAnnotation(IInterceptor.Direction.AFTER)
public @interface ApiMockEnabled {

    /**
     * @return 使用WebResult对象包装
     */
    boolean useWebResult() default true;
}