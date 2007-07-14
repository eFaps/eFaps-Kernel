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

<%@page import="org.efaps.db.Context"%>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<%-- /** constructor for the form bean with some initialise code **/ --%>
<%
  org.efaps.beans.FormBean uiObject = (org.efaps.beans.FormBean)request.getAttribute("uiObject");
  if (uiObject==null)  {
    uiObject = cache.getFormBean(Context.getThreadContext().getParameter("command"));
    request.setAttribute("uiObject", uiObject);

uiObject.setUkTitle("");

    uiObject.ukTest();
  }
%>
<jsp:setProperty name="uiObject" property="nodeId" param="nodeId"/>


<%@include file = "../common/StdBottom.inc"%>
<html>

  <script type="text/javascript">
    function eFapsProcessEnd()  {
      <c:choose>
        <c:when test="${uiObject.ukInstance ne null}">
          parent.location.href = parent.location.href + "&ukOid=<c:out value="${uiObject.ukInstance.oid}"/>";
          alert('<fmt:message><c:out value="${commandName}"/>.UniqueKey.Message</fmt:message>');
        </c:when>
        <c:otherwise>
          parent.eFapsProcessEnd();
        </c:otherwise>
      </c:choose>
    }
  </script>

  <body onLoad="eFapsProcessEnd()">

  </body>
</html>


