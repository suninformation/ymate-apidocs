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

import net.ymate.apidocs.base.DocInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 15:26
 */
public abstract class AbstractDocRender implements IDocRender {

    private final DocInfo docInfo;

    public AbstractDocRender(DocInfo docInfo) {
        if (docInfo == null) {
            throw new NullArgumentException("docInfo");
        }
        this.docInfo = docInfo;
    }

    public DocInfo getDocInfo() {
        return docInfo;
    }

    @Override
    public void render(OutputStream output) throws IOException {
        IOUtils.write(render(), output, "UTF-8");
    }
}
