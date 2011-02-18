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

import java.util.Map;
import java.util.Set;

/**
 * Abstract Class for Importing Objects into the Database connected to eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractObject
{
    /**
     * Returns the links of the object.
     *
     * @return Set with the ForeignObjects connected to this Object
     */
    public abstract Set<ForeignObject> getLinks();

    /**
     * Returns the type of the object.
     *
     * @return type of the object
     */
    public abstract String getType();

    /**
     * Returns the value of an attribute.
     *
     * @param _attribute    attribute of the searched value
     * @return object with the Value, <code>null</code> if the attribute does
     *         not exist
     */
    public abstract Object getAttribute(final String _attribute);

    /**
     * Returns the map with all attributes.
     *
     * @return map containing all Attributes
     */
    public abstract Map<String, Object> getAttributes();

    /**
     * Returns the id of the object.
     *
     * @return string with the id of the object
     */
    public abstract long getID();

    /**
     * Sets the id of the object.
     *
     * @param _id   new id of the object
     */
    public abstract void setID(final long _id);

    /**
     * Returns the attribute which contains the parent-child relationship.
     *
     * @return String with the Name of the Attribute
     */
    public abstract String getParrentAttribute();

    /**
     * Has the object a checkin object?
     *
     * @return <i>true</i> if so, otherwise <i>false</i>
     */
    public abstract boolean isCheckinObject();

    /**
     * Returns the unique attributes.
     *
     * @return set of the unique attributes
     */
    public abstract Set<String> getUniqueAttributes();

    /**
     * Has the Object children?
     *
     * @return <i>true</i> if the Object has children, otherwise <i>false</i>
     */
    public abstract boolean hasChilds();

    /**
     * File is checked into the object.
     */
    public abstract void dbCheckObjectIn();

    /**
     * Appends in eFaps all children.
     */
    public abstract void dbAddChilds();

    /**
     * Create or update of the object in eFaps.
     *
     * @param _parent   Parent-Object of this Object
     * @param _id       id of the Object to be updated, if an empty string is
     *                  given an insert will be made
     * @return string with the id of the new or updated object
     */
    public abstract long dbUpdateOrInsert(final AbstractObject _parent,
                                          final long _id);
}
