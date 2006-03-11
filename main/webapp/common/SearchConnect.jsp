<%@page errorPage="Exception.jsp"%>
<%

  String[] oids = (String[])request.getParameterValues("selectedRow");

  org.efaps.beans.SearchBean uiObject = new org.efaps.beans.SearchBean();
  request.setAttribute("uiObject", uiObject);
  uiObject.setResponse(response);
  %>
    <%-- /** set other parameters **/ --%>
    <jsp:setProperty name="uiObject" property="searchName"  param="search"/>
    <jsp:setProperty name="uiObject" property="commandName" param="searchCommand"/>
    <jsp:setProperty name="uiObject" property="oid"         param="parentOid"/>
    <jsp:setProperty name="uiObject" property="loginName"   value="<%=session.getAttribute("login.name")%>"/>
  <%

  uiObject.execute4Connect(oids);

%>
<html>
  <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
  <body>
    <script language="Javascript">
      top.opener.eFapsCommonRefresh();
      eFapsCommonCloseWindow();
    </script>
  </body>
</html>
