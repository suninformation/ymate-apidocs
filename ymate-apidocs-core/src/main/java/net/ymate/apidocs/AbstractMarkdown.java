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

import com.alibaba.fastjson.annotation.JSONField;
import net.ymate.platform.commons.markdown.IMarkdown;
import org.apache.commons.lang.NullArgumentException;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/05 17:45
 */
public abstract class AbstractMarkdown implements IMarkdown {

    public static String i18nText(IDocs owner, String i18nKey, String defaultText) {
        return owner.getOwner().getI18n().load(owner.getConfig().getI18nResourceName(), i18nKey, defaultText);
    }

    private final IDocs owner;

    private int markdownTitleLevel;

    public AbstractMarkdown(IDocs owner) {
        if (owner == null) {
            throw new NullArgumentException("owner");
        }
        this.owner = owner;
    }

    @JSONField(serialize = false)
    public IDocs getOwner() {
        return owner;
    }

    public String i18nText(String i18nKey, String defaultText) {
        return i18nText(owner, i18nKey, defaultText);
    }

    public int getMarkdownTitleLevel() {
        return getMarkdownTitleLevel(1);
    }

    public int getMarkdownTitleLevel(int defaultLevel) {
        if (markdownTitleLevel <= 0) {
            return Math.min(defaultLevel, 1);
        }
        return markdownTitleLevel;
    }

    public void setMarkdownTitleLevel(int markdownTitleLevel) {
        this.markdownTitleLevel = markdownTitleLevel;
    }
}
