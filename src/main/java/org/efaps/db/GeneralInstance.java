/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class GeneralInstance
{
    /**
     * Name of the Table.
     */
    public static final String TABLENAME = "T_CMGENINST";

    /**
     * Name of the ID Column.
     */
    public static final String IDCOLUMN = "ID";

    /**
     * Name of the Instance Type ID Column.
     */
    public static final String ISTYPECOLUMN = "INSTTYPEID";

    /**
     * Name of the Instance ID Column.
     */
    public static final String ISIDCOLUMN = "INSTID";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GeneralInstance.class);

    /**
     * To make a Singleton.
     */
    private GeneralInstance()
    {
    }

    /**
     * @param _instance Instance the GeneralInstance will be created for.
     * @param _con      Connection the insert will be executed in
     * @throws EFapsException on  error
     */
    protected static void insert(final Instance _instance,
                                 final Connection _con)
        throws EFapsException
    {
        if (_instance.isValid() && _instance.getType().isGeneralInstance()) {
            try {
                final SQLInsert insert = Context.getDbType().newInsert(GeneralInstance.TABLENAME,
                                GeneralInstance.IDCOLUMN,
                                true);
                insert.column(GeneralInstance.ISTYPECOLUMN, _instance.getType().getId());
                insert.column(GeneralInstance.ISIDCOLUMN, _instance.getId());
                insert.execute(_con);
            } catch (final SQLException e) {
                GeneralInstance.LOG.error("executeOneStatement", e);
                throw new EFapsException(GeneralInstance.class, "create", e);
            }
        }
    }

    /**
     * @param _instance Instance the GeneralInstance will be deleted for.
     * @param _con      Connection the insert will be executed in
     * @throws EFapsException on  error
     */
    protected static void delete(final Instance _instance,
                                 final Connection _con)
        throws EFapsException
    {
        if (_instance.isValid() && _instance.getType().isGeneralInstance()) {
            try {
                final Statement stmt = _con.createStatement();
                final StringBuilder cmd = new StringBuilder();
                cmd.append(Context.getDbType().getSQLPart(SQLPart.DELETE)).append(" ")
                    .append(Context.getDbType().getSQLPart(SQLPart.FROM)).append(" ")
                    .append(Context.getDbType().getTableQuote())
                    .append(GeneralInstance.TABLENAME)
                    .append(Context.getDbType().getTableQuote()).append(" ")
                    .append(Context.getDbType().getSQLPart(SQLPart.WHERE)).append(" ")
                    .append(Context.getDbType().getColumnQuote())
                    .append(GeneralInstance.ISIDCOLUMN)
                    .append(Context.getDbType().getColumnQuote())
                    .append(Context.getDbType().getSQLPart(SQLPart.EQUAL))
                    .append(_instance.getId()).append(" ")
                    .append(Context.getDbType().getSQLPart(SQLPart.AND)).append(" ")
                    .append(Context.getDbType().getColumnQuote())
                    .append(GeneralInstance.ISTYPECOLUMN)
                    .append(Context.getDbType().getColumnQuote())
                    .append(Context.getDbType().getSQLPart(SQLPart.EQUAL))
                    .append(_instance.getType().getId()).append(" ");

                stmt.execute(cmd.toString());

                if (GeneralInstance.LOG.isDebugEnabled()) {
                    GeneralInstance.LOG.debug(cmd.toString());
                }
            } catch (final SQLException e) {
                GeneralInstance.LOG.error("executeOneStatement", e);
                throw new EFapsException(GeneralInstance.class, "create", e);
            }
        }
    }
}
