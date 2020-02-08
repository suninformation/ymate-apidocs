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

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 11:36
 */
public interface IDocRender {

    /**
     * 文档渲染
     *
     * @return 返回渲染后文档字符串
     * @throws IOException 可能产生的IO异常
     */
    String render() throws IOException;

    /**
     * 文档渲染
     *
     * @param output 视图渲染指定输出流
     * @throws IOException 可能产生的IO异常
     */
    void render(OutputStream output) throws IOException;
}
