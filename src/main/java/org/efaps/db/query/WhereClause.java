/*
 * Copyright 2003 - 2012 The eFaps Team
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

import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
//CHECKSTYLE:OFF
@Deprecated
public interface WhereClause
{
    /**
     * Append the special part to the where clause.
     * @param _completeStatement complete statement
     * @param _orderIndex        index
     * @throws EFapsException on error while accessing the context
     * @return this
     */
    WhereClause appendWhereClause(final CompleteStatement _completeStatement,
                                  int _orderIndex)
        throws EFapsException;

    /**
     * Is the where clause set to ignore case.
     * @return true if ignore case, else false
     */
    boolean isIgnoreCase();

    /**
     * Set the where clause to ignore case.
     * @param _ignoreCase value to set
     * @return this
     */
    WhereClause setIgnoreCase(boolean _ignoreCase);


    /**
     * Is this where clause an or.
     * @return true if this wherecluse is an or
     */
    boolean isOr();

    /**
     * Set this WhereClause tro be an or.
     * @param _or Or
     * @return this
     */
    WhereClause setOr(boolean _or);
}
