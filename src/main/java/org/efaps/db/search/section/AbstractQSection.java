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


package org.efaps.db.search.section;

import org.efaps.db.AbstractObjectQuery;
import org.efaps.util.EFapsException;


/**
 * Represent a section of a SQL Statement. Sections are: <br/>
 * SELECT, WHERE, ORDER BY
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractQSection
{
    /**
     * Get the Sql section as a StringBuilder.
     * @return the sql statement for this AbstractQSection
     * @throws EFapsException on error
     */
    public abstract StringBuilder getSQL()
        throws EFapsException;

    /**
     * Prepare this section.
     * @param _query Query this AbstractQSection belongs to
     * @throws EFapsException on error
     */
    public abstract void prepare(final AbstractObjectQuery<?> _query)
        throws EFapsException;

}
