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
package net.ymate.apidocs.render;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.ymate.apidocs.AbstractDocRender;
import net.ymate.apidocs.base.*;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/26 14:51
 */
public class PostmanDocRender extends AbstractDocRender {

    public PostmanDocRender(DocInfo docInfo) {
        super(docInfo);
    }

    private ParamPart processParamInfo(ParamInfo paramInfo) {
        ParamPart paramPart = new ParamPart();
        paramPart.setDescription(paramInfo.getDescription());
        paramPart.setKey(paramInfo.getName());
        paramPart.setValue(paramInfo.getDemoValue());
        paramPart.setType(paramInfo.isMultipart() ? "file" : "text");
        return paramPart;
    }

    private HeaderPart processParamInfo(HeaderInfo headerInfo) {
        HeaderPart headerPart = new HeaderPart();
        headerPart.setKey(headerInfo.getName());
        headerPart.setValue(headerInfo.getValue());
        headerPart.setType("text");
        headerPart.setDescription(headerInfo.getDescription());
        return headerPart;
    }

    private List<ItemPart> processActionInfo(ActionInfo actionInfo) {
        List<ItemPart> items = new ArrayList<>();
        if (actionInfo != null) {
            ServerInfo serverInfo = actionInfo.getApiInfo().getDocInfo().getServers().stream().findFirst().orElse(null);
            URL url;
            if (serverInfo != null) {
                try {
                    String[] hosts = StringUtils.split(serverInfo.getHost(), ":");
                    url = new URL(serverInfo.getSchemes().stream().findFirst().orElse("http"), hosts[0], hosts.length > 1 ? Integer.parseInt(hosts[1]) : 0, actionInfo.getMapping());
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            } else {
                try {
                    url = new URL("http", "localhost", 8080, actionInfo.getMapping());
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            }
            //
            for (String method : actionInfo.getMethods()) {
                if (StringUtils.equalsIgnoreCase(method, Type.HttpMethod.OPTIONS.name())) {
                    continue;
                }
                RequestPart requestPart = new RequestPart();
                requestPart.setUrl(new UrlPart(url));
                //
                List<ParamPart> paramParts = actionInfo.getApiInfo().getDocInfo().getParams().stream().filter(paramInfo -> !paramInfo.isPathVariable()).map(this::processParamInfo).collect(Collectors.toList());
                actionInfo.getApiInfo().getParams().stream().filter(paramInfo -> !paramInfo.isPathVariable()).map(this::processParamInfo).forEachOrdered(paramParts::add);
                actionInfo.getParams().stream().filter(paramInfo -> !paramInfo.isPathVariable()).map(this::processParamInfo).forEachOrdered(paramParts::add);
                //
                if (StringUtils.equalsIgnoreCase(method, Type.HttpMethod.GET.name())) {
                    requestPart.getUrl().setQuery(paramParts);
                } else {
                    boolean multipart = actionInfo.getParams().stream().anyMatch(ParamInfo::isMultipart);
                    BodyPart bodyPart;
                    if (multipart) {
                        bodyPart = new BodyPart(BodyMode.FORMDATA);
                        bodyPart.setFormdata(paramParts);
                    } else if (StringUtils.isNotBlank(actionInfo.getRequestType())) {
                        bodyPart = new BodyPart(BodyMode.RAW);
                        switch (actionInfo.getRequestType().toLowerCase()) {
                            case Type.Const.FORMAT_JSON:
                                bodyPart.setOptions(new BodyOptions(new Raw(Type.Const.FORMAT_JSON)));
                                // TODO 转换JSON参数报文
                                break;
                            case Type.Const.FORMAT_XML:
                                bodyPart.setOptions(new BodyOptions(new Raw(Type.Const.FORMAT_XML)));
                                // TODO 转换XML参数报文
                                break;
                            default:
                        }
                    } else {
                        bodyPart = new BodyPart(BodyMode.URLENCODED);
                        bodyPart.setUrlencoded(paramParts);
                    }
                    requestPart.setBody(bodyPart);
                }
                //
                List<HeaderPart> headerParts = actionInfo.getApiInfo().getDocInfo().getRequestHeaders().stream().map(this::processParamInfo).collect(Collectors.toList());
                actionInfo.getApiInfo().getRequestHeaders().stream().map(this::processParamInfo).forEach(headerParts::add);
                actionInfo.getRequestHeaders().stream().map(this::processParamInfo).forEach(headerParts::add);
                requestPart.setHeader(headerParts);
                requestPart.setMethod(method);
                requestPart.setDescription(actionInfo.getDescription());
                //
                ItemPart itemPart = new ItemPart();
                itemPart.setName(String.format("%s %s", StringUtils.defaultIfBlank(actionInfo.getDisplayName(), actionInfo.getName()), actionInfo.getMapping()));
                itemPart.setRequest(requestPart);
                items.add(itemPart);
            }
        }
        return items;
    }

    private ItemPart processApiInfo(ApiInfo apiInfo) {
        ItemPart itemPart = new ItemPart();
        itemPart.setName(apiInfo.getName());
        itemPart.setDescription(apiInfo.getDescription());
        List<ItemPart> items = new ArrayList<>();
        if (apiInfo.getGroups().isEmpty()) {
            apiInfo.getActions().stream().map(this::processActionInfo).forEach(items::addAll);
        } else {
            for (GroupInfo apiGroupInfo : apiInfo.getGroups()) {
                List<ItemPart> groupItems = new ArrayList<>();
                apiInfo.getActions(apiGroupInfo.getName()).stream().map(this::processActionInfo).forEach(groupItems::addAll);
                if (!groupItems.isEmpty()) {
                    ItemPart groupItemPart = new ItemPart();
                    groupItemPart.setName(apiGroupInfo.getName());
                    groupItemPart.setDescription(apiGroupInfo.getDescription());
                    groupItemPart.setItem(groupItems);
                    //
                    items.add(groupItemPart);
                }
            }
            apiInfo.getActions(null).stream().map(this::processActionInfo).forEach(items::addAll);
        }
        if (!items.isEmpty()) {
            itemPart.setItem(items);
            return itemPart;
        }
        return null;
    }

    @Override
    public String render() throws IOException {
        DocInfo docInfo = getDocInfo();
        InfoPart infoPart = new InfoPart(String.format("%s %s", docInfo.getTitle(), docInfo.getVersion()), docInfo.getDescription());
        List<ItemPart> items = new ArrayList<>();
        if (docInfo.getGroups().isEmpty()) {
            docInfo.getApis().stream().map(this::processApiInfo).filter(Objects::nonNull).forEachOrdered(items::add);
        } else {
            for (GroupInfo docGroupInfo : docInfo.getGroups()) {
                List<ItemPart> groupItems = new ArrayList<>();
                docInfo.getApis(docGroupInfo.getName()).stream().map(this::processApiInfo).filter(Objects::nonNull).forEachOrdered(groupItems::add);
                if (!groupItems.isEmpty()) {
                    ItemPart groupItemPart = new ItemPart();
                    groupItemPart.setName(docGroupInfo.getName());
                    groupItemPart.setDescription(docGroupInfo.getDescription());
                    groupItemPart.setItem(groupItems);
                    //
                    items.add(groupItemPart);
                }
            }
            docInfo.getApis(null).stream().map(this::processApiInfo).filter(Objects::nonNull).forEach(items::add);
        }
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.put("info", infoPart);
        jsonObject.put("item", items);
        return jsonObject.toString(SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat);
    }

    static class InfoPart implements Serializable {

        @JSONField(name = "_postman_id")
        private final String id;

        private final String schema;

        private String name;

        private String description;

        public InfoPart(String name, String description) {
            this.id = UUID.randomUUID().toString();
            this.schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
            this.name = name;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSchema() {
            return schema;
        }
    }

    static class ItemPart implements Serializable {

        private String name;

        private String description;

        private List<ItemPart> item;

        private RequestPart request;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<ItemPart> getItem() {
            return item;
        }

        public void setItem(List<ItemPart> item) {
            this.item = item;
        }

        public RequestPart getRequest() {
            return request;
        }

        public void setRequest(RequestPart request) {
            this.request = request;
        }
    }

    static class UrlPart implements Serializable {

        private final String raw;

        private final List<String> host;

        private String port;

        private List<String> path;

        private List<ParamPart> query;

        public UrlPart(URL uri) {
            this.raw = uri.toString();
            String[] hosts = StringUtils.split(uri.getHost(), ".");
            host = new ArrayList<>(Arrays.asList(hosts));
            if (uri.getPort() > 0) {
                port = String.valueOf(uri.getPort());
            }
            String[] paths = StringUtils.split(uri.getPath(), "/");
            if (paths != null) {
                path = new ArrayList<>(Arrays.asList(paths));
            }
        }

        public String getRaw() {
            return raw;
        }

        public List<String> getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public List<String> getPath() {
            return path;
        }

        public List<ParamPart> getQuery() {
            return query;
        }

        public void setQuery(List<ParamPart> query) {
            this.query = query;
        }
    }

    static class ParamPart implements Serializable {

        private String key;

        private String value;

        private String description;

        private String type;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    static class HeaderPart extends ParamPart {
    }

    enum BodyMode {

        /**
         * UrlEncoded
         */
        URLENCODED,

        /**
         * FormData
         */
        FORMDATA,

        /**
         * Raw
         */
        RAW
    }

    static class Raw {

        private final String language;

        public Raw(String language) {
            this.language = language;
        }

        public String getLanguage() {
            return language;
        }
    }

    static class BodyOptions {

        private final Raw raw;

        public BodyOptions(Raw raw) {
            this.raw = raw;
        }

        public Raw getRaw() {
            return raw;
        }
    }

    static class BodyPart implements Serializable {

        private final String mode;

        private String raw;

        private List<ParamPart> urlencoded;

        private List<ParamPart> formdata;

        private BodyOptions options;

        private BodyPart(BodyMode mode) {
            this.mode = mode.name().toLowerCase();
        }

        public String getMode() {
            return mode;
        }

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }

        public List<ParamPart> getUrlencoded() {
            return urlencoded;
        }

        public void setUrlencoded(List<ParamPart> urlencoded) {
            this.urlencoded = urlencoded;
        }

        public List<ParamPart> getFormdata() {
            return formdata;
        }

        public void setFormdata(List<ParamPart> formdata) {
            this.formdata = formdata;
        }

        public BodyOptions getOptions() {
            return options;
        }

        public void setOptions(BodyOptions options) {
            this.options = options;
        }
    }

    static class RequestPart implements Serializable {

        private String method;

        private String description;

        private List<HeaderPart> header;

        private BodyPart body;

        private UrlPart url;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<HeaderPart> getHeader() {
            return header;
        }

        public void setHeader(List<HeaderPart> header) {
            this.header = header;
        }

        public BodyPart getBody() {
            return body;
        }

        public void setBody(BodyPart body) {
            this.body = body;
        }

        public UrlPart getUrl() {
            return url;
        }

        public void setUrl(UrlPart url) {
            this.url = url;
        }
    }
}
