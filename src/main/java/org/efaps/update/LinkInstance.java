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

package org.efaps.update;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 *
 */
public class LinkInstance
{

    private final Map<String, String> valuesMap = new HashMap<String, String>();

    private int order = 0;;

    private Long childId;

    private Long id;

    private String oid;

    private boolean update = false;

    private boolean insert = false;

    /**
     * Map of Names of a attribute to the values of this attribute.
     */
    private final Map<String, String> keyAttr2Value = new HashMap<String, String>();

    /**
     * Constructor not setting nothing. Careful the keyAttrputs must be set!
     */
    public LinkInstance()
    {
    }

    /**
     * Constructor setting the only keyfield to "Name" and the value of this
     * keyfield to the given parameter.
     * @param _name key for the keyfield "Name"
     */
    public LinkInstance(final String _name)
    {
        this.keyAttr2Value.put("Name", _name);
    }

    /**
     * Constructor setting the only keyfield to "Name" and the value of this
     * keyfield to the given parameter.
     * @param _name key for the keyfield "Name"
     * @param _values valuue
     */
    public LinkInstance(final String _name, final String... _values)
    {
        this.keyAttr2Value.put("Name", _name);
        for (int i = 0; i < _values.length; i += 2) {
            this.valuesMap.put(_values[i], _values[i + 1]);
        }
    }

    /**
     * Getter method for instance variable {@link #keyAttr2Value}.
     *
     * @return value of instance variable {@link #keyAttr2Value}
     */
    public Map<String, String> getKeyAttr2Value()
    {
        return this.keyAttr2Value;
    }

    /**
     * This is the getter method for the instance variable {@link #order}.
     *
     * @return value of instance variable {@link #order}
     */
    public int getOrder()
    {
        return this.order;
    }

    /**
     * This is the setter method for the instance variable {@link #order}.
     *
     * @param _order the order to set
     */
    public void setOrder(final int _order)
    {
        this.order = _order;
    }

    /**
     * This is the getter method for the instance variable {@link #valuesMap}.
     *
     * @return value of instance variable {@link #valuesMap}
     */
    public Map<String, String> getValuesMap()
    {
        return this.valuesMap;
    }

    /**
     * @param _values values to set
     */
    public void setValues(final Map<String, String> _values)
    {
        this.valuesMap.clear();
        this.valuesMap.putAll(_values);
    }

    /**
     * This is the setter method for the instance variable {@link #childId}.
     *
     * @param _childid the _id to set
     */
    public void setChildId(final Long _childid)
    {
        this.childId = _childid;
    }

    /**
     * This is the getter method for the instance variable {@link #childId}.
     *
     * @return value of instance variable {@link #childId}
     */
    public Long getChildId()
    {
        return this.childId;
    }

    /**
     * This is the getter method for the instance variable {@link #update}.
     *
     * @return value of instance variable {@link #update}
     */
    public boolean isUpdate()
    {
        return this.update;
    }

    /**
     * This is the setter method for the instance variable {@link #update}.
     *
     * @param _update the update to set
     */
    public void setUpdate(final boolean _update)
    {
        this.update = _update;
    }

    /**
     * This is the getter method for the instance variable {@link #insert}.
     *
     * @return value of instance variable {@link #insert}
     */
    public boolean isInsert()
    {
        return this.insert;
    }

    /**
     * This is the setter method for the instance variable {@link #insert}.
     *
     * @param _insert the insert to set
     */
    public void setInsert(final boolean _insert)
    {
        this.insert = _insert;
    }

    /**
     * This is the getter method for the instance variable {@link #oid}.
     *
     * @return value of instance variable {@link #oid}
     */
    public String getOid()
    {
        return this.oid;
    }

    /**
     * This is the setter method for the instance variable {@link #oid}.
     *
     * @param _oid the oid to set
     */
    public void setOid(final String _oid)
    {
        this.oid = _oid;
    }

    /**
     * This is the getter method for the instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     */
    public Long getId()
    {
        return this.id;
    }

    /**
     * This is the setter method for the instance variable {@link #id}.
     *
     * @param _id the id to set
     */
    public void setId(final Long _id)
    {
        this.id = _id;
    }
}
