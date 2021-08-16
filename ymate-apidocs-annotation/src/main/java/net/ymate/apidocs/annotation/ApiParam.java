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
 * 接口方法参数
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/14 23:36
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ApiParams.class)
public @interface ApiParam {

    /**
     * @return 参数名称
     */
    String value() default "";

    /**
     * @return 参数说明
     */
    String description() default "";

    /**
     * @return 参数默认值
     */
    String defaultValue() default "";

    /**
     * @return 自定生成请求和响应报文示例时，为参数设置示例值
     */
    String demoValue() default "";

    /**
     * @return 参数可选值集合
     */
    String[] allowValues() default {};

    /**
     * @return 参数是否必须
     */
    boolean required() default false;

    /**
     * @return 参数类型
     */
    Class<?> type() default String.class;

    /**
     * @return 最小长度
     */
    long minLength() default 0;

    /**
     * @return 最大长度
     */
    long maxLength() default 0;

    /**
     * @return 是否为模型对象
     */
    boolean model() default false;

    /**
     * @return 是否为数组集合
     */
    boolean multiple() default false;

    /**
     * @return 是否为文件上传
     * @since 2.0.0
     */
    boolean multipart() default false;

    /**
     * @return 是否为路径变量
     * @since 2.0.0
     */
    boolean pathVariable() default false;

    /**
     * @return 参数名称是否使用蛇形命名法（即用下划线将单词连接起来）
     * @since 2.0.0
     */
    boolean snakeCase() default false;

    /**
     * @return 简单参数示例
     */
    String example() default "";

    /**
     * @return 参数示例
     */
    ApiExample[] examples() default {};

    /**
     * @return 是否隐藏
     */
    boolean hidden() default false;
}
