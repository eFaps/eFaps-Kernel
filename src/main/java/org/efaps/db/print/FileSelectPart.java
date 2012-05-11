/*
 * Copyright 2003 - 2012 The eFaps Team
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


package org.efaps.db.print;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.store.AbstractStoreResource;
import org.efaps.db.wrapper.SQLSelect;


/**
 * Joins the General Store table to the Select.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FileSelectPart
    extends GenInstSelectPart
{
    /**
     * @param _type Type the General Instance belongs to
     */
    public FileSelectPart(final Type _type)
    {
        super(_type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int join(final OneSelect _oneSelect,
                    final SQLSelect _select,
                    final int _relIndex)
    {
        final int relIndex = super.join(_oneSelect, _select, _relIndex);
        Integer ret;
        ret = _oneSelect.getTableIndex(AbstractStoreResource.TABLENAME_STORE, "ID", relIndex);
        if (ret == null) {
            ret = _oneSelect.getNewTableIndex(AbstractStoreResource.TABLENAME_STORE, "ID", relIndex);
            _select.leftJoin(AbstractStoreResource.TABLENAME_STORE, ret, "ID", relIndex, "ID");
        }
        _select.column(ret, "ID");
        return ret;
    }
}
