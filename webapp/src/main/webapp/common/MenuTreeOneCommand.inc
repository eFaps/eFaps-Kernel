
<%-- /** print command url **/ --%>
<c:set var="url" value="${one.sub.reference}"/>
<c:if test="${url eq null and (one.sub.targetTable ne null or one.sub.targetForm ne null or one.sub.targetSearch ne null)}">
  <c:set var="url" value="Link.jsp"/>
</c:if>
<c:if test="${url eq null and one.sub.action eq '1'}">
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

  <%-- /** replace servlet url **/ --%>
  <str:replace replace="${SERVLETURL}" with="../request" var="url"><c:out value="${url}" escapeXml="false"/></str:replace>

  <%-- /** replace common url **/ --%>
  <str:replace replace="${COMMONURL}" with="../common" var="url"><c:out value="${url}" escapeXml="false"/></str:replace>

  <%-- /** replace root url **/ --%>
  <str:replace replace="${ROOTURL}" with=".." var="url"><c:out value="${url}" escapeXml="false"/></str:replace>

</c:if>
"<c:out value="${url}" escapeXml="false"/>",

<%-- /** print command icon url **/ --%>
<c:choose>
  <c:when test="${one.icon ne null}">
    "<c:out value="${one.icon}" escapeXml="false"/>",
  </c:when>
  <c:otherwise>
    "",
  </c:otherwise>
</c:choose>

<%-- /** print out target position and window position **/ --%>
<c:choose>
  <c:when test="${one.sub.targetPopup}">
    "Popup","<c:out value="${one.sub.windowWidth}"/>","<c:out value="${one.sub.windowHeight}"/>"
  </c:when>
  <c:otherwise>
    "Content"
  </c:otherwise>
</c:choose>
<c:set var="url" value="${url})"/>
