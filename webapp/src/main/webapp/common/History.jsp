<%@ page errorPage="Exception.jsp"%>

<%@ taglib prefix="h"    uri="http://java.sun.com/jsf/html"%>
<%@ taglib prefix="f"    uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="t"    uri="http://myfaces.apache.org/tomahawk"%>

<html>

<!--
/*
 *
 * Copyright 2006 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 *
 */
//-->  

  <head>
    <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css"/>
  </head>

  <body><input type="submit" name="" value="" />
    <f:view>
      <t:dataTable styleClass="eFapsTable"
              headerClass="eFapsTableHeader"
              rowClasses="eFapsTableRowOdd,eFapsTableRowEven"
              preserveDataModel="true"
              renderedIfEmpty="true"
              var="row"
              value="#{history.data}"
              preserveDataModel="false"
              preserveSort="true"
              sortColumn="#{history.sortColumn}"
              sortAscending="#{history.sortAscending}">

        <t:columns value="#{history.columnHeaders}" var="column">
          <f:facet name="header">
            <t:commandSortHeader columnName="#{column.name}" arrow="true">
              <<h:panelGroup></h:panelGroup> value="#{column.label}"/>
            </t:commandSortHeader>
          </f:facet>


          <h:outputText 
              value="#{history.columnValue}" 
              converter="#{column.converter}"/>

        </t:columns>

      </t:dataTable>
    </f:view>
  </body>
</html>
