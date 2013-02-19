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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.db.query.CachedResult;
import org.efaps.util.EFapsException;

/**
 * Basic class for AttributeSets.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AttributeSet
    extends Type
{

    /**
     * Type of the attribute.
     */
    private final AttributeType attributeType;

    /**
     * Name of the attribute.
     */
    private final String attributeName;

    /**
     * attributes of this set.
     */
    private final Set<String> setAttributes = new HashSet<String>();

    /**
     * @param _id               id of this set
     * @param _type             type of his set
     * @param _name             name of this set
     * @param _attributeType    type of the attribute
     * @param _sqlColNames      name of the sql column
     * @param _tableId          id of the table
     * @param _typeLinkId       id of the type link
     * @param _uuid             UUID of this AttributeSet as String
     * @throws EFapsException on error
     */
    //CHECKSTYLE:OFF
    protected AttributeSet(final long _id,
                           final Type _type,
                           final String _name,
                           final AttributeType _attributeType,
                           final String _sqlColNames,
                           final long _tableId,
                           final long _typeLinkId,
                           final String _uuid)
        throws EFapsException
    {
        //CHECKSTYLE:ON
        super(_id, _uuid, AttributeSet.evaluateName(_type.getName(), _name));

        this.attributeName = (_name == null) ? null : _name.trim();

        Type.cacheType(this);
        readFromDB4Properties();

        this.attributeType = _attributeType;

        final Attribute attr = new Attribute(_id, _name, _sqlColNames, SQLTable.get(_tableId), AttributeType
                        .get("Link"), null, null);
        attr.setParent(this);
        addAttribute(attr, false);

        attr.setLink(_type);
        _type.addLink(attr);

        if (_typeLinkId > 0) {
            final Type parent = Type.get(_typeLinkId);
            setParentType(parent);
            parent.addChildType(this);
            getAttributes().putAll(parent.getAttributes());
        }
    }

    /**
     * Getter method for instance variable {@link #attributeType}.
     *
     * @return value of instance variable {@link #attributeType}
     */
    public AttributeType getAttributeType()
    {
        return this.attributeType;
    }

    /**
     * Method to read value from the database.
     *
     * @param _rs               cached result
     * @param _index2expression map index to expression
     * @return read values from the database
     * @throws EFapsException on error
     */
    public Map<String, List<Object>> readValues(final CachedResult _rs,
                                                final Map<Integer, String> _index2expression)
        throws EFapsException
    {
        return ((IMultipleAttributeType) this.attributeType.getDbAttrType()).readValues(_rs, _index2expression);
    }

    /**
     * This is the getter method for instance variable {@link #sqlColNames}.
     *
     * @return value of instance variable {@link #sqlColNames}
     * @see #sqlColNames
     */
    public List<String> getSqlColNames()
    {
        return getAttribute(this.attributeName).getSqlColNames();
    }

    /**
     * Getter method for instance variable {@link #attributeName}.
     *
     * @return value of instance variable {@link #attributeName}
     */
    public String getAttributeName()
    {
        return this.attributeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addAttribute(final Attribute _attribute,
                                final boolean _inherited)
    {
        super.addAttribute(_attribute, _inherited);
        // in the superconstructur this method is called, so the set might not
        // be initialised
        if (this.setAttributes != null) {
            this.setAttributes.add(_attribute.getName());
        }
    }

    /**
     * Getter method for instance variable {@link #setAttributes}.
     *
     * @return value of instance variable {@link #setAttributes}
     */
    public Set<String> getSetAttributes()
    {
        return this.setAttributes;
    }

    /**
     * Evaluate the name. (Build the name as the set is cached).
     *
     * @param _typeName name of the type
     * @param _name name of the attribute
     * @return String
     */
    public static String evaluateName(final String _typeName,
                                      final String _name)
    {
        final StringBuilder ret = new StringBuilder();
        ret.append(_typeName).append(":").append(_name).toString();
        return ret.toString();
    }

    /**
     * Method to get the type from the cache.
     *
     * @param _typeName name of the type
     * @param _name name of the attribute
     * @return AttributeSet
     */
    public static AttributeSet get(final String _typeName,
                                   final String _name)
    {
        return (AttributeSet) Type.get(AttributeSet.evaluateName(_typeName, _name));
    }

    /**
     * Method to get the type from the cache. Searches if not found in the type
     * hierarchy.
     *
     * @param _typeName name of the type
     * @param _name name of the attribute
     * @return AttributeSet
     */
    public static AttributeSet find(final String _typeName,
                                    final String _name)
    {
        AttributeSet ret = (AttributeSet) Type.get(AttributeSet.evaluateName(_typeName, _name));
        if (ret == null) {
            if (Type.get(_typeName).getParentType() != null) {
                ret = AttributeSet.find(Type.get(_typeName).getParentType().getName(), _name);
            }
        }
        return ret;
    }
}
