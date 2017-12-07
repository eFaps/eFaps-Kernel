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

package org.efaps.db.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * The Class TableIndexer.
 */
public class TableIndexer
{

    /** The current idx. */
    private int currentIdx;

    /** The tableidxs. */
    private final List<Tableidx> tableidxs = new ArrayList<>();

    /**
     * Gets the table idx.
     *
     * @param _tableName the table name
     * @param _key the key
     * @return the table idx
     */
    public Tableidx getTableIdx(final String _tableName,
                                final String _key)
    {
        Tableidx ret = null;

        final Optional<Tableidx> val = this.tableidxs.stream().filter(t -> t.getKey().equals(_key)).findFirst();

        if (val.isPresent()) {
            ret = val.get();
            ret.setCreated(false);
        } else {
            ret = new Tableidx().setCreated(true).setTable(_tableName).setIdx(this.currentIdx++).setKey(_key);
            this.tableidxs.add(ret);
        }
        return ret;
    }

    /**
     * The Class Tableidx.
     */
    public static class Tableidx
    {

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
            return this.created;
        }

        /**
         * Sets the created.
         *
         * @param _created the created
         * @return the tableidx
         */
        private Tableidx setCreated(final boolean _created)
        {
            this.created = _created;
            return this;
        }

        /**
         * Gets the table.
         *
         * @return the table
         */
        public String getTable()
        {
            return this.table;
        }

        /**
         * Sets the table.
         *
         * @param _table the table
         * @return the tableidx
         */
        private Tableidx setTable(final String _table)
        {
            this.table = _table;
            return this;
        }

        /**
         * Gets the idx.
         *
         * @return the idx
         */
        public int getIdx()
        {
            return this.idx;
        }

        /**
         * Sets the idx.
         *
         * @param _idx the idx
         * @return the tableidx
         */
        private Tableidx setIdx(final int _idx)
        {
            this.idx = _idx;
            return this;
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        public String getKey()
        {
            return this.key;
        }

        /**
         * Sets the key.
         *
         * @param _key the key
         * @return the tableidx
         */
        private Tableidx setKey(final String _key)
        {
            this.key = _key;
            return this;
        }
    }
}
