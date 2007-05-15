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

    <link rel="stylesheet" type="text/css" href="../styles/eFapsMain.css"/>
  </head>
  <body onLoad="eFapsPositionContent()" leftmargin="0" topmargin="0" rightmargin="0" marginwidth="0" scroll="no">
    <table class="eFapsMainTable">
      <%@include file="LogoRowInclude.jsp"%>
      <tr>
        <td class="eFapsMainTableRowMenu">
          <f:view>
            
            <tiles:insert page="/Main.jsp" flush="false"/>
            <t:jscookMenu layout="hbr" theme="ThemeOffice" styleLocation="../styles">
              <t:navigationMenuItems value="#{menuMainToolbar.JSFMenu}"/>
            </t:jscookMenu>
          </f:view>
        </td>
      </tr>
      <tr>
        <td class="eFapsMainTableRowContent">
          <iframe src="Content.jsp" name="Content" class="eFapsFrameContent" id="eFapsFrameContent" scrolling="no" frameborder="0"></iframe>
        </td>
      </tr>
    </table>
    <iframe src="Content.jsp" name="eFapsFrameHidden" class="eFapsFrameHidden" scrolling="no" frameborder="0" id="eFapsFrameHidden"></iframe>
  </body>
</html>
