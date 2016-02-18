/*
 * Copyright 2003 - 2016 The eFaps Team
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


package org.efaps.db.print;

import java.sql.SQLException;

import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * Abstract Select Part.
 *
 * @author The eFaps Team
 *
 */
public abstract class AbstractSelectPart
    implements ISelectPart
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void addObject(final Object[] _row)
        throws SQLException
    {
        //no objects must be added
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public  void add2Where(final OneSelect _oneselect,
                           final SQLSelect _select)
    {
        //nothing must be added
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void next()
        throws EFapsException
    {
        // we do nothing as default
    }
}
