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
 * 变更记录信息
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/15 上午1:37
 * @version 1.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiChangelog {

    /**
     * @return 日期，格式如：2018-04-15 01:37
     */
    String date();

    /**
     * @return 变更动作，默认为：UPDATE
     */
    Action action() default Action.UPDATE;

    /**
     * @return 变更作者信息
     */
    ApiAuthor author();

    /**
     * @return 变更内容描述
     */
    String description() default "";

    /**
     * 动作枚举
     */
    enum Action {
        /**
         * 创建
         */
        CREATE,

        /**
         * 更新
         */
        UPDATE,

        /**
         * 新增
         */
        ADD,

        /**
         * 移除
         */
        REMOVE,

        /**
         * 修复
         */
        FIX
    }
}
