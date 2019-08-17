/*
 * Copyright 2003 - 2019 The eFaps Team
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
package org.efaps.db.stmt;

import org.efaps.admin.event.EventType;
import org.efaps.db.Instance;
import org.efaps.db.stmt.runner.StmtRunner;
import org.efaps.db.stmt.update.Insert;
import org.efaps.eql2.IInsertStatement;
import org.efaps.eql2.StmtFlag;
import org.efaps.util.EFapsException;

/**
 * The Class InsertStmt.
 */
public class InsertStmt
    extends AbstractStmt
{
    /** The print. */
    private Insert insert;

    /**
     * Instantiates a new prints the stmt.
     */
    private InsertStmt(final StmtFlag... _flags)
    {
        super(_flags);
    }

    /**
     * Execute.
     *
     * @return the insert stmt
     * @throws EFapsException the e faps exception
     */
    public Instance execute()
        throws EFapsException
    {
        insert = new Insert((IInsertStatement) getEQLStmt());

        if (has(StmtFlag.TRIGGEROFF)) {
            StmtRunner.get().execute(insert);
        } else {
            insert.executeEvents(EventType.INSERT_PRE);

            if (!insert.executeEvents(EventType.INSERT_OVERRIDE)) {
                StmtRunner.get().execute(insert);
            }
            insert.executeEvents(EventType.INSERT_POST);
        }
        return insert.getInstance();
    }

    /**
     * Gets the.
     *
     * @param _printStmt the print stmt
     * @return the prints the stmt
     */
    public static InsertStmt get(final IInsertStatement _insertStmt)
    {
        final InsertStmt ret = new InsertStmt(_insertStmt.getFlags());
        ret.setEQLStmt(_insertStmt);
        return ret;
    }
}
