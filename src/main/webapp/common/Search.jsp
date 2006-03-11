<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@page errorPage="Exception.jsp"%>
<%
System.out.println("search="+request.getParameter("search"));
%>
<html>
  <head>
  </head>
  <link rel="StyleSheet" href="../styles/eFapsDefault.css" type="text/css"/>
  <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>


<script type="text/javascript">
  var eFapsTreeImagePath = '../images/';
</script>
<script type="text/javascript" src="../javascripts/eFapsTree.js"></script>

  <body onLoad="eFapsPositionMain()" class="eFapsFrameEdit">
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

    <%-- /** filter bar **/ --%>
    <div class="eFapsFrameFilter">
      <form>
        <select name="eFapsFrameFilter" size="1" onChange="eFapsGetFrameFilter().execute(this.options[this.selectedIndex].value);" style="display:none">
        </select>
      </form>
    </div>

    <%-- /** set the new url of the main search page **/ --%>
    <c:url var="searchUrl" value="../common/SearchMain.jsp">
      <c:param name="search" value="${param.search}"/>
      <c:param name="parentOid" value="${param.oid}"/>
    </c:url>

    <%-- /** the frame for the inbedded search **/ --%>
    <iframe frameborder="no" id="eFapsFrameMain" class="eFapsFrameMain" name="genMain" src="<c:out value="${searchUrl}" escapeXml="false"/>">
    </iframe>

    <iframe name="eFapsFrameHidden" id="eFapsFrameHidden" class="eFapsFrameHidden" src="about:blank">
    </iframe>

    <div class="eFapsFrameFooter">
    </div>
  </body>
</html>
