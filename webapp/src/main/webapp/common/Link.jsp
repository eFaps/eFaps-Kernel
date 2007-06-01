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
<%@page import="org.efaps.admin.ui.Command"%>
<%@page import="org.efaps.admin.ui.CommandAbstract"%>
<%@page import="org.efaps.admin.ui.Menu"%>
<%@page import="org.efaps.db.Insert"%>
<%@page import="org.efaps.db.Instance"%>
<%@include file = "../common/StdTop.inc"%>
<%
  CommandAbstract command = Command.get(getParameter("command"));
  if (command==null)  {
    command = Menu.get(getParameter("command"));
  }
  if (command==null)  {
  } else if (command.getTargetForm()!=null)  {
%>
    <jsp:include page = "../common/Form.jsp">
      <jsp:param name="command" value="<%=getParameter("command")%>"/>
      <jsp:param name="oid" value="<%=getParameter("oid")%>"/>
    </jsp:include>
<%
  } else if (command.getTargetTable()!=null)  {
%>
    <jsp:include page = "../common/Table.jsp">
      <jsp:param name="command" value="<%=getParameter("command")%>"/>
      <jsp:param name="oid" value="<%=getParameter("oid")%>"/>
      <jsp:param name="nodeId" value="<%=getParameter("nodeId")%>"/>
    </jsp:include>
<%
  } else if (command.getTargetSearch()!=null)  {
%>
    <jsp:include page = "../common/Search.jsp">
      <jsp:param name="search" value="<%=command.getTargetSearch().getName()%>"/>
    </jsp:include>
<%
  }
  if (command.hasTrigger()){
    command.executeTrigger();
  
  }
%>
