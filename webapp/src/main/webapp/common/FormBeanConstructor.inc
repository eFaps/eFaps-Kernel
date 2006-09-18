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

<%-- /** constructor for the form bean with some initialise code **/ --%>
<%
  org.efaps.beans.AbstractBean uiObject = (org.efaps.beans.AbstractBean)request.getAttribute("uiObject");
  if (uiObject==null)  {
    uiObject = cache.getFormBean(context.getParameter("command"));
    request.setAttribute("uiObject", uiObject);

    uiObject.setResponse(response);
    uiObject.setRequest(request);

%>
<%--

<c:choose>
  <c:when test="${param.formCommand eq null or param.formCommand eq''}">
      <jsp:setProperty name="uiObject" property="commandName" param="command"/>
      <c:set var="commandName" value="${param.command}"/>
  </c:when>
  <c:otherwise>
      <jsp:setProperty name="uiObject" property="commandName" param="formCommand"/>
      <c:set var="commandName" value="${param.formCommand}"/>
  </c:otherwise>
</c:choose>
      <jsp:setProperty name="uiObject" property="oid" param="oid"/>
      <jsp:setProperty name="uiObject" property="ukOid" param="ukOid"/>
--%>





      <c:set var="title"><str:replace replace="'" with="\\'"><fmt:message><c:out value="${uiObject.command.targetTitle}"/></fmt:message></str:replace></c:set>
<%
System.out.println("title="+pageContext.getAttribute("title"));
%>
      <jsp:setProperty name="uiObject" property="title" value="<%=pageContext.getAttribute("title")%>"/>
<jsp:setProperty name="uiObject" property="ukTitle" value=""/>
<%--
      <c:set var="title"><str:replace replace="'" with="\\'"><fmt:message><c:out value="${commandName}"/>.UniqueKey.Title</fmt:message></str:replace></c:set>
      <jsp:setProperty name="uiObject" property="ukTitle" value="<%=pageContext.getAttribute("title")%>"/>
--%>
      <c:remove var="title"/>

      <%-- /** execute method **/ --%>
      <c:choose>
        <c:when test="${method eq 'ukTest'}">
          <%((org.efaps.beans.FormBean)uiObject).ukTest();%>
        </c:when>
        <c:when test="${method eq 'execute'}">
          <%((org.efaps.beans.FormBean)uiObject).execute();%>
        </c:when>
        <c:when test="${method eq 'process'}">
          <%((org.efaps.beans.FormBean)uiObject).process();%>
        </c:when>
      </c:choose>
    <%
  }
%>
<jsp:setProperty name="uiObject" property="nodeId" param="nodeId"/>

