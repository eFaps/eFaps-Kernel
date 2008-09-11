/*
 * Copyright 2003-2008 The eFaps Team
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventType;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Insert extends Update {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Insert.class);

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * @param _type
   *                type of instance to insert
   * @see #addCreateUpdateAttributes
   * @see #addTables
   */
  public Insert(final Type _type) throws EFapsException {
    super(_type, null);

    addCreateUpdateAttributes();
    addTables();
  }

  /**
   * @param _type
   *                type of instance to insert
   * @see #Insert(Type)
   */
  public Insert(final String _type) throws EFapsException {
    this(Type.get(_type));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Add all tables of the type to the expressions, because for the type an
   * insert must be made for all tables!!!
   */
  private void addTables() {
    for (final SQLTable table : getType().getTables()) {
      if (getExpr4Tables().get(table) == null) {
        getExpr4Tables().put(table,
            new HashMap<String, AttributeTypeInterface>());
      }
    }

  }

  /**
   * Add all attributes of the type which must be always updated.
   *
   * @throws EFapsException
   *                 from called method
   */
  private void addCreateUpdateAttributes() throws EFapsException {
    final Iterator<?> iter = getType().getAttributes().entrySet().iterator();
    while (iter.hasNext()) {
      final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
      final Attribute attr = (Attribute) entry.getValue();
      final AttributeType attrType = attr.getAttributeType();
      if (attrType.isCreateUpdate()) {
        add(attr, null, false);
      }
    }
  }

  /**
   */
  @Override
  public void execute() throws EFapsException {
    final boolean hasAccess =
        getType().hasAccess(new Instance(getType()),
            AccessTypeEnums.CREATE.getAccessType());

    if (!hasAccess) {
      throw new EFapsException(getClass(), "execute.NoAccess", getType());
    }
    executeWithoutAccessCheck();
  }

  /**
   * Executes the insert without checking the access rights (but with triggers):
   * <ol>
   * <li>executes the pre insert trigger (if exists)</li>
   * <li>executes the insert trigger (if exists)</li>
   * <li>executes if no insert trigger exists or the insert trigger is not
   * executed the update ({@see #executeWithoutTrigger})</li>
   * <li>executes the post insert trigger (if exists)</li>
   * </ol>
   *
   * @throws EFapsException
   *                 thrown from {@link #executeWithoutTrigger} or when the
   *                 Status is invalid
   * @see #executeWithoutTrigger
   */
  @Override
  public void executeWithoutAccessCheck() throws EFapsException {
    executeEvents(EventType.INSERT_PRE);
    if (!executeEvents(EventType.INSERT_OVERRIDE)) {
      executeWithoutTrigger();
    }
    executeEvents(EventType.INSERT_POST);
  }

  /**
   * The insert is done without calling triggers and check of access rights.
   *
   * @throws EFapsException
   *                 if update not possible (unique key, object does not exists,
   *                 etc...)
   */
  @Override
  public void executeWithoutTrigger() throws EFapsException {
    final Context context = Context.getThreadContext();
    ConnectionResource con = null;
    try {

      con = context.getConnectionResource();

      if (test4Unique(context)) {
        throw new EFapsException(getClass(),
            "executeWithoutAccessCheck.UniqueKeyError");
      }

      final SQLTable mainTable = getType().getMainTable();

      final long id =
          executeOneStatement(context, con, mainTable, getExpr4Tables().get(
              mainTable), 0);

      setInstance(new Instance(getInstance().getType(), id));

      for (final Map.Entry<SQLTable, Map<String, AttributeTypeInterface>> entry : getExpr4Tables()
          .entrySet()) {
        final SQLTable table = entry.getKey();
        if ((table != mainTable) && !table.isReadOnly() && entry.getValue().size() > 0) {
          executeOneStatement(context, con, table, entry.getValue(), id);
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
      throw new EFapsException(getClass(),
          "executeWithoutAccessCheck.Throwable");
    }
  }

  /**
   * A new statement must be created an executed for one table. If the parameter
   * '_id' is set to <code>0</code>, a new id is generated. If the JDBC
   * driver supports method <code>getGeneratedKeys</code>, this method is
   * used, otherwise method {@link org.efaps.db.databases#getNewId} is used to
   * retrieve a new id value.
   *
   * @param _context
   *                context for this request
   * @param _con
   *                connection resource
   * @param _table
   *                sql table used to insert
   * @param _expressions
   * @param _id
   *                new created id
   * @return new created id if parameter <i>_id</i> is set to <code>0</code>
   * @see #createOneStatement
   */
  private long executeOneStatement(final Context _context,
                                   final ConnectionResource _con,
                                   final SQLTable _table,
                                   final Map<?, ?> _expressions, final long _id)
                                                                                throws EFapsException {

    long ret = _id;
    PreparedStatement stmt = null;
    try {
      if ((ret == 0) && !Context.getDbType().supportsGetGeneratedKeys()) {
        ret =
            Context.getDbType().getNewId(_con.getConnection(),
                _table.getSqlTable(), _table.getSqlColId());
      }

      stmt = createOneStatement(_con, _table, _expressions, ret);

      final int rows = stmt.executeUpdate();
      if (rows == 0) {
        throw new EFapsException(getClass(), "executeOneStatement.NotInserted",
            _table.getName());
      }
      if (ret == 0) {
        final ResultSet resultset = stmt.getGeneratedKeys();
        if (resultset.next()) {
          ret = resultset.getLong(1);
        }
        resultset.close();
      }
    } catch (final EFapsException e) {
      throw e;
    } catch (final Exception e) {
      throw new EFapsException(getClass(), "executeOneStatement.Exception", e,
          _table.getName());
    }
    finally {
      try {
        stmt.close();
      } catch (final Exception e) {
      }
    }
    return ret;
  }

  /**
   * @param _id
   *                new created id, if null, the table is an autoincrement SQL
   *                table and the id is not set
   * @return new created prepared statement
   */
  private PreparedStatement createOneStatement(final ConnectionResource _con,
                                               final SQLTable _table,
                                               final Map<?, ?> _expressions,
                                               final long _id)
                                                              throws SQLException {

    final List<AttributeTypeInterface> list =
        new ArrayList<AttributeTypeInterface>();
    final StringBuilder cmd = new StringBuilder();
    final StringBuilder val = new StringBuilder();
    boolean first = true;
    cmd.append("insert into ").append(_table.getSqlTable()).append("(");
    if (_id != 0) {
      cmd.append(_table.getSqlColId());
      first = false;
    }
    final Iterator<?> iter = _expressions.entrySet().iterator();
    while (iter.hasNext()) {
      final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();

      if (first) {
        first = false;
      } else {
        cmd.append(",");
        val.append(",");
      }
      cmd.append(entry.getKey());

      final AttributeTypeInterface attr =
          (AttributeTypeInterface) entry.getValue();
      if (!attr.prepareUpdate(val)) {
        list.add(attr);
      }
    }
    if (_table.getSqlColType() != null) {
      cmd.append(",").append(_table.getSqlColType());
      val.append(",?");
    }
    cmd.append(") values (");
    if (_id != 0) {
      cmd.append(_id);
    }
    cmd.append("").append(val).append(")");

    if (LOG.isTraceEnabled()) {
      LOG.trace(cmd.toString());
    }

    PreparedStatement stmt;
    if (_id == 0) {
      if (Context.getDbType().supportsMultiGeneratedKeys()) {
        stmt =
            _con.getConnection().prepareStatement(cmd.toString(),
                new String[] { _table.getSqlColId() });
      } else {
        stmt =
            _con.getConnection().prepareStatement(cmd.toString(),
                Statement.RETURN_GENERATED_KEYS);
      }
    } else {
      stmt = _con.getConnection().prepareStatement(cmd.toString());
    }
    for (int i = 0, j = 1; i < list.size(); i++, j++) {
      final AttributeTypeInterface attr = list.get(i);

      if (LOG.isTraceEnabled()) {
        LOG.trace(attr.toString());
      }
      // TODO remove List
      final List<Integer> x = new ArrayList<Integer>();
      x.add(j);
      attr.update(null, stmt, x);
    }
    if (_table.getSqlColType() != null) {
      stmt.setLong(list.size() + 1, getType().getId());
    }
    return stmt;
  }
}
