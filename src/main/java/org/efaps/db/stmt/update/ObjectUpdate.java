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

import java.util.HashSet;
import java.util.Set;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.access.user.AccessCache;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.eql2.IUpdateElement;
import org.efaps.eql2.IUpdateObjectStatement;
import org.efaps.util.EFapsException;

/**
 * Update for one given Instance.
 */
public class ObjectUpdate
    extends AbstractObjectUpdate
{

    public ObjectUpdate(final IUpdateObjectStatement _eqlStmt)
        throws EFapsException
    {
        super(_eqlStmt);
        this.instance = Instance.get(_eqlStmt.getOid());
        AccessCache.registerUpdate(getInstance());

        final Set<Attribute> attributes = new HashSet<>();
        final Type type = getInstance().getType();
        for (final IUpdateElement element : _eqlStmt.getUpdateElements()) {
            final Attribute attr = type.getAttribute(element.getAttribute());
            final AttributeType attrType = attr.getAttributeType();
            if (!attrType.isAlwaysUpdate()) {
                attributes.add(attr);
            }
        }
        if (!getInstance().getType().hasAccess(getInstance(), AccessTypeEnums.MODIFY.getAccessType(), attributes)) {
            throw new EFapsException(getClass(), "execute.NoAccess", Context.getThreadContext().getPerson());
        }
    }
}
