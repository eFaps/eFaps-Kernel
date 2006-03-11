<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<%@include file = "../common/StdTop.inc"%>

<fmt:bundle basename="StringResource">

  <%-- /** constructor for the menu tree bean with some initialise code **/ --%>
  <jsp:useBean id="uiObject" class="org.efaps.beans.MenuTabBean" scope="request">
    <jsp:setProperty name="uiObject" property="loginName" value="<%=session.getAttribute("login.name")%>"/>
    <%
      uiObject.setResponse(response);
      uiObject.setRequest(request);
      uiObject.execute();
    %>
  </jsp:useBean>

  <html>
    <link rel="StyleSheet" href="../styles/eFapsDefault.css" type="text/css"/>
    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
    <script type="text/javascript">
      var eFapsTreeImagePath = "../images";
    </script>
    <script type="text/javascript" src="../javascripts/eFapsTree.js"></script>

    <script type="text/javascript">
      function treeLoad4OnLoad()  {
        var tree=new eFapsTree("","");
        with (tree)  {
          <c:forEach items="${uiObject.menuHolder.subs}" var="one1">
            <c:set var="one" value="${one1}"/>
            with (addSubNode("","<fmt:message><c:out value="${one1.sub.name}"/>.Label</fmt:message>",false,<%@include file="MenuTreeOneCommand.inc"%>))  {
              <c:forEach items="${one1.subs}" var="one2">
                <c:set var="one" value="${one2}"/>
                with (addSubNode("","<fmt:message><c:out value="${one2.sub.name}"/>.Label</fmt:message>",false,<%@include file="MenuTreeOneCommand.inc"%>))  {
                  <c:if test="${one2.menu}">
                    <c:forEach items="${one2.subs}" var="one3">
                      <c:set var="one" value="${one3}"/>
                      addSubNode("","<fmt:message><c:out value="${one3.sub.name}"/>.Label</fmt:message>",false,<%@include file="MenuTreeOneCommand.inc"%>);
                    </c:forEach>
                  </c:if>
                }
              </c:forEach>
            }
          </c:forEach>
        }

        tree.createInTab(document.getElementById('tree'));
      }
    </script>
    <body onLoad="treeLoad4OnLoad()" class="eFapsTreeTab">
<script type="text/javascript">
parent.mainMenu.doMenu(window.name);
document.onclick = top.hideMenus;
</script>
      <div id="tree" class="eFapsTreeTabShow">
        <p style="text-align:right">
          <a href="javascript:eFapsTreeTabHide()">
            <img src="../images/eFapsTreeTabButtonHide.gif" class="eFapsTreeTabButtonShowHide"/>
          </a>
        </p>
      </div>
      <div id="buttonShow" class="eFapsTreeTabHide">
        <a href="javascript:eFapsTreeTabShow()">
          <img src="../images/eFapsTreeTabButtonShow.gif" class="eFapsTreeTabButtonShowHide"/>
        </a>
      </div>
    </body>
  </html>
</fmt:bundle>
<%@include file = "../common/StdBottom.inc"%>
