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
package org.efaps.db.stmt.delete;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.attributetype.IStatusChangeListener;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.Listener;
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
        eqlStmt = _eqlStmt;
    }

    /**
     * Check access for all Instances. If one does not have access an error will
     * be thrown.
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
        return eqlStmt;
    }

    public List<Instance> getInstances()
    {
        return instances;
    }

    public boolean executeEvents(final EventType _eventType)
        throws EFapsException
    {
        boolean ret = false;
        for (final Instance instance : getInstances()) {
            final List<EventDefinition> triggers = instance.getType().getEvents(_eventType);
            if (triggers != null) {
                ret = true;
                final Parameter parameter = new Parameter();
                parameter.put(ParameterValues.INSTANCE, instance);
                for (final EventDefinition evenDef : triggers) {
                    evenDef.execute(parameter);
                }
            }
        }
        return ret;
    }

    public void triggerListeners()
        throws EFapsException
    {
        for (final Instance instance : getInstances()) {
            if (instance.getType().isCheckStatus()) {
                for (final IStatusChangeListener listener : Listener.get()
                                .<IStatusChangeListener>invoke(IStatusChangeListener.class)) {
                    listener.onDelete(instance);
                }
            }
        }
    }
}
