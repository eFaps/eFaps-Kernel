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
package org.efaps.db.stmt;

import org.efaps.db.stmt.runner.StmtRunner;
import org.efaps.db.stmt.update.AbstractUpdate;
import org.efaps.db.stmt.update.ListUpdate;
import org.efaps.db.stmt.update.ObjectUpdate;
import org.efaps.db.stmt.update.QueryUpdate;
import org.efaps.eql2.IUpdateListStatement;
import org.efaps.eql2.IUpdateObjectStatement;
import org.efaps.eql2.IUpdateQueryStatement;
import org.efaps.eql2.IUpdateStatement;
import org.efaps.util.EFapsException;

/**
 * The Class UpdateStmt.
 */
public class UpdateStmt
    extends AbstractStmt
{

    /** The print. */
    private AbstractUpdate update;

    /**
     * Instantiates a new update stmt.
     */
    private UpdateStmt()
    {
    }

    /**
     * Execute.
     *
     * @return the update stmt
     * @throws EFapsException the e faps exception
     */
    public UpdateStmt execute()
        throws EFapsException
    {
        if (getEQLStmt() instanceof IUpdateObjectStatement) {
            this.update = new ObjectUpdate((IUpdateObjectStatement) getEQLStmt());
        } else if (getEQLStmt() instanceof IUpdateListStatement) {
            this.update = new ListUpdate((IUpdateListStatement) getEQLStmt());
        } else if (getEQLStmt() instanceof IUpdateQueryStatement) {
            this.update = new QueryUpdate((IUpdateQueryStatement) getEQLStmt());
        }
        StmtRunner.get().execute(this.update);
        return this;
    }

    /**
     * Gets the.
     *
     * @param _printStmt the print stmt
     * @return the prints the stmt
     */
    public static UpdateStmt get(final IUpdateStatement<?> _updateStmt)
    {
        final UpdateStmt ret = new UpdateStmt();
        ret.setEQLStmt(_updateStmt);
        return ret;
    }
}
