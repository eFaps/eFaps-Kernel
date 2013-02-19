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


package org.efaps.db.search;

import java.util.ArrayList;
import java.util.List;

import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;


/**
 * Represents a SQL "AND".
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QAnd
    extends AbstractQPart
{
    /**
     * List of parts that will be connected by "AND".
     */
    private final List<AbstractQPart> parts = new ArrayList<AbstractQPart>();


    /**
     * Constructor setting the parts of this AND.
     * @param _parts parts for this and
     */
    public QAnd(final AbstractQPart... _parts)
    {
        for (final AbstractQPart part : _parts) {
            this.parts.add(part);
        }
    }

    /**
     * Add a part to be included in the and.
     * @param _part part to be include
     * @return this
     */
    public AbstractQPart addPart(final AbstractQPart _part)
    {
        this.parts.add(_part);
        return this;
    }

    /**
     * Getter method for the instance variable {@link #parts}.
     *
     * @return value of instance variable {@link #parts}
     */
    protected List<AbstractQPart> getParts()
    {
        return this.parts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QAnd appendSQL(final SQLSelect _sql)
        throws EFapsException
    {
        _sql.addPart(SQLPart.PARENTHESIS_OPEN);
        boolean first = true;
        for (final AbstractQPart part : this.parts) {
            if (first) {
                first = false;
            } else {
                _sql.addPart(SQLPart.AND);
            }
            part.appendSQL(_sql);
        }
        _sql.addPart(SQLPart.PARENTHESIS_CLOSE);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QAnd prepare(final AbstractObjectQuery<?> _query,
                                 final AbstractQPart _part)
        throws EFapsException
    {
        for (final AbstractQPart part : this.parts) {
            part.prepare(_query, this);
        }
        return this;
    }
}
