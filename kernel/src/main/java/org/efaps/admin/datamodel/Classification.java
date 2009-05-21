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

package org.efaps.admin.datamodel;

import java.util.HashSet;
import java.util.Set;

import org.efaps.admin.EFapsClassNames;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Classification extends Type
{

    public enum Keys {
        TYPE("type"),
        RELTYPE ("relType"),
        RELLINKATTR ("relLinkAttribute"),
        RELTYPEATTR ("relTypeAttribute"),
        LINKATTR ("classLinkAttribute");

        private final String value;

        private Keys(final String _value) {
          this.value = _value;
        }

        /**
         * Getter method for instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */
        public String getValue()
        {
            return this.value;
        }
    }



    /**
     * Instance variable for the parent classification  this type is child from.
     */
    private Classification parent = null;

    /**
     * Instance variable for all child classification of this type.
     */
    private final Set<Classification> childs = new HashSet<Classification>();

    private Type classifiesType;

    private Type classifyRelation;

    private String linkAttribute;

    private String relLinkAttribute;

    private String relTypeAttribute;


    /**
     * @param _id
     * @param _uuid
     * @param _name
     * @throws CacheReloadException
     */
    protected Classification(final long _id, final String _uuid, final String _name) throws CacheReloadException
    {
        super(_id, _uuid, _name);
    }



    /**
     * Getter method for instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public Type getParentClassification()
    {
        return this.parent;
    }

    /**
     * Setter method for instance variable {@link #parent}.
     *
     * @param _parentClassification value for instance variable {@link #parent}
     */
    protected void setParentClassification(final Classification _parentClassification)
    {
        this.parent = _parentClassification;
    }

    /**
     * Getter method for instance variable {@link #childs}.
     *
     * @return value of instance variable {@link #childs}
     */
    public Set<Classification> getChildClassifications()
    {
        return this.childs;
    }

    /**
     * Getter method for instance variable {@link #classifiesType}.
     *
     * @return value of instance variable {@link #classifiesType}
     */
    public Type getClassifiesType()
    {
        return this.classifiesType;
    }

    /**
     * Getter method for instance variable {@link #classifyRelation}.
     *
     * @return value of instance variable {@link #classifyRelation}
     */
    public Type getClassifyRelation()
    {
        return this.classifyRelation;
    }

    /**
     * Getter method for instance variable {@link #linkAttribute}.
     *
     * @return value of instance variable {@link #linkAttribute}
     */
    public String getLinkAttribute()
    {
        return this.linkAttribute;
    }



    /**
     * Getter method for instance variable {@link #relLinkAttribute}.
     *
     * @return value of instance variable {@link #relLinkAttribute}
     */
    public String getRelLinkAttribute()
    {
        return this.relLinkAttribute;
    }



    /**
     * Getter method for instance variable {@link #relTypeAttribute}.
     *
     * @return value of instance variable {@link #relTypeAttribute}
     */
    public String getRelTypeAttribute()
    {
        return this.relTypeAttribute;
    }



    /**
     *
     * Sets the link properties for this object.
     *
     * @param _linkType type of the link property
     * @param _toId to id
     * @param _toType to type
     * @param _toName to name
     * @throws EFapsException o error
     *
     */
    @Override
    protected void setLinkProperty(final EFapsClassNames _linkType, final long _toId, final EFapsClassNames _toType,
                    final String _toName) throws EFapsException
    {
        switch (_linkType) {
            case DATAMDOEL_TYPECLASSIFIES:
                this.classifiesType = get(_toId);
                break;
            case DATAMDOEL_TYPECLASSIFYRELATION:
                this.classifyRelation = get(_toId);
                break;
            default:
                super.setLinkProperty(_linkType, _toId, _toType, _toName);
        }
    }

    /**
     * The instance method sets a new property value.
     *
     * @param _name name of the property
     * @param _value value of the property
     * @see #addUniqueKey
     * @throws CacheReloadException on error
     */
    @Override
    protected void setProperty(final String _name, final String _value) throws CacheReloadException
    {
        if (_name.equals(Classification.Keys.LINKATTR.value)) {
            this.linkAttribute = _value;
        } else if (_name.equals(Classification.Keys.RELLINKATTR.value)) {
            this.relLinkAttribute = _value;
        } else if (_name.equals(Classification.Keys.RELTYPEATTR.value)) {
            this.relTypeAttribute = _value;
        } else {
            super.setProperty(_name, _value);
        }
    }


}
