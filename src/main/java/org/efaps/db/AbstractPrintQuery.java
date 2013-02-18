/*
 * Copyright 2003 - 2012 The eFaps Team
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

import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Type;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.ci.CIAttribute;
import org.efaps.db.print.OneSelect;
import org.efaps.db.print.Phrase;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class all print queries are based on.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractPrintQuery
{
    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(PrintQuery.class);

    /**
     * Mapping of Select statements to OneSelect.
     */
    private final Map<String, OneSelect> selectStmt2OneSelect = new HashMap<String, OneSelect>();

    /**
     * Mapping of attributes to OneSelect.
     */
    private final Map<String, OneSelect> attr2OneSelect = new HashMap<String, OneSelect>();

    /**
     * Mapping of sql tables to table index.
     * @see #tableIndex
     */
    private final Map<String, Integer> sqlTable2Index = new HashMap<String, Integer>();

    /**
     * Mapping of key to Phrase.
     */
    private final Map<String, Phrase> key2Phrase = new HashMap<String, Phrase>();

    /**
     * List of all OneSelect belonging to this PrintQuery.
     */
    private final List<OneSelect> allSelects = new ArrayList<OneSelect>();

    /**
     * Index of an sqltable.
     */
    private int tableIndex = 0;

    /**
     * Must the list of instance be in the same order as given.
     * (There are some cases the sequence might be different returned from the
     * database. To enforce the exact sequence this flag can be set. But sorting
     * takes time and should not be used by default.)
     */
    private boolean enforceSorted;

    /**
     * Index of the type column.
     */
    private int typeColumnIndex = 0;

    /**
     * Add an attribute to the PrintQuery. It is used to get editable values
     * from the eFaps DataBase.
     *
     * @param _attributes    Attribute to add
     * @return this PrintQuery
     * @throws EFapsException on error
     */
    public AbstractPrintQuery addAttribute(final CIAttribute... _attributes)
        throws EFapsException
    {
        if (isMarked4execute()) {
            for (final CIAttribute attr : _attributes) {
                addAttribute(attr.name);
            }
        }
        return this;
    }

    /**
     * Add an attribute to the PrintQuery. It is used to get editable values
     * from the eFaps DataBase.
     *
     * @param _attributes    Attribute to add
     * @return this PrintQuery
     */
    public AbstractPrintQuery addAttribute(final Attribute... _attributes)
    {
        if (isMarked4execute()) {
            for (final Attribute attr : _attributes) {
                final OneSelect oneselect = new OneSelect(this, attr);
                this.allSelects.add(oneselect);
                this.attr2OneSelect.put(attr.getName(), oneselect);
            }
        }
        return this;
    }

    /**
     * Add a oneselect to this print query.
     *
     * @param _oneSelect select to be added
     */
    protected void addOneSelect(final OneSelect _oneSelect)
    {
        this.allSelects.add(_oneSelect);
    }

    /**
     * Getter method for instance variable {@link #allSelects}.
     *
     * @return value of instance variable {@link #allSelects}
     */
    protected List<OneSelect> getAllSelects()
    {
        return this.allSelects;
    }

    /**
     * Method to get the attribute for an <code>_attributeName</code>.
     *
     * @param _attributeName name of the attribute
     * @return Attribute
     */
    public Attribute getAttribute4Attribute(final String _attributeName)
    {
        final OneSelect oneselect = this.attr2OneSelect.get(_attributeName);
        return oneselect == null ? null : oneselect.getAttribute();
    }

    /**
     * Method to get the instance for an <code>_attributeName</code>.
     * @param _attributeName name of the attribute
     * @return list of instance
     * @throws EFapsException on error
     */
    public List<Instance> getInstances4Attribute(final String _attributeName)
        throws EFapsException
    {
        final OneSelect oneselect = this.attr2OneSelect.get(_attributeName);
        return oneselect == null ? null : oneselect.getInstances();
    }

    /**
     * Get the object returned by the given name of an attribute.
     *
     * @param <T>               class the return value will be casted to
     * @param _attribute        attribute the object is wanted for
     * @return object for the select statement
     * @throws EFapsException on error
     */
    public <T> T getAttribute(final CIAttribute _attribute)
        throws EFapsException
    {
        return this.<T>getAttribute(_attribute.name);
    }

    /**
     * Get the object returned by the given name of an attribute.
     *
     * @param <T>               class the return value will be casted to
     * @param _attributeName    name of the attribute the object is wanted for
     * @return object for the select statement
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(final String _attributeName)
        throws EFapsException
    {
        final OneSelect oneselect = this.attr2OneSelect.get(_attributeName);
        return oneselect == null ? null : (T) oneselect.getObject();
    }

    /**
     * Get the object returned by the given Attribute.
     *
     * @param <T>           class the return value will be casted to
     * @param _attribute    the object is wanted for
     * @return object for the select statement
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(final Attribute _attribute)
        throws EFapsException
    {
        return (T) getAttribute(_attribute.getName());
    }

    /**
     * Method returns the Main type of the query. In case that the query is
     * based on only one type, this Type is returned. In case that the query
     * contains different Types, the type returned must be the type, all other
     * types are derived from.
     *
     * @return Type
     */
    public abstract Type getMainType();

    /**
     * Method to get the instances this PrintQuery is executed on.
     *
     * @return List of instances
     */
    public abstract List<Instance> getInstanceList();

    /**
     * Method to get the current Instance.
     *
     * @return current Instance
     */
    public abstract Instance getCurrentInstance();

    /**
     * Add an AttributeSet to the PrintQuery. It is used to get editable values
     * from the eFaps DataBase.
     *
     * @param _setName    Name of the AttributeSet to add
     * @return this PrintQuery
     * @throws EFapsException  on error
     */
    public AbstractPrintQuery addAttributeSet(final String _setName)
        throws EFapsException
    {
        final Type type = getMainType();
        final AttributeSet set = AttributeSet.find(type.getName(), _setName);
        addAttributeSet(set);
        return this;
    }

    /**
     * Add an AttributeSet to the PrintQuery. It is used to get editable values
     * from the eFaps DataBase. The AttributeSet is internally transformed into
     * an linkfrom query.
     *
     * @param _set    AttributeSet to add
     * @return this PrintQuery
     * @throws EFapsException  on error
     */
    public AbstractPrintQuery addAttributeSet(final AttributeSet _set)
        throws EFapsException
    {
        final String key = "linkfrom[" + _set.getName() + "#" + _set.getAttributeName() + "]";
        final OneSelect oneselect = new OneSelect(this, key);
        this.allSelects.add(oneselect);
        this.attr2OneSelect.put(_set.getAttributeName(), oneselect);
        oneselect.analyzeSelectStmt();
        for (final String setAttrName :  _set.getSetAttributes()) {
            if (!setAttrName.equals(_set.getAttributeName())) {
                oneselect.getFromSelect().addOneSelect(new OneSelect(this, _set.getAttribute(setAttrName)));
            }
        }
        oneselect.getFromSelect().getMainOneSelect().setAttribute(_set.getAttribute(_set.getAttributeName()));
        return this;
    }

    /**
     * Get the object returned by the given name of an AttributeSet.
     *
     * @param <T>           class the return value will be casted to
     * @param _setName      name of the AttributeSet the object is wanted for
     * @return object for the select statement
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttributeSet(final String _setName)
        throws EFapsException
    {
        final OneSelect oneselect = this.attr2OneSelect.get(_setName);
        Map<String, Object> ret = null;
        if (oneselect.getFromSelect().hasResult()) {
            ret = new HashMap<String, Object>();
            // in an attributset the first one is fake
            boolean first = true;
            for (final OneSelect onsel : oneselect.getFromSelect().getAllSelects()) {
                if (first) {
                    first = false;
                } else {
                    final ArrayList<Object> list = new ArrayList<Object>();
                    final Object object = onsel.getObject();
                    if (object instanceof List<?>) {
                        list.addAll((List<?>) object);
                    } else {
                        list.add(object);
                    }
                    ret.put(onsel.getAttribute().getName(), list);
                }
            }
        }
        return (T) ret;
    }
    /**
     * Add an attribute to the PrintQuery. It is used to get editable values
     * from the eFaps DataBase.
     *
     * @param _attrNames    Name of the Attribute to add
     * @return this PrintQuery
     * @throws EFapsException on error
     */
    public AbstractPrintQuery addAttribute(final String... _attrNames)
        throws EFapsException
    {
        final Type type = getMainType();
        if (type != null) {
            for (final String attrName : _attrNames) {
                final Attribute attr = type.getAttribute(attrName);
                if (attr == null) {
                    final AttributeSet set = AttributeSet.find(type.getName(), attrName);
                    if (set != null) {
                        addAttributeSet(set);
                    }
                } else {
                    addAttribute(attr);
                }
            }
        }
        return this;
    }

    /**
     * Add an expression to this PrintQuery. An expresson is something like:
     * <code>if class[Emperador_Products_ClassFloorLaminate].linkto[SurfaceAttrId].
     * attribute[Value] == "abc" then 'hallo' else 'ifdef'</code>
     *
     * @param _key          key to the expression
     * @param _expression   expression to add
     * @return this PrintQuery
     * @throws EFapsException   always!!
     */
    public AbstractPrintQuery addExpression(final String _key,
                                            final String _expression)
        throws EFapsException
    {
        throw new EFapsException("PrintQuery.addExpression id not yet implemented", null);
    }

    /**
     * Get the object returned by the expression belonging to the given key.
     *
     * @param <T>           class the return value will be casted to
     * @param _key          key for an expression the object is wanted for
     * @return object for the expression
     * @throws EFapsException always
     */
    public <T> T getExpression(final String _key)
        throws EFapsException
    {
        throw new EFapsException("PrintQuery.getExpression id not yet implemented", null);
    }

    /**
     * Add a Phrase to this PrintQuery. A Phrase is something like:
     * <code>"$&lt;attribute[LastName]&gt; - $&lt;attribute[FirstName]&gt;"</code>
     * This would return " John - Doe". One Phrase can contain various selects
     * as defined for {@link #addSelect(String...)} and string to connect them.
     *
     * @param _key          key the phrase can be accessed
     * @param _phraseStmt   phrase to add
     * @throws EFapsException on error
     * @return this PrintQuery
     */
    public AbstractPrintQuery addPhrase(final String _key,
                                        final String _phraseStmt)
        throws EFapsException
    {
        ValueList list = null;

        final ValueParser parser = new ValueParser(new StringReader(_phraseStmt));
        try {
            list = parser.ExpressionString();
        } catch (final ParseException e) {
            throw new EFapsException(PrintQuery.class.toString(), e);
        }
        final Phrase phrase = new Phrase(_key, _phraseStmt, list);
        this.key2Phrase.put(_key, phrase);

        for (final String selectStmt : list.getExpressions()) {
            final OneSelect oneselect = new OneSelect(this, selectStmt);
            this.allSelects.add(oneselect);
            phrase.addSelect(oneselect);
            oneselect.analyzeSelectStmt();
        }
        return this;
    }

    /**
     * Get the String representation of a phrase.
     *
     * @param _key  key to the phrase
     * @return  String representation of the phrase
     * @throws EFapsException on error
     */
    public String getPhrase(final String _key)
        throws EFapsException
    {
        final Phrase phrase = this.key2Phrase.get(_key);
        return phrase == null ? null : phrase.getPhraseValue(getCurrentInstance());
    }

    /**
     * Add an select to the PrintQuery. A select is something like:
     * <code>class[Emperador_Products_ClassFloorLaminate].linkto[SurfaceAttrId].attribute[Value]</code>
     * <br>
     * The use of the key words like "class" etc is mandatory. Contrary to
     * {@link #addPhrase(String, String)} the values will not be parsed! The
     * values will not be editable.
     *
     * @param _selectBldrs selectBuilder to be added
     * @return this PrintQuery
     * @throws EFapsException   on error
     */
    public AbstractPrintQuery addSelect(final SelectBuilder... _selectBldrs)
        throws EFapsException
    {
        if (isMarked4execute()) {
            for (final SelectBuilder selectBldr : _selectBldrs) {
                addSelect(selectBldr.toString());
            }
        }
        return this;
    }

    /**
     * Add an select to the PrintQuery. A select is something like:
     * <code>class[Emperador_Products_ClassFloorLaminate].linkto[SurfaceAttrId].attribute[Value]</code>
     * <br>
     * The use of the key words like "class" etc is mandatory. Contrary to
     * {@link #addPhrase(String, String)} the values will not be parsed! The
     * values will not be editable.
     *
     * @param _selectStmts selectStatments to be added
     * @return this PrintQuery
     * @throws EFapsException   on error
     */
    public AbstractPrintQuery addSelect(final String... _selectStmts)
        throws EFapsException
    {
        if (isMarked4execute()) {
            for (final String selectStmt : _selectStmts) {
                final OneSelect oneselect = new OneSelect(this, selectStmt);
                this.allSelects.add(oneselect);
                this.selectStmt2OneSelect.put(selectStmt, oneselect);
                oneselect.analyzeSelectStmt();
            }
        }
        return this;
    }

    /**
     * Get the object returned by the given select statement.
     *
     * @param <T>           class the return value will be casted to
     * @param _selectStmt   select statement the object is wanted for
     * @return object for the select statement
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public <T> T getSelect(final String _selectStmt)
        throws EFapsException
    {
        final OneSelect oneselect = this.selectStmt2OneSelect.get(_selectStmt);
        return oneselect == null ? null : (T) oneselect.getObject();
    }

    /**
     * Get the object returned by the given select statement.
     *
     * @param <T>           class the return value will be casted to
     * @param _selectBldr   select bldr the object is wanted for
     * @return object for the select statement
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public <T> T getSelect(final SelectBuilder _selectBldr)
        throws EFapsException
    {
        final OneSelect oneselect = this.selectStmt2OneSelect.get(_selectBldr.toString());
        return oneselect == null ? null : (T) oneselect.getObject();
    }

    /**
     * Method to get the Attribute used for an select.
     *
     * @param _selectStmt   selectstatement the attribute is wanted for
     * @return  Attribute for the selectstatement
     */
    public Attribute getAttribute4Select(final String _selectStmt)
    {
        final OneSelect oneselect = this.selectStmt2OneSelect.get(_selectStmt);
        return oneselect == null ? null : oneselect.getAttribute();
    }

    /**
     * Method to get the instances used for an select.
     *
     * @param _selectStmt   selectstatement the attribute is wanted for
     * @return List of instances for the select or an empty list in case
     *  that the onselect is not found
     * @throws EFapsException on error
     */
    public List<Instance> getInstances4Select(final String _selectStmt)
        throws EFapsException
    {
        final OneSelect oneselect = this.selectStmt2OneSelect.get(_selectStmt);
        return oneselect == null ? new ArrayList<Instance>() : oneselect.getInstances();
    }

    /**
     * Method to determine it the select statement returns more than one value.
     *
     * @param _selectStmt   selectstatement the attribute is wanted for
     * @return true it the oneselect is muliple, else false
     * @throws EFapsException on error
     */
    public boolean isList4Select(final String _selectStmt)
        throws EFapsException
    {
        final OneSelect oneselect = this.selectStmt2OneSelect.get(_selectStmt);
        return oneselect == null ? false : oneselect.isMultiple();
    }

    /**
     * Getter method for the instance variable {@link #enforceSorted}.
     *
     * @return value of instance variable {@link #enforceSorted}
     */
    public boolean isEnforceSorted()
    {
        return this.enforceSorted;
    }


    /**
     * Setter method for instance variable {@link #enforceSorted}.
     *
     * @param _enforceSorted value for instance variable {@link #enforceSorted}
     */

    public void setEnforceSorted(final boolean _enforceSorted)
    {
        this.enforceSorted = _enforceSorted;
    }


    /**
     * The instance method executes the query.
     *
     * @return true if the query contains values, else false
     * @throws EFapsException on error
     */
    public boolean execute() throws EFapsException
    {
        return executeWithoutAccessCheck();
    }

    /**
     * The instance method executes the query without an access check.
     *
     * @return true if the query contains values, else false
     * @throws EFapsException on error
     */
    public boolean executeWithoutAccessCheck()
        throws EFapsException
    {
        boolean ret = false;
        if (isMarked4execute()) {
            if (getInstanceList().size() > 0) {
                ret =  executeOneCompleteStmt(createSQLStatement(), this.allSelects);
            }

            if (ret) {
                for (final OneSelect onesel : this.allSelects) {
                    if (onesel.getFromSelect() != null) {
                        onesel.getFromSelect().execute(onesel);
                    }
                }
            }
        }
        return ret;
    }


    /**
     * Method to create on Statement out of the different parts.
     *
     * @return StringBuilder containing the SQL statement
     */
    private String createSQLStatement()
    {

        final SQLSelect select = new SQLSelect()
            .column(0, "ID")
            .from(getMainType().getMainTable().getSqlTable(), 0);
        for (final OneSelect oneSel : this.allSelects) {
            oneSel.append2SQLFrom(select);
        }

        int colIndex = select.getColumns().size() + 1;
        // if the main table has a column for the type it is selected also
        if (getMainType().getMainTable().getSqlColType() != null) {
            select.column(0, getMainType().getMainTable().getSqlColType());
            this.typeColumnIndex = colIndex;
            colIndex++;
        }

        for (final OneSelect onesel : this.allSelects) {
            if (onesel.getValueSelect() != null) {
                colIndex += onesel.append2SQLSelect(select, colIndex);
            }
        }

        select.addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);

        int i = 0;
        for (final Instance instance : getInstanceList()) {
            if (Context.getDbType().getMaxExpressions() > -1 && i > Context.getDbType().getMaxExpressions()) {
                select.addPart(SQLPart.PARENTHESIS_CLOSE)
                    .addPart(SQLPart.OR)
                    .addColumnPart(0, "ID").addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);
                i = 0;
            }
            if (i > 0) {
                select.addPart(SQLPart.COMMA);
            }
            select.addValuePart(instance.getId());
            i++;
        }
        select.addPart(SQLPart.PARENTHESIS_CLOSE);

        for (final OneSelect oneSel : this.allSelects) {
            oneSel.append2SQLWhere(select);
        }

        if (AbstractPrintQuery.LOG.isDebugEnabled()) {
            AbstractPrintQuery.LOG.debug(select.getSQL());
        }
        return select.getSQL();
    }

    /**
     * The instance method executes exact one complete statement and populates
     * the result in the cached result {@link #cachedResult}.
     *
     * @param _complStmt    complete statement instance to execute
     * @param _oneSelects   lsit of OneSelects the statement is executed for
     * @return true if the query contains values, else false
     * @throws EFapsException on error
     */
    protected boolean executeOneCompleteStmt(final String _complStmt,
                                             final List<OneSelect> _oneSelects)
        throws EFapsException
    {
        boolean ret = false;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();

            if (AbstractPrintQuery.LOG.isDebugEnabled()) {
                AbstractPrintQuery.LOG.debug(_complStmt.toString());
            }

            final Statement stmt = con.getConnection().createStatement();
            final ResultSet rs = stmt.executeQuery(_complStmt.toString());
            final List<Object[]> values = new ArrayList<Object[]>();

            while (rs.next()) {
                if (getMainType().getMainTable().getSqlColType() != null) {
                    values.add(new Object[]{rs.getLong(this.typeColumnIndex), rs.getLong(1)});
                } else {
                    values.add(new Object[]{rs.getLong(1)});
                }
                for (final OneSelect onesel : _oneSelects) {
                    onesel.addObject(rs);
                }
                ret = true;
            }
            rs.close();
            stmt.close();
            con.commit();

            final List<Instance> tmpList = new ArrayList<Instance>();
            final Map<Instance, Integer> sortMap = new HashMap<Instance, Integer>();
            int i = 0;
            for (final Object[] row : values) {
                final Instance instance;
                if (row.length == 2) {
                    instance = Instance.get(Type.get((Long) row[0]), (Long) row[1]);
                } else {
                    instance = Instance.get(getMainType(), (Long) row[0]);
                }
                sortMap.put(instance, i);
                tmpList.add(instance);
                i++;
            }

            if (this.enforceSorted) {
                for (final OneSelect onesel : _oneSelects) {
                    onesel.sortByInstanceList(getInstanceList(), sortMap);
                }
            } else {
                getInstanceList().clear();
                getInstanceList().addAll(tmpList);
            }

        } catch (final SQLException e) {
            throw new EFapsException(InstanceQuery.class, "executeOneCompleteStmt", e);
        } finally {
            if (con != null && con.isOpened()) {
                con.abort();
            }
        }
        return ret;
    }

    /**
     * Method to get an table index from {@link #sqlTable2Index}.
     *
     * @param _tableName    tablename the index is wanted for
     * @param _column       name of the column, used for the relation
     * @param _relIndex     relation the table is used in
     * @return  index of the table or null if not found
     */
    public Integer getTableIndex(final String _tableName,
                                 final String _column,
                                 final int _relIndex)
    {
        return this.sqlTable2Index.get(_relIndex + "__" + _tableName + "__" + _column);
    }

    /**
     * Get a new table index and add the table to the map of existing table
     * indexes.
     * @param _tableName    tablename the index is wanted for
     * @param _column       name of the column, used for the relation
     * @param _relIndex     relation the table is used in
     * @return new index for the table
     */
    public Integer getNewTableIndex(final String _tableName,
                                    final String _column,
                                    final Integer _relIndex)
    {
        this.tableIndex++;
        this.sqlTable2Index.put(_relIndex + "__" + _tableName + "__" + _column, this.tableIndex);
        return this.tableIndex;
    }

    /**
     * A PrintQuery will only be executed if at least one
     * Instance is given to be executed on.
     *
     * @return true if this PrintQuery will be executed.
     */
    public boolean isMarked4execute()
    {
        return !getInstanceList().isEmpty();
    }
}
