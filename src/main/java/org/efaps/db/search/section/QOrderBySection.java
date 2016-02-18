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


package org.efaps.db.search.section;

import java.util.ArrayList;
import java.util.List;

import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.search.AbstractQPart;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;


/**
 * Comparison for max value.
 *
 * @author The eFaps Team
 *
 */
public class QOrderBySection
     extends AbstractQSection
{

    /**
     * List of attributes this will be ordered.
     */
    private final List<AbstractQPart> parts = new ArrayList<AbstractQPart>();

    /**
     * Constructor adding Attributes.
     * @param _parts Array of AbstractQPart
     */
    public QOrderBySection(final AbstractQPart... _parts)
    {
        for (final AbstractQPart part : _parts) {
            this.parts.add(part);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QOrderBySection appendSQL(final SQLSelect _select)
        throws EFapsException
    {
        _select.addPart(SQLPart.ORDERBY);
        boolean first = true;
        for (final AbstractQPart part : this.parts) {
            if (first) {
                first = false;
            } else {
                _select.addPart(SQLPart.COMMA);
            }
            part.appendSQL(_select);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QOrderBySection prepare(final AbstractObjectQuery<?> _query)
        throws EFapsException
    {
        for (final AbstractQPart part : this.parts) {
            part.prepare(_query, null);
        }
        return this;
    }

    /**
     * Add an attribute to the list of attributes the query will be ordered by.
     *
     * @param _part AbstractQPart to be added
     * @return this
     */
    public QOrderBySection addPart(final AbstractQPart _part)
    {
        this.parts.add(_part);
        return this;
    }
}
