/*
 * Copyright 2003 - 2009 The eFaps Team
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
import org.efaps.admin.datamodel.attributetype.DateTimeType;
import org.efaps.db.AbstractQuery;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The class represents an equal where clause between two attributes.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class WhereClauseAttributeCompareValueAbstract
    implements WhereClause
{

    /**
     * Attribute the whereclause belongs to.
     */
    private final Attribute attr;

    /**
     * Value to be compared.
     */
    private final Object value;

    /**
     * Type of the select.
     */
    private final AbstractQuery.SelectType selType;

    /**
     * Query this whereclause belongs to.
     */
    private final AbstractQuery query;

    /**
     * Must the clause ignore case.
     */
    private boolean ignoreCase;

    /**
     * Is this WhereClause an or.
     */
    private boolean or;


    /**
     * Constructor.
     *
     * @param _query    query for this whereclause
     * @param _attr     attribute for this whereclause
     * @param _value    value used for this whereclause
     */
    public WhereClauseAttributeCompareValueAbstract(final AbstractQuery _query,
                                                    final Attribute _attr,
                                                    final Object _value)
    {
        this.attr = _attr;
        this.value = _value;
        this.selType = _query.getSelectType(getAttr().getParent());
        this.query = _query;
        getSelType().add4Where(getAttr());

    }

    /**
     * @param _completeStatement    the CompleteStatement the whereclause will be
     *                              appended to
     * @param _orderIndex           index in the clause
     * @param _operator             operator to be used
     * @throws EFapsException on error
     * @return this
     * TODO: compare does not work if an attribute has more than one sql
     *       column!!
     */
    protected WhereClause appendWhereClause(final CompleteStatement _completeStatement,
                                            final int _orderIndex,
                                            final String _operator)
        throws EFapsException
    {

        if (_orderIndex < 0 || getSelType().getOrderIndex() < _orderIndex) {

            final String sqlColName = getAttr().getSqlColNames().get(0);
            _completeStatement.isWhere();
            _completeStatement.appendWhereAnd();
            if (isOr()) {
                _completeStatement.appendWhereOr();
            }

            if (isIgnoreCase()) {
                _completeStatement.appendWhere("UPPER(");
            }
            _completeStatement.appendWhere(getAttr().getTable().getSqlTable()).appendWhere(getSelType().getTypeIndex())
                            .appendWhere(".").appendWhere(sqlColName);
            if (isIgnoreCase()) {
                _completeStatement.appendWhere(")");
            }

            _completeStatement.appendWhere(_operator);

            // TODO: bug-fixing wg. cloudescape
            if (getAttr().getLink() != null || getAttr().getName().equals("ID")) {
                _completeStatement.appendWhere(getValue()).appendWhere("");
            } else {
                // in case of DateTimeType the value must be cast to a timestamp
                if (this.attr.getAttributeType().getClassRepr().equals(DateTimeType.class)) {
                    _completeStatement.appendWhere(" timestamp ");
                }
                _completeStatement.appendWhere("'").appendWhere(getValue()).appendWhere("'");
            }
        }
        return this;
    }

    /**
     * This is the getter method for instance variable {@link #attr}.
     *
     * @return value of instance variable {@link #attr}
     * @see #attr
     */
    protected Attribute getAttr()
    {
        return this.attr;
    }

    /**
     * This is the getter method for instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     * @throws EFapsException on error while retrieving local from context
     * @see #value
     */
    protected String getValue()
        throws EFapsException
    {
        final String strValue = this.attr.getAttributeType().getDbAttrType().toString4Where(this.value);
        return isIgnoreCase()
                ? strValue.toUpperCase(Context.getThreadContext().getLocale())
                : strValue;
    }

    /**
     * This is the getter method for instance variable {@link #selType}.
     *
     * @return value of instance variable {@link #selType}
     * @see #selType
     */
    protected AbstractQuery.SelectType getSelType()
    {
        return this.selType;
    }

    /**
     * Getter method for instance variable {@link #query}.
     *
     * @return value of instance variable {@link #query}
     */
    protected AbstractQuery getQuery()
    {
        return this.query;
    }

    /**
     * {@inheritDoc}
     */
    public WhereClause setIgnoreCase(final boolean _ignoreCase)
    {
        this.ignoreCase = _ignoreCase;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isIgnoreCase()
    {
        return this.ignoreCase;
    }


    /**
     * Getter method for the instance variable {@link #or}.
     *
     * @return value of instance variable {@link #or}
     */
    public boolean isOr()
    {
        return this.or;
    }


    /**
     * Setter method for instance variable {@link #or}.
     *
     * @param _or value for instance variable {@link #or}
     * @return this
     */
    public WhereClause setOr(final boolean _or)
    {
        this.or = _or;
        return this;
    }
}
