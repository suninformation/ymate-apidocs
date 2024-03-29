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
 * 声明API接口文档信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 00:52
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Apis {

    /**
     * @return 文档标题
     */
    String title();

    /**
     * @return 版本信息
     */
    String version();

    /**
     * @return 是否使用蛇形命名法输出属性名称（即用下划线将单词连接起来）
     * @since 2.0.0
     */
    boolean snakeCase() default false;

    /**
     * @return 自定义排序
     * @since 2.0.0
     */
    int order() default 0;

    /**
     * @return 文档描述
     */
    String description() default "";
}
