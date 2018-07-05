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
 * 接口访问权限
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/15 上午3:42
 * @version 1.0
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiSecurity {

    /**
     * @return 角色集合
     */
    ApiRole[] roles() default {};

    /**
     * @return 权限码集合
     */
    ApiPermission[] value() default {};

    /**
     * @return 逻辑类型
     */
    LogicType logicType() default LogicType.OR;

    /**
     * @return 描述
     */
    String description() default "";

    enum LogicType {
        AND, OR
    }
}
