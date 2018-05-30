<%--
  User: 刘镇 (suninformation@163.com)
  Desc: 基本页面模板
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://www.ymate.net/ymweb_core" prefix="ymweb" %>
<%@ taglib uri="http://www.ymate.net/ymweb_bs" prefix="bs" %>
<ymweb:ui cleanup="false">
    <ymweb:layout cleanup="false">
        <bs:page title="@{title} - yMate.Net">
            <jsp:attribute name="headerBody">
                @{meta}
                <%--<link href="//fonts.googleapis.com/css?family=Open+Sans:400,300,600,700&amp;subset=all" rel="stylesheet" type="text/css">--%>
                <link href="@{pagePath}assets/apidocs/styles/font-awesome.min.css" rel="stylesheet" type="text/css">
                <link href="@{pagePath}assets/apidocs/styles/simple-line-icons.min.css" rel="stylesheet" type="text/css">
                <link href="@{pagePath}assets/apidocs/styles/bootstrap.min.css" rel="stylesheet" type="text/css">
                <%--<link href="@{pagePath}assets/apidocs/styles/bootstrap-theme.min.css" rel="stylesheet" type="text/css">--%>
                <link href="@{pagePath}assets/apidocs/styles/doc.min.css" rel="stylesheet" type="text/css">
                <link href="@{pagePath}assets/apidocs/styles/patch.css" rel="stylesheet" type="text/css">
                @{css}
                @{page.styles}
            </jsp:attribute>
            <jsp:body>
                @{body}
                <div class="bs-docs-footer">
                    <bs:container>
                        <ul class="bs-docs-footer-links">
                            <bs:item href="https://github.com/suninformaiton/ymate-platform-v2">YMP v2</bs:item>
                            <bs:item href="https://github.com/suninformaiton/ymate-webui">WebUI</bs:item>
                            <bs:item href="https://getbootstrap.com/docs/3.3/">Bootstrap v3.3</bs:item>
                        </ul>
                        <p>Copyright &copy; 2018 <a href="http://www.ymate.net/" target="_blank">yMate.Net</a>, Code licensed <a href="http://apache.org/licenses/LICENSE-2.0.txt" target="_blank">Apache-2.0</a></p>
                    </bs:container>
                </div>
                <!--[if lt IE 9]>
                <script src="@{pagePath}assets/apidocs/scripts/respond.min.js" type="text/javascript"></script>
                <script src="@{pagePath}assets/apidocs/scripts/excanvas.min.js" type="text/javascript"></script>
                <![endif]-->
                <script src="@{pagePath}assets/apidocs/scripts/jquery.min.js" type="text/javascript"></script>
                <script src="@{pagePath}assets/apidocs/scripts/jquery-ui.min.js" type="text/javascript"></script>
                <script src="@{pagePath}assets/apidocs/scripts/bootstrap.min.js" type="text/javascript"></script>

                <script src="@{pagePath}assets/apidocs/scripts/holder.min.js" type="text/javascript"></script>
                <script src="@{pagePath}assets/apidocs/scripts/doc.min.js" type="text/javascript"></script>
                <script>
                    $(function () {
                        $('[data-tip="tooltip"]').tooltip();
                        $('[data-popover="popover"]').popover();
                    });
                </script>
                @{script}
            </jsp:body>
        </bs:page>
    </ymweb:layout>
</ymweb:ui>