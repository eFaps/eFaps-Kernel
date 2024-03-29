/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.update.schema.datamodel;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.databases.information.ColumnInformation;
import org.efaps.db.databases.information.ForeignKeyInformation;
import org.efaps.db.databases.information.TableInformation;
import org.efaps.db.databases.information.UniqueKeyInformation;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Handles the import / update of SQL tables for eFaps read from a XML
 * configuration item file.
 *
 * @author The eFaps Team
 */
public class SQLTableUpdate
    extends AbstractUpdate
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SQLTableUpdate.class);

    /**
     * Flag to indicate that the SQL table was created and that the auto
     * increment of the foreign key to parent SQL table must be created in the
     * database.
     *
     * @see Definition#createSQLTable()
     */
    private boolean created;

    /**
     * Default constructor to initialize this SQL table update instance for
     * given <code>_url</code>.
     *
     * @param _installFile the install file
     */
    public SQLTableUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_DataModel_SQLTable");
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

    /**
     * The class defines a column in a SQL table.
     */
    private static final class Column
    {
        /**
         * Name of the column.
         */
        private final String name;

        /**
         * Type of the column.
         */
        private final AbstractDatabase.ColumnType type;

        /**
         * Length of the Column / Precision of a decimal.
         */
        private final int length;

        /**
         * Is null allowed in the column?
         */
        private final boolean isNotNull;

        /**
         * Scale of a decimal.
         */
        private final int scale;

        /**
         * @param _name     Name
         * @param _type     column type
         * @param _length   length
         * @param _scale    scale
         * @param _notNull  not null
         */
        private Column(final String _name,
                       final AbstractDatabase.ColumnType _type,
                       final int _length,
                       final int _scale,
                       final boolean _notNull)
        {
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
        public String toString()
        {
            return new ToStringBuilder(this)
                .append("name", this.name)
                .append("type", this.type)
                .append("isNotNull", this.isNotNull)
                .toString();
        }
    }

    /**
     * Defines an unique key in a SQL table.
     */
    private static final class UniqueKey
    {
        /**
         * Name of the unique key.
         */
        private final String name;

        /**
         * Columns of the unique key.
         */
        private final String columns;

        /**
         * Default constructor.
         *
         * @param _name     name of the unique key
         * @param _columns  SQL table columns
         */
        private UniqueKey(final String _name,
                          final String _columns)
        {
            String nameTmp = null;
            try {
                nameTmp = Context.getDbType().getConstrainName(_name);
            } catch (final EFapsException e) {
                SQLTableUpdate.LOG.error("UniqueKey could not be retrieved. Name {}, columns: {} ", _name, _columns);
            }
            this.name = nameTmp;
            this.columns = _columns;
        }

        /**
         * Returns a string representation with values of all instance
         * variables of a column.
         *
         * @return string representation of this definition of a column
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this)
                .append("name", this.name)
                .append("columns", this.columns)
                .toString();
        }
    }

    /**
     * The class defines a foreign key in a sql table.
     */
    private static final class ForeignKey
    {
        /**
         * Name of the foreign key.
         */
        private final String name;

        /**
         * Key of the foreign key.
         */
        private final String key;

        /**
         * Reference of the foreign key.
         */
        private final String reference;

        /**
         * Should a delete be cascaded?
         */
        private final boolean cascade;

        /**
         *
         * @param _name         name of the foreign key
         * @param _key          key of the foreign key
         * @param _reference    reference of the foreign key
         * @param _cascade      <i>true</i> if cascade; otherwise <i>false</i>
         */
        private ForeignKey(final String _name,
                           final String _key,
                           final String _reference,
                           final boolean _cascade)
        {
            String nameTmp = null;
            try {
                nameTmp =  Context.getDbType().getConstrainName(_name);
            } catch (final EFapsException e) {
                SQLTableUpdate.LOG.error("ForeignKey could not be retrieved. Name {}, key: {} ", _name, _key);
            }
            this.name = nameTmp;
            this.key = _key;
            this.reference = _reference;
            this.cascade = _cascade;
        }

        /**
         * Returns a string representation with values of all instance
         * variables of a column.
         *
         * @return string representation of this definition of a column
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this)
                .append("name", this.name)
                .append("key", this.key)
                .append("reference", this.reference)
                .toString();
        }
    }

    /**
     * The class defines a check constraint in a sql table.
     */
    private static final class CheckKey
    {
        /**
         * Name of the check constraint.
         */
        private final String name;

        /**
         * Condition of the check constraint.
         */
        private final String condition;

        /**
         * Default constructor.
         *
         * @param _name         name of the check key
         * @param _condition    condition of the check key
         */
        private CheckKey(final String _name,
                         final String _condition)
        {
            this.name = _name;
            this.condition = _condition;
        }

        /**
         * Returns a string representation with values of all instance
         * variables of a column.
         *
         * @return string representation of this definition of a column
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this)
                .append("name", this.name)
                .append("condition", this.condition)
                .toString();
        }
    }

    private static final class Index
    {

        private final String name;
        private final String columns;

        private Index(final String name,
                      final String columns)
        {
            this.name = name;
            this.columns = columns;
        }
    }

    /**
     * Definition for SQLTable.
     */
    protected class Definition
        extends AbstractDefinition
    {
        /**
         * The SQL table name of the parent table (as name in the SQL database).
         */
        private String parentSQLTableName;

        /**
         * The SQL table name of the parent table (as internal name in eFaps).
         */
        private String parent;

        /**
         * SQL statement which is directly executed (e.g. to create a SQL view).
         *
         * @see #addSQL
         * @see #executeSQLs
         */
        private final List<String> sqls = new ArrayList<>();

        /**
         * Defines columns of the SQL table.
         */
        private final List<SQLTableUpdate.Column> columns = new ArrayList<>();

        /**
         * Defined unique keys of the SQL table.
         */
        private final List<SQLTableUpdate.UniqueKey> uniqueKeys = new ArrayList<>();

        /**
         * Defined foreign keys of the table.
         */
        private final List<SQLTableUpdate.ForeignKey> foreignKeys = new ArrayList<>();

        /**
         * Defined check keys of the table.
         */
        private final List<SQLTableUpdate.CheckKey> checkKeys = new ArrayList<>();

        private final List<SQLTableUpdate.Index> indexes = new ArrayList<>();

        /**
         * Is this table a view.
         */
        private boolean view;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
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
                        final int length = lengthStr != null
                                           ? Integer.parseInt(lengthStr)
                                           : 0;
                        final int scale = scaleStr != null
                                          ? Integer.parseInt(scaleStr)
                                          : 0;
                        this.columns.add(new Column(_attributes.get("name"),
                                            Enum.valueOf(AbstractDatabase.ColumnType.class,
                                                         _attributes.get("type")),
                                            length,
                                            scale,
                                            "true".equals(_attributes.get("not-null"))));
                    } else if ("index".equals(subValue))  {
                        this.indexes.add(new Index(_attributes.get("name"), _attributes.get("columns")));
                    } else if ("foreign".equals(subValue))  {
                        this.foreignKeys.add(new ForeignKey(_attributes.get("name"),
                                                            _attributes.get("key"),
                                                            _attributes.get("reference"),
                                                            "true".equals(_attributes.get("cascade"))));
                    } else if ("parent-table".equals(subValue))  {
                        this.parentSQLTableName = Context.getDbType().getTableName(_text);
                    } else if ("sql".equals(subValue))  {
                        this.sqls.add(_text);
                    } else if ("table-name".equals(subValue))  {
                        addValue("SQLTable", Context.getDbType().getTableName(_text));
                        addValue("SQLColumnID", "ID");
                    } else if ("view-name".equals(subValue))  {
                        addValue("SQLTable", Context.getDbType().getTableName(_text));
                        addValue("SQLColumnID", "ID");
                        this.view = true;
                    } else if ("unique".equals(subValue))  {
                        this.uniqueKeys.add(new UniqueKey(_attributes.get("name"),
                                                          _attributes.get("columns")));
                    }
                }
            } else if ("parent".equals(value))  {
                if (_text != null && !"".equals(_text)) {
                    this.parent = _text;
                }
            } else if ("typeid-column".equals(value))  {
                addValue("SQLColumnType", _text);
            } else  {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * Appends to the update the SQL table specific attribute values to the
         * <code>_insert</code>.
         *
         * @param _insert   insert instance to append SQL table specific
         *                  attributes
         * @throws InstallationException if insert of the eFaps definition for
         *                               the SQL table failed
         */
        @Override
        protected void createInDB(final Insert _insert)
            throws InstallationException
        {
            try {
                _insert.add("SQLTable", getValue("SQLTable"));
            } catch (final EFapsException e) {
                throw new InstallationException("Could not add SQLTable attribute", e);
            }
            try {
                _insert.add("SQLColumnID", getValue("SQLColumnID"));
            } catch (final EFapsException e) {
                throw new InstallationException("Could not add SQLColumnID attribute", e);
            }
            super.createInDB(_insert);
        }

        /**
         * Specific update instructions for the update for SQL tables.
         * Depending on the {@link UpdateLifecycle life cycle}
         * <code>_step</code> following is done:
         * <table border="1">
         * <tr><th>Life Cycle Step</th><th>Description</th></tr>
         * <tr><td>{@link UpdateLifecycle#SQL_CREATE_TABLE SQL_CREATE_TABLE}
         *         </td>
         *     <td></td></tr>
         * <tr><td>{@link UpdateLifecycle#SQL_UPDATE_ID SQL_UPDATE_ID}</td>
         *     <td></td></tr>
         * <tr><td>{@link UpdateLifecycle#SQL_UPDATE_TABLE SQL_UPDATE_TABLE}
         *         </td>
         *     <td></td></tr>
         * <tr><td>{@link UpdateLifecycle#SQL_RUN_SCRIPT SQL_RUN_SCRIPT}</td>
         *     <td></td></tr>
         * <tr><td>{@link UpdateLifecycle#EFAPS_UPDATE EFAPS_UPDATE}</td>
         *     <td></td></tr>
         * </table>
         *
         * @param _step             current life cycle update step
         * @param _allLinkTypes     all link types
         * @throws InstallationException if update failed
         * @see #createSQLTable()
         * @see #updateColIdSQLTable()
         * @see #updateSQLTable()
         * @see #executeSQLs()
         */
        @Override
        public final MultiValuedMap<String, String> updateInDB(final UpdateLifecycle _step,
                                                               final Set<Link> _allLinkTypes)
            throws InstallationException
        {
            MultiValuedMap<String, String> ret = MultiMapUtils.newSetValuedHashMap();
            try {
                if (_step == UpdateLifecycle.SQL_CREATE_TABLE)  {
                    if (!this.view) {
                        createSQLTable();
                    }
                    ret = super.updateInDB(_step, _allLinkTypes);
                } else if (_step == UpdateLifecycle.SQL_UPDATE_ID && !this.view)  {
                    updateColIdSQLTable();
                    ret = super.updateInDB(_step, _allLinkTypes);
                } else if (_step == UpdateLifecycle.SQL_UPDATE_TABLE && !this.view)  {
                    updateSQLTable();
                    ret = super.updateInDB(_step, _allLinkTypes);
                } else if (_step == UpdateLifecycle.SQL_RUN_SCRIPT)  {
                    executeSQLs();
                    ret = super.updateInDB(_step, _allLinkTypes);
                } else if (_step == UpdateLifecycle.EFAPS_UPDATE)  {
                    if (getValue("Name") != null) {
                        // search for the parent SQL table name instance (if defined)
                        if (this.parent != null) {
                            final QueryBuilder queryBldr = new QueryBuilder(CIAdminDataModel.SQLTable);
                            queryBldr.addWhereAttrEqValue(CIAdminDataModel.SQLTable.Name, this.parent);
                            final InstanceQuery query = queryBldr.getQuery();
                            query.executeWithoutAccessCheck();
                            if (query.next()) {
                                final Instance instance = query.getCurrentValue();
                                addValue(CIAdminDataModel.SQLTable.DMTableMain.name, "" + instance.getId());
                            }
                        }
                        ret = super.updateInDB(_step, _allLinkTypes);
                    }
                } else  {
                    ret = super.updateInDB(_step, _allLinkTypes);
                }
            } catch (final EFapsException e) {
                throw new InstallationException(" SQLTable can not be updated", e);
            }
            return ret;
        }

        @Override
        protected void removeObsoleteEvents(final Instance _instance,
                                            final List<Instance> _eventInstList)
            throws EFapsException
        {
            // in this case no events are possible so do nothing
        }

        /**
         * Execute defined {@link #sqls SQL statements} in the database.
         *
         * @throws InstallationException if SQL scripts could not be executed
         * @see #sqls
         * @see #updateInDB
         */
        @SuppressFBWarnings(value = { "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE" },
                        justification = "The script cannot be made static")
        protected void executeSQLs()
            throws InstallationException
        {
            if (!this.sqls.isEmpty()) {
                if (SQLTableUpdate.LOG.isInfoEnabled())  {
                    SQLTableUpdate.LOG.info("    Execute Script for DB SQL '" + getValue("SQLTable") + "'");
                }
                Connection con = null;
                try {
                    con = Context.getConnection();
                    if (this.view) {
                        final String tableName = getValue("SQLTable");
                        if (Context.getDbType().existsView(con, tableName)) {
                            Context.getDbType().deleteView(con, tableName);
                        }
                    }

                    final Statement stmt = con.createStatement();
                    for (final String sql : this.sqls) {
                        if (SQLTableUpdate.LOG.isDebugEnabled()) {
                            SQLTableUpdate.LOG.debug("    ..SQL> " + sql);
                        }
                        stmt.execute(sql);
                    }
                    stmt.close();
                    con.commit();
                } catch (final EFapsException e) {
                    throw new InstallationException("SQLTable can not be updated", e);
                } catch (final SQLException e) {
                    throw new InstallationException("SQLTable can not be updated", e);
                } finally {
                    try {
                        if (con != null && !con.isClosed()) {
                            con.close();
                        }
                    } catch (final SQLException e) {
                        throw new InstallationException("SQLTable can not be updated", e);
                    }
                }
            }
        }

        /**
         * If the SQL table does not exists in the database, create the SQL table.
         *
         * @throws EFapsException if create of the SQL tables failed
         * @see #updateInDB(UpdateLifecycle, Set)
         */
        protected void createSQLTable()
            throws EFapsException
        {
            Connection con = null;
            final String tableName = getValue("SQLTable");
            try {
                con = Context.getConnection();

                if (!Context.getDbType().existsTable(con, tableName)
                        && !Context.getDbType().existsView(con, tableName))  {

                    SQLTableUpdate.LOG.info("    Create DB SQL Table '{}' for '{}'", tableName, getValue("Name"));
                    Context.getDbType().createTable(con, tableName);
                    SQLTableUpdate.this.created = true;
                }
                con.commit();
            } catch (final SQLException e) {
                SQLTableUpdate.LOG.error("SQLTableUpdate.createSQLTable.EFapsException", e);
                throw new EFapsException("SQLTableUpdate.createSQLTable.EFapsException", e);
            } finally {
                try {
                    if (con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    throw new CacheReloadException("Cannot read a type for an attribute.", e);
                }
            }
        }

        /**
         * Update the column ID of SQL table in the database. The column ID
         * must be auto increment or defined as foreign key to another SQL
         * table (if {@link #parentSQLTableName parent SQL table name} is
         * defined in the XML configuration item file).
         *
         * @throws EFapsException if update of the SQL tables failed
         * @throws InstallationException if update of the SQL tables failed
         * @see #updateInDB(UpdateLifecycle, Set)
         */
        protected void updateColIdSQLTable()
            throws EFapsException, InstallationException
        {
            if (SQLTableUpdate.this.created)  {
                SQLTableUpdate.this.created = false;
                Connection con = null;
                final String tableName = getValue("SQLTable");
                if (SQLTableUpdate.LOG.isInfoEnabled()) {
                    if (this.parentSQLTableName != null)  {
                        SQLTableUpdate.LOG.info("    Define ID column for SQL Table '" + tableName + "' "
                                + "(parent '" + this.parentSQLTableName + "')");
                    } else  {
                        SQLTableUpdate.LOG.info("    Define ID column for SQL Table '" + tableName + "'");
                    }
                }
                try {
                    con = Context.getConnection();

                    if (this.parentSQLTableName != null)  {
                        Context.getDbType().defineTableParent(con, tableName, this.parentSQLTableName);
                    } else  {
                        Context.getDbType().defineTableAutoIncrement(con, tableName);
                    }
                    con.commit();
                }  catch (final SQLException e) {
                    SQLTableUpdate.LOG.error("SQLTableUpdate.updateSQLTable.EFapsException", e);
                    throw new EFapsException(getClass(), "updateSQLTable.Throwable", e);
                } finally {
                    try {
                        if (con != null && !con.isClosed()) {
                            con.close();
                        }
                    } catch (final SQLException e) {
                        throw new CacheReloadException("Cannot read a type for an attribute.", e);
                    }
                }
            }
        }

        /**
         * Update the SQL table in the database.
         *
         * @throws InstallationException if update of the SQL tables failed
         * @see #updateInDB(UpdateLifecycle, Set)
         */
        protected void updateSQLTable()
            throws InstallationException
        {
            Connection con = null;
            final String tableName = getValue("SQLTable");
            SQLTableUpdate.LOG.info("    Update DB SQL Table '{}'", tableName);
            try {
                con = Context.getConnection();
                final TableInformation tableInfo = Context.getDbType().getRealTableInformation(con, tableName);

                for (final Column column : this.columns)  {
                    final ColumnInformation colInfo = tableInfo.getColInfo(column.name);
                    if (colInfo != null)  {
                        SQLTableUpdate.LOG.debug("column '{}' already defined in table '{}'", column.name, tableName);
                        // null must be handeled sperately
                        if (colInfo.isNullable() == column.isNotNull) {
                            Context.getDbType().updateColumnIsNotNull(con, tableName, column.name,
                                            column.isNotNull);
                        }
                        // the scale must never be made smaller, because it would lead to data loss
                        if (column.length > 0
                                        && colInfo.getSize() < column.length && colInfo.getScale() <= column.scale) {
                            try {
                                Context.getDbType().updateColumn(con, tableName, column.name,
                                                column.type, column.length, column.scale);
                            } catch (final SQLException e) {
                                SQLTableUpdate.LOG
                                    .warn("Catched SQL Exception while trying to update Table: '{}', Column: '{}'",
                                                tableName, column.name);
                            }
                        }
                    } else  {
                        Context.getDbType().addTableColumn(con, tableName,
                                column.name, column.type, null, column.length, column.scale);
                        if (column.isNotNull) {
                            Context.getDbType().updateColumnIsNotNull(con, tableName, column.name,
                                            column.isNotNull);
                        }
                    }
                }

                // add unique keys
                for (final UniqueKey uniqueKey : this.uniqueKeys)  {
                    final UniqueKeyInformation ukInfo = tableInfo.getUKInfo(uniqueKey.name);
                    if (ukInfo != null)  {
                        if (SQLTableUpdate.LOG.isDebugEnabled())  {
                            SQLTableUpdate.LOG.debug("unique key '" + uniqueKey.name + "' already defined in "
                                + "table '" + tableName + "'");
                        }
                    } else  {
                        // check if a unique key exists for same column names
                        final UniqueKeyInformation ukInfo2 = tableInfo.getUKInfoByColNames(uniqueKey.columns);
                        if (ukInfo2 != null)  {
                            SQLTableUpdate.LOG.error("unique key for columns " + uniqueKey.columns + " exists");
                        } else  {
                            Context.getDbType().addUniqueKey(con, tableName,
                                    uniqueKey.name, uniqueKey.columns);
                        }
                    }
                }

                // add foreign keys
                for (final ForeignKey foreignKey : this.foreignKeys) {
                    final ForeignKeyInformation fkInfo = tableInfo.getFKInfo(foreignKey.name);
                    if (fkInfo != null)  {
                        if (SQLTableUpdate.LOG.isDebugEnabled())  {
                            SQLTableUpdate.LOG.debug("foreign key '" + foreignKey.name + "' already defined in "
                                + "table '" + tableName + "'");
                        }
                    } else  {
                        Context.getDbType().addForeignKey(con, tableName,
                                foreignKey.name, foreignKey.key, foreignKey.reference,
                                foreignKey.cascade);
                    }
                }
                // update check keys
                for (final CheckKey checkKey : this.checkKeys) {
                    Context.getDbType().addCheckKey(con, tableName,
                            checkKey.name, checkKey.condition);
                }

                // add indexes
                for (final var index: indexes) {
                    Context.getDbType().upsertIndex(con, tableName, index.name, index.columns);
                }
                con.commit();
            } catch (final EFapsException e) {
                throw new InstallationException("update of the SQL table failed", e);
            } catch (final SQLException e) {
                throw new InstallationException("update of the SQL table failed", e);
            } finally {
                try {
                    if (con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    throw new InstallationException("Cannot read a type for an attribute.", e);
                }
            }
        }
    }
}
