<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>
<%@taglib prefix="un"   uri="http://jakarta.apache.org/taglibs/unstandard-1.0"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<%@include file = "../common/StdTop.inc"%>

<fmt:bundle basename="StringResource">

  <%-- /** constructor for the menu tree bean with some initialise code **/ --%>
  <jsp:useBean id="uiObject" class="org.efaps.beans.MenuTreeBean" scope="request">
    <jsp:setProperty name="uiObject" property="loginName" value="<%=session.getAttribute("login.name")%>"/>
    <%
      uiObject.setResponse(response);
      uiObject.setRequest(request);
    %>
    <jsp:setProperty name="uiObject" property="oid" param="oid"/>
    <c:set var="label"><str:replace replace="'" with="\\'"><fmt:message><c:out value="${uiObject.instance.type.treeMenuName}.Label"/></fmt:message></str:replace></c:set>

    <jsp:setProperty name="uiObject" property="menuLabel" value="<%=pageContext.getAttribute("label")%>"/>

    <c:remove var="label"/>
    <%
      uiObject.execute();
    %>
  </jsp:useBean>

<html>
  <link rel="StyleSheet" type="text/css" href="../styles/eFapsDefault.css"/>
  <link rel="StyleSheet" type="text/css" href="../menu/mapb_menu_b_style.css">
  <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
  <script type="text/javascript">
    var eFapsTreeImagePath = "../images";
  </script>
  <script type="text/javascript" src="../javascripts/eFapsTree.js"></script>

  <script type="text/javascript">
    function eFapsProcessEnd()  {
      <c:set var="one" value="${uiObject.menuHolder}"/>
      <%-- /** is a node id is given? if not create a new tree, otherwise completly new **/ --%>
      <c:choose>
        <%-- /** test, if in the tree the subtree already exists **/ --%>
        <c:when test="${param.nodeId ne null and param.nodeId ne ''}">
          var tree;
          var subTree;

var contentFrame = eFapsCommonFindFrame(top, "Content");
if (contentFrame && contentFrame.TreeNavigation)  {
  tree = contentFrame.TreeNavigation.eFapsTree.prototype.allTrees[<c:out value="${param.nodeId}"/>];
} else if (top.TreeNavigation)  {
  tree = top.TreeNavigation.eFapsTree.prototype.allTrees[<c:out value="${param.nodeId}"/>];
} else  {
alert("Tree not found!");
}
subTree = tree.getSubNodeWithId("<c:out value="${uiObject.instance.oid}"/>");

//          if (top.Login.Content && top.Login.Content.TreeNavigation)  {
//            tree =top.Login.Content.TreeNavigation.eFapsTree.prototype.allTrees[<c:out value="${param.nodeId}"/>];
//            subTree = tree.getSubNodeWithId("<c:out value="${uiObject.instance.oid}"/>");
//          }
          var selected;
          if (subTree)  {
            selected = subTree;
          } else  {
            tree=tree.addSubNode(
                "<c:out value="${uiObject.instance.oid}"/>",
                "<c:out value="${uiObject.menuLabel}" escapeXml="false"/>",
                true,
                <%@include file="MenuTreeOneCommand.inc"%>);
        </c:when>
        <%-- /** create a complete new tree **/ --%>
        <c:otherwise>
          var selected;
          var tree=new eFapsTree(
              "<c:out value="${uiObject.instance.oid}"/>",
              "<c:out value="${uiObject.menuLabel}" escapeXml="false"/>",
              false,
              <%@include file="MenuTreeOneCommand.inc"%>);
        </c:otherwise>
      </c:choose>
      with (tree)  {
        <c:forEach items="${uiObject.menuHolder.subs}" var="one">
          <c:if test="${one.sub.defaultSelected}">
            selected =
          </c:if>
          addSubNode("",
              "<fmt:message><c:out value="${one.sub.name}"/>.Label</fmt:message>",
              false,
              <%@include file="MenuTreeOneCommand.inc"%>);
        </c:forEach>
      }

      <c:choose>
        <c:when test="${param.nodeId ne null and param.nodeId ne ''}">
          }
        </c:when>
        <c:otherwise>
          tree.createInTree(document.getElementById('tree'));
        </c:otherwise>
      </c:choose>
      if (selected)  {
        selected.nodeSelect();
      } else  {
        tree.nodeSelect();
      }
    }
  </script>
  <body onLoad="eFapsProcessEnd()" class="eFapsTreeNav">
<script type="text/javascript">
//  top.mainMenu.doMenu(document, window.name);
//  document.onclick = top.mainMenu.hideAll();
</script>
    <div id="tree" class="eFapsTreeTabShow">
    </div>
  </body>
</html>
</fmt:bundle>
<%@include file = "../common/StdBottom.inc"%>
