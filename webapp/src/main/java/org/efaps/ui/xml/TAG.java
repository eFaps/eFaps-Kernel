/*
 * Copyright 2003 - 2009 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 */

package org.efaps.ui.xml;

/**
 * This enum contains the tags wich will be used for creating a xml-Document.
 *
 * @author jmox
 * @version $Id$
 */
public enum TAG {
  FORM("form"),
  FORM_CELL("f_cell"),
  FORM_ROW("f_row"),
  HEADING("heading"),
  LABEL("label"),
  ROOT("eFaps"),
  TABLE("table"),
  TABLE_BODY("t_body"),
  TABLE_CELL("t_cell"),
  TABLE_HEADER("t_header"),
  TABLE_ROW("t_row"),
  TITLE("title"),
  TIMESTAMP("TimeStamp"),
  VALUE("value");

  public String value;

  private TAG(final String _value) {
    this.value = _value;
  }
}
