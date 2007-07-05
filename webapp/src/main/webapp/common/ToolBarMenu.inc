<%--
 
  Copyright 2003 - 2007 The eFaps Team
 
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
<%@page import="org.efaps.admin.ui.Command"%>


<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="h"    uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="t"    uri="http://myfaces.apache.org/tomahawk"%>
<%@taglib prefix="s"    uri="http://myfaces.apache.org/sandbox"  %>
<%@taglib prefix="eFaps"   uri="eFaps.tld"%>

 <h:form id="eFapsFrameToolbarMenu">
    <h:panelGrid>
      <t:jscookMenu layout="hbr" theme="ThemeOffice" styleLocation="../styles">
        <t:navigationMenuItems value="#{menuTableToolbar.JSFMenu}"/>
      </t:jscookMenu>
    </h:panelGrid>
  </h:form>
  
<style type="text/css">
       .dojoDialog {
           background: #eee;
           border: 2px solid #999;
           -moz-border-radius: 5px;
           padding: 4px; /*positioning outside of the visible scope
to prevent some ui glitches visibility hidden does not work*/
           position: absolute;
           left: -800px;
           top: -800px;
       }

       .viewDialog {
           background: #eee;
           border: 1px solid #999;
           -moz-border-radius: 5px;
           padding: 40px; /*positioning outside of the visible scope
to prevent some ui glitches visibility hidden does not work*/
           position: absolute;
           left: -800px;
           top: -800px;
       }

       .viewDialogDecoration {
           width: 600px;
       }

       .viewDialogContent {
           width: 600px;
           height: 500px;
       }

   </style>
  


<eFaps:ModalDialogs dialogAttr="bgColor='white' bgOpacity='0.8' toggle='fade' toggleDuration='250'"
    styleClass="dojoDialog"  >

</eFaps:ModalDialogs>
