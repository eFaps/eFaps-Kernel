<%--
 
  Copyright 2003-2007 The eFaps Team
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 
  Author:          tmo
  Revision:        $Rev$
  Last Changed:    $Date$
  Last Changed By: $Author$
 
--%>

<%@page errorPage="Exception.jsp"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>
<%@taglib prefix="un"   uri="http://jakarta.apache.org/taglibs/unstandard-1.0"%>

<%@page import="org.efaps.admin.dbproperty.DBProperties"%>

  <%-- /** constructor for the menu tree bean with some initialise code **/ --%>
  <jsp:useBean id="uiObject" class="org.efaps.beans.MenuTreeBean" scope="request">
    <jsp:setProperty name="uiObject" property="oid" param="oid"/>
    <c:set var="label"><str:replace replace="'" with="\\'"><c:out value="${uiObject.instance.type.treeMenuName}"/></str:replace></c:set>

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
              "<c:out value="${one.sub.labelProperty}"/>",
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


