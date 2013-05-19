/*
 * Copyright 2003 - 2013 The eFaps Team
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
import org.efaps.admin.ui.Command;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldPicker
    extends Field
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ID of the Command to be executed on the picker field.
     */
    private long commandID;

    /**
     * @param _id       id of the field
     * @param _uuid     UUID of the field
     * @param _name     name of the field
     */
    public FieldPicker(final long _id,
                       final String _uuid,
                       final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Getter method for the instance variable {@link #command}.
     *
     * @return value of instance variable {@link #command}
     * @throws CacheReloadException on error
     */
    public Command getCommand()
        throws CacheReloadException
    {
        return Command.get(this.commandID);
    }

    /**
     *
     * Set a link property for the field.
     *
     * @param _linkType type of the link property
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
        if (_linkType.isKindOf(CIAdminUserInterface.LinkField2Command.getType())) {
            this.commandID = _toId;
        } else {
            super.setLinkProperty(_linkType, _toId, _toType, _toName);
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Field}.
     *
     * @param _id       id to search in the cache
     * @return instance of class {@link Field}
     */
    public static FieldPicker get(final long _id)
    {
        return (FieldPicker) Field.get(_id);
    }
}
