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

package org.efaps.update.datamodel;

import java.io.IOException;
import java.net.URL;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * This class is responsible for generating and updating of SQLTables in eFpas.<br>
 * It reads the definition of a SQL-Table with the
 * <code>org.apache.commons.digester.Digester</code> and generates the Objects
 * needed for a SQL-Tabel-definition.
 *
 * @author tmo
 * @version $Id$
 */
public class SQLTableUpdate extends AbstractUpdate {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(SQLTableUpdate.class);

  private final static Set<Link> ALLLINKS = new HashSet<Link>();

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public SQLTableUpdate() {
    super("Admin_DataModel_SQLTable", ALLLINKS);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static methods

  public static SQLTableUpdate readXMLFile(final URL _url) {
    SQLTableUpdate ret = null;

    try {
      final Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("datamodel-sqltable", SQLTableUpdate.class);
      // set the UUID
      digester.addCallMethod("datamodel-sqltable/uuid", "setUUID", 1);
      digester.addCallParam("datamodel-sqltable/uuid", 0);

      // create a new SQL-Table-Definition
      final String definition = "datamodel-sqltable/definition";
      digester.addObjectCreate(definition, Definition.class);
      digester.addSetNext(definition, "addDefinition");

      digester.addCallMethod(definition + "/version", "setVersion", 4);
      digester.addCallParam(definition + "/version/application", 0);
      digester.addCallParam(definition + "/version/global", 1);
      digester.addCallParam(definition + "/version/local", 2);
      digester.addCallParam(definition + "/version/mode", 3);
      // set the name of the SQL-Table-Definition
      digester.addCallMethod(definition + "/name", "setName", 1);
      digester.addCallParam(definition + "/name", 0);

      // set the column wich contains the TypeId
      digester.addCallMethod(definition + "/typeid-column", "setTypeIdColumn",
          1);
      digester.addCallParam(definition + "/typeid-column", 0);

      // set the parent
      digester.addCallMethod(definition + "/parent", "setParent", 1);
      digester.addCallParam(definition + "/parent", 0);

      final String database = definition + "/database";

      digester.addCallMethod(database + "/sql", "addSQL", 1);
      digester.addCallParam(database + "/sql", 0);

      digester.addCallMethod(database + "/table-name", "setSQLTableName", 1);
      digester.addCallParam(database + "/table-name", 0);

      digester.addCallMethod(database + "/parent-table",
          "setParentSQLTableName", 1);
      digester.addCallParam(database + "/parent-table", 0);

      digester.addCallMethod(database + "/table-name", "setSQLTableName", 1);
      digester.addCallParam(database + "/table-name", 0);

      digester.addCallMethod(database + "/create", "setCreate", 1,
          new Class[] { Boolean.class });
      digester.addCallParam(database + "/create", 0);

      digester.addCallMethod(database + "/update", "setUpdate", 1,
          new Class[] { Boolean.class });
      digester.addCallParam(database + "/update", 0);

      digester.addCallMethod(database + "/column", "addColumn", 4, new Class[] {
          String.class, String.class, Integer.class, Boolean.class });
      digester.addCallParam(database + "/column", 0, "name");
      digester.addCallParam(database + "/column", 1, "type");
      digester.addCallParam(database + "/column", 2, "length");
      digester.addCallParam(database + "/column", 3, "not-null");

      digester.addCallMethod(database + "/unique", "addUniqueKey", 2);
      digester.addCallParam(database + "/unique", 0, "name");
      digester.addCallParam(database + "/unique", 1, "columns");

      digester
          .addCallMethod(database + "/foreign", "addForeignKey", 4,
              new Class[] { String.class, String.class, String.class,
                  Boolean.class });
      digester.addCallParam(database + "/foreign", 0, "name");
      digester.addCallParam(database + "/foreign", 1, "key");
      digester.addCallParam(database + "/foreign", 2, "reference");
      digester.addCallParam(database + "/foreign", 3, "cascade");

      digester.addCallMethod(database + "/check", "addCheckKey", 2);
      digester.addCallParam(database + "/check", 0, "name");
      digester.addCallParam(database + "/check", 1, "condition");

      ret = (SQLTableUpdate) digester.parse(_url);

      if (ret != null) {
        ret.setURL(_url);
      }
    } catch (IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return ret;
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The class defines a column in a sql table.
   */
  private static class Column {

    /** Name of the column. */
    private final String name;

    /** Type of the column. */
    private final AbstractDatabase.ColumnType type;

    /** Length of the Column. */
    private final int length;

    /** Is null allowed in the column? */
    private final boolean isNotNull;

    private Column(final String _name, final AbstractDatabase.ColumnType _type,
                   final int _length, final boolean _notNull) {
      this.name = _name;
      this.type = _type;
      this.length = _length;
      this.isNotNull = _notNull;
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a column.
     *
     * @return string representation of this definition of a column
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).append("name", this.name).append("type",
          this.type).append("isNotNull", this.isNotNull).toString();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The class defines a unqiue key in a sql table.
   */
  private static class UniqueKey {

    /** Name of the unique key. */
    private final String name;

    /** Columns of the unique key. */
    private final String columns;

    private UniqueKey(final String _name, final String _columns) {
      this.name = _name;
      this.columns = _columns;
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a column.
     *
     * @return string representation of this definition of a column
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).append("name", this.name).append(
          "columns", this.columns).toString();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The class defines a foreign key in a sql table.
   */
  private static class ForeignKey {

    /** Name of the foreign key. */
    private final String name;

    /** Key of the foreign key. */
    private final String key;

    /** Reference of the foreign key. */
    private final String reference;

    /** Should a delete be cascaded? */
    private final boolean cascade;

    private ForeignKey(final String _name, final String _key,
                       final String _reference, final boolean _cascade) {
      this.name = _name;
      this.key = _key;
      this.reference = _reference;
      this.cascade = _cascade;
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a column.
     *
     * @return string representation of this definition of a column
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).append("name", this.name).append("key",
          this.key).append("reference", this.reference).toString();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The class defines a check constraint in a sql table.
   */
  private static class CheckKey {

    /** Name of the check constraint. */
    private final String name;

    /** Condition of the check constraint. */
    private final String condition;

    private CheckKey(final String _name, final String _condition) {
      this.name = _name;
      this.condition = _condition;
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a column.
     *
     * @return string representation of this definition of a column
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).append("name", this.name).append(
          "condition", this.condition).toString();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends AbstractDefinition {

    // /////////////////////////////////////////////////////////////////////////
    // instance variables

    /**
     * The SQL table name of the parent table (as name in the SQL database).
     */
    private String parentSQLTableName = null;

    /**
     * The SQL table name of the parent table (as internal name in eFaps).
     */
    private String parent = null;

    /**
     * SQL statement which is directly executed (e.g. to create a SQL view).
     *
     * @see #addSQL
     * @see #executeSQLs
     */
    private final List<String> sqls = new ArrayList<String>();

    private boolean create = false;

    private boolean update = false;

    private final List<Column> columns = new ArrayList<Column>();

    private final List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();

    private final List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

    private final List<CheckKey> checkKeys = new ArrayList<CheckKey>();

    // /////////////////////////////////////////////////////////////////////////

    /**
     * @see #values
     */
    public void setSQLTableName(final String _value) {
      addValue("SQLTable", _value);
      addValue("SQLColumnID", "ID");
    }

    /**
     *
     */
    public void setTypeIdColumn(final String _typeIdColumn) {
      addValue("SQLColumnType", _typeIdColumn);
    }

    /**
     * @see #values
     */
    public void setParentSQLTableName(final String _parentSQLTableName) {
      this.parentSQLTableName = _parentSQLTableName;
    }

    /**
     * Defines sql statements which is directly executed (e.g. to create a
     * view).
     *
     * @param _sql
     *                sql statement to execute
     * @see #sqls
     */
    public void addSQL(final String _sql) {
      this.sqls.add(_sql);
    }

    /**
     * @todo throw Exception is not allowed
     */
    public void setParent(final String _parent) throws Exception {
      if ((_parent != null) && (_parent.length() > 0)) {
        this.parent = _parent;
      }
    }

    public void setCreate(final boolean _create) {
      this.create = _create;
    }

    public void setUpdate(final boolean _update) {
      this.update = _update;
    }

    public void addColumn(final String _name, final String _type,
                          final int _length, final boolean _notNull) {
      this.columns.add(new Column(_name, Enum.valueOf(
          AbstractDatabase.ColumnType.class, _type), _length, _notNull));
    }

    public void addUniqueKey(final String _name, final String _columns) {
      this.uniqueKeys.add(new UniqueKey(_name, _columns));
    }

    public void addForeignKey(final String _name, final String _key,
                              final String _reference, final boolean _cascade) {
      this.foreignKeys.add(new ForeignKey(_name, _key, _reference, _cascade));
    }

    public void addCheckKey(final String _name, final String _condition) {
      this.checkKeys.add(new CheckKey(_name, _condition));
    }

    /**
     * @see #executeSQL
     * @see #createSQLTable
     * @see #updateSQLTable
     */
    @Override
    public void updateInDB(final Type _dataModelType, final String _uuid,
                           final Set<Link> _allLinkTypes,
                           final boolean _abstractType) throws EFapsException,
                                                       Exception {

      executeSQLs();
      if (this.create) {
        createSQLTable();
      }
      if (this.update) {
        updateSQLTable();
      }
      if (getValue("Name") != null) {

        // search for the parent SQL table name instance (if defined)
        if (this.parent != null) {
          final SearchQuery query = new SearchQuery();
          query.setQueryTypes("Admin_DataModel_SQLTable");
          query.addWhereExprEqValue("Name", this.parent);
          query.addSelect("OID");
          query.executeWithoutAccessCheck();
          if (query.next()) {
            final Instance instance = new Instance((String) query.get("OID"));
            addValue("DMTableMain", "" + instance.getId());
          }
          query.close();
        }

        super.updateInDB(_dataModelType, _uuid, _allLinkTypes, _abstractType);
      }
    }

    /**
     * Execute defined SQL statement in the database.
     *
     * @see #sqls
     * @see #updateInDB
     */
    protected void executeSQLs() throws EFapsException {
      final Context context = Context.getThreadContext();
      ConnectionResource con = null;
      try {
        con = context.getConnectionResource();
        final Statement stmt = con.getConnection().createStatement();
        for (String sql : this.sqls) {
          if (LOG.isDebugEnabled()) {
            LOG.info("    ..SQL> " + sql);
          }
          stmt.execute(sql);
        }
        con.commit();
      } catch (EFapsException e) {
        LOG.error("SQLTableUpdate.executeSQL.EFapsException", e);
        if (con != null) {
          con.abort();
        }
        throw e;
      } catch (Throwable e) {
        LOG.error("SQLTableUpdate.executeSQL.Throwable", e);
        if (con != null) {
          con.abort();
        }
        throw new EFapsException(getClass(), "executeSQL.Throwable", e);
      }
    }

    /**
     * Create the SQL table in the database.
     *
     * @see #updateInDB
     */
    protected void createSQLTable() throws EFapsException {
      final Context context = Context.getThreadContext();
      ConnectionResource con = null;
      String tableName = getValue("SQLTable");
      if (LOG.isInfoEnabled()) {
        LOG.info("    Create DB SQL Table '" + tableName + "'");
      }
      try {
        con = context.getConnectionResource();

        Context.getDbType().createTable(con.getConnection(), tableName,
            this.parentSQLTableName);
        con.commit();

      } catch (EFapsException e) {
        LOG.error("SQLTableUpdate.createSQLTable.EFapsException", e);
        if (con != null) {
          con.abort();
        }
        throw e;
      } catch (Throwable e) {
        LOG.error("SQLTableUpdate.createSQLTable.Throwable", e);
        if (con != null) {
          con.abort();
        }
        throw new EFapsException(getClass(), "createSQLTable.Throwable", e);
      }
    }

    /**
     * Udpate the SQL table in the database.
     *
     * @see #updateInDB
     */
    protected void updateSQLTable() throws EFapsException {
      final Context context = Context.getThreadContext();
      ConnectionResource con = null;
      final String tableName = getValue("SQLTable");
      if (LOG.isInfoEnabled()) {
        LOG.info("    Update DB SQL Table '" + tableName + "'");
      }
      try {
        con = context.getConnectionResource();

        // add columns
        for (final Column column : this.columns) {
          Context.getDbType().addTableColumn(con.getConnection(), tableName,
              column.name, column.type, null, column.length, column.isNotNull);
        }

        // add unique keys
        for (final UniqueKey uniqueKey : this.uniqueKeys) {
          Context.getDbType().addUniqueKey(con.getConnection(), tableName,
              uniqueKey.name, uniqueKey.columns);
        }

        // add foreign keys
        for (final ForeignKey foreignKey : this.foreignKeys) {
          Context.getDbType().addForeignKey(con.getConnection(), tableName,
              foreignKey.name, foreignKey.key, foreignKey.reference,
              foreignKey.cascade);
        }

        // update check keys
        for (final CheckKey checkKey : this.checkKeys) {
          Context.getDbType().addCheckKey(con.getConnection(), tableName,
              checkKey.name, checkKey.condition);
        }

        con.commit();

      } catch (EFapsException e) {
        LOG.error("SQLTableUpdate.updateSQLTable.EFapsException", e);
        if (con != null) {
          con.abort();
        }
        throw e;
      } catch (Throwable e) {
        LOG.error("SQLTableUpdate.updateSQLTable.Throwable", e);
        if (con != null) {
          con.abort();
        }
        throw new EFapsException(getClass(), "updateSQLTable.Throwable", e);
      }
    }
  }
}
