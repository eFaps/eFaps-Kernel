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

package org.efaps.update.datamodel;

import static org.efaps.db.Context.getDbType;

import java.net.URL;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.databases.information.ColumnInformation;
import org.efaps.db.databases.information.ForeignKeyInformation;
import org.efaps.db.databases.information.TableInformation;
import org.efaps.db.databases.information.UniqueKeyInformation;
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
public class SQLTableUpdate extends AbstractUpdate
{

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(SQLTableUpdate.class);

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   * @param _url        URL of the file
   */
  public SQLTableUpdate(final URL _url)
  {
    super(_url, "Admin_DataModel_SQLTable");
  }

  /**
   * Creates new instance of class {@link Definition}.
   *
   * @return new definition instance
   * @see Definition
   */
  @Override
  protected AbstractDefinition newDefinition()
  {
    return new Definition();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The class defines a column in a sql table.
   */
  private static class Column {

    /** Name of the column. */
    private final String name;

    /** Type of the column. */
    private final AbstractDatabase.ColumnType type;

    /** Length of the Column / Precision of a decimal. */
    private final int length;

    /** Is null allowed in the column? */
    private final boolean isNotNull;

    /** Scale of a decimal. */
    private final int scale;

    private Column(final String _name, final AbstractDatabase.ColumnType _type,
                   final int _length, final int _scale,
                   final boolean _notNull) {
      this.name = _name;
      this.type = _type;
      this.length = _length;
      this.scale = _scale;
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
      return new ToStringBuilder(this)
                .append("name", this.name)
                .append("key", this.key)
                .append("reference", this.reference)
                .toString();
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

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public class Definition extends AbstractDefinition {

    ///////////////////////////////////////////////////////////////////////////
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

    private final List<Column> columns = new ArrayList<Column>();

    private final List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();

    private final List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

    private final List<CheckKey> checkKeys = new ArrayList<CheckKey>();

    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String,String> _attributes,
                           final String _text)
    {
      final String value = _tags.get(0);
      if ("database".equals(value))  {
        if (_tags.size() > 1)  {
          final String subValue = _tags.get(1);
          if ("check".equals(subValue))  {
            this.checkKeys.add(new CheckKey(_attributes.get("name"),
                                            _attributes.get("condition")));
          } else if ("column".equals(subValue))  {
            final String lengthStr = _attributes.get("length");
            final String scaleStr = _attributes.get("scale");
            final int length = (lengthStr != null)
                               ? Integer.parseInt(lengthStr)
                               : 0;
            final int scale = (scaleStr != null)
                              ? Integer.parseInt(scaleStr)
                              : 0;
            this.columns.add(new Column(_attributes.get("name"),
                                Enum.valueOf(AbstractDatabase.ColumnType.class,
                                             _attributes.get("type")),
                                length,
                                scale,
                                "true".equals(_attributes.get("not-null"))));
          } else if ("foreign".equals(subValue))  {
            this.foreignKeys.add(new ForeignKey(_attributes.get("name"),
                                    _attributes.get("key"),
                                    _attributes.get("reference"),
                                    "true".equals(_attributes.get("cascade"))));
          } else if ("parent-table".equals(subValue))  {
            this.parentSQLTableName = _text;
          } else if ("sql".equals(subValue))  {
              this.sqls.add(_text);
          } else if ("table-name".equals(subValue))  {
            addValue("SQLTable", _text);
            addValue("SQLColumnID", "ID");
          } else if ("unique".equals(subValue))  {
            this.uniqueKeys.add(new UniqueKey(_attributes.get("name"),
                                              _attributes.get("columns")));
          }
        }
      } else if ("parent".equals(value))  {
        if ((_text != null) && !"".equals(_text)) {
          this.parent = _text;
        }
      } else if ("typeid-column".equals(value))  {
        addValue("SQLColumnType", _text);
      } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }

    /**
     * A new SQL table could only be created if a name is specified.
     *
     * @param _dataModelType  type to create
     */
    @Override
    public void createInDB()
        throws EFapsException
    {
      createSQLTable();
      if (getValue("Name") != null) {
        super.createInDB();
      }
    }

    @Override
    protected void createInDB(final Insert _insert) throws EFapsException
    {
      _insert.add("SQLTable", getValue("SQLTable"));
      _insert.add("SQLColumnID", getValue("SQLColumnID"));
      super.createInDB(_insert);
    }

    /**
     * @see #executeSQL
     * @see #createSQLTable
     * @see #updateSQLTable
     */
    @Override
    public void updateInDB(final Set<Link> _allLinkTypes)
        throws EFapsException
    {
      executeSQLs();
      updateSQLTable();
      if (getValue("Name") != null) {

        // search for the parent SQL table name instance (if defined)
        if (this.parent != null) {
          final SearchQuery query = new SearchQuery();
          query.setQueryTypes("Admin_DataModel_SQLTable");
          query.addWhereExprEqValue("Name", this.parent);
          query.addSelect("OID");
          query.executeWithoutAccessCheck();
          if (query.next()) {
            final Instance instance = Instance.get((String) query.get("OID"));
            addValue("DMTableMain", "" + instance.getId());
          }
          query.close();
        }

        super.updateInDB(_allLinkTypes);
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
        for (final String sql : this.sqls) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("    ..SQL> " + sql);
          }
          stmt.execute(sql);
        }
        con.commit();
      } catch (final EFapsException e) {
        LOG.error("SQLTableUpdate.executeSQL.EFapsException", e);
        if (con != null) {
          con.abort();
        }
        throw e;
      } catch (final Throwable e) {
        LOG.error("SQLTableUpdate.executeSQL.Throwable", e);
        if (con != null) {
          con.abort();
        }
        throw new EFapsException(getClass(), "executeSQL.Throwable", e);
      }
    }

    /**
     * If the SQL table does not exists in the database, create the SQL table.
     *
     * @see #updateInDB
     * @todo check for parent table if defined
     */
    protected void createSQLTable() throws EFapsException {
      final Context context = Context.getThreadContext();
      ConnectionResource con = null;
      final String tableName = getValue("SQLTable");
      try {
        con = context.getConnectionResource();

        if (!getDbType().existsTable(con.getConnection(), tableName)
            && !getDbType().existsView(con.getConnection(), tableName))  {
          if (LOG.isInfoEnabled()) {
            LOG.info("    Create DB SQL Table '" + tableName + "'");
          }

          getDbType().createTable(con.getConnection(),
                                          tableName,
                                          this.parentSQLTableName);
        }
        con.commit();

      } catch (final EFapsException e) {
        LOG.error("SQLTableUpdate.createSQLTable.EFapsException", e);
        if (con != null) {
          con.abort();
        }
        throw e;
      } catch (final Throwable e) {
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

        final TableInformation tableInfo = getDbType().getTableInformation(con.getConnection(), tableName);


        for (final Column column : this.columns)  {
          final ColumnInformation colInfo = tableInfo.getColInfo(column.name);
          if (colInfo != null)  {
            if (LOG.isDebugEnabled())  {
              LOG.debug("column '" + column.name + "' already defined in "
                        + "table '" + tableName + "'");
            }
// TODO: check for column types, column length and isNotNull
          } else  {
            getDbType().addTableColumn(con.getConnection(), tableName,
                column.name, column.type, null, column.length, column.scale,
                column.isNotNull);
          }
        }

        // add unique keys
        for (final UniqueKey uniqueKey : this.uniqueKeys)  {
          final UniqueKeyInformation ukInfo = tableInfo.getUKInfo(uniqueKey.name);
          if (ukInfo != null)  {
            if (LOG.isDebugEnabled())  {
              LOG.debug("unique key '" + uniqueKey.name + "' already defined in "
                        + "table '" + tableName + "'");
            }
// TODO: check for column names
          } else  {
            // check if a unique key exists for same column names
            final UniqueKeyInformation ukInfo2 = tableInfo.getUKInfoByColNames(uniqueKey.columns);
            if (ukInfo2 != null)  {
              LOG.error("unique key for columns " + uniqueKey.columns + " exists");
            } else  {
              getDbType().addUniqueKey(con.getConnection(), tableName,
                  uniqueKey.name, uniqueKey.columns);
            }
          }
        }

        // add foreign keys
        for (final ForeignKey foreignKey : this.foreignKeys) {
          final ForeignKeyInformation fkInfo = tableInfo.getFKInfo(foreignKey.name);
          if (fkInfo != null)  {
            if (LOG.isDebugEnabled())  {
              LOG.debug("foreign key '" + foreignKey.name + "' already defined in "
                        + "table '" + tableName + "'");
            }
// TODO: further updates
          } else  {
            getDbType().addForeignKey(con.getConnection(), tableName,
                foreignKey.name, foreignKey.key, foreignKey.reference,
                foreignKey.cascade);
          }
        }

        // update check keys
        for (final CheckKey checkKey : this.checkKeys) {
          getDbType().addCheckKey(con.getConnection(), tableName,
              checkKey.name, checkKey.condition);
        }

        con.commit();

      } catch (final EFapsException e) {
        LOG.error("SQLTableUpdate.updateSQLTable.EFapsException", e);
        if (con != null) {
          con.abort();
        }
        throw e;
      } catch (final Throwable e) {
        LOG.error("SQLTableUpdate.updateSQLTable.Throwable", e);
        if (con != null) {
          con.abort();
        }
        throw new EFapsException(getClass(), "updateSQLTable.Throwable", e);
      }
    }
  }
}
