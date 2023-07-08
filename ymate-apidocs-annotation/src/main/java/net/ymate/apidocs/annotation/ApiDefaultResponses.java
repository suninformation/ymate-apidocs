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

import java.io.Serializable;
import java.lang.annotation.*;

/**
 * 注册全局默认接口方法响应信息
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/14 12:11
 */
@Target({ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDefaultResponses {

    /**
     * @return 自定义通用响应报文结构
     */
    Class<? extends Serializable> standardType() default Serializable.class;

    /**
     * @return 自定义分页查询响应报文结构
     */
    Class<? extends Serializable> pagingType() default Serializable.class;

    /**
     * @return 自定义响应码参数名称
     * @since 2.0.1
     */
    String codeParamName() default "";

    /**
     * @return 自定义响应消息描述参数名称
     * @since 2.0.1
     */
    String msgParamName() default "";

    /**
     * @return 自定义响应业务数据参数名称
     * @since 2.0.1
     */
    String dataParamName() default "";

    /**
     * @return 响应报文示例
     */
    ApiExample[] examples() default {};
}
