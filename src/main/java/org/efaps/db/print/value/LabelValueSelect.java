/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.db.print.value;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.RateType;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.print.OneSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LabelValueSelect
    extends AbstractValueSelect
{

    /**
     * @param _oneSelect OneSelect
     */
    public LabelValueSelect(final OneSelect _oneSelect)
    {
        super(_oneSelect);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "label";
    }

    /**
     * @param _attribute Attribute this value is wanted for
     * @param _object object containing the value for the attribute
     * @return value
     * @throws EFapsException on error
     */
    protected Object getLabel(final Attribute _attribute,
                              final Object _object)
        throws EFapsException
    {
        final Object ret;
        if (_attribute.getAttributeType().getDbAttrType() instanceof RateType) {
            ret = getRate(_attribute, _object);
        }  else {
            ret = _object;
        }
        return ret;
    }

    /**
     * @param _attribute Attribute this value is wanted for
     * @param _object object the rate is wanted for
     * @return Object
     * @throws EFapsException on error
     */
    protected Object getRate(final Attribute _attribute,
                             final Object _object)
        throws EFapsException
    {
        Object ret = null;
        if (_object instanceof Object[]) {
            final FieldValue fieldValue = new FieldValue(null, _attribute, _object, null, null);
            fieldValue.setTargetMode(TargetMode.VIEW);
            ret = fieldValue.getObject4Compare();
        }
        return ret;
    }
}
