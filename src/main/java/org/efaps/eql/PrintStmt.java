/*
 * Copyright 2003 - 2017 The eFaps Team
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.eql.stmt.AbstractPrintStmt;
import org.efaps.eql.stmt.parts.select.AbstractSelect;
import org.efaps.eql.stmt.parts.select.ExecSelect;
import org.efaps.eql.stmt.parts.select.SimpleSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
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
            for (final Entry<String, AbstractSelect> entry : getAlias2Selects().entrySet()) {
                if (entry.getValue() instanceof SimpleSelect) {
                    multi.addSelect(entry.getValue().getSelect());
                }
            }
            multi.execute();
            final Map<String, IEsjpSelect> esjpSelects = getEsjpSelect(multi.getInstanceList());
            while (multi.next()) {
                final Map<String, Object> map = new HashMap<>();
                this.data.add(map);
                for (final Entry<String, AbstractSelect> entry : getAlias2Selects().entrySet()) {
                    if (entry.getValue() instanceof SimpleSelect) {
                        map.put(entry.getKey(), multi.getSelect(entry.getValue().getSelect()));
                    } else if (entry.getValue() instanceof ExecSelect) {
                        if (esjpSelects.containsKey(entry.getKey())) {
                            map.put(entry.getKey(),
                                            esjpSelects.get(entry.getKey()).getValue(multi.getCurrentInstance()));
                        }
                    }
                }
            }
        }
        return this.data;
    }

    /**
     * Gets the esjp select.
     *
     * @param _instances the _instances
     * @return the esjp select
     * @throws Exception the exception
     */
    private Map<String, IEsjpSelect> getEsjpSelect(final List<Instance> _instances)
        throws Exception
    {
        final Map<String, IEsjpSelect> ret = new HashMap<>();
        if (!_instances.isEmpty()) {
            for (final Entry<String, AbstractSelect> entry : getAlias2Selects().entrySet()) {
                if (entry.getValue() instanceof ExecSelect) {
                    final Class<?> clazz = Class.forName(entry.getValue().getSelect(), false, EFapsClassLoader
                                    .getInstance());
                    final IEsjpSelect esjp = (IEsjpSelect) clazz.newInstance();
                    final List<String> parameters = ((ExecSelect) entry.getValue()).getParameters();
                    if (parameters.isEmpty()) {
                        esjp.initialize(_instances);
                    } else {
                        esjp.initialize(_instances, parameters.toArray(new String[parameters.size()]));
                    }
                    ret.put(entry.getKey(), esjp);
                }
            }
        }
        return ret;
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
        final MultiPrintQuery ret;
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
