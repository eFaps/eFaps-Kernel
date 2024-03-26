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
package org.efaps.eql.builder;

import java.util.Collection;
import java.util.stream.Stream;

import org.efaps.admin.datamodel.Status;
import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIStatus;
import org.efaps.db.Instance;
import org.efaps.eql2.bldr.AbstractWhereBuilder;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Where
    extends AbstractWhereBuilder<Where>
{

    private static final Logger LOG = LoggerFactory.getLogger(Where.class);

    @Override
    protected Where getThis()
    {
        return this;
    }

    public Where in(final Collection<Instance> _instances)
    {
        in(_instances.stream().map(inst -> inst.getId()).toArray(Long[]::new));
        return getThis();
    }

    public Where attribute(final CIAttribute _ciAttr)
    {
        return super.attribute(_ciAttr.name);
    }

    public Where linkto(final CIAttribute _ciAttr)
    {
        return super.linkto(_ciAttr.name);
    }

    public Where eq(final Instance _instance)
    {
        return eq(_instance.getId());
    }

    public Where eq(final Status _status)
    {
        return eq(_status.getId());
    }

    public Where eq(final CIStatus _ciStatus)
        throws CacheReloadException
    {
        return eq(Status.find(_ciStatus));
    }

    public Where in(final CIStatus... _ciStatus)
    {
        final var ids = Stream.of(_ciStatus).map(ciStatus -> {
            try {
                return Status.find(ciStatus).getId();
            } catch (final CacheReloadException e) {
                LOG.error("Catched", e);
            }
            return 0;
        }).toArray(Long[]::new);
        return in(ids);
    }

    public Where in(final Status... _status)
    {
        return in(Stream.of(_status).map(status -> {
            return status.getId();
        }).toArray(Long[]::new));
    }

    public Where in(final Instance... _instances)
    {
        return in(Stream.of(_instances).map(instance -> {
            return instance.getId();
        }).toArray(Long[]::new));
    }

    /**
     * Stmt.
     *
     * @return the prints the stmt
     */
    @Override
    public Print select()
    {
        return (Print) super.select();
    }
}
