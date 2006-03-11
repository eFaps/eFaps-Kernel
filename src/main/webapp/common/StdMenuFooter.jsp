<%@taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@page import="org.efaps.admin.ui.Menu"%>
<jsp:useBean id="uiObject" class="java.util.HashMap" scope="request"/>

var footerMenu = parent.eFapsGetFrameFooterMenu();
footerMenu.clean();
footerMenu.setForm(document.forms[0]);

<c:set var="varName"    value="footerMenu"/>
<c:set var="commitUrl"  value="${param.commitUrl}"/>
<c:set var="oid"        value="${param.oid}"/>
<c:set var="nodeId"     value="${param.nodeId}"/>

<c:forEach items="${uiObject['menuFooter'].commands}" var="row">
  <%@include file = "../common/StdMenuOneCommand.inc"%>
</c:forEach>
