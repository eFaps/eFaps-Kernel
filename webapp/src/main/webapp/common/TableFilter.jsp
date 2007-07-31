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
 
  Author:          jmo
  Revision:        $Rev$
  Last Changed:    $Date$
  Last Changed By: $Author$
 
--%>
<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="t" uri="http://myfaces.apache.org/tomahawk"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="h"    uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="f"    uri="http://java.sun.com/jsf/core"%>

<%@page import="org.efaps.beans.TableBean"%>
<%@page import="org.efaps.db.Context"%>
<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean"
	scope="session" />
<%
      Context context = Context.getThreadContext();

      String cacheKey = context.getParameter("cacheKey");
      TableBean uiObject = (TableBean) cache.getTableBean(cacheKey);

      uiObject.setFilterKey(context.getParameter("filterKey"));
      request.setAttribute("uiObject", uiObject);
%>
<html>
<head>


</head>
<body>
<f:view>
<h:form>
  <h:panelGroup >
	<h:selectManyCheckbox layout="pageDirection" styleClass="selectManyCheckbox">
		<f:selectItems value="#{uiObject.filterList}" />
	</h:selectManyCheckbox>
  </h:panelGroup>

</h:form>

<t:commandButton onclick="window.parent.dlgFrMdl.hide();" value="Cancel"></t:commandButton>

</f:view>
</body>
</html>
