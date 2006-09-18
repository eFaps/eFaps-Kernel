<%--
 
  Copyright 2006 The eFaps Team
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 
  Author:          tmo
  Revision:        $Rev$
  Last Changed:    $Date$
  Last Changed By: $Author$
 
--%>

<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>

<%@include file = "../common/StdTop.inc"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<%
  String cacheKey  = request.getParameter("cacheKey");

  if (cacheKey==null)  {
    cacheKey = Long.toString(System.currentTimeMillis());
System.out.println("create new cacheKey = "+cacheKey);
  }

  org.efaps.beans.SearchBean uiObject = (org.efaps.beans.SearchBean)cache.get(cacheKey);
  if (uiObject==null)  {
    uiObject = new org.efaps.beans.SearchBean();
    cache.put(cacheKey, uiObject);
    request.setAttribute("uiObject", uiObject);
    uiObject.setResponse(response);
    %>
      <%-- /** set other parameters **/ --%>
      <jsp:setProperty name="uiObject" property="searchName" param="search"/>
      <jsp:setProperty name="uiObject" property="commandName" param="searchCommand"/>
      <jsp:setProperty name="uiObject" property="oid" param="parentOid"/>
    <%
uiObject.setSearchName(context.getParameter("search"));
uiObject.setCommandName(context.getParameter("searchCommand"));
uiObject.setOid(context.getParameter("parentOid"));

    uiObject.execute4ResultTable();
  } else  {
    request.setAttribute("uiObject", uiObject);
    uiObject.setResponse(response);
  }

%>

<%-- /** set the title of the search user interface object **/ --%>
<fmt:bundle basename="StringResource">
  <c:set var="title"><str:replace replace="'" with="\\'"><fmt:message key="${uiObject.search.label}"/> - <fmt:message key="${uiObject.command.label}"/> - Result</str:replace></c:set>
  <jsp:setProperty name="uiObject" property="title" value="<%=pageContext.getAttribute("title")%>"/>
  <c:remove var="title"/>
</fmt:bundle>

<jsp:include page = "../common/TableMain.jsp">
  <jsp:param name="cacheKey" value="<%=cacheKey%>"/>
  <jsp:param name="targetProcess" value=""/>
  <jsp:param name="domContext" value="parent.parent."/>
  <jsp:param name="isSearch" value="true"/>
</jsp:include>

<%@include file = "../common/StdBottom.inc"%>
