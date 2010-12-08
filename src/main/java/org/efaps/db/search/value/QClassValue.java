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

import org.efaps.admin.datamodel.Classification;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * Used as the Value in the {@link org.efaps.db.search.compare.QClassEqual} part.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QClassValue
    extends AbstractQValue
{

    /**
     * Classification of this Value.
     */
    private final Classification clazz;

    /**
     * @param _clazz Classification
     */
    public QClassValue(final Classification _clazz)
    {
        this.clazz = _clazz;
    }

    /**
     * Getter method for the instance variable {@link #clazz}.
     *
     * @return value of instance variable {@link #clazz}
     */
    public Classification getClassification()
    {
        return this.clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QClassValue appendSQL(final SQLSelect _sql)
        throws EFapsException
    {
        _sql.addValuePart(this.clazz.getId());
        return this;
    }
}
