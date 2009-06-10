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

package org.efaps.admin.datamodel.ui;

import java.util.List;

import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.util.EFapsException;

/**
 * Class to represent a String for the user interface.
 *
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class StringUI extends AbstractUI
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Method to get the Value for viewing in an html document.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @param _mode target mode
     * @return value for field
     *
     */
    @Override
    public String getReadOnlyHtml(final FieldValue _fieldValue, final TargetMode _mode)
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();

        if (value instanceof List) {
            final List<?> values = (List<?>) value;
            boolean first = true;
            for (final Object obj : values) {
                final String tmp = obj.toString();
                if (tmp != null) {
                    if (first) {
                        first = false;
                    } else {
                        ret.append("<br/>");
                    }
                    ret.append(tmp.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\n", "<br/>"));
                }
            }
        } else {
            final String tmp = value != null ? value.toString() : "";
            if (tmp != null) {
                ret.append("<span name=\"").append(field.getName()).append("\" ").append(EFAPSTMPTAG).append(">")
                    .append(tmp.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\n", "<br/>"))
                    .append("</span>");
            }
        }
        return ret.toString();
    }

    /**
     * Method to get the Value for editing in an html document.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @param _mode target mode
     * @return value for field
     *
     */
    @Override
    public String getEditHtml(final FieldValue _fieldValue, final TargetMode _mode)
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        if (_mode.equals(TargetMode.SEARCH)) {
            ret.append("<input type=\"text\"").append(" size=\"").append(field.getCols()).append("\" name=\"").append(
                           field.getName()).append("\" value=\"").append((value != null ? value : "*")).append("\" />");
        } else {
            if (field.getRows() > 1) {
                ret.append("<textarea type=\"text\"")
                    .append(" cols=\"").append(field.getCols())
                    .append("\" rows=\"").append(field.getRows())
                    .append("\" name=\"").append(field.getName()).append("\"")
                    .append(EFAPSTMPTAG).append("/>");
                if (value != null) {
                    ret.append(value);
                }
                ret.append("</textarea>");
            } else {
                ret.append("<input type=\"text\" size=\"").append(field.getCols())
                    .append("\" name=\"").append(field.getName())
                    .append("\" value=\"").append(value != null ? value : "").append("\"")
                    .append(EFAPSTMPTAG).append("/>");
            }
        }
        return ret.toString();
    }

    /**
     * @see org.efaps.admin.datamodel.ui.UIInterface#getStringValue(org.efaps.admin.datamodel.ui.FieldValue, org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode)
     * @param _fieldValue Fieldvalue the representation is requested
     * @param _mode the target mode
     * @return "String-Value"
     * @throws EFapsException on error
     */
    @Override
    public String getStringValue(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        return _fieldValue.getValue() == null ? "" : _fieldValue.getValue().toString();
    }

    /**
     * Method to compare the values.
     *
     * @param _fieldValue first Value
     * @param _fieldValue2 second Value
     * @return 0
     */
    @Override
    public int compare(final FieldValue _fieldValue, final FieldValue _fieldValue2)
    {
        final String value = _fieldValue.getValue().toString();
        final String value2 = _fieldValue2.getValue().toString();
        return value.compareTo(value2);
    }
}
