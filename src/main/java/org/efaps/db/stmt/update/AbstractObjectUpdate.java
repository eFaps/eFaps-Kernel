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

package org.efaps.db.stmt.update;

import org.efaps.db.Instance;
import org.efaps.eql2.IUpdateElementsStmt;

/**
 * The Class AbstractObjectUpdate.
 */
public abstract class AbstractObjectUpdate
    extends AbstractUpdate
{

    /** The instance. */
    protected Instance instance;

    /**
     * Instantiates a new abstract object update.
     *
     * @param _eqlStmt the eql stmt
     */
    public AbstractObjectUpdate(final IUpdateElementsStmt<?> _eqlStmt)
    {
        super(_eqlStmt);
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
}
