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

package org.efaps.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.efaps.db.store.AbstractStoreResource;
import org.efaps.db.store.JCRStoreResource;
import org.efaps.db.store.JDBCStoreResource;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLDelete.DeleteDefintion;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
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
     * Name of the Exchange ID Column.
     */
    public static final String EXIDCOLUMN = "EXID";

    /**
     * Name of the Exchange System ID Column.
     */
    public static final String EXSYSIDCOLUMN = "EXSYSID";

    /**
     * SQL select statement to select a type from the database by its Name.
     */
    private static final String SQL = new SQLSelect()
                    .column(GeneralInstance.IDCOLUMN)
                    .column(GeneralInstance.EXSYSIDCOLUMN)
                    .column(GeneralInstance.EXIDCOLUMN)
                    .from(GeneralInstance.TABLENAME, 0)
                    .addPart(SQLPart.WHERE)
                    .addColumnPart(0, GeneralInstance.ISIDCOLUMN).addPart(SQLPart.EQUAL).addValuePart("?")
                    .addPart(SQLPart.AND)
                    .addColumnPart(0, GeneralInstance.ISTYPECOLUMN).addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();



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
                                 final ConnectionResource _con)
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
                insert.column(GeneralInstance.EXIDCOLUMN, _instance.getExchangeId(false));
                insert.column(GeneralInstance.EXSYSIDCOLUMN, _instance.getExchangeSystemId(false));
                ret = insert.execute(_con);
                _instance.setGeneralId(ret);
                _instance.setGeneralised(true);
            } catch (final SQLException e) {
                GeneralInstance.LOG.error("executeOneStatement", e);
                throw new EFapsException(GeneralInstance.class, "create", e);
            }
        }
        return ret;
    }

    /**
     * @param _instance Instance the id of the GeneralInstance will be retrieved for.
     * @param _con      Connection the query will be executed in
     * @throws EFapsException on  error
     */
    protected static void generaliseInstance(final Instance _instance,
                                             final ConnectionResource _con)
        throws EFapsException
    {
        if (_instance.isValid() && _instance.getType().isGeneralInstance()) {
            PreparedStatement stmt = null;
            try {
                try {
                    stmt = _con.prepareStatement(GeneralInstance.SQL);
                    stmt.setLong(1, _instance.getId());
                    stmt.setLong(2, _instance.getType().getId());

                    final ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        _instance.setGeneralId(rs.getLong(1));
                        _instance.setExchangeSystemId(rs.getLong(2));
                        _instance.setExchangeId(rs.getLong(3));
                        _instance.setGeneralised(true);
                    }
                    rs.close();

                    if (GeneralInstance.LOG.isDebugEnabled()) {
                        GeneralInstance.LOG.debug(_instance.toString());
                    }
                } catch (final SQLException e) {
                    GeneralInstance.LOG.error("executeOneStatement", e);
                    throw new EFapsException(GeneralInstance.class, "create", e);
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
            } catch (final SQLException e) {
                throw new EFapsException("generaliseInstance", e);
            }
        }
    }

    /**
     * @param _instance Instance the id of the GeneralInstance will be retrieved for.
     * @throws EFapsException on  error
     */
    protected static void generaliseInstance(final Instance _instance)
        throws EFapsException
    {
        final Context context = Context.getThreadContext();
        ConnectionResource con = null;
        con = context.getConnectionResource();
        GeneralInstance.generaliseInstance(_instance, con);
    }

    /**
     * @param _instance instance the DeleteDefintions are wanted for
     * @param _con      Connection to be used
     * @return List of DeleteDefintion
     * @throws EFapsException on error
     */
    public static Collection<? extends DeleteDefintion> getDeleteDefintion(final Instance _instance,
                                                                              final ConnectionResource _con)
        throws EFapsException
    {
        final List<DeleteDefintion> ret = new ArrayList<>();
        if (_instance.isValid() && _instance.getType().isGeneralInstance() && !_instance.getType().isHistory()) {
            GeneralInstance.generaliseInstance(_instance, _con);
            final long id = _instance.getGeneralId();
            if (id > 0) {
                if (_instance.getType().getStoreId() > 0) {
                    ret.add(new DeleteDefintion(JDBCStoreResource.TABLENAME_STORE, "ID", id));
                    ret.add(new DeleteDefintion(JCRStoreResource.TABLENAME_STORE, "ID", id));
                    ret.add(new DeleteDefintion(AbstractStoreResource.TABLENAME_STORE, "ID", id));
                }
                ret.add(new DeleteDefintion(GeneralInstance.TABLENAME, "ID", id));
            }
        }
        return ret;
    }
}
