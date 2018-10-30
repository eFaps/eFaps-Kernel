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

import java.util.Arrays;
import java.util.List;

import org.efaps.db.Instance;
import org.efaps.db.stmt.StmtFlag;
import org.efaps.db.stmt.selection.Selection;
import org.efaps.db.stmt.selection.elements.IPrimed;
import org.efaps.eql2.IPrintObjectStatement;
import org.efaps.eql2.IStatement;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class ObjectPrint.
 *
 * @author The eFaps Team
 */
public class ObjectPrint
    extends AbstractPrint
    implements IPrimed
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
    public ObjectPrint(final IPrintObjectStatement _eqlStmt, final StmtFlag... _flags)
        throws CacheReloadException
    {
        super(_flags);
        this.instance = Instance.get(_eqlStmt.getOid());
        this.eqlStmt = _eqlStmt;
        addType(this.instance.getType());
    }

    /**
     * Gets the instance.
     *
     * @return the instance
     */
    public Instance getInstance()
    {
        return this.instance;
    }

    @Override
    public Selection getSelection()
        throws EFapsException
    {
        Selection ret = super.getSelection();
        if (ret == null) {
            setSelection(Selection.get(this));
            ret = super.getSelection();
        }
        return ret;
    }

    @Override
    public IStatement<?> getStmt()
    {
        return this.eqlStmt;
    }

    @Override
    public List<Object> getObjects()
    {
        return Arrays.asList(getInstance());
    }
}
