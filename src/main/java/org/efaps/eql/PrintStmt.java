/*
 * Copyright 2003 - 2015 The eFaps Team
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.db.PrintQuery;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public class PrintStmt
    extends AbstractPrintStmt
{

    private PrintQuery print;

    private List<Map<String, Object>> data;

    @Override
    public void setInstance(final String _oid)
        throws Exception
    {
        super.setInstance(_oid);
        this.print = new PrintQuery(_oid);
    }

    @Override
    public void addSelect(final String _select,
                          final String _alias)
        throws Exception
    {
        super.addSelect(_select, _alias);
        this.print.addSelect(_select);
    }

    @Override
    public List<Map<String, Object>> getData()
        throws Exception
    {
        if (this.data == null) {
            this.data = new ArrayList<>();
            final Map<String,Object> map = new HashMap<>();
            this.data.add(map);
            this.print.execute();
            for (final Entry<String, String> entry : getAlias2Selects().entrySet()) {
                map.put(entry.getKey(), this.print.getSelect(entry.getValue()));
            }
        }
        return this.data;
    }
}
