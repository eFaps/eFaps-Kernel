<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<%
  String cacheKey  = request.getParameter("cacheKey");

  if (cacheKey==null)  {
    cacheKey = Long.toString(System.currentTimeMillis());
System.out.println("create new cacheKey = "+cacheKey);
  }

  org.efaps.beans.AbstractBean uiObject = (org.efaps.beans.AbstractBean)request.getAttribute("uiObject");
  if (uiObject==null)  {
    uiObject = cache.getTableBean(cacheKey, request.getParameter("command"));
    request.setAttribute("uiObject", uiObject);
  }
  uiObject.setResponse(response);
%>

<c:choose>
  <%-- /** initialise the user interface object **/ --%>
  <c:when test="${uiObject.initialised eq false}">
    <%-- /** set other parameters **/ --%>
    <jsp:setProperty name="uiObject" property="commandName" param="command"/>
  </c:when>
</c:choose>

<c:choose>
  <c:when test="${param.mode eq 'edit' or uiObject.editMode}">
    <c:set var="bodyClass" value="eFapsFrameEdit"/>
  </c:when>
  <c:otherwise>
    <c:set var="bodyClass" value="eFapsFrameView"/>
  </c:otherwise>
</c:choose>
<html>
<%-- /** test for mode print **/ --%>
<c:choose>

  <%-- /** table in mode print **/ --%>
  <c:when test="${param.mode ne null and param.mode eq 'print'}">
    
      <head>
        <jsp:include page="StdHeaderMetaInfo.inc"/>
      </head>
      <link rel="StyleSheet" href="../styles/eFapsDefaultPrint.css" type="text/css"/>
<link rel=stylesheet type="text/css" href="../menu/mapb_menu_b_style.css"/>
      <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

<script type="text/javascript">
  var eFapsTreeImagePath = '../images/';
</script>
<script type="text/javascript" src="../javascripts/eFapsTree.js"></script>


      <body onLoad="eFapsProcessEndPrint()">

        <%-- /** frame header **/ --%>
        <div class="eFapsFrameHeader">
          <div id="eFapsFrameHeaderTitle" class="eFapsFrameHeaderTitle">
            <span id="eFapsFrameHeaderText" class="eFapsFrameHeaderTitle">Loading...</span>
            <img id="eFapsFrameHeaderProgressBar" src="../images/eFapsProcess.gif"/>
          </div>
        </div>

        <%-- /** filter bar **/ --%>
        <div class="eFapsFrameFilter">
          <form>
            <select name="eFapsFrameFilter" size="1" onChange="eFapsGetFrameFilter().execute(this.options[this.selectedIndex].value);" style="display:none">
            </select>
          </form>
        </div>

        <%-- /** show table itself **/ --%>
        <jsp:include page="TableMain.jsp?<%=request.getQueryString()%>">
          <jsp:param name="mode" value="print"/>
          <jsp:param name="cacheKey" value="<%=cacheKey%>"/>
        </jsp:include>

        <%-- /** footer frame **/ --%>
        <div class="eFapsFrameFooter">
        </div>
      </body>
  </c:when>

  <%-- /** normal table **/ --%>
  <c:otherwise>
    
      <head>
        <jsp:include page="StdHeaderMetaInfo.inc"/>
      </head>
      <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css"/>
      <link rel="stylesheet" type="text/css" href="../styles/eFapsToolbar.css"/>

      <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

<script type="text/javascript">
  var eFapsTreeImagePath = '../images/';
</script>
<script type="text/javascript" src="../javascripts/eFapsTree.js"></script>

      <body onLoad="eFapsPositionMain()" onUnLoad="eFapsCleanUpSession('<%=cacheKey%>')" class="<c:out value="${bodyClass}"/>">

<script type="text/javascript">
  document.onclick = function()  {
    top.mainMenu.hideAll();
  }
</script>
        <%-- /** frame header **/ --%>
        <div class="eFapsFrameHeader">
          <div id="eFapsFrameHeaderTitle" class="eFapsFrameHeaderTitle">
            <span id="eFapsFrameHeaderText" class="eFapsFrameHeaderTitle">Loading...</span>
            <img id="eFapsFrameHeaderProgressBar" src="../images/eFapsProcess.gif"/>
          </div>
        </div>

        <%-- /** filter bar **/ --%>
        <div class="eFapsFrameFilter">
          <form>
            <select name="eFapsFrameFilter" size="1" onChange="eFapsGetFrameFilter().execute(this.options[this.selectedIndex].value);" style="display:none">
            </select>
          </form>
        </div>

        <%-- /** show table itself **/ --%>
        <iframe frameborder="no"
            id="eFapsFrameMain"
            class="eFapsFrameMain"
            name="genMain"
            src="TableMain.jsp?<%=request.getQueryString()%>&cacheKey=<%=cacheKey%>">
        </iframe>

        <%-- /** hidden frame **/ --%>
        <iframe name="eFapsFrameHidden" id="eFapsFrameHidden" class="eFapsFrameHidden" src="about:blank">
        </iframe>

        <%-- /** footer frame **/ --%>
        <div class="eFapsFrameFooter">
        </div>
      </body>
    
  </c:otherwise>
</c:choose>
</html>