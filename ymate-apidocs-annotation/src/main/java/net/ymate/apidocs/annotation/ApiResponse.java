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
package net.ymate.apidocs.annotation;

import java.lang.annotation.*;

/**
 * 接口方法响应信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/15 上午2:29
 * @version 1.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResponse {

    /**
     * @return HTTP响应状态值
     */
    int httpStatus() default 200;

    /**
     * @return 业务响应码
     */
    String code();

    /**
     * @return 响应信息
     */
    String message();

    /**
     * @return 响应头信息集合
     */
    ApiHeader[] headers() default {};
}
