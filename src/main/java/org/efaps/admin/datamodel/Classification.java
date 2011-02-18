/*
 * Copyright 2003 - 2011 The eFaps Team
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

import org.efaps.admin.user.Company;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * Class extending type for classification purpose.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Classification
    extends Type
{

    /**
     * Enum contains the keys for the attributes.
     */
    public enum Keys {
        /** key to the type {@link Classification#multipleSelect}. */
        MULTI("multipleSelect"),
        /** key to the type {@link Classification#classifiesType}. */
        TYPE("type"),
        /** key to the relation type {@link Classification#classifyRelation}. */
        RELTYPE ("relType"),
        /** key to attribute of the relation type {@link Classification#relLinkAttributeName}. */
        RELLINKATTR ("relLinkAttribute"),
        /**  key to attribute of the relation type {@link Classification#relTypeAttributeName}. */
        RELTYPEATTR ("relTypeAttribute"),
        /** key to attribute of the type {@link Classification#linkAttributeName}. */
        LINKATTR ("classLinkAttribute");

        /** value. */
        private final String value;

        /**
         * Private constructor setting the instance variable.
         * @param _value  value
         */
        private Keys(final String _value)
        {
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
     * Instance variable for the parent classification this classification is
     * child of.
     */
    private Classification parent = null;

    /**
     * Can multiple Classifications be selected.
     */
    private boolean multipleSelect = true;

    /**
     * Instance variable for all child classification of this classification.
     */
    private final Set<Classification> childs = new HashSet<Classification>();

    /**
     * Type this Classification is classifying.
     */
    private Type classifiesType;

    /**
     * Relation belonging to the type this Classification is classifying.
     */
    private Type classifyRelation;

    /**
     * Name of the Attribute of the Relation {@link #classifyRelation} that
     * links the Relation to the type {@link #classifiesType} that is classified.
     */
    private String relLinkAttributeName;

    /**
     * Name of the Attribute of the Relation {@link #classifyRelation} that
     * contains the ids of the classifications that are classifying the type
     *  {@link #classifiesType}.
     */
    private String relTypeAttributeName;

    /**
     * Name of the Attribute that links this Classification to the type it
     * classifies.
     */
    private String linkAttributeName;

    /**
     * Companies this Classification is assigned to.
     */
    private final Set<Company> companies = new HashSet<Company>();

    /**
     * @param _id       id of this Classification
     * @param _uuid     uuid of this Classification
     * @param _name     name of this Classification
     * @throws CacheReloadException on error
     */
    protected Classification(final long _id,
                             final String _uuid,
                             final String _name)
        throws CacheReloadException
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
     * Getter method for instance variable {@link #linkAttributeName}.
     *
     * @return value of instance variable {@link #linkAttributeName}
     */
    public String getLinkAttributeName()
    {
        return this.linkAttributeName;
    }

    /**
     * Getter method for instance variable {@link #classifiesType}. If the
     * variable is null the value for this instance variable of the parent
     * classification will be returned.
     *
     * @return value of instance variable {@link #classifiesType}
     */
    public Type getClassifiesType()
    {
        final Type ret;
        if (this.classifiesType == null && this.parent != null) {
            ret = this.parent.getClassifiesType();
        } else {
            ret = this.classifiesType;
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #classifyRelation}. If the
     * variable is null the value for this instance variable of the parent
     * classification will be returned.
     *
     * @return value of instance variable {@link #classifyRelation}
     */
    public Type getClassifyRelationType()
    {
        final Type ret;
        if (this.classifyRelation == null && this.parent != null) {
            ret = this.parent.getClassifyRelationType();
        } else {
            ret = this.classifyRelation;
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #relLinkAttributeName}. If the
     * variable is null the value for this instance variable of the parent
     * classification will be returned.
     *
     * @return value of instance variable {@link #relLinkAttributeName}
     */
    public String getRelLinkAttributeName()
    {
        final String ret;
        if (this.relLinkAttributeName == null && this.parent != null) {
            ret = this.parent.getRelLinkAttributeName();
        } else {
            ret = this.relLinkAttributeName;
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #relTypeAttributeName}. If the
     * variable is null the value for this instance variable of the parent
     * classification will be returned.
     * @return value of instance variable {@link #relTypeAttributeName}
     */
    public String getRelTypeAttributeName()
    {
        final String ret;
        if (this.relTypeAttributeName == null && this.parent != null) {
            ret = this.parent.getRelTypeAttributeName();
        } else {
            ret = this.relTypeAttributeName;
        }
        return ret;
    }

    /**
     * Is this Classification the root Classification.
     * (Meaning that it does not have a parent).
     * @return true if root classification
     */
    public boolean isRoot()
    {
        return this.parent == null;
    }

    /**
     * Getter method for the instance variable {@link #multipleSelect}.
     *
     * @return value of instance variable {@link #multipleSelect}
     */
    public boolean isMultipleSelect()
    {
        final boolean ret;
        if (isRoot()) {
            ret = this.multipleSelect;
        } else {
            ret = this.parent.isMultipleSelect();
        }
        return ret;
    }

    /**
     * Check if the root classification of this classification
     * is assigned to the given company.
     * @see #companies
     * @param _company  copmany that will be checked for assignment
     * @return true it the root classification of this classification
     *          is assigned to the given company, else
     */
    public boolean isAssigendTo(final Company _company)
    {
        final boolean ret;
        if (isRoot()) {
            ret = this.companies.isEmpty() ? true : this.companies.contains(_company);
        } else {
            ret = this.parent.isAssigendTo(_company);
        }
        return ret;
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
    protected void setLinkProperty(final Type _linkType,
                                   final long _toId,
                                   final Type _toType,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkType.isKindOf(CIAdminDataModel.TypeClassifies.getType())) {
            this.classifiesType = Type.get(_toId);
        } else if (_linkType.isKindOf(CIAdminDataModel.TypeClassifyRelation.getType())) {
            this.classifyRelation = Type.get(_toId);
        } else if (_linkType.isKindOf(CIAdminDataModel.TypeClassifyCompany.getType())) {
            this.companies.add(Company.get(_toId));
        } else {
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
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        if (_name.equals(Classification.Keys.LINKATTR.value)) {
            this.linkAttributeName = _value;
        } else if (_name.equals(Classification.Keys.RELLINKATTR.value)) {
            this.relLinkAttributeName = _value;
        } else if (_name.equals(Classification.Keys.RELTYPEATTR.value)) {
            this.relTypeAttributeName = _value;
        } else if (_name.equals(Classification.Keys.MULTI.value)) {
            this.multipleSelect = !"FALSE".equalsIgnoreCase(_value);
        } else {
            super.setProperty(_name, _value);
        }
    }
}
