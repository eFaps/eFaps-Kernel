/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.db.query.CachedResult;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class StringWithUoMType extends AbstractType
{

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(org.efaps.db.query.CachedResult, java.util.List)
     * @param rs
     * @param indexes
     * @return
     * @throws Exception
     */
    public Object readValue(final CachedResult rs, final List<Integer> indexes) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#set(java.lang.Object[])
     * @param values
     */
    public void set(final Object[] values)
    {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param object
     * @param stmt
     * @param index
     * @return
     * @throws SQLException
     */
    public int update(final Object object, final PreparedStatement stmt, final int index) throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }



}
