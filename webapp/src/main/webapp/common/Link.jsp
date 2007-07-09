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

<%@page import="org.efaps.admin.ui.Command"%>
<%@page import="org.efaps.admin.ui.CommandAbstract"%>
<%@page import="org.efaps.admin.ui.Menu"%>
<%@page import="org.efaps.admin.event.TriggerEvent"%>
<%@page import="org.efaps.db.Context"%>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<%
Context context = Context.getThreadContext();
System.out.println("Link.jsp.1");
  CommandAbstract command = Command.get(context.getParameter("command"));
System.out.println("Link.jsp.2");
  if (command==null)  {
System.out.println("Link.jsp.3");
    command = Menu.get(context.getParameter("command"));
System.out.println("Link.jsp.4");
  }
System.out.println("Link.jsp.5");

  
   
  if (command==null)  {
System.out.println("command == NULL!!!!!!!");
  } else if (command.getTargetMode() == CommandAbstract.TARGET_MODE_SEARCH)  {
System.out.println("searchresult="+context.getParameter("eFapsShowSearchResult"));
    if ("true".equalsIgnoreCase(context.getParameter("eFapsShowSearchResult")))  {
      %><%@include file="Table.inc"%><%
    } else  {
      %><%@include file="Form.inc"%><%
    }
  } else if (command.getTargetForm() != null)  {
    %><%@include file="Form.inc"%><%
  } else if (command.getTargetTable() != null)  {
    %><%@include file="Table.inc"%><%
  } else if (command.getTargetSearch() != null)  {
    %><%@include file="Search.inc"%><%
  } else if (command.hasTrigger()){
    command.executeTrigger(TriggerEvent.COMMAND,
                           (String[])request.getParameterValues("selectedRow"));
    //after a commandtrigger is executet the page is updatet                   
    if (!"true".equals(command.getProperty("NoUpdateAfterCOMMAND"))){
    %>
      <html>
        <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
        <body>
          <script language="Javascript">
            parent.eFapsCommonRefresh();
          </script>
        </body>
      </html>  
    <%
    }               
  }
%>
