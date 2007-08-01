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

package org.efaps.esjp.common.main;

import java.util.Map;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.FieldValue.HtmlType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;

/**
 * @author jmo
 * @version $Id$
 * @todo description
 */
public class PwdFieldValue implements EventExecution {

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) {
    StringBuilder ret = new StringBuilder();
    FieldValue fieldvalue =
        (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
    String field =
        (String) ((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES))
            .get("field");

    HtmlType htmltype = fieldvalue.getHtmlType();

    Return retVal = new Return();

    if (htmltype == HtmlType.CREATEHTML) {
      if (field.equals("1")) {
        ret
            .append("<script type=\"text/javascript\">")
            .append("function testForEqual()")
            .append(
                "{ var old=document.getElementById(\"eFapsPasswordOld\").value; ")
            .append(
                "var A=document.getElementById(\"eFapsPasswordNew\").value; ")
            .append(
                "var B=document.getElementById(\"eFapsPasswordNew2\").value;")
            .append("if((A.length>0) && (B.length>0) && (old.length>0)){")
            .append("if((A!=B)){ eFapsOpenWarnDialog(\"")
            .append(
                DBProperties.getProperty("Common_Main_PwdChgForm/WarnMessage"))
            .append(
                "\"); document.getElementById(\"eFapsPasswordNew2\").value=\"\";}")

            .append("if((A==B)){submitLock=false;}")

            .append("}}</script>").append(
                "<br/>&nbsp;<input name=\"passwordold\" type=\"password\" ")
            .append("size=\"20\" maxlength=\"25\" ").append(
                "onfocus=\"submitLock=true;\" onblur=\"testForEqual();\" ")
            .append(" id=\"eFapsPasswordOld\">&nbsp;<br/><br/>");
      } else if (field.equals("2")) {
        ret
            .append("<br/>&nbsp;<input name=\"passwordnew\" type=\"password\" ")
            .append("size=\"20\" maxlength=\"25\" onfocus=\"submitLock=true;\"")
            .append(" onblur=\"testForEqual();\" id=\"eFapsPasswordNew\">")
            .append("&nbsp;<br/><br/>");
      } else if (field.equals("3")) {
        ret
            .append(
                "<br/>&nbsp;<input name=\"passwordnew2\" type=\"password\" ")
            .append("size=\"20\" maxlength=\"25\" onfocus=\"submitLock=true;\"")
            .append("onblur=\"testForEqual();\" id=\"eFapsPasswordNew2\">")
            .append("&nbsp;<br/><br/>");

      }
    }
    if (ret != null) {
      retVal.put(ReturnValues.VALUES, ret);
    }
    return retVal;
  }
}
