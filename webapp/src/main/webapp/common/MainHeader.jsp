<%--
 
  Copyright 2006 The eFaps Team
 
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
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<%@include file = "../common/StdTop.inc"%>
<fmt:bundle basename="StringResource">

  <%-- /** constructor for the menu tree bean with some initialise code **/ --%>
  <jsp:useBean id="uiObject" class="org.efaps.beans.MenuMainToolBarBean" scope="request">
    <%
      uiObject.setResponse(response);
      uiObject.setRequest(request);
      uiObject.execute();
    %>
  </jsp:useBean>

  <html>
    <head/>
<%--
    <link rel="StyleSheet" href="../styles/eFapsDefault.css" type="text/css"/>

--%>
<link rel=stylesheet type="text/css" href="../menu/mapb_menu_b_style.css">

    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
    <script type="text/javascript">
      var eFapsTreeImagePath = "../images";
    </script>
<%--
    <script type="text/javascript" src="../javascripts/eFapsTree.js"></script>
--%>
    <script type="text/javascript">

      function eFapsProcessEnd()  {


<%--
        var tree=new eFapsTree("","");
        with (tree)  {
          <c:forEach items="${uiObject.menuHolder.subs}" var="one">
            addSubNode("",
                "<fmt:message><c:out value="${one.sub.label}"/></fmt:message>",
                false,
                <%@include file="MenuTreeOneCommand.inc"%>);
          </c:forEach>
        }

--%>
<%--
        var tree=new eFapsTree("","");

with (tree)  {
  <c:forEach items="${uiObject.menuHolder.subs}" var="one1">
    <c:set var="one" value="${one1}"/>
    with (addSubNode("","<fmt:message><c:out value="${one1.sub.label}"/></fmt:message>",false))  {
      <%@include file="TreeOneCommand.inc"%>
      <c:if test="${one1.menu}">
        <c:forEach items="${one1.subs}" var="one2">
          <c:set var="one" value="${one2}"/>
          with (addSubNode("","<fmt:message><c:out value="${one2.sub.label}"/></fmt:message>",false))  {
            <%@include file="TreeOneCommand.inc"%>
            <c:if test="${one2.menu}">
              <c:forEach items="${one2.subs}" var="one3">
                <c:set var="one" value="${one3}"/>
                with (addSubNode("","<fmt:message><c:out value="${one3.sub.label}"/></fmt:message>",false))  {
                  <%@include file="TreeOneCommand.inc"%>
                }
              </c:forEach>
            </c:if>
          }
        </c:forEach>
      </c:if>
      setIsolator(true);
    }
  </c:forEach>
}

//        tree.createInMainToolBar(document.getElementById('toolBar'));
tree.createInMenu(document.getElementById("eFapsMainToolBar"));
--%>
      }
    </script>


    <body onLoad="eFapsProcessEnd()" class="eFapsTopHeader">
<script type="text/javascript" language="Javascript">
top.mainMenu.calcWindow = this;
  top.mainMenu.doMenu(document, window.name);
  document.onclick = top.hideMenus;
</script>

        <%-- /** menu bar **/ --%>
<%--
        <div id="eFapsMainToolBarHeader">
          <div id="eFapsMainToolBarBorder">
            <div id="eFapsMainToolBar">
            </div>
          </div>
        </div>
--%>
<%--
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr id="toolBar">
          <td width="0%" class="eFapsMainToolBarUserName">
            <fmt:message>Standard.Text.UserName</fmt:message>:
            <%=context.getPerson().getFirstName()%> <%=context.getPerson().getLastName()%>
          </td>
          <td width="100%">&nbsp;</td>
        </tr>
      </table>
--%>
    </body>
  </html>
</fmt:bundle>
<%@include file = "../common/StdBottom.inc"%>
