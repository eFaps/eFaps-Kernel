<%@page errorPage="Exception.jsp"%>
<%@include file = "../common/StdTop.inc"%>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<%-- /** constructor for the form bean **/ --%>
<c:set var="method" value="process"/>
<%@include file = "../common/FormBeanConstructor.inc"%>
<c:remove var="method"/>

<html>
  <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
  <body>
    <script language="Javascript">
      if (top.opener.name=='TreeNavigation')  {
        eFapsCommonOpenUrl('MenuTree.jsp?oid=<%=uiObject.getInstance().getOid()%>', 'Content');
      } else  {
        top.opener.parent.eFapsCommonRefresh();
      }
      eFapsCommonCloseWindow();
    </script>
  </body>
</html>

<%@include file = "../common/StdBottom.inc"%>
