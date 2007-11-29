/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.admin.datamodel.ui;

import org.efaps.admin.ui.field.Field;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @author jmo
 * @version $Id$
 */
public class FileUI extends AbstractUI {
  @Override
  public String getEditHtml(final FieldValue _fieldValue) throws EFapsException {
    StringBuffer ret = new StringBuffer();
    Field field = _fieldValue.getFieldDef().getField();
    ret.append("<input name=\"").append(field.getName()).append("\" ").append(
        "type=\"file\" ").append("size=\"").append(field.getCols()).append(
        "\" ").append(">");

    return ret.toString();
  }

  @Override
  public String getCreateHtml(final FieldValue _fieldValue)
      throws EFapsException {
    StringBuffer ret = new StringBuffer();
    Field field = _fieldValue.getFieldDef().getField();
    ret.append("<input name=\"").append(field.getName()).append("\" ").append(
        "type=\"file\" ").append("size=\"").append(field.getCols()).append(
        "\" ").append(">");
    return ret.toString();
  }

}
