/*
 * Copyright 2003 - 2019 The eFaps Team
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
package org.efaps.db.stmt.selection.elements;

import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.db.wrapper.SQLOrder;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;

/**
 * The Class AttributeSelect.
 *
 * @author The eFaps Team
 */
public class AttributeElement
    extends AbstractAttributeElement<AttributeElement>
    implements IOrderable
{

    /** The col idxs. */
    private int[] colIdxs;

    /** The attribute. */
    private Attribute attribute;

    /**
     * Gets the attribute.
     *
     * @return the attribute
     */
    public Attribute getAttribute()
    {
        return attribute;
    }

    /**
     * Sets the attribute.
     *
     * @param _attribute the attribute
     * @return the attribute element
     */
    public AttributeElement setAttribute(final Attribute _attribute)
    {
        attribute = _attribute;
        setDBTable(attribute.getTable());
        return this;
    }

    @Override
    public AttributeElement getThis()
    {
        return this;
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        if (getTable() instanceof SQLTable) {
            final TableIdx tableIdx = getTableIdx(_sqlSelect);
            for (final String colName : attribute.getSqlColNames()) {
                colIdxs = ArrayUtils.add(colIdxs, _sqlSelect.columnIndex(tableIdx.getIdx(), colName));
            }

            // in case of dependencies for the attribute they must be selected also
            for (final Attribute attr : attribute.getDependencies().values()) {
                for (final String colName : attr.getSqlColNames()) {
                    final int colidx = _sqlSelect.column(tableIdx.getIdx(), colName).getColumnIdx();
                    colIdxs = ArrayUtils.add(colIdxs, colidx);
                }
            }
        }
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        final Object ret;
        if (colIdxs.length == 1) {
            ret = _row[colIdxs[0]];
        } else {
            ret = new Object[colIdxs.length];
            for (int i = 0; i < colIdxs.length; i++) {
                ((Object[]) ret)[i] = _row[colIdxs[i]];
            }
        }
        return callAuxillary(attribute.value(Collections.singletonList(ret)));
    }

    @Override
    public void append2SQLOrder(final int _orderSequence,
                                final SQLOrder _order,
                                final boolean _desc)
        throws EFapsException
    {
        _order.addElement(_orderSequence, getTableIdx(_order.getSqlSelect()).getIdx(), attribute.getSqlColNames(), _desc);
    }
}
