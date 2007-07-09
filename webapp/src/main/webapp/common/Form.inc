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

<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="h"    uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="f"    uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="t"    uri="http://myfaces.apache.org/tomahawk"%>

<%-- /** constructor for the form bean with some initialise code **/ --%>
<%
  org.efaps.beans.FormBean uiObject = (org.efaps.beans.FormBean)request.getAttribute("uiObject");
  if (uiObject==null)  {
    uiObject = cache.getFormBean(request.getParameter("command"));
    request.setAttribute("uiObject", uiObject);

    %><%-- /** execute method **/ --%><%
    

uiObject.setUkTitle("");

    uiObject.execute();
  }
%>
<jsp:setProperty name="uiObject" property="nodeId" param="nodeId"/>

  <%
    String timeStamp  = request.getParameter("timeStamp");

    if (timeStamp==null)  {
      timeStamp = Long.toString(System.currentTimeMillis());
  System.out.println("create new timestamp = "+timeStamp);
    }

//    String urlMain = "FormMain.jsp?"+request.getQueryString()+"&timeStamp="+timeStamp;
  %>

  <c:choose>
    <c:when test="${uiObject.viewMode}">
      <c:set var="bodyClass" value="eFapsFrameView"/>
    </c:when>
    <c:otherwise>
      <c:set var="bodyClass" value="eFapsFrameEdit"/>
    </c:otherwise>
  </c:choose>


    <%-- /** normal form **/ --%>
      <html>
        <head>

<script type="text/javascript">

      function eFapsFormProcessEnd() {
        <%-- /** footer action menu:
          - in mode create show button "create"
          - in mode edit show button "update"
          - in mode search show button "show"
          - show button cancel only if target is popup **/ --%>
<%--
        <c:if test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
          var footerAction = eFapsGetFrameFooterAction();
          footerAction.clean();
          footerAction.setForm(document.forms[1]);
          footerAction.add(
                <c:choose>
                  <c:when test="${uiObject.createMode}">"Standard.Button.Create",</c:when>
                  <c:when test="${uiObject.editMode}">"Standard.Button.Edit",</c:when>
                  <c:when test="${uiObject.searchMode}">"Standard.Button.Search",</c:when>
                </c:choose>
                <c:choose>
                  <c:when test="${param.urlProcess ne null}">
                    "<c:out value="${param.urlProcess}"/>",
                  </c:when>
                  <c:otherwise>
                    "../common/FormProcess.jsp",
                  </c:otherwise>
                </c:choose>
                true,
                false,
                false,
                '',
                '',
                '',
                '',
                <c:choose>
                  <c:when test="${uiObject.searchMode}">'../images/eFapsButtonNext.gif'</c:when>
                  <c:otherwise>'../images/eFapsButtonDone.gif'</c:otherwise>
                </c:choose>
          );
        </c:if>
        <c:if test="${uiObject.popup}">
          footerAction.add(
                "Standard.Button.Cancel",
                "javascript:eFapsCommonCloseWindow()",
                false,
                false,
                false,
                '',
                '',
                '',
                '',
                '../images/eFapsButtonCancel.gif');
        </c:if>
--%>

      }

/**
 * The current form is submitted to the given href.
 *
 * @see #execute
 */
function eFapsSubmit()  {
  var div = document.getElementById("eFapsFormWithFooter");
  var form = div.getElementsByTagName("form")[1];
  
//  eFapsProcessStart();
  form.submit();


//  if (!this.submitLock)
//  {
/*    var errorMsg = checkFormForRequirements(this.getForm());
    if (errorMsg == null || errorMsg.length == 0)
    {
      this.submitLock = true;
      eFapsProcessStart();
      this.getForm().action=this.href;
      if (this.getForm().command)  {
        this.getForm().command.value=this.name;
      }
      this.getForm().submit();
    }
    else
    {
      alert(errorMsg);
    }
*/

//  }
//  else
//  {
//	  alert("Please click only once on '"+this.textHtmlElement.nodeValue+"'");
//  }
}

