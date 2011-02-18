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

package org.efaps.importer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * This class presents an Object which is connected to an
 * <code>InsertObject</code> through an id.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ForeignObject
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ForeignObject.class);

    /**
     * Contains the name of the attribute which links to an insert object.
     */
    private String linkattribute = null;

    /**
     * Contains the type of this insert object.
     */
    private String type = null;

    /**
     * Contains the attributes and the values used for the query.
     */
    private final Map<String, String> attributes = new HashMap<String, String>();

    /**
     * The attribute to be selected. Default is "ID".
     *
     * @see #setLinkAttribute(String, String, String)
     */
    private String select;

    /**
     * Adds an attribute, which will be used to construct the query.
     *
     * @param _name   Name of the attribute
     * @param _value  Value of the attribute
     */
    public void addAttribute(final String _name,
                             final String _value)
    {
        this.attributes.put(_name, _value.trim());
    }

    /**
     * Sets the link attribute and the type of the foreign object.
     *
     * @param _name     name of the link attribute
     * @param _type     type of the foreign object
     * @param _select name of the field to be selected, default "ID"
     */
    public void setLinkAttribute(final String _name,
                                 final String _type,
                                 final String _select)
    {
        this.linkattribute = _name;
        this.type = _type;
        this.select = _select != null ? _select : "ID";
    }

    /**
     * Returns the {@link #linkattribute link attribute} of this foreign
     * object.
     *
     * @return String containing the Name of the LinkAttribute
     */
    public String getLinkAttribute()
    {
        return this.linkattribute;
    }

    /**
     * <p>Fetches the id of this foreign object from eFaps.</p>
     * <p>To get the id a query is build. If the query returns
     * <code>null</code>, it will be checked if a default is defined for this
     * foreign object. If is so the default is returned, otherwise
     * <code>null</code>.
     *
     * @return string with the id of the foreign object; <code>null</code> if
     *         not found and no default is defined.
     */
    public String dbGetValue()
    {
        final SearchQuery query = new SearchQuery();
        String value = null;
        try {
            query.setQueryTypes(this.type);
            query.addSelect(this.select);
            query.setExpandChildTypes(true);

            for (final Entry<String, String> element : this.attributes.entrySet()) {
                query.addWhereExprEqValue(element.getKey().toString(), element.getValue().toString());
            }
            query.executeWithoutAccessCheck();
            if (query.next()) {
                value = query.get(this.select).toString();
            } else {
                value = DefaultObject.getDefault(this.type, this.linkattribute);
                if (value != null) {
                    ForeignObject.LOG.debug("Query did not return a Value; set Value to Defaultvalue: " + value);
                } else {
                    ForeignObject.LOG.error("the Search for a ForeignObject did return no Result!: - " + toString());
                }
            }
            query.close();
        } catch (final EFapsException e) {
            ForeignObject.LOG.error("getID()", e);
            value = null;
        }
        return value;
    }

    /**
     * Returns the link representation for this foreign object including the
     * {@link #type}, {@link #linkattribute link attribute} and all
     * {@link #attributes}.
     *
     * @return string representation of this class
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("type", this.type)
            .append("link attribute", this.linkattribute)
            .append("attributes", this.attributes.toString())
            .toString();
    }
}
