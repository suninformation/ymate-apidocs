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
 * 声明一个API接口方法
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/14 下午11:35
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiAction {

    /**
     * @return 接口方法显示示名称
     */
    String value();

    /**
     * @return 接口方法描述
     */
    String description() default "";

    /**
     * @return 请求URL地址映射
     */
    String mapping();

    /**
     * @return 接口方法提示内容
     */
    String notes() default "";

    /**
     * @return 接口方法所属分组
     */
    String[] groups() default {};

    /**
     * @return HTTP请求响应状态值
     */
    int httpStatus() default 200;

    /**
     * @return HTTP请求方法，如：GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE等
     */
    String[] httpMethod() default {};

    /**
     * @return HTTP请求头信息集合
     */
    ApiHeader[] headers() default {};

    /**
     * @return 接口方法变更记录
     */
    ApiChangelog[] changelog() default {};

    /**
     * @return 扩展信息集合
     */
    ApiExtension[] extensions() default @ApiExtension(properties = @ApiExtensionProperty(name = "", value = ""));

    /**
     * @return 接口示例
     */
    ApiExample[] examples() default {};

    /**
     * @return 是否隐藏此接口
     */
    boolean hidden() default false;
}
