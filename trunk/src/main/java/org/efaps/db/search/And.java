/*
 * Copyright 2003 - 2010 The eFaps Team
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

import org.efaps.db.Query;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class And
    extends AbstractPart
{
    /**
     * List of parts that will be connected by "AND".
     */
    private final List<AbstractPart> parts = new ArrayList<AbstractPart>();


    /**
     * Constructor setting the parts of this AND.
     * @param _parts parts for this and
     */
    public And(final AbstractPart... _parts)
    {
        for (final AbstractPart part : _parts) {
            this.parts.add(part);
        }
    }

    /**
     * Add a part to be included in the and.
     * @param _part part to be include
     * @return this
     */
    public AbstractPart addPart(final AbstractPart _part)
    {
        this.parts.add(_part);
        return this;
    }

    /**
     * Getter method for the instance variable {@link #parts}.
     *
     * @return value of instance variable {@link #parts}
     */
    protected List<AbstractPart> getParts()
    {
        return this.parts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPart appendSQL(final StringBuilder _sql)
    {
        _sql.append("(");
        boolean first = true;
        for (final AbstractPart part : this.parts) {
            if (first) {
                first = false;
            } else {
                _sql.append(" AND ");
            }
            part.appendSQL(_sql);
        }
        _sql.append(")");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPart prepare(final Query _query)
    {
        for (final AbstractPart part : this.parts) {
            part.prepare(_query);
        }
        return this;
    }
}
