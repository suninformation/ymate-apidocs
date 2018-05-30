<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.ymate.net/ymweb_core" prefix="ymweb" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" trimDirectiveWhitespaces="true" %>
<html lang="zh" class="md">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width">
    <base href="<ymweb:url/>"/>
    <title><c:choose><c:when test="${not empty requestScope.ret and requestScope.ret != 0}">出错了</c:when><c:otherwise>提示</c:otherwise></c:choose></title>
    <link href="assets/error/css/styles.css" rel="stylesheet"/>
</head>

<body>
<div class="content">
    <div class="icon <c:choose><c:when test="${not empty requestScope.ret && requestScope.ret != 0}">icon-warning</c:when><c:otherwise>icon-wrong</c:otherwise></c:choose>"></div>
    <h1><c:choose><c:when test="${not empty param.status}"><ymweb:httpStatusI18n code="${param.status}"/> (${param.status})
    </c:when><c:otherwise>${requestScope.msg}</c:otherwise></c:choose><c:if test="${not empty requestScope.ret and requestScope.ret != 0}"> (代码: ${requestScope.ret})</c:if></h1>
    <c:if test="${not empty requestScope.subtitle}">
    <p id="subtitle">
        <span>${requestScope.subtitle}</span>
        <c:if test="${not empty requestScope.moreUrl}"><a class="learn-more-button" href="${requestScope.moreUrl}">了解详情</a></c:if>
    </p>
    </c:if>
    <c:if test="${not empty requestScope.data}">
    <div>
        <div class="detail">
            <em>详细信息如下:</em>
            <ul><c:forEach var="_item" items="${requestScope.data}">
                <li>${_item.value}</li>
            </c:forEach></ul>
        </div>
        <div class="clearer"></div>
    </div>
    </c:if>
</body>
</html>