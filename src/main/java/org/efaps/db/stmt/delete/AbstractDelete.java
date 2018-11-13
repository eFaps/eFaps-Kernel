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

package org.efaps.db.stmt.delete;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.stmt.runner.AbstractRunnable;
import org.efaps.eql2.IDeleteStatement;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractDelete
    extends AbstractRunnable
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDelete.class);

    /** The eql stmt. */
    private final IDeleteStatement<?> eqlStmt;

    private final List<Instance> instances = new ArrayList<>();

    public AbstractDelete(final IDeleteStatement<?> _eqlStmt)
    {
        this.eqlStmt = _eqlStmt;
    }

    /**
     * Check access for all Instances. If one does not have access an error will be thrown.
     *
     * @throws EFapsException the eFaps exception
     */
    protected void checkAccess()
        throws EFapsException
    {
        for (final Instance instance : getInstances()) {
            if (!instance.getType().hasAccess(instance, AccessTypeEnums.DELETE.getAccessType(), null)) {
                LOG.error("Delete not permitted for Person: {} on Instance: {}", Context.getThreadContext().getPerson(),
                                instance);
                throw new EFapsException(getClass(), "execute.NoAccess", instance);
            }
        }
    }

    public IDeleteStatement<?> getEqlStmt()
    {
        return this.eqlStmt;
    }

    public List<Instance> getInstances()
    {
        return this.instances;
    }
}
