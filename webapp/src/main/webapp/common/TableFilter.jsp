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
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>

<%@page import="org.efaps.beans.TableBean"%>
<%@page import="org.efaps.db.Context"%>
<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean"
  scope="session" />
<%
      Context context = Context.getThreadContext();

      String cacheKey = context.getParameter("eFapsCacheKey");
      TableBean uiObject = (TableBean) cache.getTableBean(cacheKey);

      uiObject.setFilterKey(context.getParameter("filterKey"));
      request.setAttribute("uiObject", uiObject);
%>
<html>
<head>
    
   
    <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css"/>
  
<script type="text/javascript">
  function eFapsSubmit()  {
    var selects=document.getElementsByName("eFapsFilterForm:eFapsSelectBox");
    var c_value = new Array();
    for (var i=0; i < selects.length; i++){
     if (selects[i].checked){
      var NeuestesElement=c_value.push(selects[i].value);
     }
    }
    if(c_value.length>0){
      parent.eFapsFilterTable(c_value);
    }
    window.parent.dlgFrMdl.hide();
  }
</script>

</head>
<body>
<f:view>
  <h:form id="eFapsFilterForm">
    <h:panelGroup>
      <h:selectManyCheckbox layout="pageDirection"
        styleClass="selectManyCheckbox" id="eFapsSelectBox">
        <f:selectItems id="test" value="#{uiObject.filterList}" />
      </h:selectManyCheckbox>
    </h:panelGroup>

  </h:form>
  <table width="100%"><tr><td width="50%" align="right">
  <t:commandButton onclick="eFapsSubmit()" value="send"></t:commandButton>
  </td><td align="left">
  <t:commandButton onclick="window.parent.dlgFrMdl.hide();"
    value="Cancel"></t:commandButton>
  </td></tr>  
</table>
</f:view>
</body>
</html>
