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

package org.efaps.db;

import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Type;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.print.OneSelect;
import org.efaps.db.print.Phrase;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
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
     * Add an attribute to the PrintQuery. It is used to get editable values
     * from the eFaps DataBase.
     *
     * @param _attributes    Attribute to add
     * @return this PrintQuery
     */
    public AbstractPrintQuery addAttribute(final Attribute... _attributes)
    {
        for (final Attribute attr : _attributes) {
            final OneSelect oneselect = new OneSelect(this, attr);
            this.allSelects.add(oneselect);
            this.attr2OneSelect.put(attr.getName(), oneselect);
        }
        return this;
    }

    protected void addOneSelect(final OneSelect _oneSelect) {
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
     * Method to get the attribute for an attributename.
     * @param _attributeName name of the attribute
     * @return Attribute
     */
    public Attribute getAttribute4Attribute(final String _attributeName)
    {
        final OneSelect oneselect = this.attr2OneSelect.get(_attributeName);
        return oneselect == null ? null : oneselect.getAttribute();
    }

    /**
     * Method to get the instance for an attributename.
     * @param _attributeName name of the attribute
     * @return list of instance
     */
    public List<Instance> getInstances4Attribute(final String _attributeName)
    {
        final OneSelect oneselect = this.attr2OneSelect.get(_attributeName);
        return oneselect == null ? null :oneselect.getInstances();
    }

    /**
     * Get the object returned by the given name of an attribute.
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
     * @return Type
     */
    public abstract Type getMainType();

    /**
     * Method to get the instances this PrintQuery is executed on.
     * @return List of instances
     */
    public abstract List<Instance> getInstanceList();

    /**
     * Method to get the current Instance.
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
    public <T> T getAttributeSet(final String _setName) throws EFapsException
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
     * @throws EFapsException   allways!!
     */
    public AbstractPrintQuery addExpression(final String _key, final String _expression)
            throws EFapsException
    {
        throw new EFapsException("PrintQuery.addExpression id not yet implemented", null);
    }

    /**
     * Get the object returned by the expression belonging to the given key.
     * @param <T>           class the return value will be casted to
     * @param _key  key for an expression the object is wanted for
     * @return object for the expression
     * @throws EFapsException allways
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
     * @param _key Key the phrase can be accessed
     * @param _phraseStmt Phrase to add
     * @throws EFapsException on error
     * @return this PrintQuery
     */
    public AbstractPrintQuery addPhrase(final String _key, final String _phraseStmt)
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
     * @param _key  key to the phrase
     * @return  String representation of the phrase
     * @throws EFapsException on error
     */
    public String getPhrase(final String _key)
            throws EFapsException
    {
        final Phrase phrase = this.key2Phrase.get(_key);
        return phrase == null ? null :phrase.getPhraseValue(getCurrentInstance());
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
        for (final String selectStmt : _selectStmts) {
            final OneSelect oneselect = new OneSelect(this, selectStmt);
            this.allSelects.add(oneselect);
            this.selectStmt2OneSelect.put(selectStmt, oneselect);
            oneselect.analyzeSelectStmt();
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
     * Method to get the Attribute used for an select.
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
     * @param _selectStmt   selectstatement the attribute is wanted for
     * @return  Attribute for the selectstatement
     */
    public List<Instance> getInstances4Select(final String _selectStmt)
    {
        final OneSelect oneselect = this.selectStmt2OneSelect.get(_selectStmt);
        return oneselect.getInstances();
    }

    /**
     * Method to determine it the selectstatement returns more than one value.
     * @param _selectStmt   selectstatement the attribute is wanted for
     * @return  Attribute for the selectstatement
     */
    public boolean isList4Select(final String _selectStmt)
    {
        final OneSelect oneselect = this.selectStmt2OneSelect.get(_selectStmt);
        return oneselect.isMulitple();
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
        return ret;
    }


    /**
     * Method to create on Statement out of the different parts.
     * @return StringBuilder containing the sql statement
     */
    private StringBuilder createSQLStatement()
    {
        final StringBuilder selBldr = new StringBuilder();
        selBldr.append("select T0.ID");

        final StringBuilder fromBldr = new StringBuilder();
        fromBldr.append(" from ").append(getMainType().getMainTable().getSqlTable()).append(" T0");
        for (final OneSelect onesel : this.allSelects) {
            onesel.append2SQLFrom(fromBldr);
        }
        // if the maintable has a column for the type it is selected also
        int colIndex = 2;
        if (getMainType().getMainTable().getSqlColType() != null) {
            selBldr.append(",T0.").append(getMainType().getMainTable().getSqlColType());
            colIndex++;
        }

        for (final OneSelect onesel : this.allSelects) {
            if (onesel.getValueSelect() != null) {
                colIndex += onesel.append2SQLSelect(selBldr, colIndex);
            }
        }

        final StringBuilder whereBldr = new StringBuilder();
        whereBldr.append(" where T0.ID in (");
        boolean first = true;
        for (final Instance instance : getInstanceList()) {
            if (first) {
                first = false;
            } else {
                whereBldr.append(",");
            }
            whereBldr.append(instance.getId());
        }
        whereBldr.append(")");

        selBldr.append(fromBldr).append(whereBldr);

        if (AbstractPrintQuery.LOG.isDebugEnabled()) {
            AbstractPrintQuery.LOG.debug(selBldr.toString());
        }
        return selBldr;
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
    protected boolean executeOneCompleteStmt(final StringBuilder _complStmt, final List<OneSelect> _oneSelects)
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
            getInstanceList().clear();
            while (rs.next()) {
                final Instance instance;
                if (getMainType().getMainTable().getSqlColType() != null) {
                    instance = Instance.get(Type.get(rs.getLong(2)), rs.getLong(1));
                } else {
                    instance = Instance.get(getMainType(), rs.getLong(1));
                }
                getInstanceList().add(instance);
                for (final OneSelect onesel : _oneSelects) {
                    onesel.addObject(rs);
                }
                ret = true;
            }

            rs.close();
            stmt.close();
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
            // TODO: exception eintragen!
            throw new EFapsException(getClass(), "executeOneCompleteStmt.Throwable", e);
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
    public Integer getTableIndex(final String _tableName, final String _column, final int _relIndex)
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
    public Integer getNewTableIndex(final String _tableName, final String _column, final Integer _relIndex)
    {
        this.tableIndex++;
        this.sqlTable2Index.put(_relIndex + "__" + _tableName + "__" + _column, this.tableIndex);
        return this.tableIndex;
    }
}
