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
<%@page import="org.efaps.admin.dbproperty.DBProperties"%>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="t" uri="http://myfaces.apache.org/tomahawk"%>
<%@taglib prefix="s" uri="http://myfaces.apache.org/sandbox"%>

<%-- /** constructor for the form bean with some initialise code **/ --%>
<%
          org.efaps.beans.FormBean uiObject =
          (org.efaps.beans.FormBean) request.getAttribute("uiObject");
      if (uiObject == null) {
        uiObject = cache.getFormBean(null);
        request.setAttribute("uiObject", uiObject);
%><%-- /** execute method **/ --%>
<%
        uiObject.setUkTitle("");

        uiObject.execute();
      }
%>
<%
      String timeStamp = request.getParameter("timeStamp");

      if (timeStamp == null) {
        timeStamp = Long.toString(System.currentTimeMillis());
        System.out.println("create new timestamp = " + timeStamp);
      }

      //    String urlMain = "FormMain.jsp?"+request.getQueryString()+"&timeStamp="+timeStamp;
%>

<c:choose>
	<c:when test="${uiObject.viewMode}">
		<c:set var="bodyClass" value="eFapsFrameView" />
	</c:when>
	<c:otherwise>
		<c:set var="bodyClass" value="eFapsFrameEdit" />
	</c:otherwise>
</c:choose>


<%-- /** normal form **/ --%>
<html>
<f:subview id="Form.inc">
<head>
<t:dojoInitializer require="dojo.widget.Dialog"></t:dojoInitializer>
<c:if test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
 
  <script type="text/javascript">

  var submitLock = false;

/**
 * The current form is submitted to the given href.
 *
 * @see #execute
 */
  function eFapsSubmit()  {
    var form = document.getElementById("eFapsFormForm");
    <c:choose>
      <c:when test="${uiObject.searchMode}">
        form.action = "../common/Link.jsf";
      </c:when>
      <c:otherwise>
        form.action = "../common/FormProcess.jsp";
      </c:otherwise>
    </c:choose>
    
    if(!checkForRequirements()){
      warn("<%= DBProperties.getProperty("Common_UIForm_Mandotory.Message") %>");
    }
    if (!submitLock && checkForRequirements()){
      dlgProcess.show();
      form.submit();
      if(parent!=null){
        parent.dlgProcess.show();
        parent.location.href=parent.location.href;
        parent.location.href=parent.location.href;
        parent.dlgFrMdl.hide();
      }
    }
    return false;
  }


  function checkForRequirements(){
    var ret= true;
    var formTable = document.getElementById("eFapsFormTabel");
  
    var tableRows = formTable.getElementsByTagName("tr");
    var rowFields = new Array();
    var fieldInputs = new Array();

    for (i = tableRows.length-1; i >= 0; i--)  {
      rowFields = tableRows[i].getElementsByTagName("td");
    
      if (rowFields.length > 1)  {
        if (rowFields[0].className == "eFapsFormLabelRequiredForce"){
          rowFields[0].className="eFapsFormLabelRequired";
        }
      
        if (rowFields[0].className == "eFapsFormLabelRequired")  {
          //checke 'input' felder die einen text enthalten können
          fieldInputs = rowFields[1].getElementsByTagName("input");
        
          if (fieldInputs.length > 0 &&
              (fieldInputs[0].type == null || fieldInputs[0].type == "" || 
              fieldInputs[0].type == "text" || fieldInputs[0].type == "file" || 
              fieldInputs[0].type == "password" || fieldInputs[0].type == "hidden") && 
              (fieldInputs[0].value == null || fieldInputs[0].value == ""))
            {
          
            rowFields[0].className="eFapsFormLabelRequiredForce";
         
            ret=false;
            if (fieldInputs[0].type != "hidden"){
              fieldInputs[0].focus();
            }
          }
          // check fields of type 'textarea'
          fieldInputs = rowFields[1].getElementsByTagName("textarea");
          if (fieldInputs.length > 0 &&
            (fieldInputs[0].value == null || fieldInputs[0].value == "")){
            rowFields[0].className="eFapsFormLabelRequiredForce";
            ret=false;
            fieldInputs[0].focus();
          }
          // check fields of type 'select'
          fieldInputs = rowFields[1].getElementsByTagName("select");
          if (fieldInputs.length > 0)  {
            var selectionOptions = fieldInputs[0].options;
            var hasQualifiedSelection = false;
            for (var j = 0; j < selectionOptions.length && !hasQualifiedSelection; j++){
              if (selectionOptions[j].selected && selectionOptions[j].text != ""){
                hasQualifiedSelection = true;
              }
            }
            if (!hasQualifiedSelection){
              fieldInputs[0].focus();
            }
          }
        }
      }
    }
    return ret;
  }
 
