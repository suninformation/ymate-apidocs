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
 * 用于配合@ApiResponses注解生成示例代码
 *
 * @author 刘镇 (suninformation@163.com) on 2020/03/01 19:55
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiExampleGenerate {

    /**
     * @return 示例名称
     */
    String name() default "";

    /**
     * @return 示例描述
     */
    String description() default "";

    /**
     * @return 是否使用分页
     */
    boolean paging() default false;
}
