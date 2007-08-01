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
 
  Author:          jmo
  Revision:        $Rev$
  Last Changed:    $Date$
  Last Changed By: $Author$
 
--%>
<%@page import="org.efaps.admin.dbproperty.DBProperties"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="h"    uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="t"    uri="http://myfaces.apache.org/tomahawk"%>
<%@taglib prefix="s"    uri="http://myfaces.apache.org/sandbox"  %>

  <style type="text/css">
    #dialogFrameModal.dojoDialog {
           background: #eee;
           border: 2px solid #999;
           -moz-border-radius: 5px;
           padding: 5px; 
           position: absolute;
           left: -800px;
           top: -800px;
       }
       
       .dojoDialog {
           background: #eee;
           border: 2px solid #999;
           -moz-border-radius: 5px;
           padding: 5px; 
           position: absolute;
           left: -800px;
           top: -800px;
       }
       
       
   </style>

  <div dojoType="dialog" id="dialogWarn" bgColor="red" bgOpacity="0.1" toggle="fade" toggleDuration="250" >
   <table id="warnTable" >
   <tr>
   <td><p id="dialogWarnContent">warntext</p></td>
   </tr>
   <tr align="center">
     <td><t:commandButton onclick="dlgWarn.hide()" value="close"></t:commandButton></td>
   </tr>
   </table>
  </div>
  <div dojoType="dialog" id="dialogError" bgColor="red" bgOpacity="0.8" toggle="fade" toggleDuration="250" >
   <table id="errorTable" width="400" >
   <tr class="eFapsErrorLabel">
     <td><%=DBProperties.getProperty("JSPPage.Exception.TextId")%></td>
     <td><p id="dialogErrorId">errorID</p></td> 
   </tr>
   <tr>
     <td><%=DBProperties.getProperty("JSPPage.Exception.TextMessage")%></td>
     <td><p id="dialogErrorMsg">errorMsg</p></td>
   </tr>
   <tr>
     <td><%=DBProperties.getProperty("JSPPage.Exception.TextAction")%></td>
     <td><p id="dialogErrorAct">errorAct</p></td>
   </tr>
   <tr align="center">
     <td colspan="2"><t:commandButton onclick="dlgError.hide()" value="close"></t:commandButton></td>
   </tr>  
   </table>
  </div>
  <%-- modal dialog used for processing --%>
  <div dojoType="dialog" id="dialogProcess" bgColor="black" bgOpacity="0.3" toggle="fade" toggleDuration="250" >
    <t:outputText id="msg" value="processing..."/>
  </div>
  <div dojoType="dialog" id="dialogFrameModal" bgColor="white" bgOpacity="0.8" toggle="fade" toggleDuration="250" >
    <iframe frameborder="0" width="50" height="50" name="eFapsFrameModal" id="eFapsFrameModal" src="Blank.jsp"></iframe>
  </div> 