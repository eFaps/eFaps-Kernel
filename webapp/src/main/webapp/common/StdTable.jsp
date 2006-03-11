<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<%@page import="java.util.List"%>
<%@page import="java.util.ListIterator"%>
<%@page import="org.efaps.admin.datamodel.Attribute"%>
<%@page import="org.efaps.admin.datamodel.AttributeTypeInterface"%>
<%@page import="org.efaps.admin.ui.Table"%>
<%@page import="org.efaps.admin.ui.Field"%>
<%@page import="org.efaps.db.Instance"%>
<%@page import="org.efaps.util.StringUtil"%>
<%@include file = "../common/StdTop.inc"%>
<jsp:useBean id="uiObject" class="java.util.HashMap" scope="request"/>

<%
int sortKey = -1;

String nodeId = (String)uiObject.get("nodeId");
nodeId = (nodeId==null || nodeId.equals("null") ? null : nodeId);
System.out.println("nodeId="+nodeId);
response.setLocale(request.getLocale());
System.out.println("response.getLocale().getCountry()="+response.getLocale());
System.out.println("request.getLocale().getCountry()="+request.getLocale());
%>

<%@page import="java.util.ListIterator"%>

  <table width="100%">

    <tr>
<c:if test="${empty param.mode or param.mode ne 'print'}">
     <c:if test="${param.rowSelectable eq 'true'}">
        <th class="eFapsTableHeader" width="1%">
          <input type="checkbox" onClick="eFapsTableSelectDeselectAll(document,'selectedRow',this.checked)"/>
        </th>
      </c:if>
</c:if>
<fmt:bundle basename="StringResource">
      <c:forEach items="${uiObject['table']['fields']}" var="one">
        <%-- /** show only fields if they are not hidden! **/ --%>
        <c:if test="${one.hidden eq false}">
          <th class="eFapsTableHeader">
            <%-- /** make href for the sort **/ --%>
            <c:choose>
              <%-- /** sort dir is down if key is equal current field and direction is up **/ --%>
              <c:when test="${param.sortKey eq one.name and param.sortDir ne '-'}">
                <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.name}"/>','-')">
              </c:when>
              <%-- /** otherwise sort dir is down **/ --%>
              <c:otherwise>
                <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.name}"/>','')">
              </c:otherwise>
            </c:choose>
            <%-- /** show only the standard attribute label if no extra label is defined **/--%>
            <c:choose>
              <%-- /** no extra label is defined -> show attribute label **/ --%>
              <c:when test="${empty one.label}">
                <fmt:message key="${one.attribute}.Label"/>
              </c:when>
              <%-- /** otherwise show the field label **/ --%>
              <c:otherwise>
                <fmt:message key="${one.label}" var="label"/>
                <c:set var="keyWithQuestionMark" value="???${one.label}???"/>
                <%-- /** test if label exists in the property file **/ --%>
                <c:choose>
                  <%-- /** label does not exists in the property file, this means show the original label **/ --%>
                  <c:when test="${label eq keyWithQuestionMark}">
                    <c:out value="${one.label}"/>
                  </c:when>
                  <%-- /** label exists in the property file, this means show the internationalised label **/ --%>
                  <c:otherwise>
                    <c:out value="${label}"/>
                  </c:otherwise>
                </c:choose>
                <c:remove var="label"/>
                <c:remove var="keyWithQuestionMark"/>
              </c:otherwise>
            </c:choose>
            <%-- /** show the sort image **/ --%>
            <c:choose>
              <%-- /** show sort up image **/ --%>
              <c:when test="${param.sortKey eq one.name and param.sortDir eq '-'}">
                <img src="../images/eFapsTableButtonSortDown.gif"/>
              </c:when>
              <%-- /** show sort down image **/ --%>
              <c:when test="${param.sortKey eq one.name and param.sortDir ne '-'}">
                <img src="../images/eFapsTableButtonSortUp.gif"/>
              </c:when>
              <%-- /** otherwise show a dummy image **/ --%>
              <c:otherwise>
                <img src="../images/eFapsTableButtonSortDummy.gif"/>
              </c:otherwise>
            </c:choose>
            </a>
          </th>
        </c:if>
      </c:forEach>
</fmt:bundle>
    </tr>

<%--
<--c:set var="odd" value="true"/>
  <--c:forEach items="${uiObject['values']}" var="row">
    <--c:choose>
      <--c:when test="${odd eq 'true'}">
        <--tr class="eFapsTableRowOdd">
        <--c:set var="odd" value="false"/>
      <--/c:when>
      <--c:otherwise>
        <--tr class="eFapsTableRowEven">
        <--c:set var="odd" value="true"/>
      <--/c:otherwise>
    <--/c:choose>
    <--c:forEach items="${row}" var="column">
      <--td><--c:out value="${column}"/><--/td>
<--%
%>
    <--/c:forEach>
    <--/tr>
  <--/c:forEach>
--%>

<c:set var="odd" value="true"/>
<%
List tableValues = (List)uiObject.get("values");
Table table = (Table)uiObject.get("table");
ListIterator iter;
String sortDirStr = request.getParameter("sortDir");
boolean sortDown = (sortDirStr!=null && sortDirStr.equals("-") ? true: false);
if (sortDown)  {
  iter = tableValues.listIterator(tableValues.size());
} else  {
  iter = tableValues.listIterator();
}
        while (true)  {
          List row;
if (sortDown && iter.hasPrevious())  {
  row = (List)iter.previous();
} else if (!sortDown && iter.hasNext()) {
  row = (List)iter.next();
} else  {
  break;
}
Instance instance = (Instance)row.get(0);
%>
<c:choose>
  <c:when test="${odd eq 'true'}">
    <tr class="eFapsTableRowOdd">
    <c:set var="odd" value="false"/>
  </c:when>
  <c:otherwise>
    <tr class="eFapsTableRowEven">
    <c:set var="odd" value="true"/>
  </c:otherwise>
</c:choose>
<c:if test="${empty param.mode or param.mode ne 'print'}">
  <c:if test="${param.rowSelectable eq 'true'}">
    <td><input type="checkbox" name="selectedRow" value="<%=instance.getOid()%>"/></td>
  </c:if>
</c:if>
<%
          for (int j=0, k=1; j<table.getFields().size(); j++, k++)  {
            Field field = (Field)table.getFields().get(j);
if (!field.isHidden())  {
            AttributeTypeInterface attrValue = (AttributeTypeInterface)row.get(k);
            %><td><%
            if (field.getReference()!=null)  {
              String href = field.getReference();
href = StringUtil.replace(href, "${SERVLETURL}", "/eFaps/request");
href = StringUtil.replace(href, "${COMMONURL}", "/eFaps/common");
              if (href.indexOf("?")>=0)  {
                href += "&";
              } else  {
                href += "?";
              }
              href += "oid=" + instance.getOid();
              if (nodeId!=null)  {
                href += "&nodeId=" + nodeId;
              }
              context.print("<a href=\"javascript:eFapsCommonOpenUrl('").print(href).print("','Content')\">");
              %><%=attrValue.getViewHtml(response.getLocale())%></a><%
            } else  {
try  {
              %><%=attrValue.getViewHtml(response.getLocale())%><%
} catch (Exception e)  {
e.printStackTrace();
}
            }
            %></td><%
}
          }
          %></tr><%
        }
%>

  </table>
<%@include file = "../common/StdBottom.inc"%>
