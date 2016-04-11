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
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * A boolean value is shown in create mode with radio boxes which are only
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
    extends AbstractProvider
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

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

    @Override
    public String validateValue(final UIValue _uiValue)
        throws EFapsException
    {
        return null;
    }
}
