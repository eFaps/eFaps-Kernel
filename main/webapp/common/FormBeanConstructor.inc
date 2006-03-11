<%-- /** constructor for the form bean with some initialise code **/ --%>
<%
  org.efaps.beans.AbstractBean uiObject = (org.efaps.beans.AbstractBean)request.getAttribute("uiObject");
  if (uiObject==null)  {
    uiObject = cache.getFormBean(getParameter("command"));
    request.setAttribute("uiObject", uiObject);

    uiObject.setResponse(response);
    uiObject.setRequest(request);
System.out.println("parameters="+parameters);
    uiObject.setParameters(parameters, fileParameters);
    %>
      <jsp:setProperty name="uiObject" property="loginName" value="<%=session.getAttribute("login.name")%>"/>

<%
//uiObject.setUkOid(getParameter("ukOid"));


//request.setAttribute("commandName", ui);
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

