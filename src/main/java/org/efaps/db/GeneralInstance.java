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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.store.AbstractStoreResource;
import org.efaps.db.store.JCRStoreResource;
import org.efaps.db.store.JDBCStoreResource;
import org.efaps.db.transaction.ConnectionResource;
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
     * @return id of the new instance
     * @throws EFapsException on  error
     */
    protected static long insert(final Instance _instance,
                                 final Connection _con)
        throws EFapsException
    {
        long ret = 0;
        if (_instance.isValid() && _instance.getType().isGeneralInstance()) {
            try {
                final SQLInsert insert = Context.getDbType().newInsert(GeneralInstance.TABLENAME,
                                GeneralInstance.IDCOLUMN,
                                true);
                insert.column(GeneralInstance.ISTYPECOLUMN, _instance.getType().getId());
                insert.column(GeneralInstance.ISIDCOLUMN, _instance.getId());
                ret = insert.execute(_con);
            } catch (final SQLException e) {
                GeneralInstance.LOG.error("executeOneStatement", e);
                throw new EFapsException(GeneralInstance.class, "create", e);
            }
        }
        return ret;
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
            final long id = GeneralInstance.getId(_instance, _con);
            if (id > 0) {
                if (_instance.getType().getStoreId() > 0) {
                    GeneralInstance.del4Table(id, _con, JDBCStoreResource.TABLENAME_STORE);
                    GeneralInstance.del4Table(id, _con, JCRStoreResource.TABLENAME_STORE);
                    GeneralInstance.del4Table(id, _con, AbstractStoreResource.TABLENAME_STORE);
                }
                GeneralInstance.del4Table(id, _con, GeneralInstance.TABLENAME);
            }
        }
    }

    /**
     * @param _id           id to be deleted
     * @param _con          connection used for the execute
     * @param _tableName    nem of the table the value must be deleted from
     * @throws EFapsException on error
     */
    private static void del4Table(final long _id,
                                  final Connection _con,
                                  final String _tableName)
        throws EFapsException
    {
        try {
            final AbstractDatabase<?> db = Context.getDbType();
            final StringBuilder cmd = new StringBuilder();
            cmd.append(db.getSQLPart(SQLPart.DELETE)).append(" ")
                .append(db.getSQLPart(SQLPart.FROM)).append(" ")
                .append(db.getTableQuote())
                .append(_tableName)
                .append(db.getTableQuote()).append(" ")
                .append(db.getSQLPart(SQLPart.WHERE)).append(" ")
                .append(db.getColumnQuote())
                .append("ID")
                .append(db.getColumnQuote())
                .append(db.getSQLPart(SQLPart.EQUAL))
                .append(_id).append(" ");

            final Statement stmt = _con.createStatement();
            stmt.executeUpdate(cmd.toString());
            stmt.close();

            if (GeneralInstance.LOG.isDebugEnabled()) {
                GeneralInstance.LOG.debug(cmd.toString());
            }
        } catch (final SQLException e) {
            GeneralInstance.LOG.error("executeOneStatement", e);
            throw new EFapsException(GeneralInstance.class, "create", e);
        }
    }

    /**
     * @param _instance Instance the id of the GeneralInstance will be retrieved for.
     * @param _con      Connection the query will be executed in
     * @throws EFapsException on  error
     * @return id of the current General Instance
     */
    protected static long getId(final Instance _instance,
                                final Connection _con)
        throws EFapsException
    {
        long ret = 0;
        if (_instance.isValid() && _instance.getType().isGeneralInstance()) {
            try {
                final Statement queryStmt = _con.createStatement();
                final AbstractDatabase<?> db = Context.getDbType();

                final StringBuilder cmd = new StringBuilder();
                cmd.append(db.getSQLPart(SQLPart.SELECT)).append(" ")
                    .append(db.getColumnQuote())
                    .append(GeneralInstance.IDCOLUMN)
                    .append(db.getColumnQuote()).append(" ")
                    .append(db.getSQLPart(SQLPart.FROM)).append(" ")
                    .append(db.getTableQuote())
                    .append(GeneralInstance.TABLENAME)
                    .append(db.getTableQuote()).append(" ")
                    .append(db.getSQLPart(SQLPart.WHERE)).append(" ")
                    .append(db.getColumnQuote())
                    .append(GeneralInstance.ISIDCOLUMN)
                    .append(db.getColumnQuote())
                    .append(db.getSQLPart(SQLPart.EQUAL))
                    .append(_instance.getId()).append(" ")
                    .append(db.getSQLPart(SQLPart.AND)).append(" ")
                    .append(db.getColumnQuote())
                    .append(GeneralInstance.ISTYPECOLUMN)
                    .append(db.getColumnQuote())
                    .append(db.getSQLPart(SQLPart.EQUAL))
                    .append(_instance.getType().getId()).append(" ");

                final ResultSet rs = queryStmt.executeQuery(cmd.toString());
                while (rs.next()) {
                    ret = rs.getLong(1);
                }
                queryStmt.close();
                if (GeneralInstance.LOG.isDebugEnabled()) {
                    GeneralInstance.LOG.debug(cmd.toString());
                }
            } catch (final SQLException e) {
                GeneralInstance.LOG.error("executeOneStatement", e);
                throw new EFapsException(GeneralInstance.class, "create", e);
            }
        }
        return ret;
    }

    /**
     * @param _instance Instance the id of the GeneralInstance will be retrieved for.
     * @throws EFapsException on  error
     * @return id of the current General Instance
     */
    protected static long getId(final Instance _instance)
        throws EFapsException
    {
        long ret = 0;
        final Context context = Context.getThreadContext();
        ConnectionResource con = null;
        try {
            con = context.getConnectionResource();
            ret = GeneralInstance.getId(_instance, con.getConnection());
            con.commit();
        } finally {
            if (con != null && con.isOpened()) {
                con.abort();
            }
        }
        return ret;
    }
}
