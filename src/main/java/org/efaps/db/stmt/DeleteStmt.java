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
import org.efaps.db.stmt.delete.AbstractDelete;
import org.efaps.db.stmt.delete.ListDelete;
import org.efaps.db.stmt.delete.ObjectDelete;
import org.efaps.db.stmt.runner.StmtRunner;
import org.efaps.eql2.IDeleteListStatement;
import org.efaps.eql2.IDeleteObjectStatement;
import org.efaps.eql2.IDeleteStatement;
import org.efaps.eql2.StmtFlag;
import org.efaps.util.EFapsException;

public class DeleteStmt
    extends AbstractStmt
{

    /** The delete. */
    private AbstractDelete delete;

    /**
     * Instantiates a new delete stmt.
     *
     * @param _flags the flags
     */
    private DeleteStmt(final StmtFlag... _flags)
    {
        super(_flags);
    }

    public DeleteStmt execute()
        throws EFapsException
    {
        if (getEQLStmt() instanceof IDeleteObjectStatement) {
            delete = new ObjectDelete((IDeleteObjectStatement) getEQLStmt());
        } else if (getEQLStmt() instanceof IDeleteListStatement) {
            delete = new ListDelete((IDeleteListStatement) getEQLStmt());
        }
        if (has(StmtFlag.TRIGGEROFF)) {
            StmtRunner.get().execute(delete);
        } else {
            delete.executeEvents(EventType.DELETE_PRE);

            if (!delete.executeEvents(EventType.DELETE_OVERRIDE)) {
                StmtRunner.get().execute(delete);
            }
            delete.executeEvents(EventType.DELETE_POST);
        }
        return this;
    }

    /**
     * Gets the.
     *
     * @param _printStmt the print stmt
     * @return the prints the stmt
     */
    public static DeleteStmt get(final IDeleteStatement<?> _deleteStmt)
    {
        final DeleteStmt ret = new DeleteStmt();
        ret.setEQLStmt(_deleteStmt);
        return ret;
    }
}
