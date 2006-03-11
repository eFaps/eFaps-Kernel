<%@page errorPage="Exception.jsp"%>
<%@page import="org.efaps.admin.ui.Command"%>
<%@page import="org.efaps.admin.ui.CommandAbstract"%>
<%@page import="org.efaps.admin.ui.Menu"%>
<%@page import="org.efaps.db.Insert"%>
<%@page import="org.efaps.db.Instance"%>
<%@include file = "../common/StdTop.inc"%>
<%
  CommandAbstract command = Command.get(context, getParameter("command"));
  if (command==null)  {
    command = Menu.get(context, getParameter("command"));
  }
System.out.println("Link.command="+command);
System.out.println("Link.command.getTargetForm()="+command.getTargetForm());
System.out.println("Link.command.getTargetTable()="+command.getTargetTable());
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
System.out.println("=command.getTargetSearch().getName()="+command.getTargetSearch().getName());
%>
    <jsp:include page = "../common/Search.jsp">
      <jsp:param name="search" value="<%=command.getTargetSearch().getName()%>"/>
    </jsp:include>
<%
  }
%>
<%@include file = "../common/StdBottom.inc"%>