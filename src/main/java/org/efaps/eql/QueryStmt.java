/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.eql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public class QueryStmt
    extends AbstractQueryStmt
{

    private QueryBuilder queryBdr;
    private List<Map<String, Object>> data;

    @Override
    public void addType(final String _type)
        throws EFapsException
    {
        if (this.queryBdr == null) {
            this.queryBdr = new QueryBuilder(Type.get(_type));
        } else {
            this.queryBdr.addType(Type.get(_type));
        }
    }

    @Override
    public void addWhereAttrEq(final String _attr,
                               final String _value)
        throws EFapsException
    {
        this.queryBdr.addWhereAttrEqValue(_attr, _value);
    }

    @Override
    public void addWhereAttrNotEq(final String _attr,
                                  final String _value)
        throws EFapsException

    {
        this.queryBdr.addWhereAttrNotEqValue(_attr, _value);
    }

    @Override
    public void addWhereAttrGreater(final String _attr,
                                    final String _value)
        throws EFapsException
    {
        this.queryBdr.addWhereAttrGreaterValue(_attr, _value);
    }

    @Override
    public void addWhereAttrLess(final String _attr,
                                 final String _value)
        throws EFapsException
    {
        this.queryBdr.addWhereAttrLessValue(_attr, _value);
    }

    @Override
    public void addWhereAttrLike(final String _attr,
                                 final String _value)
        throws EFapsException
    {
        this.queryBdr.addWhereAttrMatchValue(_attr, _value);
    }

    @Override
    public void addWhereAttrIn(final String _attr,
                               final Collection<String> _values)
        throws EFapsException
    {
        this.queryBdr.addWhereAttrEqValue(_attr, _values.toArray());
    }

    @Override
    public void addWhereSelectEq(final String _select,
                                 final String _value)
        throws EFapsException
    {
        this.queryBdr.addWhereSelectEqValue(_select, _value);
    }

    @Override
    public void addWhereSelectGreater(final String _select,
                                      final String _value)
        throws EFapsException
    {
        this.queryBdr.addWhereSelectGreaterValue(_select, _value);
    }

    @Override
    public void addWhereSelectLess(final String _select,
                                   final String _value)
        throws EFapsException
    {
        this.queryBdr.addWhereSelectLessValue(_select, _value);
    }

    @Override
    public void addWhereSelectLike(final String _select,
                                   final String _value)
        throws Exception
    {
        this.queryBdr.addWhereSelectMatchValue(_select, _value);
    }

    @Override
    public List<Map<String, Object>> getData()
        throws Exception
    {
        if (this.data == null) {
            this.data = new ArrayList<>();
            final MultiPrintQuery multi = this.queryBdr.getPrint();
            for (final String sel : getAlias2Selects().values()) {
                multi.addSelect(sel);
            }
            multi.execute();
            while (multi.next()) {
                final Map<String, Object> map = new HashMap<>();
                this.data.add(map);
                for (final Entry<String, String> entry : getAlias2Selects().entrySet()) {
                    map.put(entry.getKey(), multi.getSelect(entry.getValue()));
                }
            }
        }
        return this.data;
    }
}
