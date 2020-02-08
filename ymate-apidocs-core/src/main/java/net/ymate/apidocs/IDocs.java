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
package net.ymate.apidocs;

import net.ymate.apidocs.annotation.Api;
import net.ymate.apidocs.base.DocInfo;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/01 00:34
 */
@Ignored
public interface IDocs extends IInitialization<IApplication>, IDestroyable {

    String MODULE_NAME = "module.docs";

    /**
     * 获取所属应用容器
     *
     * @return 返回所属应用容器实例
     */
    IApplication getOwner();

    /**
     * 获取配置
     *
     * @return 返回配置对象
     */
    IDocsConfig getConfig();

    /**
     * 根据指定类查找声明了Apis注解的包对象，若找不到则返回默认包对象
     *
     * @param targetClass 目标类
     * @return 返回包对象
     */
    Package apisPackageLookup(Class<? extends Api> targetClass);

    /**
     * 注册API文档接口
     *
     * @param targetClass 目标类型
     * @throws Exception 可能产生的任何异常
     */
    void registerApi(Class<? extends Api> targetClass) throws Exception;

    /**
     * 获取全部已注册文档
     *
     * @return 返回文档映射
     */
    Map<String, DocInfo> getDocs();
}
