<%--
  The JSP page gets the name of a image from parameter 'name'. This image is
  searched in eFaps. Then a redirect to the checkout servlet with this OID is
  made.

--%><%@page errorPage="Exception.jsp"%><%

%><%@include file = "../common/StdTop.inc"%><%

  String imgName = request.getParameter("name");

  org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();

  query.setQueryTypes(context, "Admin_UI_Image");
  query.addWhereExprEqValue(context, "Name", imgName);
  query.addSelect(context, "OID");
  query.execute(context);

  if (query.next())  {
    String oid = (String)query.get(context, "OID");
    response.sendRedirect("/eFaps/servlet/checkout?oid="+oid);
  }
  query.close();

%><%@include file = "../common/StdBottom.inc"%>