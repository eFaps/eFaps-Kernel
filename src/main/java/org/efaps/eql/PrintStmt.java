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

import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.eql.stmt.AbstractPrintStmt;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public class PrintStmt
    extends AbstractPrintStmt
{

    /** The data. */
    private List<Map<String, Object>> data;

    @Override
    public List<Map<String, Object>> getData()
        throws Exception
    {
        if (this.data == null) {
            this.data = new ArrayList<>();

            final MultiPrintQuery multi = getMultiPrint();
            for (final Entry<String, String> entry : getAlias2Selects().entrySet()) {
                multi.addSelect(entry.getValue());
            }
            multi.execute();
            while (multi.next()) {
                final Map<String, Object> map = new HashMap<>();
                this.data.add(map);
                for (final Entry<String, String> entry : getAlias2Selects().entrySet()) {
                    map.put(entry.getKey(), multi.getSelect(entry.getValue()));
                }
            }
        }
        return this.data;
    }

    /**
     * Gets the multi print.
     *
     * @return the multi print
     * @throws EFapsException on error
     */
    private MultiPrintQuery getMultiPrint()
        throws EFapsException
    {
        MultiPrintQuery ret;
        if (getInstances().isEmpty()) {
            ret = new MultiPrintQuery(QueryBldrUtil.getInstances(this));
        } else {
            final List<Instance> instances = new ArrayList<>();
            for (final String oid : getInstances()) {
                instances.add(Instance.get(oid));
            }
            ret = new MultiPrintQuery(instances);
        }
        return ret;
    }
}
