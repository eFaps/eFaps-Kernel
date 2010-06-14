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
import org.efaps.ci.CIType;


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
     * Standard constructor.
     */
    public SelectBuilder()
    {
    }

    /**
     * Constructor setting a SelectBuilder as bases for this SelectBuilder.
     * @param _selectBuilder SelectBuilder to be used
     */
    public SelectBuilder(final SelectBuilder _selectBuilder)
    {
        this.bldr.append(_selectBuilder.toString());
    }


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
     * @param _type         type to link from
     * @param _attribute    attribute to link from
     * @return this
     */
    public SelectBuilder linkfrom(final CIType _type,
                                  final CIAttribute _attribute)
    {
        return linkfrom(_type.getType().getName(), _attribute.name);
    }

    /**
     * @param _type         type to link from
     * @param _attribute    attribute to linkto
     * @return this
     */
    public SelectBuilder linkfrom(final String _type,
                                  final String _attribute)
    {
        addPoint();
        this.bldr.append("linkfrom[").append(_type).append("#").append(_attribute).append("]");
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
     * Add oid select part.
     *
     * @return this
     */
    public SelectBuilder oid()
    {
        addPoint();
        this.bldr.append("oid");
        return this;
    }

    /**
     * Add type select part.
     *
     * @return this
     */
    public SelectBuilder type()
    {
        addPoint();
        this.bldr.append("type");
        return this;
    }

    /**
     * Add type select part.
     *
     * @return this
     */
    public SelectBuilder value()
    {
        addPoint();
        this.bldr.append("value");
        return this;
    }

    /**
     * Add label select part.
     *
     * @return this
     */
    public SelectBuilder label()
    {
        addPoint();
        this.bldr.append("label");
        return this;
    }

    /**
     * Add id select part.
     *
     * @return this
     */
    public SelectBuilder id()
    {
        addPoint();
        this.bldr.append("id");
        return this;
    }

    /**
     * Add uuid select part.
     *
     * @return this
     */
    public SelectBuilder uuid()
    {
        addPoint();
        this.bldr.append("uuid");
        return this;
    }

    /**
     * Add uuid select part.
     *
     * @return this
     */
    public SelectBuilder clazz()
    {
        addPoint();
        this.bldr.append("class");
        return this;
    }

    /**
     * @param _class attribute to be added
     * @return this
     */
    public SelectBuilder clazz(final CIType _class)
    {
        return clazz(_class.getType().getName());
    }

    /**
     * @param _class attribute to be added
     * @return this
     */
    public SelectBuilder clazz(final String _class)
    {
        addPoint();
        this.bldr.append("class[").append(_class).append("]");
        return this;
    }

    /**
     * @param _attribute attribute to be added
     * @return this
     */
    public SelectBuilder attributeset(final CIAttribute _attribute)
    {
        return attribute(_attribute.name);
    }

    /**
     * @param _attribute attribute to be added
     * @return this
     */
    public SelectBuilder attributeset(final String _attribute)
    {
        addPoint();
        this.bldr.append("attribute[").append(_attribute).append("]");
        return this;
    }

    /**
     * Add format select part.
     * @param _pattern pattern to be applied
     *
     * @return this
     */
    public SelectBuilder format(final String _pattern)
    {
        addPoint();
        this.bldr.append("format[").append(_pattern).append("]");
        return this;
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
