/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventType;
import org.efaps.ci.CIType;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class Insert extends Update
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Insert.class);

    /**
     * @param _type type of instance to insert
     * @see #addCreateUpdateAttributes
     * @see #addTables
     * @throws EFapsException on error
     */
    public Insert(final Type _type) throws EFapsException
    {
        super(_type, null);
        addCreateUpdateAttributes();
        addTables();
    }

    /**
     * @param _type type of instance to insert
     * @see #Insert(Type)
     * @throws EFapsException on error
     */
    public Insert(final String _type) throws EFapsException
    {
        this(Type.get(_type));
    }

    /**
     * @param _ciType  CIType to be inserted
     * @see #Insert(Type)
     * @throws EFapsException on error
     */
    public Insert(final CIType _ciType) throws EFapsException
    {
        this(_ciType.uuid);
    }

    /**
     * @param _uuid _uuid of the type to be inserted
     * @see #Insert(Type)
     * @throws EFapsException on error
     */
    public Insert(final UUID _uuid) throws EFapsException
    {
        this(Type.get(_uuid));
    }

    /**
     * Add all tables of the type to the expressions, because for the type an
     * insert must be made for all tables!!!
     */
    private void addTables()
    {
        for (final SQLTable table : getType().getTables()) {
            if (!getExpr4Tables().containsKey(table)) {
                getExpr4Tables().put(table, new HashMap<Attribute, Update.Value>());
            }
        }
    }

    /**
     * Add all attributes of the type which must be always updated and the
     * default values.
     *
     * @throws EFapsException from called method
     */
    private void addCreateUpdateAttributes() throws EFapsException
    {
        final Iterator<?> iter = getType().getAttributes().entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
            final Attribute attr = (Attribute) entry.getValue();
            final AttributeType attrType = attr.getAttributeType();
            if (attrType.isCreateUpdate()) {
                add(attr, false, (Object) null);
            }
            if (attr.getDefaultValue() != null) {
                add(attr, false, attr.getDefaultValue());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws EFapsException
    {
        final boolean hasAccess = getType().hasAccess(Instance.get(getType(), 0),
                        AccessTypeEnums.CREATE.getAccessType());

        if (!hasAccess) {
            throw new EFapsException(getClass(), "execute.NoAccess", getType());
        }
        executeWithoutAccessCheck();
    }

    /**
     * Executes the insert without checking the access rights. (but with
     * triggers):
     * <ol>
     * <li>executes the pre insert trigger (if exists)</li>
     * <li>executes the insert trigger (if exists)</li>
     * <li>executes if no insert trigger exists or the insert trigger is not
     * executed the update ({@see #executeWithoutTrigger})</li>
     * <li>executes the post insert trigger (if exists)</li>
     * </ol>
     *
     * @throws EFapsException thrown from {@link #executeWithoutTrigger} or when
     *             the Status is invalid
     * @see #executeWithoutTrigger
     */
    @Override
    public void executeWithoutAccessCheck() throws EFapsException
    {
        executeEvents(EventType.INSERT_PRE);
        if (!executeEvents(EventType.INSERT_OVERRIDE)) {
            executeWithoutTrigger();
        }
        executeEvents(EventType.INSERT_POST);
    }

    /**
     * The insert is done without calling triggers and check of access rights.
     *
     * @throws EFapsException if update not possible (unique key, object does
     *             not exists, etc...)
     */
    @Override
    public void executeWithoutTrigger() throws EFapsException
    {
        final Context context = Context.getThreadContext();
        ConnectionResource con = null;
        try {

            con = context.getConnectionResource();

            if (test4Unique()) {
                throw new EFapsException(getClass(), "executeWithoutAccessCheck.UniqueKeyError");
            }

            final SQLTable mainTable = getType().getMainTable();

            final long id = executeOneStatement(con, mainTable, getExpr4Tables().get(mainTable).values(), 0);

            setInstance(Instance.get(getInstance().getType(), id));

            for (final Entry<SQLTable, Map<Attribute, Value>> entry : getExpr4Tables().entrySet()) {
                final SQLTable table = entry.getKey();
                if ((table != mainTable) && !table.isReadOnly()) {
                    executeOneStatement(con, table, entry.getValue().values(), id);
                }
            }
            con.commit();
        } catch (final EFapsException e) {
            if (con != null) {
                con.abort();
            }
            throw e;
        } catch (final Throwable e) {
            if (con != null) {
                con.abort();
            }
            throw new EFapsException(getClass(), "executeWithoutAccessCheck.Throwable");
        }
    }

    /**
     * A new statement must be created an executed for one table. If the
     * parameter '_id' is set to <code>0</code>, a new id is generated. If the
     * JDBC driver supports method <code>getGeneratedKeys</code>, this method is
     * used, otherwise method {@link org.efaps.db.databases#getNewId} is used to
     * retrieve a new id value.
     *
     * @param _con      connection resource
     * @param _table    sql table used to insert
     * @param _expressions expressions
     * @param _id       new created id
     * @return new created id if parameter <i>_id</i> is set to <code>0</code>
     * @see #createOneStatement
     * @throws EFapsException on error
     */
    private long executeOneStatement(final ConnectionResource _con,
                                     final SQLTable _table,
                                     final Collection<Update.Value> _values,
                                     final long _id)
        throws EFapsException
    {
        long ret = _id;
        try {
            final SQLInsert insert = Context.getDbType().newInsert(_table.getSqlTable(),
                                                                   _table.getSqlColId(),
                                                                   (_id == 0));

            if (_id != 0) {
                insert.column(_table.getSqlColId(), _id);
            }
            if (_table.getSqlColType() != null) {
                insert.column(_table.getSqlColType(), getType().getId());
            }

            for (final Update.Value value : _values) {
                value.attribute.prepareDBInsert(insert, value.getValues());
            }

            final Long bck = insert.execute(_con.getConnection());
            if (bck != null)  {
                ret = bck;
            }

        } catch (final SQLException e) {
            Insert.LOG.error("executeOneStatement", e);
            throw new EFapsException(getClass(), "executeOneStatement.Exception", e, _table.getName());
        }
        return ret;
    }
}
