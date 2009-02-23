<?xml version="1.0" encoding="UTF-8"?>
<!--
  
  Copyright 2003 - 2009 The eFaps Team
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  
  Author:          jmox
  Revision:        $Rev$
  Last Changed:    $Date$
  Last Changed By: $Author$
  
-->

<!-- 
 @eFapsPackage  xsl
 @eFapsUUID     2b800665-5e64-4d8e-a050-aa46d030ae1a
 @eFapsRevision $Rev$
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:output indent="yes" encoding="UTF-8" />

  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

      <fo:layout-master-set>
        <fo:simple-page-master master-name="DinA4" page-width="210mm" page-height="297mm" margin-top="10mm" margin-bottom="10mm" margin-left="10mm"
          margin-right="10mm"
        >
          <fo:region-body />
        </fo:simple-page-master>
      </fo:layout-master-set>

      <fo:page-sequence master-reference="DinA4">
        <fo:flow flow-name="xsl-region-body">
          <xsl:apply-templates />

        </fo:flow>
      </fo:page-sequence>

    </fo:root>
  </xsl:template>

  <xsl:template match="title">
    <fo:block font-size="13pt" font-family="arial, verdana, helvetica, sans-serif" line-height="18pt" color="#008800" font-weight="bold">
      <xsl:value-of select="/eFaps/title" />
    </fo:block>
  </xsl:template>

  <xsl:template match="form">
    <fo:table>
      <fo:table-column column-width="25%" />
      <fo:table-column column-width="25%" />
      <fo:table-column column-width="25%" />
      <fo:table-column column-width="25%" />
      <fo:table-body>
        <xsl:for-each select="/eFaps/form/f_row">
          <fo:table-row>
            <xsl:for-each select="f_cell">
              <fo:table-cell>
                <xsl:if test="@type = 'Label'">
                  <fo:block font-size="9pt" font-weight="bold" color="#008800" background-color="#D0EDB8" border-style="solid" border-color="#008800"
                    border-width="2px" margin-right="3px"
                  >
                    <xsl:value-of select=".//value" />
                  </fo:block>
                </xsl:if>
                <xsl:if test="@type = 'Value'">
                  <xsl:attribute name="number-columns-spanned">
                      <xsl:value-of select="@column-span" />
                    </xsl:attribute>
                  <xsl:choose>
                    <xsl:when test='.//value[.!=""]'>
                      <fo:block font-size="9pt" border-bottom-style="dotted" border-bottom-width="1pt" border-bottom-color="#008800"
                        padding-top="2pt"
                      >
                        <xsl:value-of select=".//value" />
                      </fo:block>
                    </xsl:when>
                    <xsl:otherwise>
                      <fo:block font-size="9pt" border-bottom-style="dotted" border-bottom-width="1pt" border-bottom-color="#008800"
                        padding-top="12pt"
                      >
                      </fo:block>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:if>
              </fo:table-cell>
            </xsl:for-each>
          </fo:table-row>
        </xsl:for-each>
      </fo:table-body>
    </fo:table>
  </xsl:template>

  <xsl:template match="heading">
    <fo:block font-size="13pt" font-family="arial, verdana, helvetica, sans-serif" line-height="18pt" color="#008800" font-weight="bold"
      padding-top="20pt"
    >
      <xsl:value-of select=".//value" />
    </fo:block>
  </xsl:template>

  <xsl:template match="table">
    <fo:block font-size="10pt">
      <fo:table>
        <xsl:for-each select=".//t_header/t_cell">
          <fo:table-column>
            <xsl:attribute name="column-width">
            <xsl:value-of select="@width" />
          </xsl:attribute>
          </fo:table-column>
        </xsl:for-each>

        <fo:table-header>
          <fo:table-row>
            <xsl:for-each select=".//t_header/t_cell">
              <fo:table-cell >
                <fo:block font-weight="bold" background-color="#008800" color="white">
                  <xsl:value-of select=".//value" />
                </fo:block>
              </fo:table-cell>
            </xsl:for-each>
          </fo:table-row>
        </fo:table-header>

        <xsl:if test="count(/eFaps/table/t_body) &lt; 1">
          <fo:table-body>
            <fo:table-row>
              <fo:table-cell>
                <fo:block font-size="9pt">
                  <xsl:text>No Data available!</xsl:text>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </xsl:if>

        <xsl:for-each select=".//t_body">
          <fo:table-body>
            <xsl:for-each select=".//t_row">
              <fo:table-row>
                <xsl:for-each select=".//t_cell">
                  <fo:table-cell>
                    <fo:block font-size="9pt" border-bottom-style="dotted" border-bottom-width="1pt" border-bottom-color="#008800">
                      <xsl:value-of select=".//value" />
                    </fo:block>
                  </fo:table-cell>
                </xsl:for-each>
              </fo:table-row>
            </xsl:for-each>
          </fo:table-body>
        </xsl:for-each>
      </fo:table>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>