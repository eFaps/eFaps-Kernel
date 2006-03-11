<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>

<c:choose>
  <c:when test="${param.nodeId eq null or param.nodeId eq ''}">
    <html>
      <head>
        <title></title>
      </head>
      <frameset cols="240,*">
        <frame src="MenuTreeShow.jsp?<%=request.getQueryString()%>" name="TreeNavigation"/>
        <frame src="about:blank" name="Content" scrolling="auto" marginwidth="30" marginheight="0"  frameborder="yes"/>
    </frameset>
    </html>
  </c:when>
  <c:otherwise>
    <jsp:include page="MenuTreeShow.jsp"/>
  </c:otherwise>
</c:choose>
