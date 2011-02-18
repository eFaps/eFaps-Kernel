/*
 * Copyright 2003 - 2011 The eFaps Team
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

import java.text.Collator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * Class to represent a String for the user interface.
 *
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class StringUI
    extends AbstractUI
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReadOnlyHtml(final FieldValue _fieldValue)
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();

        if (value instanceof List<?>) {
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
                    ret.append(StringEscapeUtils.escapeHtml(tmp).replaceAll("\\n", "<br/>"));
                }
            }
        } else {
            final String tmp = value != null ? value.toString() : "";
            if (tmp != null) {
                ret.append("<span name=\"").append(field.getName()).append("\" ").append(UIInterface.EFAPSTMPTAG)
                    .append(">").append(StringEscapeUtils.escapeHtml(tmp).replaceAll("\\n", "<br/>"))
                    .append("</span>");
            }
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHiddenHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        final String tmp = value != null ? value.toString() : "";

        ret.append("<input type=\"hidden\" ").append(" name=\"").append(field.getName())
            .append("\" value=\"").append(StringEscapeUtils.escapeHtml(tmp)).append("\"")
            .append(UIInterface.EFAPSTMPTAG).append("/>");

        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditHtml(final FieldValue _fieldValue)
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        if (_fieldValue.getTargetMode().equals(TargetMode.SEARCH)) {
            final String tmp = value != null ? value.toString() : "*";
            ret.append("<input type=\"text\"")
                .append(" size=\"").append(field.getCols())
                .append("\" name=\"").append(field.getName())
                .append("\" value=\"").append(StringEscapeUtils.escapeHtml(tmp)).append("\" />");
        } else {
            final String tmp = value != null ? value.toString() : "";
            if (field.getRows() > 1) {
                ret.append("<textarea type=\"text\"")
                                .append(" cols=\"").append(field.getCols())
                                .append("\" rows=\"").append(field.getRows())
                                .append("\" name=\"").append(field.getName()).append("\"")
                                .append(UIInterface.EFAPSTMPTAG).append("/>");
                if (value != null) {
                    ret.append(StringEscapeUtils.escapeHtml(tmp));
                }
                ret.append("</textarea>");
            } else {
                ret.append("<input type=\"text\" size=\"").append(field.getCols())
                    .append("\" name=\"").append(field.getName())
                    .append("\" value=\"").append(StringEscapeUtils.escapeHtml(tmp))
                    .append("\"").append(UIInterface.EFAPSTMPTAG).append("/>");
            }
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue(final FieldValue _fieldValue)
        throws EFapsException
    {
        return _fieldValue.getValue() == null ? "" : _fieldValue.getValue().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
    {
        final String value = _fieldValue.getValue().toString();
        final String value2 = _fieldValue2.getValue().toString();
        Collator collator;
        try {
            collator = Collator.getInstance(Context.getThreadContext().getLocale());
        } catch (final EFapsException e) {
            // on an error the default locale is used
            collator =  Collator.getInstance();
        }
        collator.setStrength(Collator.PRIMARY);
        return collator.compare(value, value2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateValue(final String _value,
                                final Attribute _attribute)
    {
        String ret = null;
        if (_attribute != null) {
            if (_value.length() > _attribute.getSize()) {
                ret = DBProperties.getProperty(StringUI.class.getName() + ".InvalidValue") + " " + _attribute.getSize();
            }
        }
        return ret;
    }
}
