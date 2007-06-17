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

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<%
  CommandAbstract command = Command.get(request.getParameter("command"));
  if (command==null)  {
    command = Menu.get(request.getParameter("command"));
  }
  if (command==null)  {
  } else if (command.getTargetForm()!=null)  {
    %>
      <%@include file="Form.inc"%>
<%--
      <jsp:forward page = "../common/Form.jsp">
        <jsp:param name="command" value="<%=request.getParameter("command")%>"/>
        <jsp:param name="oid" value="<%=request.getParameter("oid")%>"/>
        <jsp:param name="nodeId" value="<%=request.getParameter("nodeId")%>"/>
      </jsp:forward>
--%>
    <%
  } else if (command.getTargetTable()!=null)  {
    %>
      <%@include file="Table.inc"%>
<%--
      <jsp:forward page = "../common/Table.jsp">
        <jsp:param name="command" value="<%=request.getParameter("command")%>"/>
        <jsp:param name="oid" value="<%=request.getParameter("oid")%>"/>
        <jsp:param name="nodeId" value="<%=request.getParameter("nodeId")%>"/>
      </jsp:forward>
--%>
    <%
  } else if (command.getTargetSearch()!=null)  {
    %>
      <jsp:include page = "../common/Search.jsp">
        <jsp:param name="search" value="<%=command.getTargetSearch().getName()%>"/>
      </jsp:include>
    <%
  } else if (command.hasTrigger()){
    command.executeTrigger();
  }
%>
