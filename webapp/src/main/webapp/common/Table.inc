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

<%@page import="org.efaps.admin.datamodel.ui.FieldValue"%>
<%@page import="org.efaps.beans.TableBean"%>

<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="h"    uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="f"    uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="t"    uri="http://myfaces.apache.org/tomahawk"%>

<%
  String cacheKey  = context.getParameter("cacheKey");

  if (cacheKey==null)  {
    cacheKey = Long.toString(System.currentTimeMillis());
System.out.println("create new cacheKey = "+cacheKey);
  }

  TableBean uiObject = (TableBean) request.getAttribute("uiObject");
  if (uiObject==null)  {
    uiObject = (TableBean) cache.getTableBean(cacheKey);
    request.setAttribute("uiObject", uiObject);
  }

  // initialise the user interface object (if needed)
  if (uiObject.isInitialised())  {
    uiObject.setSortKey(context.getParameter("sortKey"));
    uiObject.setSortDirection(context.getParameter("sortDir"));
    uiObject.sort();
  } else  {
System.out.println(".............................START:"+new java.util.Date());
    uiObject.execute();
System.out.println("............................MIDDLE:"+new java.util.Date());
  }
%>

<html>


  <head>

    <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css"/>
    
    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

<script type="text/javascript">
	var cleanUp=true;
    
    function eFapsTableProcessEnd() {
      eFapsPositionTable();
      window.onresize = function() { eFapsPositionTable();}
    }

    /**
     */
    function eFapsPositionTable() {
      <c:choose>
        <c:when test="${uiObject.searchMode}">
          var div = document.getElementById("eFapsTableContentWithFooter");
        </c:when>
        <c:otherwise>
          var div = document.getElementById("eFapsTableContent");
        </c:otherwise>
      </c:choose>
      var headerTable = document.getElementById("eFapsTableHeader");
      var bodyTable = document.getElementById("eFapsTableBody");

      var row = bodyTable.rows[0];
      var headers = headerTable.getElementsByTagName("div");

      if (row)  {
        for (var i=0; i<row.cells.length; i++)  {
          if (row.cells[i].offsetWidth)  {
            headers[i].style.width = row.cells[i].offsetWidth + "px";
            headers[i].style.left = (row.cells[i].offsetLeft - div.scrollLeft + 5) + "px";
          }
        }
      } else  {
        var pos = 4;
        for (var i=0; i<headers.length; i++)  {
          headers[i].style.left = pos + "px";
          pos = pos + 2 + headers[i].offsetWidth;
        }
      }
    }
 
  function eFapsSortTable(_sortKey, _sortDir)  {
     cleanUp=false;
     var url = location.href;
     var index = url.indexOf('?');
     if (index>=0)  {
       url = url.substring(0, index);
     }
     url = url + "?command=<c:out value="${uiObject.command.name}"/>&cacheKey=<%=cacheKey%>&sortKey=" + _sortKey;
     if (location.href.indexOf('mode=print')>=0)  {
       url = url + "&mode=print";
     }
     if (_sortDir)  {
       url += "&sortDir=" + _sortDir;
     }
     <c:if test="${uiObject.instance ne null}">
       url += "&oid=<c:out value="${uiObject.instance.oid}"/>";
     </c:if>
     
     location.href = url;
   } 
   
  function geteFapsCleanUpSession(){
    if(cleanUp){
      eFapsCleanUpSession('<%=cacheKey%>')
    }
  } 
    
</script> 

  </head>  

  <body onLoad="eFapsTableProcessEnd()" onUnLoad="geteFapsCleanUpSession()">
  
<c:choose>
  <c:when test="${uiObject.searchMode}">
    <div id="eFapsTableWithFooter">
  </c:when>
  <c:otherwise>
    <div id="eFapsTable">
  </c:otherwise>
</c:choose>




    <%-- /** filter bar **/ --%>
