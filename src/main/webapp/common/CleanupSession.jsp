<%@page errorPage="Exception.jsp"%>
<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>
<%
  String cacheKey = request.getParameter("cacheKey");
System.out.println("############################# cleanupsession called with cache = "+cacheKey);
  if (cacheKey!=null && cacheKey.length()>0)  {
System.out.println("############################# remove from cache = "+cacheKey);
    cache.remove(cacheKey);
  }
%>
