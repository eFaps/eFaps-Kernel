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

package org.efaps.admin.ui.field;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.ui.Table;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldTable
    extends Field
{

    /**
     * The instance variable stores the target user interface table object which
     * is shown by the this field.
     *
     * @see #getTargetTable
     * @see #setTargetTable
     */
    private Table targetTable = null;

    /**
     * Costructor.
     *
     * @param _id id of this FieldTable
     * @param _uuid uuid of this FieldTable
     * @param _name name of this FieldTable
     */
    public FieldTable(final long _id,
                      final String _uuid,
                      final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * @see org.efaps.admin.ui.field.Field#setLinkProperty(org.efaps.admin.EFapsClassNames,
     *      long, org.efaps.admin.EFapsClassNames, java.lang.String)
     * @param _linkType link type
     * @param _toId to id
     * @param _toType to type
     * @param _toName to name
     * @throws EFapsException on error
     */
    @Override
    protected void setLinkProperty(final Type _linkType,
                                   final long _toId,
                                   final Type _toType,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkType.isKindOf(CIAdminUserInterface.LinkTargetTable.getType())) {
            this.targetTable = Table.get(_toId);
        } else {
            super.setLinkProperty(_linkType, _toId, _toType, _toName);
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Field}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Field}
     */
    public static FieldTable get(final long _id)
    {
        return (FieldTable) Field.get(_id);
    }

    /**
     * This is the getter method for the instance variable {@link #targetTable}.
     *
     * @return value of instance variable {@link #targetTable}
     */
    public Table getTargetTable()
    {
        return this.targetTable;
    }

}
