<%@page errorPage="Exception.jsp"%>
<%@include file = "../common/StdTop.inc"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<fmt:bundle basename="StringResource">

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<%-- /** constructor for the form bean **/ --%>
<c:set var="method" value="ukTest"/>
<%@include file = "../common/FormBeanConstructor.inc"%>
<c:remove var="method"/>

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

</fmt:bundle>
