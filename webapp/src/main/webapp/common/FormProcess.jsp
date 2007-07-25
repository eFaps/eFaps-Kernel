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
<%@page import="org.efaps.admin.event.EventType"%>
<%@page import="org.efaps.admin.event.Parameter.ParameterValues"%>
<%@page import="org.efaps.db.Context"%>
<%@page import="org.efaps.db.Instance"%>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean"
	scope="session" />

<%-- /** constructor for the form bean with some initialise code **/ --%>
<%
  org.efaps.beans.FormBean uiObject =
          (org.efaps.beans.FormBean) request.getAttribute("uiObject");
  if (uiObject == null) {
    uiObject = cache.getFormBean(Context.getThreadContext()
        .getParameter("command"));
    request.setAttribute("uiObject", uiObject);

    uiObject.setUkTitle("");

    Context context = Context.getThreadContext();
    String cmdName = context.getParameter("command");
    if ((cmdName == null) || (cmdName.length() == 0)
        || ("undefined".equals(cmdName))) {
      cmdName = context.getParameter("eFapsOriginalCommand");
    }
    CommandAbstract command = Command.get(cmdName);
    if (command == null) {
      command = Menu.get(cmdName);
    }
    
    if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
      command.executeEvents(EventType.UI_COMMAND_EXECUTE,
              ParameterValues.INSTANCE, new Instance(context
                  .getParameter("oid")));
    }
  }
%>

<html>
<script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
<body>
<script language="Javascript">
      if (top.opener.name=='TreeNavigation')  {
        <% if(uiObject.getInstance()!=null){ %>
        eFapsCommonOpenUrl('MenuTree.jsp?oid=<%=uiObject.getInstance().getOid()%>', 'Content');
      <%}%>
      } else  {
        top.opener.eFapsCommonRefresh();
      }
      eFapsCommonCloseWindow();
    </script>
</body>
</html>

