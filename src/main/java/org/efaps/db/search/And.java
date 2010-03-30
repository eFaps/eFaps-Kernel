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
     * {@inheritDoc}
     */
    @Override
    public AbstractPart appendSQL(final StringBuilder _sql)
    {
        _sql.append("(");
        for (final AbstractPart part : this.parts) {
            part.appendSQL(_sql);
            if (!this.parts.isEmpty()) {
                _sql.append(" AND ");
            }
        }
        _sql.append(")");
        return this;
    }
}
