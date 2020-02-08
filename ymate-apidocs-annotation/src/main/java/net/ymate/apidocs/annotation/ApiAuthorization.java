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
package net.ymate.apidocs.annotation;

import java.lang.annotation.*;

/**
 * 定义接口授权验证信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 02:49
 */
@Target({ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiAuthorization {

    /**
     * @return 授权类型名称
     */
    String value();

    /**
     * @return 授权服务URl地址
     */
    String url();

    /**
     * @return 授权方式
     */
    String type() default "";

    /**
     * @return 令牌名称
     */
    String tokenName() default "";

    /**
     * @return 令牌存储方式
     */
    TokenStore tokenStore() default TokenStore.PARAMETER;

    /**
     * 令牌HTTP请求类型，如：GET, POST等
     */
    String requestType() default "POST";

    /**
     * @return 令牌请求参数集合
     */
    ApiParam[] requestParams() default {};

    /**
     * @return 授权范围集合
     */
    ApiScope[] scopes() default {};

    /**
     * @return 描述
     */
    String description() default "";

    /**
     * 令牌存储方式枚举
     */
    enum TokenStore {

        /**
         * 请求头
         */
        HEADER,

        /**
         * 请求参数
         */
        PARAMETER
    }
}
