<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>


<%
  org.efaps.beans.SearchBean uiObject = new org.efaps.beans.SearchBean();
  request.setAttribute("uiObject", uiObject);
  uiObject.setResponse(response);
%>

<%-- /** initialise the user interface object **/ --%>
<c:if test="${uiObject.initialised eq false}">
  <%-- /** set other parameters **/ --%>
  <jsp:setProperty name="uiObject" property="searchName" param="search"/>
</c:if>

<fmt:bundle basename="StringResource">
  <html>
    <link rel="StyleSheet" href="../styles/eFapsDefault.css" type="text/css"/>
    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

    <script language="Javascript">
      function eFapsProcessEnd() {
        parent.parent.eFapsSetTitle('<fmt:message key="${uiObject.search.label}"/>');

      <%-- /** footer action menu:
        - show button cancel **/ --%>
      var footerAction = parent.parent.eFapsGetFrameFooterAction();
      footerAction.clean();
      footerAction.setForm(document.forms[0]);
      footerAction.add(
            "<fmt:message key="Standard.Button.Cancel"/>",
            "javascript:eFapsCommonCloseWindow()",
            false,
            false,
            false,
            '',
            '',
            '',
            '',
            '../images/eFapsButtonCancel.gif');

        parent.parent.eFapsProcessEnd();
      }
    </script>
    <body onLoad="eFapsProcessEnd()">

      <c:forEach items="${uiObject.search.commands}" var="menu">
        <div class="eFapsSearchMenu">
          <a class="eFapsSearchMenu" href="javascript:eFapsSearchPlusMinus('<c:out value="${menu.name}"/>')">
          <img align="absmiddle" border="0" src="../images/eFapsSearchMinus.gif"/>
            <fmt:message key="${menu.label}"/>
          </a>
          <br/>
          <div class="eFapsSearchCommand" id="<c:out value="${menu.name}"/>">
            <c:forEach items="${menu.commands}" var="command">
              <a id="<c:out value="${command.name}"/>" class="eFapsSearchCommand" href="javascript:eFapsSearchSelectOld('<c:out value="${command.name}"/>')">
                <fmt:message key="${command.label}"/>
              </a>
              <br/>
            </c:forEach>
          </div>
        </div>
      </c:forEach>

    </body>

  </html>
</fmt:bundle>
