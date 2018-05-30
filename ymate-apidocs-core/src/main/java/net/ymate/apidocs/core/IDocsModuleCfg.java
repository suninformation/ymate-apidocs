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
package net.ymate.apidocs.core;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 下午 12:07
 * @version 1.0
 */
public interface IDocsModuleCfg {

    /**
     * @return 设置当前项目文档标题文字, 默认值: ApiDocs
     */
    String getTitle();

    /**
     * @return 设置当前项目文档徽标文字, 默认值: 同title属性
     */
    String getBrand();

    /**
     * @return 设置当前项目文档简介文字, 默认值: "A simple development tool for document generation."
     */
    String getDescription();

    /**
     * @return 是否已禁用接口文档生成服务, 默认值: false
     */
    boolean isDisabled();

    /**
     * @return 文档自定义参数映射
     */
    Map<String, String> getParams();
}