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

package org.efaps.db.query;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.AbstractQuery;

/**
 * The class represents an equal where clause between two attributes.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Deprecated
public class WhereClauseAttrEqAttr
    implements WhereClause
{
    private final Attribute attr1;
    private final Attribute attr2;
    private final AbstractQuery.SelectType selectType1;
    private final AbstractQuery.SelectType selectType2;

    public WhereClauseAttrEqAttr(final AbstractQuery _query,
                                 final Attribute _attr1,
                                 final Attribute _attr2)
    {
        this.attr1 = _attr1;
        this.attr2 = _attr2;
        this.selectType1 = _query.getSelectType(this.attr1.getParent());
        this.selectType2 = _query.getSelectType(this.attr2.getParent());
    }

    public WhereClauseAttrEqAttr(final AbstractQuery.SelectType _selectType1,
                                 final Attribute _attr1,
                                 final AbstractQuery.SelectType _selectType2,
                                 final Attribute _attr2)
    {
        this.attr1 = _attr1;
        this.attr2 = _attr2;
        this.selectType1 = _selectType1;
        this.selectType2 = _selectType2;
    }

    /**
     * TODO:  compare does not work if an attribute has more than one SQL
     *       column!!
     */
    public WhereClause appendWhereClause(final CompleteStatement _completeStatement,
                                         final int _orderIndex)
    {
        if ((_orderIndex < 0)
                        || ((this.selectType1.getOrderIndex() < _orderIndex) && (this.selectType2.getOrderIndex() < _orderIndex))) {
            final String sqlColName1 = this.attr1.getSqlColNames().get(0);
            final String sqlColName2 = this.attr2.getSqlColNames().get(0);
            _completeStatement.appendWhereAnd();
            _completeStatement.appendWhere(this.attr1.getTable().getSqlTable());
            _completeStatement.appendWhere(this.selectType1.getTypeIndex());
            _completeStatement.appendWhere(".");
            // _completeStatement.appendWhere(getAttr1().getSqlColName());
            _completeStatement.appendWhere(sqlColName1);
            _completeStatement.appendWhere("=");
            _completeStatement.appendWhere(this.attr2.getTable().getSqlTable());
            _completeStatement.appendWhere(this.selectType2.getTypeIndex());
            _completeStatement.appendWhere(".");
            // _completeStatement.appendWhere(getAttr2().getSqlColName());
            _completeStatement.appendWhere(sqlColName2);
        } else if ((_orderIndex >= 0) && (this.selectType1.getOrderIndex() < _orderIndex)
                        && (this.selectType2.getOrderIndex() == _orderIndex)) {
            final String sqlColName1 = this.attr1.getSqlColNames().get(0);
            _completeStatement.appendWhereAnd();
            _completeStatement.appendWhere(this.attr1.getTable().getSqlTable());
            _completeStatement.appendWhere(this.selectType1.getTypeIndex());
            _completeStatement.appendWhere(".");
            // _completeStatement.appendWhere(getAttr1().getSqlColName());
            _completeStatement.appendWhere(sqlColName1);
            _completeStatement.appendWhere(" is null");
        } else if ((_orderIndex >= 0) && (this.selectType1.getOrderIndex() == _orderIndex)
                        && (this.selectType2.getOrderIndex() < _orderIndex)) {
            final String sqlColName2 = this.attr2.getSqlColNames().get(0);
            _completeStatement.appendWhereAnd();
            _completeStatement.appendWhere(this.attr2.getTable().getSqlTable());
            _completeStatement.appendWhere(this.selectType2.getTypeIndex());
            _completeStatement.appendWhere(".");
            // _completeStatement.appendWhere(getAttr2().getSqlColName());
            _completeStatement.appendWhere(sqlColName2);
            _completeStatement.appendWhere(" is null");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isIgnoreCase()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public WhereClause setIgnoreCase(final boolean _ignoreCase)
    {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOr()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WhereClause setOr(final boolean or)
    {
        return this;
    }
}
