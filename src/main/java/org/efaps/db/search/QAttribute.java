/*
 * Copyright 2003 - 2012 The eFaps Team
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


package org.efaps.db.search;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.search.compare.AbstractQAttrCompare;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;


/**
 * Represents the Attribute in the Query.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QAttribute
    extends AbstractQPart
{

    /**
     * Attribute this QueryAttribute is based on.
     */
    private Attribute attribute;

    /**
     * Name of the attribute this QueryAttribute is based on.
     */
    private final String attributeName;

    /**
     * Index of the table the attribute belongs to.
     */
    private Integer tableIndex;

    /**
     * Is this attribute used in a compare applying ignore case.
     */
    private boolean ignoreCase = false;

    /**
     * @param _attribute Attribute
     */
    public QAttribute(final Attribute _attribute)
    {
        this.attribute = _attribute;
        this.attributeName = this.attribute.getName();
    }

    /**
     * @param _attributeName Name of the attribute
     */
    public QAttribute(final String _attributeName)
    {
        this.attributeName = _attributeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractQPart prepare(final AbstractObjectQuery<?> _query,
                                 final AbstractQPart _part)
    {
        if (_part instanceof AbstractQAttrCompare) {
            this.ignoreCase = ((AbstractQAttrCompare) _part).isIgnoreCase();
        }
        if (this.attribute == null) {
            this.attribute =  _query.getBaseType().getAttribute(this.attributeName);
        }
        this.tableIndex = _query.getIndex4SqlTable(this.attribute.getTable());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractQPart appendSQL(final SQLSelect _sql)
    {
        if (this.ignoreCase) {
            _sql.addPart(SQLPart.UPPER).addPart(SQLPart.PARENTHESIS_OPEN);
        }
        _sql.addColumnPart(this.tableIndex, this.attribute.getSqlColNames().get(0));
        if (this.ignoreCase) {
            _sql.addPart(SQLPart.PARENTHESIS_CLOSE);
        }
        return this;
    }
}
