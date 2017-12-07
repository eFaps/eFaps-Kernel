/*
 * Copyright 2003 - 2017 The eFaps Team
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
 */

package org.efaps.db.stmt.print;

import org.efaps.db.Instance;
import org.efaps.db.stmt.runner.ISQLProvider;
import  org.efaps.db.stmt.selection.Select;
import org.efaps.db.stmt.selection.Selection;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.Tableidx;
import org.efaps.eql2.IPrintObjectStatement;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class ObjectPrint.
 *
 * @author The eFaps Team
 */
public class ObjectPrint
    extends AbstractPrint
    implements ISQLProvider
{

    /** The instance. */
    private final Instance instance;

    /** The eql stmt. */
    private final IPrintObjectStatement eqlStmt;

    /**
     * Instantiates a new object print.
     *
     * @param _eqlStmt the eql stmt
     * @throws CacheReloadException on error
     */
    public ObjectPrint(final IPrintObjectStatement _eqlStmt)
        throws CacheReloadException
    {
        this.instance = Instance.get(_eqlStmt.getOid());
        this.eqlStmt = _eqlStmt;
    }

    @Override
    public void prepare()
        throws CacheReloadException
    {
        setSelection(Selection.get(this.eqlStmt.getSelection(), this.instance.getType()));
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect) throws EFapsException
    {
        for (final Select select : getSelection().getSelects()) {
            for (final AbstractElement<?> element : select.getElements()) {
                element.append2SQLSelect(_sqlSelect);
            }
        }
        final String tableName = this.instance.getType().getMainTable().getSqlTable();
        final Tableidx tableidx = _sqlSelect.getIndexer().getTableIdx(tableName, tableName);
        if (tableidx.isCreated()) {
            _sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
        }

        _sqlSelect.addPart(SQLPart.WHERE)
            .addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart(this.instance.getId());

        if (this.instance.getType().getMainTable().getSqlColType() != null) {
            _sqlSelect.addPart(SQLPart.AND).addColumnPart(0, this.instance.getType().getMainTable().getSqlColType())
                .addPart(SQLPart.EQUAL).addValuePart(this.instance.getType().getId());
        }
    }
}
