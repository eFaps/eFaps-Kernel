<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>

<%@page import="org.efaps.admin.ui.Command"%>
<%@page import="org.efaps.admin.ui.CommandAbstract"%>
<%@page import="org.efaps.admin.ui.Menu"%>

<%@include file = "../common/StdTop.inc"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<fmt:bundle basename="StringResource">

  <%-- /** constructor for the form bean **/ --%>
  <c:set var="method" value="execute"/>
  <%@include file = "../common/FormBeanConstructor.inc"%>
  <c:remove var="method"/>

  <%
    String timeStamp  = request.getParameter("timeStamp");

    if (timeStamp==null)  {
      timeStamp = Long.toString(System.currentTimeMillis());
  System.out.println("create new timestamp = "+timeStamp);
    }

    String urlMain = "FormMain.jsp?"+request.getQueryString()+"&timeStamp="+timeStamp;
  %>

  <c:choose>
    <c:when test="${uiObject.viewMode}">
      <c:set var="bodyClass" value="eFapsFrameView"/>
    </c:when>
    <c:otherwise>
      <c:set var="bodyClass" value="eFapsFrameEdit"/>
    </c:otherwise>
  </c:choose>

  <%-- /** test for mode print **/ --%>
  <c:choose>

    <%-- /** form in mode print **/ --%>
    <c:when test="${param.mode ne null and param.mode eq 'print'}">
     <html>
        <head>
        </head>
        <link rel="StyleSheet" type="text/css" href="../styles/eFapsDefaultPrint.css"/>
        <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
        <body onLoad="eFapsProcessEnd()">

          <%-- /** frame header **/ --%>
          <div class="eFapsFrameHeader">
            <div id="eFapsFrameHeaderTitle" class="eFapsFrameHeaderTitle">
              <span id="eFapsFrameHeaderText" class="eFapsFrameHeaderTitle">Loading...</span>
              <img id="eFapsFrameHeaderProgressBar" src="../images/eFapsProcess.gif"/>
            </div>
          </div>

          <%-- /** show form itself **/ --%>
          <jsp:include page="<%=urlMain%>">
            <jsp:param name="mode" value="print"/>
          </jsp:include>
        </body>
      </html>
    </c:when>

    <%-- /** normal form **/ --%>
    <c:otherwise>
      <html>
        <head>
        </head>
        <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css"/>
        <link rel="stylesheet" type="text/css" href="../styles/eFapsToolbar.css"/>
        <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

        <script type="text/javascript">
          var eFapsTreeImagePath = '../images/';
        </script>
        <script type="text/javascript" src="../javascripts/eFapsTree.js"></script>

        <body onLoad="eFapsPositionMain()" onUnLoad="eFapsCleanUpSession('<%=timeStamp%>')" class="<c:out value="${bodyClass}"/>">

          <div class="eFapsFrameHeader">
            <div id="eFapsFrameHeaderTitle" class="eFapsFrameHeaderTitle">
              <span id="eFapsFrameHeaderText" class="eFapsFrameHeaderTitle">Loading...</span>
              <img id="eFapsFrameHeaderProgressBar" src="../images/eFapsProcess.gif"/>
            </div>
          </div>

          <%-- /** menu bar **/ --%>
          <div id="eFapsFrameMenuHeader">
            <div id="eFapsFrameMenuBorder">
              <div id="eFapsFrameMenu">
              </div>
            </div>
          </div>

          <iframe frameborder="no" id="eFapsFrameMain" class="eFapsFrameMain" name="genMain" src="<%=urlMain%>">
          </iframe>


          <iframe name="eFapsFrameHidden" id="eFapsFrameHidden" class="eFapsFrameHidden" src="about:blank">
          </iframe>
          <div class="eFapsFrameFooter">
          </div>
        </body>
      </html>
    </c:otherwise>
  </c:choose>

</fmt:bundle>

<%@include file = "../common/StdBottom.inc"%>
