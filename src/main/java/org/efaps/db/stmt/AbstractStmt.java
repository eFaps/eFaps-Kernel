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

import java.util.Arrays;
import java.util.EnumSet;

import org.apache.commons.lang3.ArrayUtils;
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

    /** The flags. */
    private final EnumSet<StmtFlag> flags;

    protected AbstractStmt(final StmtFlag... _flags)
    {
        if (ArrayUtils.isEmpty(_flags)) {
            this.flags = EnumSet.noneOf(StmtFlag.class);
        } else {
            this.flags = EnumSet.copyOf(Arrays.asList(_flags));
        }
    }

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

    /**
     * Checks for.
     *
     * @param _flag the flag
     * @return true, if successful
     */
    public boolean has(final StmtFlag _flag)
    {
        return this.flags.contains(_flag);
    }
}
