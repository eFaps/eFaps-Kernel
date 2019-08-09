/*
 * Copyright 2003 - 2018 The eFaps Team
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.efaps.db.Instance;
import org.efaps.db.stmt.StmtFlag;
import org.efaps.db.stmt.selection.Selection;
import org.efaps.db.stmt.selection.elements.IPrimed;
import org.efaps.eql2.IPrintListStatement;
import org.efaps.eql2.IStatement;
import org.efaps.util.EFapsException;

/**
 * The Class ListPrint.
 */
public class ListPrint
    extends AbstractPrint
    implements IPrimed
{

    private final IPrintListStatement eqlStmt;

    private final List<Instance> instances;

    public ListPrint(final IPrintListStatement _eqlStmt, final EnumSet<StmtFlag>  _flags)
    {
        super(_flags);
        eqlStmt = _eqlStmt;
        instances = _eqlStmt.getOidsList().stream()
                        .map(oid -> Instance.get(oid))
                        .collect(Collectors.toList());
        instances.forEach(instance -> addType(instance.getType()));
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
        return eqlStmt;
    }

    public List<Instance> getInstances()
    {
        return instances;
    }

    @Override
    public List<Object> getObjects()
    {
        return new ArrayList<>(getInstances());
    }
}
