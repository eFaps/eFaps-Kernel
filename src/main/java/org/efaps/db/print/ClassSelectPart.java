/*
 * Copyright 2003 - 2013 The eFaps Team
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Select Part for <code>class[CLASSIFICATIONNAME]</code>.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassSelectPart
    extends AbstractSelectPart
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OneSelect.class);

    /**
     * Classification this select part belongs to.
     */
    private final Classification classification;

    /**
     * @param _classification   classification
     * @throws CacheReloadException on error
     */
    public ClassSelectPart(final String _classification)
        throws CacheReloadException
    {
        this.classification = (Classification) Type.get(_classification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int join(final OneSelect _oneSelect,
                    final SQLSelect _select,
                    final int _relIndex)
        throws EFapsException
    {
        Integer ret;
        final String tableName = this.classification.getMainTable().getSqlTable();
        final Attribute attr = this.classification.getAttribute(this.classification.getLinkAttributeName());
        if (attr == null) {
            ClassSelectPart.LOG.error("Could not find attribute: '{}'", this.classification.getLinkAttributeName());
            throw new EFapsException(ClassSelectPart.class, "joinNoAttribute");
        }
        final String column = attr.getSqlColNames().get(0);
        ret = _oneSelect.getTableIndex(tableName, column, _relIndex);
        if (ret == null) {
            ret = _oneSelect.getNewTableIndex(tableName, column, _relIndex);
            _select.leftJoin(tableName, ret, column, _relIndex, "ID");
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getType()
    {
        return this.classification;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
