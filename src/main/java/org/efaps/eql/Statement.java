/*
 * Copyright 2003 - 2014 The eFaps Team
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

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Statement
    implements IStatement
{

    private final Map<String, String> alias2select = new LinkedHashMap<>();

    private QueryBuilder queryBdr;

    private MultiPrintQuery multiPrint;

    /**
     * No public constructor is wanted.
     */
    private Statement()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addType(final String _type)
    {
        try {
            if (this.queryBdr == null) {
                this.queryBdr = new QueryBuilder(Type.get(_type));
            } else {
                this.queryBdr.addType(Type.get(_type));
            }
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelect(final String _select)
    {
        addSelect(_select, new Integer(this.alias2select.size() + 1).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelect(final String _select,
                          final String _alias)
    {
        try {
            if (this.multiPrint == null) {
                this.multiPrint = this.queryBdr.getPrint();
            }
            this.multiPrint.addSelect(_select);
            this.alias2select.put(_alias, _select);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Getter method for the instance variable {@link #multiPrint}.
     *
     * @return value of instance variable {@link #multiPrint}
     */
    public MultiPrintQuery getMultiPrint()
    {
        return this.multiPrint;
    }

    public static final Statement getStatement(final String _stmtStr)
    {
        final Statement ret = new Statement();
        final EQLParser parser = new EQLParser(new StringReader(_stmtStr));
        try {
            parser.parseStatement(ret);
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #alias2select}.
     *
     * @return value of instance variable {@link #alias2select}
     */
    public Map<String, String> getAlias2Selects()
    {
        return this.alias2select;
    }
}
