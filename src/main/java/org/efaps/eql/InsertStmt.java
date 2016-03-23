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

package org.efaps.eql;

import java.util.Map.Entry;

import org.efaps.db.Insert;
import org.efaps.eql.stmt.AbstractInsertStmt;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class InsertStmt
    extends AbstractInsertStmt
{

    @Override
    public int execute()
        throws Exception
    {
        int ret = 0;
        final Insert insert = new Insert(getType());
        for (final Entry<String, String> entry : getAttr2Value().entrySet()) {
            insert.add(entry.getKey(), entry.getValue());
        }
        insert.execute();
        setInstance(insert.getInstance().getOid());
        ret++;
        return ret;
    }
}
