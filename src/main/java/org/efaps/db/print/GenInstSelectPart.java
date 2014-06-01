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


package org.efaps.db.print;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.GeneralInstance;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class GenInstSelectPart
    extends AbstractSelectPart
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GenInstSelectPart.class);

    /**
     * Type the General Instance belongs to.
     */
    private final Type type;

    /**
     * Index of the GernalInstance table.
     */
    private Integer tableIdx;

    /**
     * Add the type clause.
     */
    private boolean addTypeClause = false;

    /**
     * @param _type Type the General Instance belongs to
     */
    public GenInstSelectPart(final Type _type)
    {
        this.type = _type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int join(final OneSelect _oneSelect,
                    final SQLSelect _select,
                    final int _relIndex)
    {
        this.tableIdx = _oneSelect.getTableIndex(GeneralInstance.TABLENAME,
                        GeneralInstance.ISIDCOLUMN + "_" + GeneralInstance.ISTYPECOLUMN, _relIndex, null);
        if (this.tableIdx == null) {
            this.tableIdx = _oneSelect.getNewTableIndex(GeneralInstance.TABLENAME,
                            GeneralInstance.ISIDCOLUMN + "_" + GeneralInstance.ISTYPECOLUMN, _relIndex, null);
            if (getType().getMainTable().getSqlColType() != null) {
                _select.leftJoin(GeneralInstance.TABLENAME, this.tableIdx,
                                new String[] {GeneralInstance.ISIDCOLUMN, GeneralInstance.ISTYPECOLUMN},
                                _relIndex, new String[] {"ID", getType().getMainTable().getSqlColType()});
            } else {
                _select.leftJoin(GeneralInstance.TABLENAME, this.tableIdx, GeneralInstance.ISIDCOLUMN, _relIndex, "ID");
                this.addTypeClause = true;
            }
        }
        _select.column(this.tableIdx, "ID");
        return this.tableIdx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getType()
    {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add2Where(final OneSelect _oneselect,
                          final SQLSelect _select)
    {
        try {
            if (this.addTypeClause) {
                _select.addPart(SQLPart.AND).addColumnPart(this.tableIdx, GeneralInstance.ISTYPECOLUMN)
                    .addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);
                _select.addValuePart(this.type.getId());
                for (final Type childType : this.type.getChildTypes()) {
                    _select.addPart(SQLPart.COMMA);
                    _select.addValuePart(childType.getId());
                }
                _select.addPart(SQLPart.PARENTHESIS_CLOSE);
            }
        } catch (final CacheReloadException e) {
            GenInstSelectPart.LOG.error("Could not build where part for General Instance: {}", this);
        }
    }
}
