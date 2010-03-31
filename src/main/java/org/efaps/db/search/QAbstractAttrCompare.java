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

import org.efaps.db.InstanceQuery;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class QAbstractAttrCompare
    extends QAbstractPart
{
    /**
     * The attribute used for this equal.
     */
    private final QAttribute attribute;

    /**
     * The value the attribute will be compared to.
     */
    private final QAbstractValue value;

    /**
     * Must this compare ignore the case.
     */
    private boolean ignoreCase;

    /**
     * Constructor setting attribute and value.
     * @param _attribute Attribute to be checked for equal
     * @param _value     value as criteria
     */
    public QAbstractAttrCompare(final QAttribute _attribute,
                               final QAbstractValue _value)
    {
        this.attribute = _attribute;
        this.value = _value;
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

    /**
     * Getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    public QAbstractValue getValue()
    {
        return this.value;
    }

    /**
     * Getter method for the instance variable {@link #ignoreCase}.
     *
     * @return value of instance variable {@link #ignoreCase}
     */
    public boolean isIgnoreCase()
    {
        return this.ignoreCase;
    }

    /**
     * Setter method for instance variable {@link #ignoreCase}.
     *
     * @param _ignoreCase value for instance variable {@link #ignoreCase}
     * @return this
     */
    public QAbstractAttrCompare setIgnoreCase(final boolean _ignoreCase)
    {
        this.ignoreCase = _ignoreCase;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QAbstractPart prepare(final InstanceQuery _query,
                                final QAbstractPart _part)
        throws EFapsException
    {
        this.attribute.prepare(_query, this);
        this.value.prepare(_query, this);
        return this;
    }
}
