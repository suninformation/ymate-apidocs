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
 * @author 刘镇 (suninformation@163.com) on 2019-11-03 13:37
 * @since 2.0.0
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ApiServers.class)
public @interface ApiServer {

    String SCHEMES_HTTPS = "https";

    String SCHEMES_HTTP = "http";

    /**
     * @return 模式
     */
    String[] schemes() default {SCHEMES_HTTPS, SCHEMES_HTTP};

    /**
     * @return 主机访问域名或IP地址
     */
    String host();

    /**
     * @return 描述
     */
    String description() default "";
}
