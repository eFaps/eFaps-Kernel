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


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractAttrCompare
    extends AbstractPart
{
    /**
     * The attribute used for this equal.
     */
    private final QueryAttribute attribute;


    /**
     * The value the attribute will be compared to.
     */
    private final AbstractValue value;


    /**
     * Constructor setting attribute and value.
     * @param _attribute Attribute to be checked for equal
     * @param _value     value as criteria
     */
    public AbstractAttrCompare(final QueryAttribute _attribute,
                               final AbstractValue _value)
    {
        this.attribute = _attribute;
        this.value = _value;
    }

    /**
     * Getter method for the instance variable {@link #attribute}.
     *
     * @return value of instance variable {@link #attribute}
     */
    public QueryAttribute getAttribute()
    {
        return this.attribute;
    }

    /**
     * Getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    public AbstractValue getValue()
    {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPart prepare(final InstanceQuery _query)
    {
        this.attribute.prepare(_query);
        this.value.prepare(_query);
        return this;
    }
}
