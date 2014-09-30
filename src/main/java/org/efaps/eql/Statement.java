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

package org.efaps.eql;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Statement
    implements IStatement
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void addType(final String _type)
    {
        System.out.println(_type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelect(final String _select)
    {
        System.out.println(_select);
    }

}
