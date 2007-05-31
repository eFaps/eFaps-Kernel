<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>
<%@taglib prefix="un"   uri="http://jakarta.apache.org/taglibs/unstandard-1.0"%>

<%@page import="org.efaps.db.Update"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<%@include file = "../common/StdTop.inc"%>
<%
  String cacheKey =   request.getParameter("eFapsCacheKey");

  org.efaps.beans.TableBean uiObject = (org.efaps.beans.TableBean)request.getAttribute("uiObject");
  if (uiObject==null)  {
    uiObject = cache.getTableBean(cacheKey, request.getParameter("command"));
    request.setAttribute("uiObject", uiObject);
  }
  uiObject.setResponse(response);



  for (int i=0; i<uiObject.getValues().size(); i++)  {
    org.efaps.beans.TableBean.Row row = (org.efaps.beans.TableBean.Row)uiObject.getValues().get(i);

    for (int j=0; j<row.getValues().size(); j++)  {
      org.efaps.beans.AbstractCollectionBean.Value col = (org.efaps.beans.AbstractCollectionBean.Value)row.getValues().get(j);
      if (col.getField().isEditable())  {
        String[] values = (String[])request.getParameterValues(col.getField().getName());
        Update update = new Update(context, col.getInstance());
        update.add( col.getField().getExpression(), values[i]);
        update.execute();
      }
    }
  }


//  for (int i=0; i<uiObject.getTable().getFields().size(); i++)  {
//    Field field = (Field)uiObject.getTable().getFields().get(i);
//    if (field.getReference()!=null)  {
//      addAllFromString(_context, field.getReference());
//    }
//  }


%>
<%@include file = "../common/StdBottom.inc"%>

<html>
  <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
  <body>
    <script language="Javascript">
      top.opener.eFapsCommonRefresh();
      eFapsCommonCloseWindow();
    </script>
  </body>
</html>
