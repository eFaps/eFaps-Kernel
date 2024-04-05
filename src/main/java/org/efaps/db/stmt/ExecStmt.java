/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.stmt;

import java.util.List;
import java.util.Map;

import org.efaps.eql2.IExecStatement;
import org.efaps.eql2.StmtFlag;
import org.efaps.json.data.DataList;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecStmt
    extends AbstractStmt
{

    private static final Logger LOG = LoggerFactory.getLogger(ExecStmt.class);

    public ExecStmt(final StmtFlag... _flags)
    {
        super(_flags);
    }

    public static ExecStmt get(final IExecStatement execStmt)
    {
        final var ret = new ExecStmt(execStmt.getFlags());
        ret.setEQLStmt(execStmt);
        return ret;
    }

    public ExecStmt execute()
    {
        return this;
    }

    public List<Map<String, Object>> getData()
        throws EFapsException
    {
        LOG.warn("That does not work with the kernel");
        return null;
    }

    public DataList getDataList()
        throws EFapsException
    {
        LOG.warn("That does not work with the kernel");
        return null;
    }

}