</script>
        </head>
        <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css"/>
        <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

        <script type="text/javascript">
          var eFapsTreeImagePath = '../images/';
        </script>
        <script type="text/javascript" src="../javascripts/eFapsTree.js"></script>

        <body onLoad="eFapsFormProcessEnd()" onUnLoad="eFapsCleanUpSession('<%=timeStamp%>')" class="<c:out value="${bodyClass}"/>">

<c:choose>
  <c:when test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
    <div id="eFapsFormWithFooter">
  </c:when>
  <c:otherwise>
    <div id="eFapsForm">
  </c:otherwise>
</c:choose>


          <%-- /** menu bar **/ --%>

<f:view>

  <h:panelGrid id="eFapsFrameTitle">
    <h:outputText value="#{titleBean.title}"/>
  </h:panelGrid>
  <%@include file="ToolBarMenu.inc"%>

<c:choose>
  <c:when test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
    <div id="eFapsFormContentWithFooter">
  </c:when>
  <c:otherwise>
    <div id="eFapsFormContent">
  </c:otherwise>
</c:choose>



      <form method="post" enctype="multipart/form-data" target="<c:out value="${targetProcess}"/>" action="../common/FormProcess.jsp">
        <input type="hidden" name="command" value=""/>

        <%-- /** show the hidden values for this user interface **/ --%>
        <c:forEach items="${uiObject.hiddenValues}" var="hiddenValue">
          <input type="hidden" name="<c:out value="${hiddenValue.name}"/>" value="<c:out value="${hiddenValue.value}"/>"/>
        </c:forEach>

        <table>
          <c:if test="${uiObject.values ne null}">
            <c:if test="${uiObject.values[0] ne null}">
              <input type="hidden" name="oid" value="<c:out value="${uiObject.values[0].instance.oid}"/>"/>
            </c:if>

            <%-- /** group count and row group count is default equal 1,
                     because only one field should be shown per row **/ --%>
            <c:set var="groupCount" value="1"/>
            <c:set var="rowGroupCount" value="1"/>

            <c:forEach items="${uiObject.values}" var="value" begin="1">

              <c:choose>

                <%-- /** if the field is a group count field, store the value in the row group count variable,
                         (only if not create node or field is creatable) **/ --%>
                <c:when test="${value.fieldDef.field.groupCount > 0 && (uiObject.createMode ne true or value.fieldDef.field.creatable)  && (uiObject.searchMode ne true or value.fieldDef.field.searchable)}">
                  <c:set var="rowGroupCount" value="${value.fieldDef.field.groupCount}"/>
                </c:when>

                <%-- /* only if:
                        - in createmode field is createable
                        - in searchmode field is searchable
                        - otherwise show field */ --%>
                <c:when test="${(uiObject.createMode ne true or value.fieldDef.field.creatable) and (uiObject.searchMode ne true or value.fieldDef.field.searchable)}">

                  <%-- /** start start row tag if first column **/ --%>
                  <c:if test="${groupCount == 1}">
                      <tr>
                  </c:if>

                  <%-- /** use required style class only if defined and not in edit or create mode **/ --%>
                  <c:choose>
                    <c:when test="${value.fieldDef.field.required and (uiObject.editMode or uiObject.createMode)}">
                      <td class="eFapsFormLabelRequired">
                    </c:when>
                    <c:otherwise>
                      <td class="eFapsFormLabel">
                    </c:otherwise>
                  </c:choose>

                  <%-- /** show the label of the field **/ --%>
                  <c:out value="${value.fieldDef.label}"/></td>

                  <%-- /** make column with correct span **/ --%>
                  <td class="eFapsFormInputField" colspan="<c:out value="${2 * (uiObject.maxGroupCount - rowGroupCount) + 1}"/>">

                    <%-- /** show href START **/ --%>
                    <c:if test="${value.fieldDef.field.reference ne null and !(empty value.instance.oid) and uiObject.createMode ne true and uiObject.searchMode ne true}">
                      <c:set var="targetUrl" value="${value.fieldDef.field.reference}&oid=${value.instance.oid}"/>

                      <c:choose>
                        <c:when test="${value.fieldDef.field.targetPopup}">
                          <c:set var="targetWindow" value="Popup"/>
                        </c:when>
                        <c:otherwise>
                          <c:set var="targetWindow" value="Content"/>
                          <c:if test="${uiObject.nodeId ne null}">
                            <c:set var="targetUrl" value="${targetUrl}&nodeId=${uiObject.nodeId}"/>
                          </c:if>
                        </c:otherwise>
                      </c:choose>

                      <a href="javascript:eFapsCommonOpenUrl('<c:out value="${targetUrl}" escapeXml="false"/>','<c:out value="${targetWindow}" escapeXml="false"/>')">

                      <c:remove var="targetUrl"/>
                      <c:remove var="targetWindow"/>
                    </c:if>


                    <%-- /** show type icon if needed **/ --%>
                    <c:if test="${value.fieldDef.field.showTypeIcon and !(empty value.instance.oid) and uiObject.createMode ne true and uiObject.searchMode ne true}">
                      <img src="<c:out value="${value.instance.type.icon}"/>"/>&nbsp;
                    </c:if>

                    <%-- /** show value **/ --%>
                    <c:choose>
                      <c:when test="${uiObject.createMode and value.fieldDef.field.creatable}">
                        <c:out value="${value.createHtml}" escapeXml="false"/>
                      </c:when>
                      <c:when test="${uiObject.editMode and value.fieldDef.field.editable}">
                        <c:out value="${value.editHtml}" escapeXml="false"/>
                      </c:when>
                      <c:when test="${uiObject.searchMode and value.fieldDef.field.searchable}">
                        <c:out value="${value.searchHtml}" escapeXml="false"/>
                      </c:when>
                      <c:otherwise>
                        <c:out value="${value.viewHtml}" escapeXml="false"/>
                      </c:otherwise>
                    </c:choose>

                    <%-- /** show href END **/ --%>
                    <c:if test="${value.fieldDef.field.reference ne null and !(empty value.instance.oid) and uiObject.createMode ne true and uiObject.searchMode ne true}">
                      </a>
                    </c:if>

                  </td>

                  <%-- /** show the end row tag only if last table column **/ --%>
                  <c:if test="${groupCount == rowGroupCount}">
                    </tr>
                  </c:if>

                  <c:choose>
                    <%-- /** if group count less than row group count, increase by 1;
                             meaning the row has more than one field! **/ --%>
                    <c:when test="${groupCount < rowGroupCount}">
                      <c:set var="groupCount" value="${groupCount + 1}"/>
                    </c:when>
                    <%-- /** if last column, reset group counters **/ --%>
                    <c:when test="${groupCount == rowGroupCount}">
                      <c:set var="groupCount" value="1"/>
                      <c:set var="rowGroupCount" value="1"/>
                    </c:when>
                  </c:choose>
                </c:when>
              </c:choose>
            </c:forEach>

          </c:if>
        </table>
      </form>
</div>


          <c:if test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
            <div id="eFapsFooter">
              <h:form>
                <h:commandLink onclick="eFapsSubmit();return false;" title="Create">
                  <h:graphicImage value="../images/eFapsButtonDone.gif"/>
                  <h:outputText value="Edit"/>
                </h:commandLink>
                <h:commandLink onclick="eFapsCommonCloseWindow();return false;" title="Create">
                  <h:graphicImage value="../images/eFapsButtonCancel.gif"/>
                  <h:outputText value="Close"/>
                </h:commandLink>
              </h:form>
            </div>
          </c:if>

</f:view>
          <iframe name="eFapsFrameHidden" id="eFapsFrameHidden" class="eFapsFrameHidden" src="about:blank">
          </iframe>
</div>          
        </body>
      </html>


<%@include file = "../common/StdBottom.inc"%>
