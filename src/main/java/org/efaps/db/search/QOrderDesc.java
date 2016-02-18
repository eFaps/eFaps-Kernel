/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */


package org.efaps.db.search;

import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class QOrderDesc
    extends QOrderAsc
{

    /**
     * @param _attribute QAttribute
     */
    public QOrderDesc(final QAttribute _attribute)
    {
        super(_attribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractQPart appendSQL(final SQLSelect _sql)
        throws EFapsException
    {
        getAttribute().appendSQL(_sql);
        _sql.addPart(SQLPart.DESC);
        return this;
    }
}
