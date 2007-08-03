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

<%@taglib prefix="h"    uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="f"    uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="t"    uri="http://myfaces.apache.org/tomahawk"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">

<html>
  <head>
    <title></title>
    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
    <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css"/>
  </head>
  <body leftmargin="0" topmargin="0" rightmargin="0" marginwidth="0" scroll="no">
    <table class="eFapsMainTable">
      <%@include file="LogoRowInclude.jsp"%>
      <tr>
        <td class="eFapsMainTableRowMenu" id="eFapsMainTableRowMenu">
          <f:view>
            <t:dojoInitializer require="dojo.widget.Dialog"></t:dojoInitializer>
            <h:form>
              <t:jscookMenu layout="hbr" theme="ThemeOffice" styleLocation="../styles">
                <t:navigationMenuItems value="#{menuMainToolbar.JSFMenu}"/>
              </t:jscookMenu>
            </h:form>
          </f:view>
        </td>
      </tr>
      <tr>
        <td>
          <iframe
              width="100%"
              style="eFapsFrameContent"
              src="Content.jsp"
              name="Content"
              id="eFapsFrameContent"
              scrolling="no"
              frameborder="0">
          </iframe>
        </td>
      </tr>
    </table>

    <iframe
        src="Content.jsp"
        name="eFapsFrameHidden"
        class="eFapsFrameHidden"
        scrolling="no"
        frameborder="0"
        id="eFapsFrameHidden">
    </iframe>

    <script language="JavaScript">
      eFapsPositionContent();
      window.onresize = function() { eFapsPositionContent(); }
   
      function init(e) {
        dlgErrorMain = dojo.widget.byId("dialogError");
      }
  
      dojo.addOnLoad(init);
      
      function eFapsOpenErrorDialog(_Id, _Msg, _Act){
        eFapsAddChildText(document.getElementById("dialogErrorId"), _Id);
        eFapsAddChildText(document.getElementById("dialogErrorMsg"), _Msg);
        eFapsAddChildText(document.getElementById("dialogErrorAct"), _Act);
        dlgErrorMain.show();
      }
      
      
    </script>
  <%-- modal dialog used for errors --%>
  <div dojoType="dialog" id="dialogError" bgColor="red" bgOpacity="0.8" toggle="fade" toggleDuration="250" >
   <table id="errorTable" width="400" >
   <tr valign="top">
     <td><%=DBProperties.getProperty("JSPPage.Exception.TextId")%></td>
     <td><p id="dialogErrorId">errorID</p></td> 
   </tr>
   <tr>
     <td valign="top"><%=DBProperties.getProperty("JSPPage.Exception.TextMessage")%></td>
     <td><p id="dialogErrorMsg">errorMsg</p></td>
   </tr>
   <tr>
     <td valign="top"><%=DBProperties.getProperty("JSPPage.Exception.TextAction")%></td>
     <td><p id="dialogErrorAct">errorAct</p></td>
   </tr>
   </table>
   <center>
     <Button onclick="dlgErrorMain.hide()">Close</Button>
     <br/><br/>
   </center>
  </div>  
  </body>
</html>
