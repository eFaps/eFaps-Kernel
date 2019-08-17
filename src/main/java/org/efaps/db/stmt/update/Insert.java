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

import java.util.List;
import java.util.UUID;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.eql2.IInsertStatement;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Insert.
 */
public class Insert
    extends AbstractObjectUpdate
{
    private static final Logger LOG = LoggerFactory.getLogger(Insert.class);

    /** The type. */
    private Type type;

    /**
     * Instantiates a new insert.
     *
     * @param _eqlStmt the eql stmt
     * @throws EFapsException
     */
    public Insert(final IInsertStatement _eqlStmt)
        throws EFapsException
    {
        super(_eqlStmt);
        final String typeStr = ((IInsertStatement) getEqlStmt()).getTypeName();
        if (UUIDUtil.isUUID(typeStr)) {
            type = Type.get(UUID.fromString(typeStr));
        } else {
            type = Type.get(typeStr);
        }

        if (!getType().hasAccess(Instance.get(getType(), 0), AccessTypeEnums.CREATE.getAccessType(), null)) {
            Insert.LOG.error("Insert not permitted for Person: {} on Type: {}", Context.getThreadContext().getPerson(),
                            getType());
            throw new EFapsException(getClass(), "execute.NoAccess", getType());
        }
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Evaluate instance.
     *
     * @param _created the created
     */
    public void evaluateInstance(final Long _created)
    {
       instance = Instance.get(getType(), _created);
    }

    public boolean executeEvents(final EventType _eventType)
        throws EFapsException
    {
        boolean ret = false;
        final List<EventDefinition> triggers = type.getEvents(_eventType);
        if (triggers != null) {
            ret = true;
            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.INSTANCE, instance);
            for (final EventDefinition evenDef : triggers) {
                evenDef.execute(parameter);
            }
        }
        return ret;
    }
}
