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
package org.efaps.db.stmt.runner;

import java.util.EnumSet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.db.stmt.AbstractFlagged;
import org.efaps.eql2.StmtFlag;

public abstract class AbstractRunnable
    extends AbstractFlagged
    implements IRunnable
{

    protected AbstractRunnable(final StmtFlag... _flags)
    {
        super(_flags);
    }

    protected AbstractRunnable( final EnumSet<StmtFlag> _flags)
    {
        super(_flags);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
