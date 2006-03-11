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
    <jsp:setProperty name="uiObject" property="loginName" value="<%=session.getAttribute("login.name")%>"/>
    <%
      uiObject.setResponse(response);
      uiObject.setRequest(request);
      uiObject.execute();
    %>
  </jsp:useBean>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">

<html>
  <head>
    <title></title>
    <script type="text/javascript" src="../javascripts/eFapsToolbar.js"></script>
    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>



<script type="text/javascript">
top.mainMenu = new eFapsToolbar(this, 'mainMenu', 0, 13);

//top.mainMenu.frameMenu = 'TopActionMenu';

//Menu definition
with (top.mainMenu.createCommandHolder())  {

  <c:forEach items="${uiObject.menuHolder.subs}" var="one1">
    <c:set var="one" value="${one1}"/>

    with (addCommand('<c:out value="${one.sub.name}"/>', '<fmt:message><c:out value="${one.sub.label}"/></fmt:message>'))  {
      <%@include file="TreeOneCommand.inc"%>
      <c:if test="${one.menu}">
        with (createCommandHolder())  {
          <c:forEach items="${one1.subs}" var="one2">
            <c:set var="one" value="${one2}"/>
            with (addCommand('<c:out value="${one.sub.name}"/>', '<fmt:message><c:out value="${one.sub.label}"/></fmt:message>'))  {
              <%@include file="TreeOneCommand.inc"%>
              <c:if test="${one.menu}">
                with (createCommandHolder())  {
                  <c:forEach items="${one2.subs}" var="one3">
                    <c:set var="one" value="${one3}"/>
                    with (addCommand('<c:out value="${one.sub.name}"/>', '<fmt:message><c:out value="${one.sub.label}"/></fmt:message>'))  {
                      <%@include file="TreeOneCommand.inc"%>
                    }
                  </c:forEach>
                }
              </c:if>
            }
          </c:forEach>
        }
      </c:if>
    }
  </c:forEach>
}

</script>


<%--

<c:out value="${one1.menu}"/>
    <c:if test="${one1.menu}">
      <c:forEach items="${one1.subs}" var="one2">

  <c:set var="one" value="${one2}"/>

with (top.mainMenu.addItem('<fmt:message><c:out value="${one.sub.label}"/></fmt:message>',{'url':'<c:out value="${url}" escapeXml="false"/>'}, false))  {
  <%@include file="TreeOneCommand.inc"%>
}

        <c:if test="${one2.menu}">
          <c:forEach items="${one2.subs}" var="one3">
  <c:set var="one" value="${one3}"/>

with (top.mainMenu.addItem('<fmt:message><c:out value="${one.sub.label}"/></fmt:message>',{'url':'<c:out value="${url}" escapeXml="false"/>'}, false))  {
  <%@include file="TreeOneCommand.inc"%>
}
          </c:forEach>
top.mainMenu.addItem('',{'url':'1'}, true);
        </c:if>


      </c:forEach>
top.mainMenu.addItem('',{'url':'1'}, true);
    </c:if>

--%>


    <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css">
    <link rel="stylesheet" type="text/css" href="../styles/eFapsToolbar.css">

  </head>
<!--
  <frameset rows="50,*,0" frameborder="no" border="0" framespacing="0">
    <frame src="MainHeader.jsp" name="TopActionMenu"    scrolling="NO" noresize >
    <frame src="Content.jsp"    name="Content"          scrolling="NO" noresize >
    <frame src="about:blank"    name="eFapsFrameHidden" class="eFapsFrameHidden"/>
  </frameset>
-->

<body onLoad="eFapsPositionContent()" leftmargin="0" topmargin="0" rightmargin="0" marginwidth="0">
    <script type="text/javascript">
      top.mainMenu.doMenu(document);
      document.onclick = function()  {
        top.mainMenu.hideAll();
      }
    </script>
 <iframe src="Content.jsp" name="Content" class="eFapsFrameContent" id="eFapsFrameContent" scrolling="no" frameborder="0"></iframe>
 <iframe src="Content.jsp" name="eFapsFrameHidden" class="eFapsFrameHidden" scrolling="no" frameborder="0" id="eFapsFrameHidden"></iframe>
</body>
</html>


</fmt:bundle>
<%@include file = "../common/StdBottom.inc"%>
