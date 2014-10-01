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


package org.efaps.db;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SelectBuilder
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SelectBuilder.class);

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
     * @param _attribute    attribute to link from
     * @return this
     */
    public SelectBuilder linkfrom(final CIAttribute _attribute)
    {
        return linkfrom(_attribute.ciType, _attribute);
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
     * @param _typeUUID     UUID of the type to link from
     * @param _attribute    attribute to link from
     * @return this
     */
    public SelectBuilder linkfrom(final UUID _typeUUID,
                                  final CIAttribute _attribute)
    {
        String typeName = "";
        try {
            typeName = Type.get(_typeUUID).getName();
        } catch (final CacheReloadException e) {
            SelectBuilder.LOG.error("Could not read type from Cache for uuid: {}", _typeUUID);
        }
        return linkfrom(typeName, _attribute.name);
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
     * Add oid select part.
     *
     * @return this
     */
    public SelectBuilder instance()
    {
        addPoint();
        this.bldr.append("instance");
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
     * Add name select part.
     *
     * @return this
     */
    public SelectBuilder name()
    {
        addPoint();
        this.bldr.append("name");
        return this;
    }

    /**
     * Add file select part.
     *
     * @return this
     */
    public SelectBuilder file()
    {
        addPoint();
        this.bldr.append("file");
        return this;
    }

    /**
     * Add length select part.
     *
     * @return this
     */
    public SelectBuilder length()
    {
        addPoint();
        this.bldr.append("length");
        return this;
    }

    /**
     * Add base select part.
     *
     * @return this
     */
    public SelectBuilder base()
    {
        addPoint();
        this.bldr.append("base");
        return this;
    }

    /**
     * Add uom select part.
     *
     * @return this
     */
    public SelectBuilder uom()
    {
        addPoint();
        this.bldr.append("uom");
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
     * @param _typeUUID UUID of the classification type
     * @return this
     */
    public SelectBuilder clazz(final UUID _typeUUID)
    {
        String typeName = "";
        try {
            typeName = Type.get(_typeUUID).getName();
        } catch (final CacheReloadException e) {
            SelectBuilder.LOG.error("Could not read type from Cache for uuid: {}", _typeUUID);
        }
        return clazz(typeName);
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
        return attributeset(_attribute.name);
    }

    /**
     * @param _attribute attribute to be added
     * @return this
     */
    public SelectBuilder attributeset(final String _attribute)
    {
        addPoint();
        this.bldr.append("attributeset[").append(_attribute).append("]");
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
     * @return new SelectBuilder
     */
    public static SelectBuilder get()
    {
        return new SelectBuilder();
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
