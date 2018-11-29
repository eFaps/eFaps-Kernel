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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.access.user.AccessCache;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.eql2.IUpdateListStatement;
import org.efaps.util.EFapsException;

public class ListUpdate
    extends AbstractUpdate
{

    final List<Instance> instances;

    public ListUpdate(final IUpdateListStatement _eqlStmt)
                    throws EFapsException
    {
        super(_eqlStmt);
        this.instances = _eqlStmt.getOidsList().stream().map(oid -> Instance.get(oid)).collect(Collectors.toList());

        this.instances.forEach(instance -> AccessCache.registerUpdate(instance));

        final Map<Type, List<Instance>> typeMap = this.instances.stream().collect(Collectors.groupingBy(
                        Instance::getType));

        for (final Entry<Type, List<Instance>> entry : typeMap.entrySet()) {
            final Map<Instance, Boolean> access = entry.getKey().checkAccess(entry.getValue(), AccessTypeEnums.MODIFY
                            .getAccessType());
            if (access.values().contains(Boolean.FALSE)) {
                throw new EFapsException(getClass(), "execute.NoAccess", Context.getThreadContext().getPerson());
            }
        }
    }

    public List<Instance> getInstances()
    {
        return Collections.unmodifiableList(this.instances);
    }
}
