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
 * 接口方法参数
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/14 下午11:36
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {

    /**
     * @return 参数名称
     */
    String name() default "";

    /**
     * 参数说明
     */
    String value() default "";

    /**
     * @return 参数默认值
     */
    String defaultValue() default "";

    /**
     * @return 参数可选值
     */
    String allowValues() default "";

    /**
     * @return 参数是否必须
     */
    boolean required() default false;

    /**
     * @return 参数类型
     */
    String type() default "";

    /**
     * @return 是否为模型对象
     */
    boolean model() default false;

    /**
     * @return 是否为数组集合
     */
    boolean multiple() default false;

    /**
     * @return 参数示例
     */
    ApiExample[] examples() default {};

    /**
     * @return 是否隐藏此接口
     */
    boolean hidden() default false;
}
