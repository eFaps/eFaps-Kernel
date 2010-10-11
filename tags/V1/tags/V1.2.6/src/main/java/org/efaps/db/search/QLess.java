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

import org.efaps.util.EFapsException;



/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QLess
    extends QAbstractAttrCompare
{

    /**
     * Constructor setting attribute and value.
     * @param _attribute Attribute to be checked for greater
     * @param _value     value as criteria
     */
    public QLess(final QAttribute _attribute,
                   final QAbstractValue _value)
    {
        super(_attribute, _value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QLess appendSQL(final StringBuilder _sql)
        throws EFapsException
    {
        getAttribute().appendSQL(_sql);
        _sql.append(" < ");
        getValue().appendSQL(_sql);
        return this;
    }
}
