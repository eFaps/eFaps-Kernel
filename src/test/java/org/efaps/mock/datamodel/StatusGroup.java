/*
 * Copyright 2003 - 2018 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.efaps.mock.datamodel;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.RandomUtils;
import org.efaps.test.EFapsQueryHandler;
import org.efaps.test.IMockResult;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.RowList4.Impl;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

public class StatusGroup
    implements IMockResult
{

    private static final String SQLSTATUSID = "select ID,TYPEID,KEY,DESCR from T_DMSTATUS T0 where T0.ID = ?";

    private static final String SQLGRPUUID = "select T0.ID,T0.TYPEID,KEY,DESCR "
                    + "from T_DMSTATUS T0 "
                    + "inner join T_CMABSTRACT T1 on T0.TYPEID=T1.ID where T1.UUID = ?";

    private final BidiMap<Long, String> keys = new DualHashBidiMap<>();
    private final Type type;
    private String currentKey = null;

    protected StatusGroup(final StatusGroupBuilder _builder)
    {
        for (final String key : _builder.keys) {
            this.keys.put(RandomUtils.nextLong(), key);
        }
        this.type = _builder.type;
    }

    public Long getStatusId(final String _key)
    {
        return this.keys.getKey(_key);
    }

    @Override
    public String[] getSqls()
    {
        return new String[] { SQLSTATUSID, SQLGRPUUID };
    }

    @Override
    public boolean applies(final String _sql, final List<Parameter> _parameters)
    {
        boolean ret = false;
        this.currentKey = null;
        if (_parameters.size() == 1) {
            final Parameter parameter = _parameters.get(0);
            if (SQLSTATUSID.equals(_sql)) {
                ret = this.keys.containsKey(parameter.right);
                this.currentKey = this.keys.get(parameter.right);
            } else if (SQLGRPUUID.equals(_sql)) {
                ret = this.type.getUuid().toString().equals(parameter.right);
            }
        }
        return ret;
    }

    @Override
    public QueryResult getResult()
    {
        Impl<Long, Long, String, String> rowlist = RowLists.rowList4(Long.class, Long.class, String.class,
                        String.class);
        if (this.currentKey == null) {
            for (final Entry<Long, String> entry : this.keys.entrySet()) {
                rowlist = rowlist.append(entry.getKey(), this.type.getId(), entry.getValue(), entry.getValue()
                                + "-Description");
            }
        } else {
            rowlist = rowlist.append(getStatusId(this.currentKey), this.type.getId(), this.currentKey, this.currentKey
                            + "-Description");
        }
        return rowlist.asResult();
    }

    /**
     * Builder.
     *
     * @return the attribute builder
     */
    public static StatusGroupBuilder builder()
    {
        return new StatusGroupBuilder();
    }

    public static class StatusGroupBuilder
    {
        private long id;
        private String name;
        private String[] keys;
        private Type type;

        public StatusGroupBuilder withId(final long _id)
        {
            this.id = _id;
            return this;
        }

        public StatusGroupBuilder withName(final String _name)
        {
            this.name = _name;
            return this;
        }

        public StatusGroupBuilder withKeys(final String... _keys)
        {
            this.keys = _keys;
            return this;
        }

        public StatusGroup build()
        {
            // register the StausGroup as a type
            this.type = Type.builder()
                .withId(this.id)
                .withName(this.name)
                .build();
            final StatusGroup ret = new StatusGroup(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}
