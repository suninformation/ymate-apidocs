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
 * 自定义属性
 *
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 00:00
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiProperty {

    /**
     * @return 属性名称
     */
    String name() default "";

    /**
     * @return 属性值
     */
    String value() default "";

    /**
     * @return 自定生成请求和响应报文示例时，为属性设置示例值
     */
    String demoValue() default "";

    /**
     * @return 是否为模型对象
     */
    boolean model() default false;

    /**
     * @return 指定模型对象类型(主要用于当成员对象为集合类型时)
     */
    Class<?> modelClass() default Void.class;

    /**
     * @return 属性描述
     */
    String description() default "";
}
