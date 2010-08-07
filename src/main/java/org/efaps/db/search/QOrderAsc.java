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
public class QOrderAsc
    extends AbstractQPart
{
    /**
     * The attribute used for this order element.
     */
    private final QAttribute attribute;

    /**
     * @param _attribute QAttribute
     */
    public QOrderAsc(final QAttribute _attribute)
    {
        this.attribute = _attribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractQPart appendSQL(final StringBuilder _sql)
        throws EFapsException
    {
        getAttribute().appendSQL(_sql);
        _sql.append(" asc ");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractQPart prepare(final AbstractObjectQuery<?> _query,
                                 final AbstractQPart _part)
        throws EFapsException
    {
        getAttribute().prepare(_query, this);
        return this;
    }

    /**
     * Getter method for the instance variable {@link #attribute}.
     *
     * @return value of instance variable {@link #attribute}
     */
    public QAttribute getAttribute()
    {
        return this.attribute;
    }
}