<%--
    <div class="eFapsFrameFilter">
      <form>
        <select name="eFapsFrameFilter" size="1" onChange="eFapsGetFrameFilter().execute(this.options[this.selectedIndex].value);" style="display:none">
        </select>
      </form>
    </div>
--%>

<f:view>
  <h:panelGrid id="eFapsFrameTitle">
    <h:outputText value="#{titleBean.title}"/>
  </h:panelGrid>

  <%-- menu bar --%>
  <%@include file="ToolBarMenu.inc"%>


    <%-- /*******************************************************************/ --%>
    <%-- /** Table Header                                                  **/ --%>
    <%-- /*******************************************************************/ --%>

    <div id="eFapsTableHeader">
      <%-- /** show select checkbox only if not mode print and show check boxes is defined **/ --%>
      <c:if test="${(empty param.mode or param.mode ne 'print') and uiObject.showCheckBoxes}">
        <div width="1%">
          <input type="checkbox" onClick="eFapsTableSelectDeselectAll(document,'selectedRow',this.checked)"/>
        </div>
      </c:if>

      <%-- /** show table header labels **/ --%>
      <c:forEach items="${uiObject.fieldDefs}" var="one">
        <%-- /** show only fields if they are not hidden! **/ --%>
        <c:if test="${one.field.hidden eq false}">
          <div title="<c:out value="${one.label}"/>" 
            <%-- /** make href for the sort **/ --%>
            <c:if test="${one.field.sortAble eq true}">
              <c:choose>
                <%-- /** sort dir is down if key is equal current field and direction is up **/ --%>
                <c:when test="${uiObject.sortKey eq one.field.name and uiObject.sortDirection ne '-' }">
                  onclick="eFapsSortTable('<c:out value="${one.field.name}"/>','-')"
                </c:when>
                <%-- /** otherwise sort dir is down **/ --%>
                <c:otherwise>
                  onclick="eFapsSortTable('<c:out value="${one.field.name}"/>','')"
                </c:otherwise>
              </c:choose>
             style="cursor:pointer"
             </c:if>
             >
            <%-- /** show label **/ --%>
            <c:out value="${one.label}"/>
            <%-- /** show the sort image **/ --%>
            <c:choose>
              <%-- /** show sort up image **/ --%>
              <c:when test="${uiObject.sortKey eq one.field.name and uiObject.sortDirection eq '-'}">
                <img src="../images/eFapsTableButtonSortDown.gif"/>
              </c:when>
              <%-- /** show sort down image **/ --%>
              <c:when test="${uiObject.sortKey eq one.field.name and uiObject.sortDirection ne '-'}">
                <img src="../images/eFapsTableButtonSortUp.gif"/>
              </c:when>
              <%-- /** otherwise show a dummy image **/ --%>
              <c:otherwise>
                <img src="../images/eFapsTableButtonSortDummy.gif"/>
              </c:otherwise>
            </c:choose>
          </div>
        </c:if>
      </c:forEach>
    </div>

    <%-- /*******************************************************************/ --%>
    <%-- /** Table Values                                                  **/ --%>
    <%-- /*******************************************************************/ --%>

<c:choose>
  <c:when test="${uiObject.searchMode}">
    <div id="eFapsTableContentWithFooter" onScroll="eFapsPositionTable()">
  </c:when>
  <c:otherwise>
    <div id="eFapsTableContent" onScroll="eFapsPositionTable()">
  </c:otherwise>
</c:choose>



    <form id="eFapsSubmitForm" name="eFapsSubmitForm" method="post" target="" >
      <input type="hidden" name="command" value=""/>
      <input type="hidden" name="tableCommand" value="<c:out value="${uiObject.command.name}"/>"/>
      <input type="hidden" name="eFapsTableFilter" value=""/>
      <input type="hidden" name="eFapsCacheKey" value="<%=cacheKey%>"/>

      <%-- /** show the hidden values for this user interface **/ --%>
      <c:forEach items="${uiObject.hiddenValues}" var="hiddenValue">
        <input type="hidden" name="<c:out value="${hiddenValue.name}"/>" value="<c:out value="${hiddenValue.value}"/>"/>
      </c:forEach>


    <table id="eFapsTableBody">




