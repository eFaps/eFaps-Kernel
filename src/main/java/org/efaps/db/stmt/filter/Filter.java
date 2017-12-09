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

package org.efaps.db.stmt.filter;

import java.util.Arrays;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLWhere;
import org.efaps.db.wrapper.TableIndexer.Tableidx;
import org.efaps.eql2.IWhere;
import org.efaps.eql2.IWhereElement;
import org.efaps.eql2.IWhereElementTerm;
import org.efaps.eql2.IWhereTerm;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class Filter.
 */
public class Filter
{

    /** The i where. */
    private IWhere iWhere;

    /** The types. */
    private List<Type> types;

    /**
     * Analyze.
     *
     * @param _where the where
     * @param _types the types
     * @return the filter
     */
    private Filter analyze(final IWhere _where,
                           final List<Type> _types)
    {
        this.iWhere = _where;
        this.types = _types;
        return this;
    }

    /**
     * Append two SQL select.
     *
     * @param _sqlSelect the sql select
     */
    public void append2SQLSelect(final SQLSelect _sqlSelect)
    {
        final SQLWhere sqlWhere = new SQLWhere();
        for (final IWhereTerm<?> term : this.iWhere.getTerms()) {
            if (term instanceof IWhereElementTerm) {
                final IWhereElement element = ((IWhereElementTerm) term).getElement();
                final String attrName = element.getAttribute();
                if (attrName != null) {
                    for (final Type type : this.types) {
                        final Attribute attr = type.getAttribute(attrName);
                        if (attr != null) {
                            final SQLTable table = attr.getTable();
                            final String tableName = table.getSqlTable();
                            final Tableidx tableidx = _sqlSelect.getIndexer().getTableIdx(tableName, tableName);
                            attr.getSqlColNames();

                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the.
     *
     * @param _where the where
     * @param _baseTypes the base types
     * @return the selection
     * @throws CacheReloadException the cache reload exception
     */
    public static Filter get(final IWhere _where,
                             final Type... _baseTypes)
        throws CacheReloadException
    {
        return new Filter().analyze(_where, Arrays.asList(_baseTypes));
    }
}
