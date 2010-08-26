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


package org.efaps.db.search.value;

import org.efaps.db.wrapper.SQLSelect;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QNullValue
    extends AbstractQValue
{
    /**
     * {@inheritDoc}
     */
    @Override
    public QNullValue appendSQL(final SQLSelect _sql)
    {
        _sql.addPart(SQLSelect.SQLPart.NULL);
        return this;
    }
}
