/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.admin.datamodel.ui;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.util.EFapsException;

/**
 * This Class is the representation of
 * {@link org.efaps.admin.datamodel.attributetype.LinkWithRanges} for the user
 * interface.<br>
 * Depending on the access mode (e.g. edit) the Value of a Field is presented in
 * an editable or non editable mode.
 *
 * @author The eFaps Team
 *
 */
public class LinkWithRangesUI
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
    public String getReadOnlyHtml(final FieldValue _fieldValue) throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Attribute attribute = _fieldValue.getAttribute();
        if (_fieldValue.getValue() != null) {
            if (attribute.hasEvents(EventType.RANGE_VALUE)) {
                for (final Return values : attribute.executeEvents(EventType.RANGE_VALUE,
                                                           ParameterValues.UIOBJECT, _fieldValue,
                                                           ParameterValues.ACCESSMODE, _fieldValue.getTargetMode())) {
                    final Map<?, ?> map = (Map<?, ?>) values.get(ReturnValues.VALUES);
                    for (final Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getValue().equals(_fieldValue.getValue().toString())) {
                            ret.append(entry.getKey().toString());
                        }
                    }
                }
            }
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Attribute attribute = _fieldValue.getAttribute();
        if (_fieldValue.getTargetMode().equals(TargetMode.SEARCH)) {
            final Field field = _fieldValue.getField();
            ret.append("<input type=\"text\" ")
                .append("size=\"").append(field.getCols())
                .append("\" name=\"").append(field.getName())
                .append("\" value=\"*\"").append("/>").toString();
        } else {
            if (attribute.hasEvents(EventType.RANGE_VALUE)) {
                for (final Return values : attribute.executeEvents(EventType.RANGE_VALUE,
                                                           ParameterValues.UIOBJECT, _fieldValue,
                                                           ParameterValues.ACCESSMODE, _fieldValue.getTargetMode())) {
                    ret.append("<select name=\"").append(_fieldValue.getField().getName()).append("\" ")
                        .append(UIInterface.EFAPSTMPTAG).append(" size=\"1\">");
                    final Iterator<?> iter = ((TreeMap<?, ?>) values.get(ReturnValues.VALUES)).entrySet().iterator();

                    while (iter.hasNext()) {
                        final Entry<?, ?> entry = (Entry<?, ?>) iter.next();
                        ret.append("<option value=\"").append(entry.getValue());
                        if (_fieldValue.getValue() != null
                                        && ((!_fieldValue.getTargetMode().equals(TargetMode.CREATE)
                                                        && _fieldValue.getValue().toString().equals(entry.getValue()))
                                        || (_fieldValue.getTargetMode().equals(TargetMode.CREATE)
                                                        && _fieldValue.getValue().toString().equals(entry.getKey())))) {
                            ret.append("\" selected=\"selected");
                        }
                        ret.append("\">").append(entry.getKey()).append("</option>");
                    }
                    ret.append("</select>");
                }
            }
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
        throws EFapsException
    {
        return _fieldValue.getValue().toString().compareTo(_fieldValue2.getValue().toString());
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getValue(final UIValue _uiValue)
        throws EFapsException
    {
        final Map<Object, Object> ret = new TreeMap<Object, Object>();
        final Attribute attribute = _uiValue.getAttribute();
        if (attribute != null && attribute.hasEvents(EventType.RANGE_VALUE)) {
            for (final Return values : attribute.executeEvents(EventType.RANGE_VALUE,
                                                       ParameterValues.UIOBJECT, _uiValue,
                                                       ParameterValues.ACCESSMODE, _uiValue.getTargetMode())) {
                ret.putAll((Map<Object, Object>) values.get(ReturnValues.VALUES));
            }
        }
        return ret;
    }
}
