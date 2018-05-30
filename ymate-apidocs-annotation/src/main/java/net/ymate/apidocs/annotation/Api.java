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
 * 声明一个类为API接口并支持文档自动生成
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/2 上午4:19
 * @version 1.0
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
     * @return 所属API分组
     */
    String group() default "";

    /**
     * @return 接口方法分组
     */
    ApiGroup[] groups() default {};

    /**
     * @return 参数集合
     */
    ApiParam[] params() default {};

    /**
     * @return 接口变更记录
     */
    ApiChangelog[] changelog() default {};

    /**
     * @return API接口描述
     */
    String description() default "";

    /**
     * @return 是否隐藏此接口
     */
    boolean hidden() default false;
}
