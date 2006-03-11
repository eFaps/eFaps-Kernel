
<%-- /** print command url **/ --%>
<c:set var="url" value="${one.sub.reference}"/>
<c:if test="${url eq null and ((one.sub.targetTable ne null) or (one.sub.targetForm ne null) or (one.sub.targetSearch ne null) or (one.sub.targetCreateType ne null))}">
  <c:set var="url" value="Link.jsp"/>
</c:if>
<c:if test="${url eq null and one.sub.submit}">
  <c:set var="url" value="${commitUrl}"/>
</c:if>
<c:if test="${url ne null}">

  <%-- /** add the object id to the url reference **/ --%>
  <str:countMatches var="countQuestionMark" substring="?"><c:out value="${url}" escapeXml="false"/></str:countMatches>
  <c:choose>
    <%-- /** add object id with question mark **/ --%>
    <c:when test="${countQuestionMark eq 0}">
      <c:set var="url" value="${url}?oid=${param.oid}"/>
    </c:when>
    <%-- /** add object id with and mark **/ --%>
    <c:otherwise>
      <c:set var="url" value="${url}&oid=${param.oid}"/>
    </c:otherwise>
  </c:choose>

  <%-- /** add command name string to url **/ --%>
  <c:set var="url" value="${url}&command=${one.sub.name}"/>

  <%-- /** add nodeId to the reference **/ --%>
  <c:if test="${nodeId ne null and nodeId ne 'null'}">
    <c:set var="url" value="${url}&nodeId=${nodeId}"/>
  </c:if>

  setUrl("<c:out value="${url}" escapeXml="false"/>");
</c:if>

<%-- /** print command icon url **/ --%>
<c:if test="${one.sub.icon ne null}">
    setIcon("<str:replace replace="${ICONURL}" with="../images"><c:out value="${one.sub.icon}" escapeXml="false"/></str:replace>");
</c:if>

<%-- /** print out target position and window position **/ --%>
<c:choose>
  <c:when test="${one.sub.targetPopup}">
    setTarget("Popup");
    setWidth("<c:out value="${one.sub.windowWidth}"/>");
    setHeight("<c:out value="${one.sub.windowHeight}"/>");
  </c:when>
  <c:when test="${one.sub.targetHidden or one.sub.submit}">
    setTarget("eFapsFrameHidden");
  </c:when>
  <c:otherwise>
    setTarget("Content");
  </c:otherwise>
</c:choose>
<c:set var="url" value="${url})"/>

<%-- /** test, if in the command the setting 'AskUser' is set. If yes, set the question **/ --%>
<c:if test="${one.sub.askUser}">
    setQuestion("<fmt:message><c:out value="${one.sub.name}"/>.Question</fmt:message>");
</c:if>

<%-- /**  **/ --%>
<c:if test="${one.sub.submit}">
    setSubmit(true);
    setSubmitForm(document.forms[0]);
</c:if>


<%-- /** test, if this is a search **/ --%>
<c:if test="${param.isSearch eq true and !one.menu}">
    setJavaScript("eFapsSearchSelect('<c:out value="${one.sub.name}"/>')");
</c:if>
