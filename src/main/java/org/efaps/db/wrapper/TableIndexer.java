/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.db.wrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;


/**
 * The Class TableIndexer.
 */
public class TableIndexer
{

    /** The current idx. */
    private int currentIdx;

    /** The tableidxs. */
    private final List<TableIdx> tableidxs = new ArrayList<>();

    /**
     * Gets the table idx.
     *
     * @param _tableName the table name
     * @param _keys the key
     * @return the table idx
     */
    public TableIdx getTableIdx(final String _tableName,
                                final String... _keys)
    {
        TableIdx ret = null;
        final String key = _tableName + "-" + StringUtils.join(_keys, "-");
        final Optional<TableIdx> val = tableidxs.stream().filter(t -> t.getKey().equals(key)).findFirst();
        if (val.isPresent()) {
            ret = val.get();
            ret.setCreated(false);
        } else {
            ret = new TableIdx().setCreated(true).setTable(_tableName).setIdx(currentIdx++).setKey(key);
            tableidxs.add(ret);
        }
        return ret;
    }

    /**
     * The Class Tableidx.
     */
    public static class TableIdx
        implements Serializable
    {
        /** */
        private static final long serialVersionUID = 1L;

        /** The key. */
        private String key;

        /** The created. */
        private boolean created;

        /** The table. */
        private String table;

        /** The idx. */
        private int idx;

        /**
         * Checks if is created.
         *
         * @return true, if is created
         */
        public boolean isCreated()
        {
            return created;
        }

        /**
         * Sets the created.
         *
         * @param _created the created
         * @return the tableidx
         */
        private TableIdx setCreated(final boolean _created)
        {
            created = _created;
            return this;
        }

        /**
         * Gets the table.
         *
         * @return the table
         */
        public String getTable()
        {
            return table;
        }

        /**
         * Sets the table.
         *
         * @param _table the table
         * @return the tableidx
         */
        private TableIdx setTable(final String _table)
        {
            table = _table;
            return this;
        }

        /**
         * Gets the idx.
         *
         * @return the idx
         */
        public int getIdx()
        {
            return idx;
        }

        /**
         * Sets the idx.
         *
         * @param _idx the idx
         * @return the tableidx
         */
        private TableIdx setIdx(final int _idx)
        {
            idx = _idx;
            return this;
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        public String getKey()
        {
            return key;
        }

        /**
         * Sets the key.
         *
         * @param _key the key
         * @return the tableidx
         */
        private TableIdx setKey(final String _key)
        {
            key = _key;
            return this;
        }

        @Override
        public boolean equals(final Object _obj)
        {
            final boolean ret;
            if (_obj instanceof TableIdx) {
                final TableIdx obj = (TableIdx) _obj;
                ret = key.equals(obj.key) && idx == obj.idx && table.equals(obj.table);
            } else {
                ret = super.equals(_obj);
            }
            return ret;
        }

        @Override
        public int hashCode()
        {
            return table.hashCode() + Long.valueOf(idx).hashCode();
        }
    }
}
