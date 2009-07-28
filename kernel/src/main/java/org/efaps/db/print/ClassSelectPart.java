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

package org.efaps.db.print;

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;



/**
 * Select Part for <code>class[CLASSIFICATIONNAME]</code>.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassSelectPart implements ISelectPart
{

    /**
     * Classification this select part belongs to.
     */
    private final Classification classification;

    /**
     * @param _classification   classification
     */
    public ClassSelectPart(final String _classification)
    {
        this.classification = (Classification) Classification.get(_classification);
    }

    /**
     * {@inheritDoc}
     */
    public int join(final OneSelect _oneSelect, final StringBuilder _fromBldr, final int _relIndex)
    {
        Integer ret;
        final String tableName = this.classification.getMainTable().getSqlTable();
        ret = _oneSelect.getTableIndex(tableName, _relIndex);
        if (ret == null) {
            ret = _oneSelect.getNewTableIndex(tableName, _relIndex);
            _fromBldr.append(" left join ").append(tableName).append(" T").append(ret).append(" on T").append(
                            _relIndex).append(".ID=").append("T").append(ret).append(".").append(
                            this.classification.getLinkAttributeName());
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public Type getType()
    {
        return this.classification;
    }
}
