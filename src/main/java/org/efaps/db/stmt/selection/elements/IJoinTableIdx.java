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

package org.efaps.db.stmt.selection.elements;

import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;

/**
 * The Interface IJoinTableIdx.
 */
public interface IJoinTableIdx
{

    /**
     * Gets the join table idx.
     *
     * @param _sqlSelect the sql select
     * @return the join table idx
     * @throws EFapsException the e faps exception
     */
    TableIdx getJoinTableIdx(SQLSelect _sqlSelect)
        throws EFapsException;
}
