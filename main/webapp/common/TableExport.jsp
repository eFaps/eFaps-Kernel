<%@page errorPage="Exception.jsp"%><%
%><%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%><%
%><%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %><%
%><%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%><%
%><%
%><%@page import="java.util.List"%><%
%><%@page import="java.util.ListIterator"%><%
%><%@page import="java.util.Map"%><%
%><%@page import="org.efaps.admin.datamodel.AttributeTypeInterface"%><%
%><%@page import="org.efaps.admin.ui.Command"%><%
%><%@page import="org.efaps.admin.ui.Field"%><%
%><%@page import="org.efaps.admin.ui.Table"%><%
%><%
%><%@page contentType="text/csv"%><%
%><%
%><jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/><%

  String cacheKey =   request.getParameter("cacheKey");

  org.efaps.beans.AbstractBean uiObject = (org.efaps.beans.AbstractBean)request.getAttribute("uiObject");
  if (uiObject==null)  {
    uiObject = cache.getTableBean(cacheKey, request.getParameter("command"));
    request.setAttribute("uiObject", uiObject);
  }
  uiObject.setResponse(response);


System.out.println("uiObject="+uiObject);
  response.addHeader("Content-Disposition", "inline; filename=\""+((org.efaps.beans.TableBean)uiObject).getCommand().getName()+"."+cacheKey+".csv\"");

%><fmt:bundle basename="StringResource"><%
  %><c:forEach items="${uiObject['table']['fields']}" var="one"><%
    %><%-- /** show only fields if they are not hidden! **/ --%><%
    %><c:if test="${one.hidden eq false}"><%
      %><%-- /** show only the standard attribute label if no extra label is defined **/--%><%
      %><c:choose><%-- /** no extra label is defined -> show attribute label **/ --%><c:when test="${empty one.label}"><%
            %>"<fmt:message key="${one.attribute}.Label"/>"<%
          %></c:when><%-- /** otherwise show the field label **/ --%><c:otherwise><%
            %><fmt:message key="${one.label}" var="label"/><%
            %><c:set var="keyWithQuestionMark" value="???${one.label}???"/><%
            %><%-- /** test if label exists in the property file **/ --%><%
            %><c:choose><%-- /** label does not exists in the property file, this means show the original label **/ --%><c:when test="${label eq keyWithQuestionMark}"><%
                %>"<c:out value="${one.label}"/>"<%
              %></c:when><%-- /** label exists in the property file, this means show the internationalised label **/ --%><c:otherwise><%
                %>"<c:out value="${label}"/>"<%
              %></c:otherwise></c:choose><%
            %><c:remove var="label"/><%
            %><c:remove var="keyWithQuestionMark"/><%
          %></c:otherwise></c:choose><%
      %>;<%
    %></c:if><%
  %></c:forEach><%
%></fmt:bundle><%
  ListIterator iter = ((org.efaps.beans.TableBean)uiObject).getValues().listIterator();
  while (iter.hasNext())  {
    out.write("\n");
    org.efaps.beans.TableBean.Row row = (org.efaps.beans.TableBean.Row)iter.next();
    for (int j=0; j<((org.efaps.beans.TableBean)uiObject).getTable().getFields().size(); j++)  {
      Field field = (Field)((org.efaps.beans.TableBean)uiObject).getTable().getFields().get(j);
      if (!field.isHidden())  {
        org.efaps.beans.TableBean.Value value = (org.efaps.beans.TableBean.Value)row.getValues().get(j);
        out.write("\"");
        out.print(value.getViewHtml());
        out.write("\";");
      }
    }
  }
  %>
<%
%>