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

import org.efaps.db.AbstractObjectQuery;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class QAbstractPart
{

    /**
     * Method adds the sql statement parts to the given StringBuilder.
     *
     * @param _sql StringBuilder to append to
     * @return this AbstractPart
     * @throws EFapsException on any error
     */
    public abstract QAbstractPart appendSQL(final StringBuilder _sql)
        throws EFapsException;

    /**
     * Method is executed to prepare the different parts for execution
     * of the sql statement.
     * @param _query    query the part belong to
     * @param _part     Part this part is nested in
     * @return this AbstractPart
     * @throws EFapsException on any error
     */
    public abstract QAbstractPart prepare(final AbstractObjectQuery<?> _query,
                                          final QAbstractPart _part)
        throws EFapsException;
}