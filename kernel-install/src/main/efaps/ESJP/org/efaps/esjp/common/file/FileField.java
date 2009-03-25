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

package org.efaps.esjp.common.file;

import java.util.Map;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.field.Field;

/**
 * The ESJP could be used to show an input field for file upload.
 * <br>
 * <br>
 * <b>Properties:</b><br>
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Value</th>
 * <th>Default</th>
 * <th>mandatory</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>FileNameField</td>
 * <td>-</td>
 * <td>-</td>
 * <td>no</td>
 * <td>Name of the field the filename will be set </td>
 * </tr>
 * </table><br>
 *
 * To use this ESJP configure a field in a form like this following example:
 * <pre>
 * <code>
 * &lt;field name="file"&gt;
 *     &lt;property name="Editable"&gt;true&lt;/property&gt;
 *     &lt;property name="Label"&gt;Admin_UI_File/File.Label&lt;/property&gt;
 *     &lt;trigger name="Admin_UI_File/file"
 *              event="UI_FIELD_VALUE"
 *              program="org.efaps.esjp.common.file.FileField"
 *              method="getFieldValueUI"/&gt;
 * &lt;/field&gt;
 * </code>
 * </pre>
 * By setting the property "FileNameField" a field can be definded that will
 * be filled via JavaScript with the name of the selected field.
 *
 * <pre>
 * <code>
 * &lt;field name="file"&gt;
 *     &lt;property name="Editable"&gt;true&lt;/property&gt;
 *     &lt;property name="Label"&gt;Admin_UI_File/File.Label&lt;/property&gt;
 *     &lt;trigger name="Admin_UI_File/file"
 *              event="UI_FIELD_VALUE"
 *              program="org.efaps.esjp.common.file.FileField"
 *              method="getFieldValueUI"&gt;
 *       &lt;property name="FileNameField"&gt;name&lt;/property&gt;
 *     &lt;/trigger&gt;
 * &lt;/field&gt;
 * </code>
 * </pre>
 *
 *
 * @author tmo
 * @version $Id$
 */
@EFapsUUID("7f08c93d-96fd-474a-a1f0-772d610deaba")
@EFapsRevision("$Rev$")
public class FileField {

  /**
   * Key to the property defining the name of the field that should be filled
   * with the name of the file.
   */
  private static String PROPERTYKEY_FILEFIELD = "FileNameField";

  /**
   * Method to get the FieldValue for the FileField.
   * @param _parameter Parameter as passed by the eFaps.
   * @return  Return
   */
  public Return getFieldValueUI(final Parameter _parameter) {
    final FieldValue fieldvalue =
                          (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
    final Field field = fieldvalue.getField();
    final Map<?, ?> properties
                      = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
    final StringBuilder ret = new StringBuilder();

    if (properties.containsKey(PROPERTYKEY_FILEFIELD)) {
      ret.append("<script type=\"text/javascript\">")
        .append("function setFileName(_str) {\n")
        .append("  var sv = _str.lastIndexOf('/');\n")
        .append("  if (sv < 0) {\n")
        .append("    sv = _str.lastIndexOf('\\\\');\n")
        .append("  }\n")
        .append("  if (sv >= 0) {")
        .append("    _str=_str.substr(sv+1);")
        .append("  }")
        .append("  document.getElementsByName('")
          .append(properties.get(PROPERTYKEY_FILEFIELD))
          .append("')[0].value=_str;")
        .append("}")
        .append("</script>");
    }

    ret.append("<input")
      .append(" name=\"").append(field.getName()).append("\" ")
      .append(" type=\"file\" ")
      .append(" size=\"").append(field.getCols()).append("\" ");

    if (properties.containsKey(PROPERTYKEY_FILEFIELD)) {
     ret.append(" onChange=\"setFileName(this.value)\"");
    }
    ret.append("/>");

    final Return retVal = new Return();
    retVal.put(ReturnValues.SNIPLETT, ret);
    return retVal;
  }
}
