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
import net.ymate.apidocs.annotation.ApiServer;
import net.ymate.apidocs.annotation.ApiServers;
import net.ymate.platform.commons.markdown.IMarkdown;
import net.ymate.platform.commons.markdown.MarkdownBuilder;
import net.ymate.platform.commons.markdown.Table;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/05 18:54
 */
public class ServerInfo implements IMarkdown {

    public static ServerInfo create(String name) {
        return new ServerInfo(name);
    }

    public static List<ServerInfo> create(ApiServers apiServers) {
        if (apiServers != null) {
            return Arrays.stream(apiServers.value()).map(ServerInfo::create).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static ServerInfo create(ApiServer apiServer) {
        if (apiServer != null && StringUtils.isNotBlank(apiServer.host())) {
            return new ServerInfo(apiServer.host())
                    .addSchemes(Arrays.asList(apiServer.schemes()))
                    .setDescription(apiServer.description());
        }
        return null;
    }

    public static String toMarkdown(IDocs owner, List<ServerInfo> servers) {
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        if (!servers.isEmpty()) {
            markdownBuilder.append(Table.create()
                    .addHeader(AbstractMarkdown.i18nText(owner, "server.host", "Host"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "server.schemes", "Schemes"), Table.Align.LEFT)
                    .addHeader(AbstractMarkdown.i18nText(owner, "server.description", "Description"), Table.Align.LEFT));
            servers.forEach(markdownBuilder::append);
        }
        return markdownBuilder.toMarkdown();
    }

    /**
     * 主机访问域名或IP地址
     */
    private String host;

    /**
     * 模式
     */
    private final List<String> schemes = new ArrayList<>();

    /**
     * 描述信息
     */
    private String description;

    public ServerInfo(String host) {
        if (StringUtils.isBlank(host)) {
            throw new NullArgumentException("host");
        }
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public List<String> getSchemes() {
        return schemes;
    }

    public ServerInfo addSchemes(List<String> schemes) {
        if (schemes != null) {
            schemes.forEach(this::addScheme);
        }
        return this;
    }

    public ServerInfo addScheme(String scheme) {
        if (StringUtils.isNotBlank(scheme)) {
            if (!this.schemes.contains(scheme)) {
                this.schemes.add(scheme);
            }
        }
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ServerInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toMarkdown() {
        Table.Row row = Table.create().addRow().addColumn(host);
        MarkdownBuilder markdownBuilder = MarkdownBuilder.create();
        for (String scheme : schemes) {
            markdownBuilder.code(scheme).space();
        }
        return row.addColumn(markdownBuilder).addColumn(description).build().toMarkdown();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return host.equals(((ServerInfo) o).host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host);
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
