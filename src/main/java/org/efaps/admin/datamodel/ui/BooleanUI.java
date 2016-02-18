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

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.BooleanUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * A boolean value is shown in create mode with radio boxen which are only
 * preselected if a defaultvalue for the attribute was defined. In edit mode,
 * the user could select a value. The value is received from the DBProperties
 * using the AttributeName and a parameter.<br>
 * e.g. <br>
 * Key = TypeName/AttributeName.false <br>
 * Value = inactive
 *
 * @author The eFaps Team
 *
 */
public class BooleanUI
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
    public String getEditHtml(final FieldValue _fieldValue)
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Attribute attribute = _fieldValue.getAttribute();

        final Boolean bool;
        if (_fieldValue.getValue() instanceof Boolean) {
            bool = (Boolean) _fieldValue.getValue();
        } else if (_fieldValue.getValue() instanceof String && ((String) _fieldValue.getValue()).length() > 0) {
            if (((String) _fieldValue.getValue()).equalsIgnoreCase("TRUE")) {
                bool = true;
            } else {
                bool = false;
            }
        } else {
            bool = null;
        }

        if (_fieldValue.getTargetMode().equals(TargetMode.SEARCH)) {
            ret.append("<script language=\"javascript\" type=\"text/javascript\">")
                .append("var checked").append(field.getName()).append(";")
                .append("function Clear").append(field.getName()).append("(_btn) {")
                .append("if (checked").append(field.getName()).append(" == _btn){")
                .append("_btn.checked = false;")
                .append("checked").append(field.getName()).append(" = null;")
                .append("} else { ")
                .append("checked").append(field.getName()).append(" = _btn; }")
                .append("}").append("</script>")
                .append("<input type=\"radio\" ").append((bool != null && bool) ? "checked=\"checked\" " : "")
                .append("name=\"").append(field.getName()).append("\" ").append("value=\"").append("TRUE")
                .append("\" onclick=\"Clear").append(field.getName()).append("(this)\"/>")
                .append(getTrue(attribute)).append("<br/>")
                .append("<input type=\"radio\" ").append((bool != null && !bool) ? "checked=\"checked\" " : "")
                .append("name=\"").append(field.getName()).append("\" ").append("value=\"").append("FALSE")
                .append("\" onclick=\"Clear").append(field.getName()).append("(this)\"/>").append(getFalse(attribute));
        } else {
            ret.append("<input type=\"radio\" ").append((bool != null && bool) ? "checked=\"checked\" " : "")
                .append("name=\"").append(field.getName()).append("\" ").append("value=\"").append("TRUE")
                .append("\"/>").append(getTrue(attribute)).append("<br/>")
                .append("<input type=\"radio\" ").append((bool != null && !bool) ? "checked=\"checked\" " : "")
                .append("name=\"").append(field.getName()).append("\" ").append("value=\"").append("FALSE")
                .append("\"/>").append(getFalse(attribute));
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReadOnlyHtml(final FieldValue _fieldValue)
    {
        String ret = null;
        final Attribute attribute = _fieldValue.getAttribute();
        if (_fieldValue.getValue() instanceof Boolean) {
            final boolean bool = (Boolean) _fieldValue.getValue();
            if (bool) {
                ret = getTrue(attribute);
            } else {
                ret = getFalse(attribute);
            }
        }
        return ret;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public Object getObject4Compare(final FieldValue _fieldValue)
    {
        Object ret = null;
        if (_fieldValue.getValue() instanceof Boolean) {
            final boolean bool = (Boolean) _fieldValue.getValue();
            if (bool) {
                ret = getTrue(_fieldValue.getAttribute());
            } else {
                ret = getFalse(_fieldValue.getAttribute());
            }
        }
        return ret;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
    {
        String value = null;
        String value2 = null;
        // in case we have a boolean
        if (_fieldValue.getValue() instanceof Boolean && _fieldValue2.getValue() instanceof Boolean) {
            if ((Boolean) _fieldValue.getValue()) {
                value = getTrue(_fieldValue.getAttribute());
            } else {
                value = getFalse(_fieldValue.getAttribute());
            }
            if ((Boolean) _fieldValue2.getValue()) {
                value2 = getTrue(_fieldValue.getAttribute());
            } else {
                value2 = getFalse(_fieldValue.getAttribute());
            }
        }
        // in case we have allready a string
        if (_fieldValue.getValue() instanceof String && _fieldValue2.getValue() instanceof String) {
            value = (String) _fieldValue.getValue();
            value2 = (String) _fieldValue2.getValue();
        }
        return value.compareTo(value2);
    }

    /**
     * Method to evaluate a String representation for the boolean.
     *
     * @param _attribute Attribute the String representation is wanted for
     * @return String representation, default "FALSE"
     */
    private String getFalse(final Attribute _attribute)
    {
        String ret;

        if (DBProperties.hasProperty(_attribute.getKey() + ".false")) {
            ret = DBProperties.getProperty(_attribute.getKey() + ".false");
        } else {
            ret = "FALSE";
        }
        return ret;
    }

    /**
     * Method to evaluate a String representation for the boolean.
     *
     * @param _attribute Attribute the String representation is wanted for
     * @return String representation, default "TRUE"
     */
    private String getTrue(final Attribute _attribute)
    {
        String ret;
        if (DBProperties.hasProperty(_attribute.getKey() + ".true")) {
            ret = DBProperties.getProperty(_attribute.getKey() + ".true");
        } else {
            ret = "TRUE";
        }
        return ret;
    }

    /**
     * Method to evaluate a String representation for the boolean.
     *
     * @param _uiValue  UIValue the String representation is wanted for
     * @param _key      key the String representation is wanted for
     * @return String representation
     * @throws CacheReloadException the cache reload exception
     */
    private String getLabel(final UIValue _uiValue,
                            final Boolean _key)
        throws CacheReloadException
    {
        String ret = BooleanUtils.toStringTrueFalse(_key);
        if (_uiValue.getAttribute() != null
                        && DBProperties.hasProperty(_uiValue.getAttribute().getKey() + "."
                                        + BooleanUtils.toStringTrueFalse(_key))) {
            ret = DBProperties.getProperty(_uiValue.getAttribute().getKey() + "."
                            + BooleanUtils.toStringTrueFalse(_key));
        } else if (DBProperties
                        .hasProperty(_uiValue.getField().getLabel() + "." + BooleanUtils.toStringTrueFalse(_key))) {
            ret = DBProperties.getProperty(_uiValue.getField().getLabel() + "." + BooleanUtils.toStringTrueFalse(_key));
        }
        return ret;
    }

    @Override
    public Object getValue(final UIValue _uiValue)
        throws EFapsException
    {
        final Map<Object, Object> ret = new TreeMap<Object, Object>();
        ret.put(getLabel(_uiValue, Boolean.TRUE), Boolean.TRUE);
        ret.put(getLabel(_uiValue, Boolean.FALSE), Boolean.FALSE);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object transformObject(final UIValue _uiValue,
                                  final Object _object)
        throws EFapsException
    {
        Object ret = null;
        if (_object instanceof Map) {
            ret = _object;
        } else if (_object instanceof Serializable) {
            _uiValue.setDbValue((Serializable) _object);
            ret = getValue(_uiValue);
        }
        return ret;
    }
}
