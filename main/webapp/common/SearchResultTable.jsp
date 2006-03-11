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
    uiObject.setParameters(parameters, fileParameters);
    %>
      <%-- /** set other parameters **/ --%>
      <jsp:setProperty name="uiObject" property="loginName" value="<%=session.getAttribute("login.name")%>"/>
      <jsp:setProperty name="uiObject" property="searchName" param="search"/>
      <jsp:setProperty name="uiObject" property="commandName" param="searchCommand"/>
      <jsp:setProperty name="uiObject" property="oid" param="parentOid"/>
    <%
uiObject.setSearchName(getParameter("search"));
uiObject.setCommandName(getParameter("searchCommand"));
uiObject.setOid(getParameter("parentOid"));

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
