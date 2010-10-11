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


package org.efaps.db;

import org.efaps.ci.CIAttribute;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SelectBuilder
{
    /**
     * StringBuilder use as the base for building the String.
     */
    private final StringBuilder bldr = new StringBuilder();


    /**
     * @param _attribute attribute to linkto
     * @return this
     */
    public SelectBuilder linkto(final CIAttribute _attribute)
    {
        return linkto(_attribute.name);
    }

    /**
     * @param _attribute attribute to linkto
     * @return this
     */
    public SelectBuilder linkto(final String _attribute)
    {
        addPoint();
        this.bldr.append("linkto[").append(_attribute).append("]");
        return this;
    }

    /**
     * @param _attribute attribute to be added
     * @return this
     */
    public SelectBuilder attribute(final CIAttribute _attribute)
    {
        return attribute(_attribute.name);
    }

    /**
     * @param _attribute attribute to be added
     * @return this
     */
    public SelectBuilder attribute(final String _attribute)
    {
        addPoint();
        this.bldr.append("attribute[").append(_attribute).append("]");
        return this;
    }


    /**
     * add a point.
     */
    private void addPoint()
    {
        if (this.bldr.length() > 0) {
            this.bldr.append(".");
        }
    }

    /**
     * Return the string of the StringBuilder.
     * @return StringBuilder.toString()
     */
    @Override
    public String toString()
    {
        return this.bldr.toString();
    }

}