</script>
  
</c:if>

<script type="text/javascript">
  var dlgWarn, dlgProcess, dlgError
  function init(e) {
	dlgWarn = dojo.widget.byId("dialogWarn");
	dlgProcess = dojo.widget.byId("dialogProcess");
	dlgError = dojo.widget.byId("dialogError");
	dlgFrMdl = dojo.widget.byId("dialogFrameModal");
	
  }
  
  dojo.addOnLoad(init);

  function frameModal(_height, _width){
    var fm = document.getElementById("eFapsFrameModal");
    fm.height=_height;
    fm.width=_width;
    dlgFrMdl.show()
  }

  function errorPage(_Id, _Msg, _Act){
    document.getElementById("dialogErrorId").firstChild.data = _Id;
    document.getElementById("dialogErrorMsg").firstChild.data = _Msg;
    document.getElementById("dialogErrorAct").firstChild.data = _Act;
    dlgError.show();
  }
  
  function warn(_text){
    document.getElementById("dialogWarnContent").firstChild.data = _text;
    dlgWarn.show();
  }
  
  
  </script>

<link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css" />
<script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

<script type="text/javascript">
          var eFapsTreeImagePath = '../images/';
        </script>
<script type="text/javascript" src="../javascripts/eFapsTree.js"></script>




</head>
<body onUnLoad="eFapsCleanUpSession('<%=timeStamp%>')"
	class="<c:out value="${bodyClass}"/>">

<c:choose>
	<c:when
		test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
		<div id="eFapsFormWithFooter">
	</c:when>
	<c:otherwise>
		<div id="eFapsForm">
	</c:otherwise>
