/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.search.section;

import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;


/**
 * Represent a section of a SQL Statement. Sections are: <br/>
 * SELECT, WHERE, ORDER BY
 *
 * @author The eFaps Team
 *
 */
public abstract class AbstractQSection
{
    /**
     * Append to the SQLSelect.
     * @param _select SQLSelect to be appended to
     * @return this
     * @throws EFapsException on error
     *
     */
    public abstract AbstractQSection appendSQL(SQLSelect _select)
        throws EFapsException;

    /**
     * Prepare this section.
     * @param _query Query this AbstractQSection belongs to
     * @throws EFapsException on error
     * @return this
     */
    public abstract AbstractQSection prepare(AbstractObjectQuery<?> _query)
        throws EFapsException;

}