<%
// store print mode
String mode = context.getParameter("mode");
boolean printMode = (mode!=null && mode.equals("print") ? true : false);


// because of performance reason, the following is a pure java implementation
boolean odd = true;
for (java.util.Iterator rowIter = uiObject.getValues().iterator(); rowIter.hasNext(); odd=!odd)  {

  org.efaps.beans.TableBean.Row row = (org.efaps.beans.TableBean.Row)rowIter.next();

  // show the tr tag depending on the odd attribute
  if (odd)  {
    out.print("<tr class=\"eFapsTableRowOdd\">");
  } else  {
    out.print("<tr class=\"eFapsTableRowEven\">");
  }

  // show select checkbox only if not mode print and show check boxes ifdefined
  //if test="${(empty param.mode or param.mode ne 'print') and uiObject.showCheckBoxes}">
  if (!printMode && uiObject.isShowCheckBoxes())  {
    out.print("<td width=\"1%\"><input type=\"checkbox\" name=\"selectedRow\" value=\"");
    out.print(row.getOids());
    out.print("\"/></td>");
  }

  for (FieldValue col : row.getValues())  {

    if (!col.getFieldDef().getField().isHidden())  {
      out.print("<td>");

      // show href
      if (col.getFieldDef().getField().getReference()!=null)  {
        String href = col.getFieldDef().getField().getReference();
        String target = "Content";
        href += "&oid=" + col.getInstance().getOid();
        if (col.getFieldDef().getField().isTargetPopup())  {
          target = "Popup";
        } else if (uiObject.getNodeId()!=null) {
          href += "&nodeId=" + uiObject.getNodeId();
        }
        out.print("<a href=\"javascript:eFapsCommonOpenUrl('");
        out.print(href);
        out.print("', '");
        out.print(target);
        out.print("')\">");
      }

      // image
      if (col.getFieldDef().getField().getIcon()!=null)  {
        out.print("<img src=\"");
        out.print(col.getFieldDef().getField().getIcon());
        out.print("\"/>");
      }

      // show type icon?
      if (col.getFieldDef().getField().isShowTypeIcon())  {
        String imgUrl = col.getInstance().getType().getIcon();
        if (imgUrl!=null)  {
          out.print("<img src=\"");
          out.print(imgUrl);
          out.print("\"/>");
          out.print("&nbsp;");
        };
      }

      // show column value
      if (col.getValue()!=null)  {
        if (uiObject.isCreateMode() && col.getFieldDef().getField().isEditable())  {
          out.print(col.getCreateHtml());
        } else if (uiObject.isEditMode() && col.getFieldDef().getField().isEditable())  {
          out.print(col.getEditHtml());
        } else  {
          out.print(col.getViewHtml());
        }
      }

      // end href
      if (col.getFieldDef().getField().getReference()!=null)  {
        out.print("</a>");
      }

      out.print("</td>");
    }
  }

  out.print("</tr>");
}


%>
      </table>

    </form>
</div>

    <%-- footer frame (if needed) --%>
    <c:if test="${uiObject.searchMode}">
      <div id="eFapsFooter">
        <h:form>
          <h:commandLink id="Close" onclick="eFapsCommonCloseWindow();return false;" title="Create">
            <h:graphicImage value="../images/eFapsButtonCancel.gif"/>
            <h:outputText value="Close"/>
          </h:commandLink>
        </h:form>
      </div>
    </c:if>

</f:view>
<%
System.out.println("...............................END:"+new java.util.Date());
%>

    <%-- /** hidden frame **/ --%>
    <iframe name="eFapsFrameHidden" id="eFapsFrameHidden" class="eFapsFrameHidden" src="Blank.jsp">
    </iframe>

  </body>
    

</html>