</c:choose>

  <%@include file="Dialogs.inc"%>

	<h:panelGrid id="eFapsFrameTitle">
		<f:verbatim>
			<c:out value="${uiObject.title}" />
		</f:verbatim>
	</h:panelGrid>

	<%-- menu bar --%>
	 <%@include file="ToolBarMenu.inc"%>
	<c:choose>
		<c:when
			test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
			<div id="eFapsFormContentWithFooter">
		</c:when>
		<c:otherwise>
		 x
			<div id="eFapsFormContent">
		</c:otherwise>
	</c:choose>


  <form id="eFapsFormForm"
        method="post"
        enctype="multipart/form-data"
        target="<c:out value="${targetProcess}"/>"
        action=""
        onsubmit="return eFapsSubmit()">

    <%-- submit button is needed that the user could press enter --%>
    <input type="submit" style="display: none">
    
    <c:if test="${uiObject.searchMode}">
        <input type="hidden" name="eFapsShowSearchResult" value="true"/>
    </c:if>
          
    <%-- show the hidden values for this user interface --%>
    <c:forEach items="${uiObject.hiddenValues}" var="hiddenValue">
        <input type="hidden" name="<c:out value="${hiddenValue.name}"/>" value="<c:out value="${hiddenValue.value}"/>"/>
    </c:forEach>

	<table id="eFapsFormTabel">
		<c:if test="${uiObject.values ne null}">

			<%-- /** group count and row group count is default equal 1,
                     because only one field should be shown per row **/ --%>
			<c:set var="groupCount" value="1" />
			<c:set var="rowGroupCount" value="1" />

			<c:forEach items="${uiObject.values}" var="value" begin="1">

				<c:choose>

					<%-- /** if the field is a group count field, store the value in the row group count variable,
                         (only if not create node or field is creatable) **/ --%>
					<c:when
						test="${value.fieldDef.field.groupCount > 0 && (uiObject.createMode ne true or value.fieldDef.field.creatable)  && (uiObject.searchMode ne true or value.fieldDef.field.searchable)}">
						<c:set var="rowGroupCount"
							value="${value.fieldDef.field.groupCount}" />
					</c:when>

					<%-- /* only if:
                        - in createmode field is createable
                        - in searchmode field is searchable
                        - otherwise show field */ --%>
					<c:when
						test="${(uiObject.createMode ne true or value.fieldDef.field.creatable) and (uiObject.searchMode ne true or value.fieldDef.field.searchable)}">

						<%-- /** start start row tag if first column **/ --%>
						<c:if test="${groupCount == 1}">
							<tr>
						</c:if>

						<%-- /** use required style class only if defined and not in edit or create mode **/ --%>
						<c:choose>
							<c:when
								test="${value.fieldDef.field.required and (uiObject.editMode or uiObject.createMode)}">
								<td class="eFapsFormLabelRequired">
							</c:when>
							<c:otherwise>
								<td class="eFapsFormLabel">
							</c:otherwise>
						</c:choose>

						<%-- /** show the label of the field **/ --%>
						<c:out value="${value.fieldDef.label}" />
						</td>

						<%-- /** make column with correct span **/ --%>
						<td class="eFapsFormInputField"
							colspan="<c:out value="${2 * (uiObject.maxGroupCount - rowGroupCount) + 1}"/>">

						<%-- /** show href START **/ --%> <c:if
							test="${value.fieldDef.field.reference ne null and !(empty value.instance.oid) and uiObject.createMode ne true and uiObject.searchMode ne true}">
							<c:set var="targetUrl"
								value="${value.fieldDef.field.reference}&oid=${value.instance.oid}" />

							<c:choose>
								<c:when test="${value.fieldDef.field.targetPopup}">
									<c:set var="targetWindow" value="Popup" />
								</c:when>
								<c:otherwise>
									<c:set var="targetWindow" value="Content" />
									<c:if test="${uiObject.nodeId ne null}">
										<c:set var="targetUrl"
											value="${targetUrl}&nodeId=${uiObject.nodeId}" />
									</c:if>
								</c:otherwise>
							</c:choose>

							<a
								href="javascript:eFapsCommonOpenUrl('<c:out value="${targetUrl}" escapeXml="false"/>','<c:out value="${targetWindow}" escapeXml="false"/>')">

							<c:remove var="targetUrl" /> <c:remove var="targetWindow" />
						</c:if> <%-- /** show type icon if needed **/ --%> <c:if
							test="${value.fieldDef.field.showTypeIcon and !(empty value.instance.oid) and uiObject.createMode ne true and uiObject.searchMode ne true}">
							<img src="<c:out value="${value.instance.type.icon}"/>" />&nbsp;
                    </c:if> <%-- /** show value **/ --%> <c:choose>
							<c:when
								test="${uiObject.createMode and value.fieldDef.field.creatable}">
								<c:out value="${value.createHtml}" escapeXml="false" />
							</c:when>
							<c:when
								test="${uiObject.editMode and value.fieldDef.field.editable}">
								<c:out value="${value.editHtml}" escapeXml="false" />
							</c:when>
							<c:when
								test="${uiObject.searchMode and value.fieldDef.field.searchable}">
								<c:out value="${value.searchHtml}" escapeXml="false" />
							</c:when>
							<c:otherwise>
								<c:out value="${value.viewHtml}" escapeXml="false" />
							</c:otherwise>
						</c:choose> <%-- /** show href END **/ --%> <c:if
							test="${value.fieldDef.field.reference ne null and !(empty value.instance.oid) and uiObject.createMode ne true and uiObject.searchMode ne true}">
							</a>
						</c:if></td>

						<%-- /** show the end row tag only if last table column **/ --%>
						<c:if test="${groupCount == rowGroupCount}">
							</tr>
						</c:if>

						<c:choose>
							<%-- /** if group count less than row group count, increase by 1;
                             meaning the row has more than one field! **/ --%>
							<c:when test="${groupCount < rowGroupCount}">
								<c:set var="groupCount" value="${groupCount + 1}" />
							</c:when>
							<%-- /** if last column, reset group counters **/ --%>
							<c:when test="${groupCount == rowGroupCount}">
								<c:set var="groupCount" value="1" />
								<c:set var="rowGroupCount" value="1" />
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
      <h:form id="eFapsFormFooter">
        <c:choose>
          <c:when test="${uiObject.createMode}">
            <h:commandLink id="eFapsCreate"
              onclick="eFapsSubmit();return false;" title="Create">
              <h:graphicImage value="../images/eFapsButtonDone.gif" />
              <h:outputText value="Create" />
            </h:commandLink>
          </c:when>
          <c:when test="${uiObject.editMode}">
            <h:commandLink id="eFapsEdit" onclick="eFapsSubmit();return false;"
              title="Edit">
              <h:graphicImage value="../images/eFapsButtonDone.gif" />
              <h:outputText value="Edit" />
            </h:commandLink>
          </c:when>
          <c:when test="${uiObject.searchMode}">
            <h:commandLink id="eFapsSearch"
              onclick="eFapsSubmit();return false;" title="Search">
              <h:graphicImage value="../images/eFapsButtonNext.gif" />
              <h:outputText value="Search" />
            </h:commandLink>
          </c:when>
        </c:choose>
        <h:commandLink onclick="parent.dlgFrMdl.hide();eFapsCommonCloseWindow();return false;"
          title="Close">
          <h:graphicImage value="../images/eFapsButtonCancel.gif" />
          <h:outputText value="Close" />
        </h:commandLink>
      </h:form>
    </div>
	</c:if>

</f:subview>

<iframe name="eFapsFrameHidden" id="eFapsFrameHidden"
	class="eFapsFrameHidden" src="Blank.jsp"> </iframe>
</div>
</body>
</html>

