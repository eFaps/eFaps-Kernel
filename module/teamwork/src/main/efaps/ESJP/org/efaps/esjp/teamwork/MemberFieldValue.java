/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.esjp.teamwork;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.FieldValue.HtmlType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;

/**
 * @author jmox
 * @version $Id$
 *
 */
public class MemberFieldValue implements EventExecution {

  private FieldValue fieldvalue;

  public Return execute(Parameter _parameter) {

    this.fieldvalue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
    String ret = null;
    final HtmlType htmltype = this.fieldvalue.getHtmlType();
    final Return retVal = new Return();

    if (htmltype == HtmlType.CREATEHTML) {
      ret = getCreateHtml();
    }
    if (ret != null) {
      retVal.put(ReturnValues.VALUES, ret);
    }
    return retVal;
  }

  private String getCreateHtml() {
    return "q.e.d";

  }
}
