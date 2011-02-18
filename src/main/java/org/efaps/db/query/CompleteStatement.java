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

/**
 * The class is used to build a complete statement with correct order etc.
 * The class is working like a string buffer.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Deprecated
public class CompleteStatement
{
    /**
     * StringBuilder used for this statement.
     */
    private final StringBuilder statement = new StringBuilder();

    /**
     * The instance variable stores if a where clause must be appended.
     *
     * @see #hasWhere
     */
    private boolean where = false;


    /**
     * The instance variable stores if a from clause must be appended.
     *
     * @see #hasWhere
     */
    private boolean from = false;

    /**
     * Is the current part an expression.
     */
    private boolean expr = false;

    /**
     * Append an <code>union</code> to this statement.
     */
    public void appendUnion()
    {
        append(" union ");
        this.expr = false;
        this.where = false;
        this.from = false;
    }

    /**
     * Append an <code>and</code> to this statement.
     * @return this
     */
    public CompleteStatement appendWhereAnd()
    {
        if (this.where && this.expr)  {
            append(" and ");
            this.expr = false;
        }
        return this;
    }

    /**
     * Append an <code>or</code> to this statement.
     */
    public void appendWhereOr()
    {
        if (this.where && this.expr)  {
            append(" or ");
            this.expr = false;
        }
    }

    /**
     * Append an <code>where</code> to this statement.
     * @return this
     */
    public CompleteStatement appendWhere()
    {
        if (!this.where)  {
            append(" where ");
            this.where = true;
        }
        this.expr = false;
        return this;
    }


    /**
     * Append an <code>where</code> to this statement.
     * @param _where where
     * @return this
     */
    public CompleteStatement appendWhere(final Object _where)
    {
        if (!this.where)  {
            append(" where ");
            this.where = true;
        }
        append(_where);
        this.expr = true;
        return this;
    }

    /**
     * Append an <code>from</code> to this statement.
     * @param _from from
     * @return this
     */
    public CompleteStatement appendFrom(final String _from)
    {
        if (!this.from)  {
            append(" from ");
            this.from = true;
        } else  {
            append(",");
        }
        append(_from);
        return this;
    }

    /**
     * Append an <code>text</code> to this statement.
     * @param _text text
     * @return this
     */
    public CompleteStatement append(final Object _text)
    {
        getStatement().append(_text);
        return this;
    }

    /**
     * This is the getter method for instance variable
     * {@link #selectStatement}.
     *
     * @return value of instance variable {@link #selectStatement}
     * @see #selectStatement
     */
    public StringBuilder getStatement()
    {
        return this.statement;
    }

    /**
     * Getter method for the instance variable {@link #where}.
     *
     * @return value of instance variable {@link #where}
     */
    public boolean isWhere()
    {
        return this.where;
    }

    /**
     * Return this statement.
     * @return this
     */
    @Override()
    public String toString()
    {
        return this.statement.toString();
    }
}
