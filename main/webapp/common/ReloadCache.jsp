<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>

<%@page import="org.efaps.db.Cache"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<%@include file = "../common/StdTop.inc"%>
<fmt:bundle basename="StringResource">

<%
  Cache.reloadCache(context);
%>

<html>

  <body onLoad="alert('Cache Reloaded')">

  </body>
<html>

</fmt:bundle>
<%@include file = "../common/StdBottom.inc"%>
