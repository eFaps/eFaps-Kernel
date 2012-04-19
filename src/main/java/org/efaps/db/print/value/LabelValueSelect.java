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

package org.efaps.db.print.value;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.AbstractWithUoMType;
import org.efaps.admin.datamodel.attributetype.RateType;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.print.FileSelectPart;
import org.efaps.db.print.OneSelect;
import org.efaps.db.store.AbstractStoreResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LabelValueSelect
    extends AbstractValueSelect
    implements IAttributeChildValueSelect
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
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final Object _object)
    {
        Object ret = _object;
        if (_object != null && _object instanceof String) {
            ret = ((String) _object).trim();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int append2SQLSelect(final Type _type,
                                final SQLSelect _select,
                                final int _tableIndex,
                                final int _colIndex)
    {
        int ret = 0;
        if (getParent() == null && getParentSelectPart() != null
                        && getParentSelectPart() instanceof FileSelectPart) {
            _select.column(_tableIndex, AbstractStoreResource.COLNAME_FILENAME);
            getColIndexs().add(_colIndex);
            ret = 1;
        }
        return ret;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final Attribute _attribute,
                      final Object _object)
        throws EFapsException
    {
        final Object ret;
        if (_attribute.getAttributeType().getDbAttrType() instanceof RateType) {
            ret = getRate(_attribute, _object);
        } else if (_attribute.getAttributeType().getDbAttrType() instanceof AbstractWithUoMType) {
            ret = getValueUOM(_object);
        }  else {
            ret = _object;
        }
        return ret;
    }

    /**
     * @param _object object the value is wanted for
     * @return Object
     */
    protected Object getValueUOM(final Object _object)
    {
        Object ret = null;
        if (_object instanceof Object[]) {
            final Object[] values = (Object[]) _object;
            ret = ((UoM) values[1]).getName();
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
