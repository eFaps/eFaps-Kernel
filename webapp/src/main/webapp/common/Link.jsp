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
<%@page import="org.efaps.admin.ui.Search"%>
<%@page import="org.efaps.admin.event.EventType"%>
<%@page import="org.efaps.admin.event.Parameter"%>
<%@page import="org.efaps.admin.event.Parameter.ParameterValues"%>
<%@page import="org.efaps.db.Context"%>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<%
  Context context = Context.getThreadContext();

  String cmdName = context.getParameter("command");
  if ((cmdName == null) || (cmdName.length() == 0) || ("undefined".equals(cmdName))) {
    cmdName = context.getParameter("eFapsOriginalCommand");
  }
  CommandAbstract command = Command.get(cmdName);
  if (command == null)  {
    command = Menu.get(cmdName);
  }


  // if a target search is defined, use other command!
  if (command.getTargetSearch() != null)  {
    Search search = command.getTargetSearch();

    context.getParameters().put("eFapsCallingCommand", new String[]{command.getName()});
    context.getParameters().put("search", new String[]{search.getName()});
    context.getParameters().put("command", new String[]{search.getDefaultCommand().getName()});
    command = search.getDefaultCommand();
  }

  if (command==null)  {
System.out.println("command == NULL!!!!!!!");
  } else if (command.getTargetMode() == CommandAbstract.TARGET_MODE_SEARCH)  {
    if ("true".equalsIgnoreCase(context.getParameter("eFapsShowSearchResult")))  {
      %><%@include file="Table.inc"%><%
    } else  {
      %><%@include file="Form.inc"%><%
    }
  } else if (command.getTargetForm() != null)  {
    %><%@include file="Form.inc"%><%
  } else if (command.getTargetTable() != null)  {
    %><%@include file="Table.inc"%><%
  } else if (command.hasEvents()){
    String[] oids = (String[])request.getParameterValues("selectedRow");
    
    if(oids!=null){
      command.executeEvents(EventType.COMMAND, ParameterValues.OTHERS, oids);
    }else {
      command.executeEvents(EventType.COMMAND);
    }
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
