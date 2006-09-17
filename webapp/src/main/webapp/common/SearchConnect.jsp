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
<%

  String[] oids = (String[])request.getParameterValues("selectedRow");

  org.efaps.beans.SearchBean uiObject = new org.efaps.beans.SearchBean();
  request.setAttribute("uiObject", uiObject);
  uiObject.setResponse(response);
  %>
    <%-- /** set other parameters **/ --%>
    <jsp:setProperty name="uiObject" property="searchName"  param="search"/>
    <jsp:setProperty name="uiObject" property="commandName" param="searchCommand"/>
    <jsp:setProperty name="uiObject" property="oid"         param="parentOid"/>
    <jsp:setProperty name="uiObject" property="loginName"   value="<%=session.getAttribute("login.name")%>"/>
  <%

  uiObject.execute4Connect(oids);

%>
<html>
  <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
  <body>
    <script language="Javascript">
      top.opener.parent.eFapsCommonRefresh();
      eFapsCommonCloseWindow();
    </script>
  </body>
</html>
