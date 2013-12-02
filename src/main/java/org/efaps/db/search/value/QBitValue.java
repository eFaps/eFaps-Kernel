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


package org.efaps.db.search.value;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.efaps.admin.datamodel.IBitEnum;
import org.efaps.admin.datamodel.attributetype.BitEnumType;
import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.search.AbstractQPart;
import org.efaps.db.search.compare.QEqual;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QBitValue
    extends AbstractQValue
{
    /**
     * BitEnum belonging to this value.
     */
    private final IBitEnum bitEnum;

    private final Set<Integer> added = new HashSet<Integer>();;

    /**
     * @param _iBitEnum
     */
    public QBitValue(final IBitEnum _bitEnum)
    {
        this.bitEnum = _bitEnum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractQPart prepare(final AbstractObjectQuery<?> _query,
                                 final AbstractQPart _part)
        throws EFapsException
    {
        if (_part instanceof QEqual) {
            final List<AbstractQValue> values = ((QEqual) _part).getValues();
            final Map<Integer, IBitEnum>enums = new TreeMap<Integer, IBitEnum>();
            for (final AbstractQValue value : values) {
                if (value instanceof QBitValue) {
                    final IBitEnum abitEnum = ((QBitValue) value).getBitEnum();
                    if (abitEnum.getInt() < this.bitEnum.getInt()) {
                        enums.put(abitEnum.getInt(), abitEnum);
                    }
                }
            }
            if (!enums.isEmpty()) {
                final IBitEnum aEnum = enums.values().iterator().next();
                final IBitEnum[] consts = aEnum.getClass().getEnumConstants();
                final int max = consts[consts.length - 1].getInt() * 2;
                for (int i = 0; i < max; i++) {
                    final Iterator<IBitEnum> iter = enums.values().iterator();
                    while (iter.hasNext()) {
                        final IBitEnum oEnum = iter.next();
                        if (BitEnumType.isSelected(i, oEnum)) {
                            this.added.add(i);
                        }
                    }
                }
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QBitValue appendSQL(final SQLSelect _sql)
    {
        final IBitEnum[] consts = this.bitEnum.getClass().getEnumConstants();
        final int max = consts[consts.length - 1].getInt() * 2;
        boolean first = true;
        for (int i = 0; i < max; i++) {
            if (!this.added.contains(i) && BitEnumType.isSelected(i, this.bitEnum)) {
                if (first) {
                    first = false;
                } else {
                    _sql.addPart(SQLPart.COMMA);
                }
                _sql.addValuePart(i);
            }
        }
        return this;
    }

    /**
     * Getter method for the instance variable {@link #bitEnum}.
     *
     * @return value of instance variable {@link #bitEnum}
     */
    public IBitEnum getBitEnum()
    {
        return this.bitEnum;
    }
}
