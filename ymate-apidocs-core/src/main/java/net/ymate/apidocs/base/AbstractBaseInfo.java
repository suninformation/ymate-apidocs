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
package net.ymate.apidocs.base;

import net.ymate.apidocs.AbstractMarkdown;
import net.ymate.apidocs.IDocs;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Table;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/11 14:40
 */
public abstract class AbstractBaseInfo<T extends AbstractBaseInfo<?>> implements IMarkdown {

    public static String toMarkdown(IDocs owner, List<? extends AbstractBaseInfo<?>> roles) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!roles.isEmpty()) {
            markdownBuilder.append(Table.create()
                    .addHeader(AbstractMarkdown.i18nText(owner, "base.name", "Name"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "base.description", "Description"), Table.Align.LEFT));
            roles.forEach(markdownBuilder::append);
        }
        return markdownBuilder.toMarkdown();
    }

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    public AbstractBaseInfo(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @SuppressWarnings("unchecked")
    public T setDescription(String description) {
        this.description = description;
        return (T) this;
    }

    @Override
    public String toMarkdown() {
        return Table.create().addRow().addColumn(name).addColumn(description).build().toMarkdown();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return name.equals(((AbstractBaseInfo<?>) o).getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
