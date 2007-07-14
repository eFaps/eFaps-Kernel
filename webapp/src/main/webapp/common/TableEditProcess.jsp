<%--
 
  Copyright 2003-2007 The eFaps Team
 
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
<%@taglib prefix="un"   uri="http://jakarta.apache.org/taglibs/unstandard-1.0"%>

<%@page import="org.efaps.db.Update"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

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
        Update update = new Update(col.getInstance());
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

<html>
  <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
  <body>
    <script language="Javascript">
      top.opener.eFapsCommonRefresh();
      eFapsCommonCloseWindow();
    </script>
  </body>
</html>
