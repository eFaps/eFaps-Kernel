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

package org.efaps.admin.datamodel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.ui.Form;
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
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

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
    private Long parent = null;

    /**
     * Can multiple Classifications be selected.
     */
    private boolean multipleSelect = true;

    /**
     * Instance variable for all child classification of this classification.
     */
    private final Set<Long> children = new HashSet<Long>();

    /**
     * Type this Classification is classifying.
     */
    private Long classifiesType;

    /**
     * Relation belonging to the type this Classification is classifying.
     */
    private Long classifyRelation;

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
     * @throws CacheReloadException on error
     */
    public Classification getParentClassification()
        throws CacheReloadException
    {
        return isRoot() ? null : Classification.get(this.parent);
    }

    /**
     * Setter method for instance variable {@link #parent}.
     *
     * @param _parentClassification value for instance variable {@link #parent}
     */
    protected void setParentClassification(final Long _parentClassification)
    {
        this.parent = _parentClassification;
        setDirty();
    }

    /**
     * Getter method for instance variable {@link #childs}.
     *
     * @return value of instance variable {@link #childs}
     */
    public Set<Classification> getChildClassifications()
        throws CacheReloadException
    {
        final Set<Classification> ret = new HashSet<Classification>();
        for (final Long id : this.children) {
            final Classification child = Classification.get(id);
            ret.add(child);
        }
        return Collections.unmodifiableSet(ret);
    }

    /**
     * Getter method for the instance variable {@link #children}.
     *
     * @return value of instance variable {@link #children}
     */
    protected Set<Long> getChildren()
    {
        return this.children;
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
        throws CacheReloadException
    {
        final Type ret;
        if (this.classifiesType == null && this.parent != null) {
            ret = getParentClassification().getClassifiesType();
        } else {
            ret = Type.get(this.classifiesType);
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
        throws CacheReloadException
    {
        final Type ret;
        if (this.classifyRelation == null && this.parent != null) {
            ret = getParentClassification().getClassifyRelationType();
        } else {
            ret = Type.get(this.classifyRelation);
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
    public String getRelLinkAttributeName() throws CacheReloadException
    {
        final String ret;
        if (this.relLinkAttributeName == null && this.parent != null) {
            ret = getParentClassification().getRelLinkAttributeName();
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
    public String getRelTypeAttributeName() throws CacheReloadException
    {
        final String ret;
        if (this.relTypeAttributeName == null && this.parent != null) {
            ret = getParentClassification().getRelTypeAttributeName();
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
        throws CacheReloadException
    {
        final boolean ret;
        if (isRoot()) {
            ret = this.multipleSelect;
        } else {
            ret = getParentClassification().isMultipleSelect();
        }
        return ret;
    }

    /**
     * Check if the root classification of this classification is assigned to
     * the given company.
     *
     * @see #companies
     * @param _company copmany that will be checked for assignment
     * @return true it the root classification of this classification is
     *         assigned to the given company, else
     */
    public boolean isAssigendTo(final Company _company)
        throws CacheReloadException
    {
        final boolean ret;
        if (isRoot()) {
            ret = this.companies.isEmpty() ? true : this.companies.contains(_company);
        } else {
            ret = getParentClassification().isAssigendTo(_company);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Form getTypeForm()
        throws EFapsException
    {
        Form ret = super.getTypeForm();
        if (ret == null && getParentClassification() != null) {
            ret = getParentClassification().getTypeForm();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLinkProperty(final UUID _linkTypeUUID,
                                   final long _toId,
                                   final UUID _toTypeUUID,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkTypeUUID.equals(CIAdminDataModel.TypeClassifies.uuid)) {
            final Type type = Type.get(_toId);
            this.classifiesType = type.getId();
            type.addClassifiedByType(this);
        } else if (_linkTypeUUID.equals(CIAdminDataModel.TypeClassifyRelation.uuid)) {
            this.classifyRelation = Type.get(_toId).getId();
        } else if (_linkTypeUUID.equals(CIAdminDataModel.TypeClassifyCompany.uuid)) {
            this.companies.add(Company.get(_toId));
        }
        super.setLinkProperty(_linkTypeUUID, _toId, _toTypeUUID, _toName);
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
        }
        super.setProperty(_name, _value);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class {@link Classification}
     * .
     *
     * @param _id id of the type to get
     * @return instance of class {@link Classification}
     * @throws CacheReloadException on error
     */
    public static Classification get(final long _id)
        throws CacheReloadException
    {
        return (Classification) Type.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class {@link Classification}
     * .
     *
     * @param _name name of the type to get
     * @return instance of class {@link Classification}
     * @throws CacheReloadException on error
     */
    public static Classification get(final String _name)
        throws CacheReloadException
    {
        return (Classification) Type.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class {@link Classification}
     * .
     *
     * @param _uuid UUID of the type to get
     * @return instance of class {@link Classification}
     * @throws CacheReloadException on error
     */
    public static Classification get(final UUID _uuid)
        throws CacheReloadException
    {
        return (Classification) Type.get(_uuid);
    }

}
