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



/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Or
    extends And
{
    /**
     * Constructor setting the parts of this OR.
     * @param _parts parts for this and
     */
    public Or(final AbstractPart... _parts)
    {
       super(_parts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPart appendSQL(final StringBuilder _sql)
    {
        _sql.append("(");
        boolean first = true;
        for (final AbstractPart part : getParts()) {
            if (first) {
                first = false;
            } else {
                _sql.append(" OR ");
            }
            part.appendSQL(_sql);
        }
        _sql.append(")");
        return this;
    }
}
