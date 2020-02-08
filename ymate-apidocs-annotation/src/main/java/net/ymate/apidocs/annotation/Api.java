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
 * 声明一个类为API接口并支持文档自动生成
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/02 04:19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Api {

    /**
     * @return API接口名称
     */
    String value();

    /**
     * @return 请求URL地址映射
     * @since 2.0.0
     */
    String mapping() default "";

    /**
     * @return 所属分组名称
     */
    String group() default "";

    /**
     * @return 自定义排序
     * @since 2.0.0
     */
    int order() default 0;

    /**
     * @return API接口描述
     */
    String description() default "";

    /**
     * @return 授权范围集合
     * @since 2.0.0
     */
    String[] scopes() default {};

    /**
     * @return 是否隐藏
     */
    boolean hidden() default false;
}
