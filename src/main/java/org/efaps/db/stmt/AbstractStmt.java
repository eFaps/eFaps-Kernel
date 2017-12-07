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


package org.efaps.db.stmt;

import org.efaps.eql2.IStatement;

/**
 * The Class AbstractStmt.
 *
 * @author The eFaps Team
 */
public abstract class AbstractStmt
{

    /** The statement. */
    private IStatement<?> eqlStmt;

    /**
     * Gets the statement.
     *
     * @return the statement
     */
    protected IStatement<?> getEQLStmt()
    {
        return this.eqlStmt;
    }

    /**
     * Sets the statement.
     *
     * @param _eqlStmt the new statement
     */
    protected void setEQLStmt(final IStatement<?> _eqlStmt)
    {
        this.eqlStmt = _eqlStmt;
    }
}
