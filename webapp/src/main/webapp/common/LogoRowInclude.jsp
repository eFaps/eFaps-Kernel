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
<%@page import="org.efaps.admin.dbproperty.DBProperties"%>

<%@include file = "../common/StdTop.inc"%>

<tr>
  <td class="eFapsLogo">
   <div class="eFapsLogoWelcome">
      <%=DBProperties.getProperty("LogoRowInclude.Welcome.Label")%>
      <%=context.getPerson().getFirstName()%>
      <%=context.getPerson().getLastName()%>
    </div>
    <div class="eFapsLogoVersion">
      <%=DBProperties.getProperty("LogoRowInclude.Version.Label")%>
    </div>
  </td>
</tr>

<%@include file = "../common/StdBottom.inc"%>
