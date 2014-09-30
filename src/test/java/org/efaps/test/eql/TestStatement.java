/*
 * Copyright 2003 - 2014 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.test.eql;

import java.util.ArrayList;
import java.util.List;

import org.efaps.eql.IStatement;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TestStatement
    implements IStatement
{

    private final List<String> types = new ArrayList<>();

    private final List<String> selects = new ArrayList<>();

    @Override
    public void addType(final String _type)
    {
        this.types.add(_type);
    }

    /**
     * Getter method for the instance variable {@link #types}.
     *
     * @return value of instance variable {@link #types}
     */
    public List<String> getTypes()
    {
        return this.types;
    }

    @Override
    public void addSelect(final String _select)
    {
        this.selects.add(_select);
    }

    /**
     * Getter method for the instance variable {@link #selects}.
     *
     * @return value of instance variable {@link #selects}
     */
    public List<String> getSelects()
    {
        return this.selects;
    }
}
