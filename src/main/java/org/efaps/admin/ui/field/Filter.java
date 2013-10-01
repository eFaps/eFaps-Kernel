/*
 * Copyright 2003 - 2013 The eFaps Team
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


package org.efaps.admin.ui.field;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Filter definition for a field inside a table.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Filter
    implements Serializable
{

    /**
     * filtertype.
     */
    public enum Type
    {
        /** Filter for classification. */
        CLASSIFICATION,
        /** Filter for freetext. */
        FREETEXT,
        /** No filter. */
        NONE,
        /** Filter presenting a picker list. */
        PICKLIST,
        /** Filter for Status Attributes. */
        STATUS;
    }

    /**
     * Filterbase.
     */
    public enum Base
    {
        /** Filter work on memory base. */
        MEMORY,
        /** Filter work against the data base. */
        DATABASE;
    }

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Field.class);

    /**
     * Is this filter required.
     */
    private boolean required = false;

    /**
     * Set the default value for a filter.
     */
    private String defaultValue;

    /**
     * String containing the attributes to be used for the filter. It may
     * contain up to two attributes separated by a comma. This allows to filter
     * by an attribute and display a phrase.
     */
    private String attributes;

    /**
     * Type of this filter (NONE is the default value).
     */
    private Type type = Type.NONE;

    /**
     * Base of this filter (Memorbased is the default value).
     */
    private Base base = Base.MEMORY;

        /**
     * Getter method for the instance variable {@link #base}.
     *
     * @return value of instance variable {@link #base}
     */
    public Base getBase()
    {
        return this.base;
    }

    /**
     * Setter method for instance variable {@link #base}.
     *
     * @param _baseName value for instance variable {@link #base}
     */
    protected void evalBase(final String _baseName)
    {
        try {
            final Base baseTmp = Base.valueOf(_baseName.toUpperCase());
            if (baseTmp != null) {
                this.base = baseTmp;
            }
        } catch (final IllegalArgumentException e) {
            Filter.LOG.error("Invalid value '{}' for FilterBase", _baseName);
        }
        activate();
    }

    /**
     * Getter method for the instance variable {@link #type}.
     *
     * @return value of instance variable {@link #type}
     */
    public Type getType()
    {
        return this.type;
    }

    /**
     * Setter method for instance variable {@link #type}.
     *
     * @param _typeName value for instance variable {@link #type}
     */
    protected void evalType(final String _typeName)
    {
        try {
            final Type typTmp = Type.valueOf(_typeName.toUpperCase());
            if (typTmp != null) {
                this.type = typTmp;
            }
        } catch (final IllegalArgumentException e) {
            Filter.LOG.error("Invalid value '{}' for FilterType", _typeName);
        }
    }

    /**
     * Getter method for the instance variable {@link #defaultValue}.
     *
     * @return value of instance variable {@link #defaultValue}
     */
    public String getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * Setter method for instance variable {@link #defaultValue}.
     *
     * @param _defaultValue value for instance variable {@link #defaultValue}
     */
    protected void setDefaultValue(final String _defaultValue)
    {
        this.defaultValue = _defaultValue;
        activate();
    }

    /**
     * Getter method for the instance variable {@link #attributes}.
     *
     * @return value of instance variable {@link #attributes}
     */
    public String getAttributes()
    {
        return this.attributes;
    }

    /**
     * Setter method for instance variable {@link #attributes}.
     *
     * @param _attributes value for instance variable {@link #attributes}
     */
    protected void setAttributes(final String _attributes)
    {
        this.attributes = _attributes;
        activate();
    }

    /**
     * Getter method for the instance variable {@link #required}.
     *
     * @return value of instance variable {@link #required}
     */
    public boolean isRequired()
    {
        return this.required;
    }

    /**
     * Setter method for instance variable {@link #required}.
     *
     * @param _required value for instance variable {@link #required}
     */
    protected void setRequired(final boolean _required)
    {
        this.required = _required;
        activate();
    }

    /**
     * Activate the filter.
     */
    private void activate()
    {
        if (this.type.equals(Type.NONE)) {
            this.type = Type.PICKLIST;
        }
    }
}
