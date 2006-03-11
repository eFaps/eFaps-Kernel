<%@taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@page import="org.efaps.admin.ui.Menu"%>
<jsp:useBean id="uiObject" class="java.util.HashMap" scope="request"/>

var headerMenu = parent.eFapsGetFrameHeaderMenu();
headerMenu.clean();
headerMenu.setForm(document.forms[0]);

<c:set var="varName"    value="headerMenu"/>
<c:set var="commitUrl"  value="${param.commitUrl}"/>
<c:set var="oid"        value="${param.oid}"/>
<c:set var="nodeId"     value="${param.nodeId}"/>

<c:forEach items="${uiObject['menuHeader'].commands}" var="row">
  <%@include file = "../common/StdMenuOneCommand.inc"%>
</c:forEach>
