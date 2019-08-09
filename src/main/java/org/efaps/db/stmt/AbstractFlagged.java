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

package org.efaps.db.stmt;

import java.util.Arrays;
import java.util.EnumSet;

import org.apache.commons.lang3.ArrayUtils;

public abstract class AbstractFlagged
    implements IFlagged
{

    private final EnumSet<StmtFlag> flags;

    protected AbstractFlagged(final StmtFlag... _flags)
    {
        this(ArrayUtils.isEmpty(_flags) ? EnumSet.noneOf(StmtFlag.class) : EnumSet.copyOf(Arrays.asList(_flags)));
    }

    protected AbstractFlagged(final EnumSet<StmtFlag> _flags)
    {
        flags = _flags;
    }

    @Override
    public boolean has(final StmtFlag _flag)
    {
        return flags.contains(_flag);
    }

    protected EnumSet<StmtFlag> getFlags()
    {
        return flags;
    }
}
