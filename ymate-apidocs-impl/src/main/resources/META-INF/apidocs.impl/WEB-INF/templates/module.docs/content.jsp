<%@ page contentType="text/html;charset=UTF-8" language="java" trimDirectiveWhitespaces="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.ymate.net/ymweb_core" prefix="ymweb" %>
<%@ taglib uri="http://www.ymate.net/ymweb_bs" prefix="bs" %>
<ymweb:ui src="module.docs/base">
    <ymweb:property name="title">${__title}</ymweb:property>
    <ymweb:property name="pagePath">../</ymweb:property>
    <ymweb:layout>
        <bs:navbar _class="bs-docs-nav" href="../apidocs" staticTop="false" container="true" collapseId="main">
            <jsp:attribute name="brand">${__brand}</jsp:attribute>
            <jsp:body>
                <bs:nav>
                    <bs:item href="../apidocs"><ymweb:i18n key="apidocs.nav.home" resourceName="apidocs-messages" defaultValue="Home"/></bs:item>
                    <bs:item href="../apidocs/content?doc=${_docInfo.id}" active="true"><ymweb:i18n key="apidocs.nav.content" resourceName="apidocs-messages" defaultValue="Content"/></bs:item>
                    <bs:item href="http://www.ymate.net/"><ymweb:i18n key="apidocs.nav.about" resourceName="apidocs-messages" defaultValue="About"/></bs:item>
                </bs:nav>
                <bs:nav right="true">
                    <bs:item href="https://github.com/suninformation/ymate-apidocs"><bs:icon fa="true" style="github" faW="true">GitHub</bs:icon></bs:item>
                </bs:nav>
            </jsp:body>
        </bs:navbar>
        <div class="bs-docs-header">
            <bs:container>
                <bs:row>
                    <bs:col md="12">
                        <h1 style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${_docInfo.title}</h1>
                        <bs:text lead="true">${_docInfo.description}</bs:text>
                    </bs:col>
                </bs:row>
            </bs:container>
        </div>
        <bs:container _class="bs-doc-container">
            <bs:row>
                <bs:col md="9">
                    <!-- Docs: Overview -->
                    <div class="bs-docs-section">
                        <bs:header _tag="h1" _id="overview"><ymweb:i18n key="apidocs.content.overview" resourceName="apidocs-messages" defaultValue="Overview"/></bs:header>
                        <h2 id="overview-basic"><ymweb:i18n key="apidocs.content.basic" resourceName="apidocs-messages" defaultValue="Basic"/></h2>
                        <p><bs:panel>
                            <bs:table hover="true" condensed="true">
                                <c:if test="${not empty _docInfo.license}">
                                    <tr style="height: 40px;">
                                        <td style="width: 120px; vertical-align: middle;"><strong><ymweb:i18n key="apidocs.content.license" resourceName="apidocs-messages" defaultValue="License"/></strong></td>
                                        <td style="vertical-align: middle;"><c:choose><c:when test="${not empty _docInfo.license.url}"><a href="${_docInfo.license.url}" target="_blank">${_docInfo.license.name}</a></c:when><c:otherwise>${_docInfo.license.name}</c:otherwise></c:choose></td>
                                    </tr>
                                </c:if>
                                <c:if test="${not empty _docInfo.contact}">
                                    <tr style="height: 40px;">
                                        <td style="width: 120px; vertical-align: middle;"><strong><ymweb:i18n key="apidocs.content.author" resourceName="apidocs-messages" defaultValue="Author"/></strong></td>
                                        <td style="vertical-align: middle;"><c:choose><c:when test="${not empty _docInfo.contact.url}"><a href="${_docInfo.contact.url}" target="_blank">${_docInfo.contact.name}</a></c:when><c:otherwise>${_docInfo.contact.name}</c:otherwise></c:choose><c:if test="${not empty _docInfo.contact.email}"> (<a href="mailto:${_docInfo.contact.email}">${_docInfo.contact.email}</a>)</c:if></td>
                                    </tr>
                                </c:if>
                                <tr style="height: 40px;">
                                    <td style="width: 120px; vertical-align: middle;"><strong><ymweb:i18n key="apidocs.content.version" resourceName="apidocs-messages" defaultValue="Version"/></strong></td>
                                    <td style="vertical-align: middle;">${_docInfo.version}</td>
                                </tr>
                            </bs:table>
                        </bs:panel></p>
                        <!-- Overview: Changelog -->
                        <c:if test="${not empty _docInfo.changelogs}">
                            <h2 id="overview-changelog"><ymweb:i18n key="apidocs.content.changelog" resourceName="apidocs-messages" defaultValue="Changelog"/></h2>
                            <c:forEach var="_changelogInfo" items="${_docInfo.changelogs}">
                                <p>${_changelogInfo.date} ${_changelogInfo.action}
                                    <small>by <c:if test="${not empty _changelogInfo.author}"><c:choose>
                                        <c:when test="${not empty _changelogInfo.author.url}"><a href="${_changelogInfo.author.url}" target="_blank">${_changelogInfo.author.name}</a></c:when>
                                        <c:otherwise>${_changelogInfo.author.name}</c:otherwise>
                                    </c:choose><c:if test="${not empty _changelogInfo.author.email}"> (<a href="mailto:${_changelogInfo.author.email}">${_changelogInfo.author.email}</a>)</c:if></c:if></small></p>
                                <bs:blockquote><p>${_changelogInfo.description}</p></bs:blockquote>
                            </c:forEach>
                        </c:if>
                        <!-- Overview: Extensions -->
                        <c:if test="${not empty _docInfo.extensions}">
                            <h2 id="overview-extensions"><ymweb:i18n key="apidocs.content.extensions" resourceName="apidocs-messages" defaultValue="Extensions"/></h2>
                            <c:forEach var="_extensionInfo" items="${_docInfo.extensions}">
                                <p>${_extensionInfo.name}</p>
                                <c:if test="${not empty _extensionInfo.description}"><p>${_extensionInfo.description}</p></c:if>
                                <bs:blockquote>
                                    <c:forEach var="_extensionProp" items="${_extensionInfo.properties}">
                                        <p>${_extensionProp.name}<c:if test="${not empty _extensionProp.description}"> <code>${_extensionProp.description}</code></c:if></p>
                                        <p>${_extensionProp.value}</p>
                                    </c:forEach>
                                </bs:blockquote>
                            </c:forEach>
                        </c:if>
                    </div>
                    <!-- ApiDocs -->
                    <ymweb:i18n var="_ConstParamRequired" key="apidocs.content.param_required" resourceName="apidocs-messages" defaultValue="Required"/>
                    <ymweb:i18n var="_ConstParamModel" key="apidocs.content.param_model" resourceName="apidocs-messages" defaultValue="Model"/>
                    <ymweb:i18n var="_ConstParamArray" key="apidocs.content.param_array" resourceName="apidocs-messages" defaultValue="Array"/>
                    <c:forEach var="_apiInfo" items="${_docInfo.apis}"><!-- Docs: ${_apiInfo.name} -->
                        <div class="bs-docs-section">
                            <bs:header _tag="h1" _id="${_apiInfo.linkUrl}">${_apiInfo.name} <small>${_apiInfo.id}</small></bs:header>
                            <p>${_apiInfo.description}</p>
                            <c:if test="${not empty _apiInfo.changelogs}">
                                <!-- Changelog -->
                                <bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.changelog" resourceName="apidocs-messages" defaultValue="Changelog"/></jsp:attribute>
                                    <jsp:body><bs:list-group><c:forEach var="_changelogInfo" items="${_apiInfo.changelogs}">
                                        <bs:list-item href="#">
                                            <jsp:attribute name="heading">
                                                ${_changelogInfo.date} ${_changelogInfo.action}
                                                <small>by <c:if test="${not empty _changelogInfo.author}"><c:choose><c:when test="${not empty _changelogInfo.author.url}">
                                                    <a href="${_changelogInfo.author.url}" target="_blank">${_changelogInfo.author.name}</a></c:when><c:otherwise>${_changelogInfo.author.name}</c:otherwise></c:choose>
                                                    <c:if test="${not empty _changelogInfo.author.email}"> (<a href="mailto:${_changelogInfo.author.email}">${_changelogInfo.author.email}</a>)</c:if></c:if></small>
                                            </jsp:attribute>
                                            <jsp:body><p>${_changelogInfo.description}</p></jsp:body>
                                        </bs:list-item>
                                    </c:forEach></bs:list-group></jsp:body>
                                </bs:panel>
                            </c:if>
                            <!-- Global Authorization -->
                            <c:if test="${not empty _apiInfo.authType}"><bs:panel>
                                <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.global_authorization_type" resourceName="apidocs-messages" defaultValue="Global authorization type"/></jsp:attribute>
                                <jsp:body><bs:panel-body>
                                    <p>${_apiInfo.authType}</p>
                                </bs:panel-body><c:if test="${not empty _apiInfo.scopes}"><bs:table hover="true" condensed="true" bordered="true" responsive="true">
                                    <thead><tr>
                                        <th><ymweb:i18n key="apidocs.content.authorization_scope" resourceName="apidocs-messages" defaultValue="Authorization scope"/></th>
                                        <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                    </tr></thead>
                                    <tbody><c:forEach var="_apiScope" items="${_apiInfo.scopes}"><tr>
                                        <td>${_apiScope.name}</td>
                                        <td>${_apiScope.description}</td>
                                    </tr></c:forEach></tbody>
                                </bs:table></c:if></jsp:body>
                            </bs:panel></c:if>
                            <!-- Global Security -->
                            <c:if test="${not empty _apiInfo.security}"><bs:panel>
                                <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.global_security" resourceName="apidocs-messages" defaultValue="Global security"/></jsp:attribute>
                                <jsp:body><bs:panel-body>
                                    <p>${_apiInfo.security.description}</p>
                                    <!-- Roles -->
                                    <c:if test="${not empty _apiInfo.security.roles}">
                                        <c:forEach var="_apiRole" items="${_apiInfo.security.roles}"><bs:label style="warning" _style="cursor:pointer" data-tip="tooltip" title="${_apiRole.description}">${_apiRole.name}</bs:label></c:forEach>
                                    </c:if></bs:panel-body>
                                    <!-- Permissions -->
                                    <c:if test="${not empty _apiInfo.security.permissions}">
                                        <bs:table hover="true" condensed="true" bordered="true" responsive="true">
                                            <thead><tr>
                                                <th><ymweb:i18n key="apidocs.content.security_permissions" resourceName="apidocs-messages" defaultValue="Permissions"/></th>
                                                <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                            </tr></thead>
                                            <tbody><c:forEach var="_apiPermission" items="${_apiInfo.security.permissions}"><tr>
                                                <td>${_apiPermission.name}</td>
                                                <td>${_apiPermission.description}</td>
                                            </tr></c:forEach></tbody>
                                        </bs:table></c:if>
                                </jsp:body>
                            </bs:panel></c:if>
                            <!-- Global parameters -->
                            <c:if test="${not empty _apiInfo.params}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.global_parameters" resourceName="apidocs-messages" defaultValue="Global parameters"/></jsp:attribute>
                                    <jsp:body><bs:table hover="true" condensed="true" bordered="true" responsive="true">
                                        <thead><tr>
                                                <th><ymweb:i18n key="apidocs.content.table_field_param_name" resourceName="apidocs-messages" defaultValue="Parameter name"/></th>
                                                <th><ymweb:i18n key="apidocs.content.table_field_type" resourceName="apidocs-messages" defaultValue="Type"/></th>
                                                <th><ymweb:i18n key="apidocs.content.table_field_default_value" resourceName="apidocs-messages" defaultValue="Default"/></th>
                                                <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                        </tr></thead>
                                        <tbody><c:forEach var="_apiParamItem" items="${_apiInfo.params}"><tr>
                                            <td>${_apiParamItem.name} <c:if test="${_apiParamItem.required eq true}"><bs:label style="danger" _style="cursor:pointer" data-tip="tooltip" title="${_ConstParamRequired}">R</bs:label></c:if> <c:if test="${_apiParamItem.model eq true}"><bs:label style="warning" _style="cursor:pointer" data-tip="tooltip" title="${_ConstParamModel}">M</bs:label></c:if> <c:if test="${_apiParamItem.multiple eq true}"><bs:label style="info" _style="cursor:pointer" data-tip="tooltip" title="${_ConstParamArray}">A</bs:label></c:if></td>
                                            <td>${_apiParamItem.type}</td>
                                            <td>${_apiParamItem.defaultValue}</td>
                                            <td><c:if test="${not empty _apiParamItem.description}"><p>${_apiParamItem.description}</p></c:if>
                                                <c:if test="${not empty _apiParamItem.allowValues}"><p><bs:label><ymweb:i18n key="apidocs.content.param_value_range" resourceName="apidocs-messages" defaultValue="Value Range"/></bs:label></p><p>${_apiParamItem.allowValues}</p></c:if>
                                                <c:if test="${not empty _apiParamItem.examples}">
                                                    <p><bs:label><ymweb:i18n key="apidocs.content.examples" resourceName="apidocs-messages" defaultValue="Examples"/></bs:label></p>
                                                    <c:forEach var="_apiParamItemExample" items="${_apiParamItem.examples}">
                                                        <div class="highlight">
                                                            <pre>${_apiParamItemExample.content}</pre>
                                                        </div>
                                                    </c:forEach>
                                                </c:if></td>
                                        </tr></c:forEach></tbody>
                                    </bs:table></jsp:body>
                            </bs:panel></c:if>
                            <!-- Global response parameters -->
                            <c:if test="${not empty _apiInfo.responseType}"><bs:panel>
                                <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.global_response_parameters" resourceName="apidocs-messages" defaultValue="Global response parameters"/></jsp:attribute>
                                <jsp:body><bs:panel-body><p>${_apiInfo.responseType.name}</p><p>${_apiInfo.responseType.description}</p></bs:panel-body>
                                    <bs:table hover="true" condensed="true" bordered="true" responsive="true">
                                    <thead>
                                    <tr>
                                        <th><ymweb:i18n key="apidocs.content.table_field_param_name" resourceName="apidocs-messages" defaultValue="Parameter name"/></th>
                                        <th><ymweb:i18n key="apidocs.content.table_field_type" resourceName="apidocs-messages" defaultValue="Type"/></th>
                                        <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="_apiResponseItem" items="${_apiInfo.responseType.properties}"><tr>
                                        <td>${_apiResponseItem.name}</td>
                                        <td>${_apiResponseItem.value}</td>
                                        <td>${_apiResponseItem.description}</td></tr></c:forEach>
                                    </tbody>
                                </bs:table></jsp:body>
                            </bs:panel></c:if>
                            <!-- Global responses -->
                            <c:if test="${not empty _apiInfo.responses}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.global_responses" resourceName="apidocs-messages" defaultValue="Global response codes"/></jsp:attribute>
                                    <jsp:body><bs:table hover="true" condensed="true" bordered="true" responsive="true">
                                        <thead>
                                            <tr>
                                                <th><ymweb:i18n key="apidocs.content.table_field_response_code" resourceName="apidocs-messages" defaultValue="Code"/></th>
                                                <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="_apiResponseItem" items="${_apiInfo.responses}"><tr>
                                            <td>${_apiResponseItem.code}</td>
                                            <td><c:if test="${not empty _apiResponseItem.message}"><p>${_apiResponseItem.message}</p></c:if><c:if test="${not empty _apiResponseItem.headers}">
                                                <p><bs:label><ymweb:i18n key="apidocs.content.response_headers" resourceName="apidocs-messages" defaultValue="Response headers"/></bs:label></p>
                                                <bs:table hover="true" condensed="true" striped="true">
                                                    <thead>
                                                    <tr>
                                                        <th><ymweb:i18n key="apidocs.content.table_field_header_name" resourceName="apidocs-messages" defaultValue="Header name"/></th>
                                                        <th><ymweb:i18n key="apidocs.content.table_field_type" resourceName="apidocs-messages" defaultValue="Type"/></th>
                                                        <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                                    </tr>
                                                    </thead>
                                                    <tbody><c:forEach var="_apiResponseItemHeader" items="${_apiResponseItem.headers}">
                                                        <tr>
                                                            <td>${_apiResponseItemHeader.name}</td>
                                                            <td>${_apiResponseItemHeader.type}</td>
                                                            <td>${_apiResponseItemHeader.description}</td>
                                                        </tr>
                                                    </c:forEach></tbody>
                                                </bs:table>
                                            </c:if></td></tr></c:forEach>
                                        </tbody>
                                    </bs:table></jsp:body>
                            </bs:panel></c:if>
                            <!-- ApiDocs: Actions -->
                            <c:forEach var="_actionInfo" items="${_apiInfo.actions}">
                                <h2 id="${_apiInfo.linkUrl}-${_actionInfo.linkUrl}">${_actionInfo.dispName} <small>${_actionInfo.mapping}</small></h2>
                                <p>${_actionInfo.description}</p>
                                <c:if test="${not empty _actionInfo.notes}"><div class="bs-callout bs-callout-danger"><h4>Notes:</h4><p>${_actionInfo.notes}</p></div></c:if>
                                <c:if test="${not empty _actionInfo.changelogs}">
                                    <!-- Changelog -->
                                    <bs:panel>
                                        <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.changelog" resourceName="apidocs-messages" defaultValue="Changelog"/></jsp:attribute>
                                        <jsp:body><bs:list-group><c:forEach var="_changelogInfo" items="${_actionInfo.changelogs}">
                                            <bs:list-item href="#">
                                            <jsp:attribute name="heading">
                                                ${_changelogInfo.date} ${_changelogInfo.action}
                                                <small>by <c:if test="${not empty _changelogInfo.author}"><c:choose><c:when test="${not empty _changelogInfo.author.url}">
                                                    <a href="${_changelogInfo.author.url}" target="_blank">${_changelogInfo.author.name}</a></c:when><c:otherwise>${_changelogInfo.author.name}</c:otherwise></c:choose>
                                                    <c:if test="${not empty _changelogInfo.author.email}"> (<a href="mailto:${_changelogInfo.author.email}">${_changelogInfo.author.email}</a>)</c:if></c:if></small>
                                            </jsp:attribute>
                                                <jsp:body><p>${_changelogInfo.description}</p></jsp:body>
                                            </bs:list-item>
                                        </c:forEach></bs:list-group></jsp:body>
                                    </bs:panel>
                                </c:if>
                                <!-- Authorization -->
                                <c:if test="${not empty _actionInfo.authType}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.authorization_type" resourceName="apidocs-messages" defaultValue="Authorization type"/></jsp:attribute>
                                    <jsp:body><bs:panel-body>
                                        <p>${_actionInfo.authType}</p>
                                    </bs:panel-body><c:if test="${not empty _actionInfo.scopes}"><bs:table hover="true" condensed="true" bordered="true" responsive="true">
                                        <thead><tr>
                                            <th><ymweb:i18n key="apidocs.content.authorization_scope" resourceName="apidocs-messages" defaultValue="Authorization scope"/></th>
                                            <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                        </tr></thead>
                                        <tbody><c:forEach var="_actionScope" items="${_actionInfo.scopes}"><tr>
                                            <td>${_actionScope.name}</td>
                                            <td>${_actionScope.description}</td>
                                        </tr></c:forEach></tbody>
                                    </bs:table></c:if></jsp:body>
                                </bs:panel></c:if>
                                <!-- Security -->
                                <c:if test="${not empty _actionInfo.security}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.security" resourceName="apidocs-messages" defaultValue="Security"/></jsp:attribute>
                                    <jsp:body><bs:panel-body>
                                        <p>${_actionInfo.security.description}</p>
                                        <!-- Roles -->
                                        <c:if test="${not empty _actionInfo.security.roles}">
                                            <c:forEach var="_actionRole" items="${_actionInfo.security.roles}"><bs:label style="warning" _style="cursor:pointer" data-tip="tooltip" title="${_actionRole.description}">${_actionRole.name}</bs:label></c:forEach>
                                        </c:if></bs:panel-body>
                                        <!-- Permissions -->
                                        <c:if test="${not empty _actionInfo.security.permissions}">
                                            <bs:table hover="true" condensed="true" bordered="true" responsive="true">
                                                <thead><tr>
                                                    <th><ymweb:i18n key="apidocs.content.security_permissions" resourceName="apidocs-messages" defaultValue="Permissions"/></th>
                                                    <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                                </tr></thead>
                                                <tbody><c:forEach var="_actionPermission" items="${_actionInfo.security.permissions}"><tr>
                                                    <td>${_actionPermission.name}</td>
                                                    <td>${_actionPermission.description}</td>
                                                </tr></c:forEach></tbody>
                                            </bs:table></c:if>
                                    </jsp:body>
                                </bs:panel></c:if>
                                <!-- Request methods -->
                                <c:if test="${not empty _actionInfo.methods}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.request_methods" resourceName="apidocs-messages" defaultValue="Request methods"/></jsp:attribute>
                                    <jsp:body><bs:panel-body><c:forEach var="_actionMethod" items="${_actionInfo.methods}">
                                        <bs:label style="primary">${_actionMethod}</bs:label></c:forEach>
                                    </bs:panel-body></jsp:body>
                                </bs:panel></c:if>
                                <!-- Request headers -->
                                <c:if test="${not empty _actionInfo.headers}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.request_headers" resourceName="apidocs-messages" defaultValue="Request headers"/></jsp:attribute>
                                    <jsp:body><bs:table hover="true" condensed="true" bordered="true">
                                        <thead>
                                        <tr>
                                            <th><ymweb:i18n key="apidocs.content.table_field_header_name" resourceName="apidocs-messages" defaultValue="Header name"/></th>
                                            <th><ymweb:i18n key="apidocs.content.table_field_type" resourceName="apidocs-messages" defaultValue="Type"/></th>
                                            <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                        </tr>
                                        </thead>
                                        <tbody><c:forEach var="_actionHeader" items="${_actionInfo.headers}">
                                            <tr>
                                                <td>${_actionHeader.name}</td>
                                                <td>${_actionHeader.type}</td>
                                                <td>${_actionHeader.description}</td>
                                            </tr>
                                        </c:forEach></tbody>
                                    </bs:table></jsp:body>
                                </bs:panel></c:if>
                                <!-- Parameters -->
                                <c:if test="${not empty _actionInfo.params}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.parameters" resourceName="apidocs-messages" defaultValue="Parameters"/></jsp:attribute>
                                    <jsp:body><bs:table hover="true" condensed="true" bordered="true">
                                        <thead><tr>
                                            <th><ymweb:i18n key="apidocs.content.table_field_param_name" resourceName="apidocs-messages" defaultValue="Parameter name"/></th>
                                            <th><ymweb:i18n key="apidocs.content.table_field_type" resourceName="apidocs-messages" defaultValue="Type"/></th>
                                            <th><ymweb:i18n key="apidocs.content.table_field_default_value" resourceName="apidocs-messages" defaultValue="Default"/></th>
                                            <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                        </tr></thead>
                                        <tbody><c:forEach var="_actionParamItem" items="${_actionInfo.params}"><tr>
                                            <td>${_actionParamItem.name} <c:if test="${_actionParamItem.required eq true}"><bs:label style="danger" _style="cursor:pointer" data-tip="tooltip" title="${_ConstParamRequired}">R</bs:label> </c:if><c:if test="${_actionParamItem.model eq true}"><bs:label style="warning" _style="cursor:pointer" data-tip="tooltip" title="${_ConstParamModel}">M</bs:label></c:if> <c:if test="${_actionParamItem.multiple eq true}"><bs:label style="info" _style="cursor:pointer" data-tip="tooltip" title="${_ConstParamArray}">A</bs:label></c:if></td>
                                            <td>${_actionParamItem.type}</td>
                                            <td>${_actionParamItem.defaultValue}</td>
                                            <td><c:if test="${not empty _actionParamItem.description}"><p>${_actionParamItem.description}</p></c:if>
                                                <c:if test="${not empty _actionParamItem.allowValues}"><p><bs:label><ymweb:i18n key="apidocs.content.param_value_range" resourceName="apidocs-messages" defaultValue="Value Range"/></bs:label></p><p>${_actionParamItem.allowValues}</p></c:if>
                                                <c:if test="${not empty _actionParamItem.examples}">
                                                    <p><bs:label><ymweb:i18n key="apidocs.content.examples" resourceName="apidocs-messages" defaultValue="Examples"/></bs:label></p>
                                                    <c:forEach var="_actionParamItemExample" items="${_actionParamItem.examples}">
                                                        <c:if test="${not empty _actionParamItemExample.type}"><p>${_actionParamItemExample.type}</p></c:if>
                                                        <div class="highlight">
                                                            <pre>${_actionParamItemExample.content}</pre>
                                                        </div>
                                                    </c:forEach>
                                                </c:if></td>
                                        </tr></c:forEach></tbody>
                                    </bs:table></jsp:body>
                                </bs:panel></c:if>
                                <!-- Response parameters -->
                                <c:if test="${not empty _actionInfo.responseType}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.response_parameters" resourceName="apidocs-messages" defaultValue="Response parameters"/></jsp:attribute>
                                    <jsp:body><bs:panel-body><p>${_actionInfo.responseType.name}</p><p>${_actionInfo.responseType.description}</p></bs:panel-body>
                                        <bs:table hover="true" condensed="true" bordered="true" responsive="true">
                                        <thead>
                                        <tr>
                                            <th><ymweb:i18n key="apidocs.content.table_field_param_name" resourceName="apidocs-messages" defaultValue="Parameter name"/></th>
                                            <th><ymweb:i18n key="apidocs.content.table_field_type" resourceName="apidocs-messages" defaultValue="Type"/></th>
                                            <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="_actionResponseItem" items="${_actionInfo.responseType.properties}"><tr>
                                            <td>${_actionResponseItem.name}</td>
                                            <td>${_actionResponseItem.value}</td>
                                            <td>${_actionResponseItem.description}</td></tr></c:forEach>
                                        </tbody>
                                    </bs:table></jsp:body>
                                </bs:panel></c:if>
                                <!-- Responses -->
                                <c:if test="${not empty _actionInfo.responses}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.responses" resourceName="apidocs-messages" defaultValue="Responses"/></jsp:attribute>
                                    <jsp:body><bs:table hover="true" condensed="true" bordered="true" responsive="true">
                                        <thead>
                                        <tr>
                                            <th><ymweb:i18n key="apidocs.content.table_field_response_code" resourceName="apidocs-messages" defaultValue="Code"/></th>
                                            <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="_actionResponseItem" items="${_actionInfo.responses}"><tr>
                                            <td>${_actionResponseItem.code}</td>
                                            <td><c:if test="${not empty _actionResponseItem.message}"><p>${_actionResponseItem.message}</p></c:if><c:if test="${not empty _actionResponseItem.headers}">
                                                <p><bs:label><ymweb:i18n key="apidocs.content.response_headers" resourceName="apidocs-messages" defaultValue="Response headers"/></bs:label></p>
                                                <bs:table hover="true" condensed="true" striped="true">
                                                    <thead>
                                                    <tr>
                                                        <th><ymweb:i18n key="apidocs.content.table_field_header_name" resourceName="apidocs-messages" defaultValue="Header name"/></th>
                                                        <th><ymweb:i18n key="apidocs.content.table_field_type" resourceName="apidocs-messages" defaultValue="Type"/></th>
                                                        <th><ymweb:i18n key="apidocs.content.table_field_description" resourceName="apidocs-messages" defaultValue="Description"/></th>
                                                    </tr>
                                                    </thead>
                                                    <tbody><c:forEach var="_actionResponseItemHeader" items="${_actionResponseItem.headers}">
                                                        <tr>
                                                            <td>${_actionResponseItemHeader.name}</td>
                                                            <td>${_actionResponseItemHeader.type}</td>
                                                            <td>${_actionResponseItemHeader.description}</td>
                                                        </tr>
                                                    </c:forEach></tbody>
                                                </bs:table>
                                            </c:if></td></tr></c:forEach>
                                        </tbody>
                                    </bs:table></jsp:body>
                                </bs:panel></c:if>
                                <!-- Extensions -->
                                <c:if test="${not empty _actionInfo.extensions}">
                                    <c:forEach var="_extensionInfo" items="${_actionInfo.extensions}">
                                        <bs:panel title="${_extensionInfo.name}">
                                            <c:if test="${not empty _extensionInfo.description}"><bs:panel-body><p>${_extensionInfo.description}</p></bs:panel-body></c:if>
                                            <bs:list-group><c:forEach var="_extensionProp" items="${_extensionInfo.properties}">
                                                <bs:list-item>
                                                    <jsp:attribute name="heading">${_extensionProp.name}<c:if test="${not empty _extensionProp.description}"> <small>${_extensionProp.description}</small></c:if></jsp:attribute>
                                                    <jsp:body><p>${_extensionProp.value}</p></jsp:body>
                                                </bs:list-item>
                                            </c:forEach></bs:list-group>
                                        </bs:panel>
                                    </c:forEach>
                                </c:if>
                                <!-- Examples -->
                                <c:if test="${not empty _actionInfo.examples}"><bs:panel>
                                    <jsp:attribute name="title"><ymweb:i18n key="apidocs.content.examples" resourceName="apidocs-messages" defaultValue="Examples"/></jsp:attribute>
                                    <jsp:body><bs:panel-body><c:forEach var="_actionExample" items="${_actionInfo.examples}">
                                        <c:if test="${not empty _actionExample.type}"><p>${_actionExample.type}</p></c:if>
                                        <div class="highlight">
                                            <pre>${_actionExample.content}</pre>
                                        </div>
                                    </c:forEach></bs:panel-body></jsp:body>
                                </bs:panel></c:if>
                            </c:forEach>
                        </div>
                    </c:forEach>
                </bs:col>
                <bs:col md="3">
                    <div class="bs-docs-sidebar hidden-print hidden-sm hidden-xs">
                        <ul class="nav bs-docs-sidenav">
                            <c:forEach var="_navItem" items="${_navs}">
                                <c:choose><c:when test="${not empty _navItem.subItems}">
                                    <bs:item href="#${_navItem.url}" subitem="true" title="${_navItem.title}">
                                        <bs:nav>
                                            <c:forEach var="_subItem" items="${_navItem.subItems}">
                                                <bs:item href="#${_navItem.url}-${_subItem.url}">${_subItem.title}</bs:item>
                                            </c:forEach>
                                        </bs:nav>
                                    </bs:item>
                                </c:when><c:otherwise>
                                    <bs:item href="#${_navItem.url}">${_navItem.title}</bs:item>
                                </c:otherwise></c:choose>
                            </c:forEach>
                        </ul>
                        <a href="#top" class="back-to-top"><ymweb:i18n key="apidocs.content.back_to_top" resourceName="apidocs-messages" defaultValue="Back to top"/></a>
                    </div>
                </bs:col>
            </bs:row>
        </bs:container>
    </ymweb:layout>
</ymweb:ui>