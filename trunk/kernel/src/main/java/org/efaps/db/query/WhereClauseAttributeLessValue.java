/*
 * Copyright 2003 - 2009 The eFaps Team
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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.AbstractQuery;
import org.efaps.util.EFapsException;

/**
 * The class represents an less where clause between an attributes and a
 * value.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class WhereClauseAttributeLessValue extends WhereClauseAttributeCompareValueAbstract
{
    /**
     * Constructor.
     *
     * @param _query    query for this whereclause
     * @param _attr     attribute for this whereclause
     * @param _value    value used for this whereclause
     */
    public WhereClauseAttributeLessValue(final AbstractQuery _query, final Attribute _attr, final String _value)
    {
        super(_query, _attr, _value);
    }

    /**
     * {@inheritDoc}
     */
    public void appendWhereClause(final CompleteStatement _completeStatement, final int _orderIndex)
        throws EFapsException
    {
        super.appendWhereClause(_completeStatement, _orderIndex, "<");
    }
}
