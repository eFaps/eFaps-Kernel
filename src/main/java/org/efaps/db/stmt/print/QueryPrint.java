/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.db.stmt.print;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.stmt.filter.Filter;
import org.efaps.db.stmt.selection.Selection;
import org.efaps.eql2.IPrintQueryStatement;
import org.efaps.eql2.IWhere;
import org.efaps.eql2.impl.PrintQueryStatement;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class QueryPrint.
 */
public class QueryPrint
    extends AbstractPrint
{

    /** The eql stmt. */
    private final IPrintQueryStatement eqlStmt;

    /**
     * Instantiates a new object print.
     *
     * @param _eqlStmt the eql stmt
     * @throws CacheReloadException on error
     */
    public QueryPrint(final IPrintQueryStatement _eqlStmt)
        throws CacheReloadException
    {
        this.eqlStmt = _eqlStmt;
        for (final String typeStr : ((PrintQueryStatement) this.eqlStmt).getQuery().getTypes()) {
            final Type type;
            if (UUIDUtil.isUUID(typeStr)) {
                type = Type.get(UUID.fromString(typeStr));
            } else {
                type = Type.get(typeStr);
            }
            addType(type);
        }
    }

    @Override
    public Selection getSelection()
        throws EFapsException
    {
        Selection ret = super.getSelection();
        if (ret == null) {
            setSelection(Selection.get(this.eqlStmt.getSelection(), getTypes().toArray(new Type[getTypes().size()])));
            ret = super.getSelection();
        }
        return ret;
    }

    public Filter getFilter()
        throws CacheReloadException
    {
        final IWhere where = this.eqlStmt.getQuery().getWhere();
        return Filter.get(where, getTypes().toArray(new Type[getTypes().size()]));
    }
}
