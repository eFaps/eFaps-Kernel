/*
 * Copyright 2003 - 2019 The eFaps Team
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

package org.efaps.eql.builder;

import java.util.Collection;

import org.efaps.ci.CIAttribute;
import org.efaps.db.Instance;
import org.efaps.eql2.IWhereElement;
import org.efaps.eql2.bldr.AbstractWhereBuilder;


public class Where
    extends AbstractWhereBuilder<Where>
    implements IEQLBuilder
{
    private IEQLBuilder parent;

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
        final IWhereElement element = getCurrentElement();
        element.setAttribute(_ciAttr.name);
        return getThis();
    }

    public Where eq(final Instance _instance)
    {
        return eq(_instance.getId());
    }

    /**
     * Stmt.
     *
     * @return the prints the stmt
     */
    public Print select()
    {
        IEQLBuilder parent = getParent();
        while (parent != null && !(parent instanceof Print)) {
            parent = parent.getParent();
        }
        return parent instanceof Print ? (Print) parent : null;
    }

    @Override
    public IEQLBuilder setParent(final IEQLBuilder _parent)
    {
        parent = _parent;
        return this;
    }

    @Override
    public IEQLBuilder getParent()
    {
        return parent;
    }
}
