<%@page errorPage="Exception.jsp"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">

<html>
  <head>
    <title></title>
    <script type="text/javascript" src="../javascripts/eFapsToolbar.js"></script>
    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
    <script type="text/javascript" src="../javascripts/eFapsMyFacesHackFix.js"></script>

    <link rel="stylesheet" type="text/css" href="../styles/eFapsMain.css">
  </head>
  <body onLoad="eFapsPositionContent()" leftmargin="0" topmargin="0" rightmargin="0" marginwidth="0" scroll="no">
    <table class="main">
      <%@include file="LogoRowInclude.jsp"%>
      <tr>
        <td class="mainMenu">
          <f:view>
            <tiles:insert page="/Main.jsp" flush="false"/>
            <t:jscookMenu layout="hbr" theme="ThemeOffice" styleLocation="../styles">
              <t:navigationMenuItems value="#{menuMainToolbar.JSFMenu}"/>
            </t:jscookMenu>
          </f:view>
        </td>
      </tr>
      <tr>
        <td><iframe src="Content.jsp" name="Content" class="eFapsFrameContent" id="eFapsFrameContent" scrolling="no" frameborder="1"></iframe></td>
      </tr>
    </table>
    <iframe src="Content.jsp" name="eFapsFrameHidden" class="eFapsFrameHidden" scrolling="no" frameborder="0" id="eFapsFrameHidden"></iframe>
  </body>
</html>
