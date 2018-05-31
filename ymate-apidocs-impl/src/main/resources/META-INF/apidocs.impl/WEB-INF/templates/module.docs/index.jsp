<%@ page contentType="text/html;charset=UTF-8" language="java" trimDirectiveWhitespaces="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.ymate.net/ymweb_core" prefix="ymweb" %>
<%@ taglib uri="http://www.ymate.net/ymweb_bs" prefix="bs" %>
<ymweb:ui src="module.docs/base">
    <ymweb:property name="title">${__title}</ymweb:property>
    <ymweb:layout>
        <bs:navbar _class="bs-docs-nav" href="apidocs" staticTop="false" container="true" collapseId="main">
            <jsp:attribute name="brand">${__brand}</jsp:attribute>
            <jsp:body>
                <bs:nav>
                    <bs:item href="apidocs" active="true"><ymweb:i18n key="apidocs.nav.home" resourceName="apidocs-messages" defaultValue="Home"/></bs:item>
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
                        <h1>${__title}</h1>
                        <bs:text lead="true">${__description}</bs:text>
                    </bs:col>
                </bs:row>
            </bs:container>
        </div>
        <bs:container _class="bs-doc-container">
            <bs:row>
                <c:choose>
                    <c:when test="${not empty _docs}">
                        <c:forEach var="_doc" items="${_docs}">
                            <bs:col md="4" sm="6">
                                <bs:thumbnail>
                                    <jsp:attribute name="caption">
                                        <h3>${_doc.title}</h3>
                                        <c:if test="${not empty _doc.version}"><p><bs:label style="success">${_doc.version}</bs:label></p></c:if>
                                        <c:if test="${not empty _doc.description}"><p><small>${_doc.description}</small></p></c:if>
                                        <p>
                                            <bs:button href="apidocs/content?doc=${_doc.id}" style="primary"><ymweb:i18n key="apidocs.button.read" resourceName="apidocs-messages" defaultValue="Read"/></bs:button>
                                            <bs:button href="apidocs/download?doc=${_doc.id}" style="default"><ymweb:i18n key="apidocs.button.download" resourceName="apidocs-messages" defaultValue="Download"/></bs:button>
                                        </p>
                                    </jsp:attribute>
                                </bs:thumbnail>
                            </bs:col>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <bs:text lead="true" alignCenter="true">Not found document data.</bs:text>
                    </c:otherwise>
                </c:choose>
            </bs:row>
        </bs:container>
    </ymweb:layout>
</ymweb:ui>