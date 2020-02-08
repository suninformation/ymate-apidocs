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
 * 声明一个API接口方法
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/14 23:35
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiAction {

    /**
     * @return 接口方法显示名称
     */
    String value();

    /**
     * @return 接口方法描述
     */
    String description() default "";

    /**
     * @return 请求URL地址映射
     */
    String mapping() default "";

    /**
     * @return 接口方法提示内容
     */
    String[] notes() default {};

    /**
     * @return 接口方法所属分组
     */
    String group() default "";

    /**
     * @return HTTP请求响应状态值
     */
    int httpStatus() default 200;

    /**
     * @return HTTP请求方法，如：GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE等
     */
    String[] httpMethod() default {};

    /**
     * @return 请求ContentType类型, 可选值: json|xml, 默认空表示标准HTTP请求
     * @since 2.0.0
     */
    String requestType() default "";

    /**
     * @return 授权范围集合
     * @since 2.0.0
     */
    String[] scopes() default {};

    /**
     * @return 自定义排序
     * @since 2.0.0
     */
    int order() default 0;

    /**
     * @return 是否隐藏
     */
    boolean hidden() default false;
}
