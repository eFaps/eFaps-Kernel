<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>

<fmt:bundle basename="StringResource">

  <%-- /** test, if in the command the setting 'AskUser' is set. If yes, set the question **/ --%>
  <c:choose>
    <c:when test="${row.askUser}">
      <fmt:message var="question"><c:out value="${row.name}"/>.Question</fmt:message>
    </c:when>
    <c:otherwise>
      <c:set var="question" value=""/>
    </c:otherwise>
  </c:choose>

  <c:set var="reference"><str:replace replace="${SERVLETURL}" with="/eFaps/request"><c:out value="${row.reference}" escapeXml="false"/></str:replace></c:set>
  <c:set var="reference"><str:replace replace="${COMMONURL}" with="/eFaps/common"><c:out value="${row.reference}" escapeXml="false"/></str:replace></c:set>
  <c:set var="reference"><str:replace replace="${ROOTURL}" with="/eFaps"><c:out value="${row.reference}" escapeXml="false"/></str:replace></c:set>
  <c:if test="${reference eq ''}"><c:set var="reference">/eFaps/common/Link.jsp</c:set></c:if>

  <%-- /** add the object id to the url reference **/ --%>
  <str:countMatches var="countQuestionMark" substring="?"><c:out value="${reference}" escapeXml="false"/></str:countMatches>
  <c:choose>
    <%-- /** add object id with question mark **/ --%>
    <c:when test="${countQuestionMark eq 0}">
      <c:set var="reference"><c:out value="${reference}" escapeXml="false"/>?oid=<c:out value="${oid}"/></c:set>
    </c:when>
    <%-- /** add object id with and mark **/ --%>
    <c:otherwise>
      <c:set var="reference"><c:out value="${reference}" escapeXml="false"/>&oid=<c:out value="${oid}"/></c:set>
    </c:otherwise>
  </c:choose>

  <%-- /** add the command name to the reference **/ --%>
  <c:set var="reference"><c:out value="${reference}" escapeXml="false"/>&command=<c:out value="${row.name}"/></c:set>

  <%-- /** add nodeId to the reference **/ --%>
  <c:if test="${nodeId ne null and nodeId ne 'null'}">
    <c:set var="reference" value="${reference}&nodeId=${nodeId}"/>
  </c:if>

  <c:choose>
    <c:when test="${row.action eq '1'}">
      <c:set var="reference" value="${commitUrl}"/>
    </c:when>
  </c:choose>

  <c:out value="${varName}"/>.add(
      "<fmt:message key="${row.name}.Label"/>",
      "<c:out value="${reference}" escapeXml="false"/>",
      <c:choose>
        <c:when test="${row.targetPopup or row.targetHidden}">
          false,
        </c:when>
        <c:otherwise>
          true,
        </c:otherwise>
      </c:choose>
      <c:out value="${row.targetPopup}"/>,
      <c:out value="${row.targetHidden}"/>,
      <c:out value="${row.windowWidth}"/>,
      <c:out value="${row.windowHeight}"/>,
      "<c:out value="${row.name}"/>",
      "<c:out value="${question}"/>",
      "");
</fmt:bundle>
