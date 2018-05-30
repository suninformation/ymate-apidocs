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

import net.ymate.apidocs.annotation.Api;
import net.ymate.apidocs.core.base.DocsInfo;
import net.ymate.platform.core.YMP;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/04/15 下午 12:07
 * @version 1.0
 */
public interface IDocs {

    String MODULE_NAME = "module.docs";

    /**
     * @return 返回所属YMP框架管理器实例
     */
    YMP getOwner();

    /**
     * @return 返回模块配置对象
     */
    IDocsModuleCfg getModuleCfg();

    /**
     * @return 返回模块是否已初始化
     */
    boolean isInited();

    /**
     * 注册API文档接口
     *
     * @param targetClass 目标类型
     */
    void registerApi(Class<? extends Api> targetClass);

    /**
     * @return 返回所有文档
     */
    Map<String, DocsInfo> getDocsMap();
}