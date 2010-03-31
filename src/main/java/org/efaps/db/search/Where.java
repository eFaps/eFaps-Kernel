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

import org.efaps.db.InstanceQuery;
import org.efaps.util.EFapsException;



/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Where
{

    /**
     * Base part for this Where.
     */
    private AbstractPart part;

    /**
     * @param _part part for this where
     */
    public Where(final AbstractPart _part)
    {
        this.part = _part;
    }

    /**
     * Getter method for the instance variable {@link #part}.
     *
     * @return value of instance variable {@link #part}
     */
    public AbstractPart getPart()
    {
        return this.part;
    }

    /**
     * Setter method for instance variable {@link #part}.
     *
     * @param _part value for instance variable {@link #part}
     */

    public void setPart(final AbstractPart _part)
    {
        this.part = _part;
    }

    /**
     * @return the sql statement for this where
     * @throws EFapsException on error
     */
    public StringBuilder getSQL()
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        ret.append(" where ");
        this.part.appendSQL(ret);
        return ret;
    }

    /**
     * @param _query Query this Where belongs to
     * @throws EFapsException on error
     */
    public void prepare(final InstanceQuery _query)
        throws EFapsException
    {
        this.part.prepare(_query, null);
    }
}
