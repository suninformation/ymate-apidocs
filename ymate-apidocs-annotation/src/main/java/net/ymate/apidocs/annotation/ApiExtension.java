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
 * 扩展信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/14 下午11:59
 * @version 1.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiExtension {

    /**
     * @return 扩展名称
     */
    String name() default "";

    /**
     * @return 扩展描述
     */
    String description() default "";

    /**
     * @return 扩展属性集合
     */
    ApiProperty[] properties();
}
