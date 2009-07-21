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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.beans.ValueList;
import org.efaps.beans.ValueList.Token;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * PrintQuery is a query uses to get the value for one object, specfied by one
 * instance. The PrintQuery is able to execute various of the partes for the
 * select from EQL definition.
 *
 * TODO description!
 * TODO .type
 * TODO .value
 * TODO .attribute[ValueUOM].number .attribute[ValueUOM].uom .attribute[ValueUOM].base
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PrintQuery
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PrintQuery.class);

    /**
     * Instance this PrintQuery is based on.
     */
    private final Instance instance;

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
     * @param _type Type to be updated
     * @param _id id to be updated
     * @throws EFapsException on error
     */
    public PrintQuery(final Type _type, final String _id)
            throws EFapsException
    {
        this(Instance.get(_type, _id));
    }

    /**
     * @param _type Type to be updated
     * @param _id id to be updated
     * @throws EFapsException on error
     */
    public PrintQuery(final String _type, final String _id)
            throws EFapsException
    {
        this(Type.get(_type), _id);
    }

    /**
     * @param _oid OID of the instance to be updated.
     * @throws EFapsException on error
     */
    public PrintQuery(final String _oid)
            throws EFapsException
    {
        this(Instance.get(_oid));
    }

    /**
     * @param _instance instance to be updated.
     * @throws EFapsException on error
     */
    public PrintQuery(final Instance _instance)
            throws EFapsException
    {
        this.instance = _instance;
    }

    /**
     * Add an attribute to the PrintQuery. It is used to get editable values
     * from the eFaps DataBase.
     *
     * @param _attrNames    Name of the Attribute to add
     * @return this PrintQuery
     */
    public PrintQuery addAttribute(final String... _attrNames)
    {
        final Type type = this.instance.getType();
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
     * Add an attribute to the PrintQuery. It is used to get editable values
     * from the eFaps DataBase.
     *
     * @param _attributes    Attribute to add
     * @return this PrintQuery
     */
    public PrintQuery addAttribute(final Attribute... _attributes)
    {
        for (final Attribute attr : _attributes) {
            final OneSelect oneselect = new OneSelect(attr);
            this.allSelects.add(oneselect);
            this.attr2OneSelect.put(attr.getName(), oneselect);
        }
        return this;
    }

    /**
     * Method to get the attribute for an attributename.
     * @param _attributeName name of the attribute
     * @return Attribute
     */
    public Attribute getAttribute4Attribute(final String _attributeName)
    {
        return this.attr2OneSelect.get(_attributeName).getAttribute();
    }

    /**
     * Method to get the instance for an attributename.
     * @param _attributeName name of the attribute
     * @return list of instance
     */
    public List<Instance> getInstances4Attribute(final String _attributeName)
    {
        final OneSelect oneselect = this.attr2OneSelect.get(_attributeName);
        return oneselect.getInstances();
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
        return (T) oneselect.getObject();
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
     * Add an AttributeSet to the PrintQuery. It is used to get editable values
     * from the eFaps DataBase.
     *
     * @param _setName    Name of the AttributeSet to add
     * @return this PrintQuery
     */
    public PrintQuery addAttributeSet(final String _setName)
    {
        final Type type = this.instance.getType();
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
     */
    public PrintQuery addAttributeSet(final AttributeSet _set)
    {
        final String key = "linkfrom[" + _set.getName() + "#" + _set.getAttributeName() + "]";
        final OneSelect oneselect = new OneSelect(key);
        this.allSelects.add(oneselect);
        this.attr2OneSelect.put(_set.getAttributeName(), oneselect);
        oneselect.analyzeSelectStmt();
        for (final String setAttrName :  _set.getSetAttributes()) {
            if (!setAttrName.equals(_set.getAttributeName())) {
                oneselect.getFromSelect().addOneSelect(new OneSelect(_set.getAttribute(setAttrName)));
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
        if (oneselect.getFromSelect().hasResult) {
            ret = new HashMap<String, Object>();
            // in an attributset the first one is fake
            boolean first = true;
            for (final OneSelect onsel : oneselect.getFromSelect().getOneSelects()) {
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
     * Add an expression to this PrintQuery. An expresson is something like:
     * <code>if class[Emperador_Products_ClassFloorLaminate].linkto[SurfaceAttrId].
     * attribute[Value] == "abc" then 'hallo' else 'ifdef'</code>
     *
     * @param _key          key to the expression
     * @param _expression   expression to add
     * @return this PrintQuery
     * @throws EFapsException   allways!!
     */
    public PrintQuery addExpression(final String _key, final String _expression)
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
    public PrintQuery addPhrase(final String _key, final String _phraseStmt)
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
            final OneSelect oneselect = new OneSelect(selectStmt);
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
        return phrase.getPhraseValue();
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
    public PrintQuery addSelect(final String... _selectStmts)
            throws EFapsException
    {
        for (final String selectStmt : _selectStmts) {
            final OneSelect oneselect = new OneSelect(selectStmt);
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
        return (T) oneselect.getObject();
    }

    /**
     * Method to get the Attribute used for an select.
     * @param _selectStmt   selectstatement the attribute is wanted for
     * @return  Attribute for the selectstatement
     */
    public Attribute getAttribute4Select(final String _selectStmt)
    {
        final OneSelect oneselect = this.selectStmt2OneSelect.get(_selectStmt);
        return oneselect.getAttribute();
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
        final boolean ret =  executeOneCompleteStmt(createSQLStatement(), this.allSelects);
        if (ret) {
            for (final OneSelect onesel : this.allSelects) {
                if (onesel.getFromSelect() != null) {
                    onesel.getFromSelect().execute();
                }
            }
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     */
    public Instance getInstance()
    {
        return this.instance;
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
        fromBldr.append(" from ").append(this.instance.getType().getMainTable().getSqlTable()).append(" T0");
        for (final OneSelect onesel : this.allSelects) {
            onesel.append2SQLFrom(fromBldr);
        }

        int colIndex = 2;
        for (final OneSelect onesel : this.allSelects) {
            colIndex += onesel.append2SQLSelect(selBldr, colIndex);
        }

        final StringBuilder whereBldr = new StringBuilder();
        whereBldr.append(" where T0.ID=").append(this.instance.getId());
        selBldr.append(fromBldr).append(whereBldr);

        if (PrintQuery.LOG.isDebugEnabled()) {
            PrintQuery.LOG.debug(selBldr.toString());
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
    private boolean executeOneCompleteStmt(final StringBuilder _complStmt, final List<OneSelect> _oneSelects)
            throws EFapsException
    {
        boolean ret = false;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();

            if (PrintQuery.LOG.isDebugEnabled()) {
                PrintQuery.LOG.debug(_complStmt.toString());
            }

            final Statement stmt = con.getConnection().createStatement();

            final ResultSet rs = stmt.executeQuery(_complStmt.toString());

            while (rs.next()) {
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
            throw new EFapsException(getClass(), "executeOneCompleteStmt.Throwable");
        }
        return ret;
    }

    /**
     * Method to get an table index from {@link #sqlTable2Index}.
     *
     * @param _tableName    tablename the index is wanted for
     * @param _relIndex     relation the table is used in
     * @return  index of the table or null if not found
     */
    private Integer getTableIndex(final String _tableName, final int _relIndex)
    {
        return this.sqlTable2Index.get(_relIndex + "__" + _tableName);
    }

    /**
     * Get a new table index and add the table to the map of existing table
     * indexes.
     * @param _tableName    tablename the index is wanted for
     * @param _relIndex     relation the table is used in
     * @return new index for the table
     */
    private Integer getNewTableIndex(final String _tableName, final Integer _relIndex)
    {
        this.tableIndex++;
        this.sqlTable2Index.put(_relIndex + "__" + _tableName, this.tableIndex);
        return this.tableIndex;
    }

    /**
     * Class is used as a wraper for a series of OneSelects as part of one
     * phrase.
     */
    public class Phrase
    {
        /**
         * Key for this Phrase.
         */
        private final String key;

        /**
         * Phrase statement for this Phrase.
         */
        private final String phraseStmt;

        /**
         * Mapping of Select statements to OneSelect.
         */
        private final Map<String, OneSelect> selectStmt2OneSelect = new HashMap<String, OneSelect>();

        /**
         * ValueList to access the parser.
         */
        private final ValueList valueList;

        /**
         * @param _key          Key for this Phrase
         * @param _phraseStmt   Phrase statement for this Phrase
         * @param _valueList    ValueList to access the parser.
         */
        public Phrase(final String _key, final String _phraseStmt, final ValueList _valueList)
        {
            this.key = _key;
            this.phraseStmt = _phraseStmt;
            this.valueList = _valueList;
        }

        /**
         * Method to get the parsed value for this phrase.
         * @return  parsed value
         * @throws EFapsException on error
         */
        public String getPhraseValue()
                throws EFapsException
        {
            final StringBuilder buf = new StringBuilder();

            for (final Token token : this.valueList.getTokens()) {
                switch (token.getType()) {
                    case EXPRESSION:
                        final OneSelect oneselect = this.selectStmt2OneSelect.get(token.getValue());
                        final Object value = oneselect.getObject();
                        buf.append((new FieldValue(null, oneselect.attribute, value, null))
                                        .getStringValue(TargetMode.VIEW, PrintQuery.this.instance, null));
                        break;
                    case TEXT:
                        buf.append(token.getValue());
                        break;
                    default:
                        break;
                }
            }
            return buf.toString();
        }

        /**
         * Add a oneselect to this Phrase.
         * @param _oneselect    OneSelect to add
         */
        public void addSelect(final OneSelect _oneselect)
        {
            this.selectStmt2OneSelect.put(_oneselect.selectStmt, _oneselect);
        }
    }

    /**
     * This class is used as a part of one complete statement to be executed
     * against the eFaps database. It will normally return the object for one
     * attribute. The OneSelect can consist from only one attribute or of a
     * series of {@link PrintQuery.ISelectPart}
     */
    public class OneSelect
    {
        /**
         * The select this OneSelect belongs to.
         */
        private final String selectStmt;

        /**
         * List of select parts.
         */
        private final List<ISelectPart> selectParts = new ArrayList<ISelectPart>();

        /**
         * FromSelect this OneSelect belong to.
         */
        private FromSelect fromSelect;

        /**
         * List of column indexes the values have in the ResultSet returned
         * from the eFaps database.
         */
        private final List<Integer> colIndexs = new ArrayList<Integer>();

        /**
         * List of objects retrieved from the ResultSet returned
         * from the eFaps database. It represent one row in a result set.
         */
        private final List<Object> objectList = new ArrayList<Object>();

        /**
         * List of ids retrieved from the ResultSet returned
         * from the eFaps database. It represent one row in a result set.
         */
        private final List<Long> idList = new ArrayList<Long>();

        /**
         * table index for this table. It will finally contain the index of
         * the table the attribute belongs to.
         */
        private int tableIndex;

        /**
         * Name of the attribute.
         */
        private String attrName;

        /**
         * Attribute.
         */
        private Attribute attribute;

        /**
         * @param _selectStmt selectStatement this OneSelect belongs to
         * @param _subQuery
         * @param tableIndex
         */
        public OneSelect(final String _selectStmt)
        {
            this.selectStmt = _selectStmt;
        }

        /**
         * @param _attr attribute to be used in this OneSelect
         */
        public OneSelect(final Attribute _attr)
        {
            this.selectStmt = null;
            this.attribute = _attr;
        }

        /**
         * Setter method for instance variable {@link #attribute}.
         * @param _attribute Attribute to set
         */
        public void setAttribute(final Attribute _attribute)
        {
           this.attribute = _attribute;

        }

        /**
         * Add an Object for this OneSelect.
         *
         * @param _rs ResultSet from the eFaps database
         * @throws SQLException on error
         */
        public void addObject(final ResultSet _rs) throws SQLException
        {
            final ResultSetMetaData metaData = _rs.getMetaData();
            // store the ids also
            this.idList.add(_rs.getLong(1));
            Object object = null;
            if (this.colIndexs.size() > 1) {
                final Object[] objArray = new Object[this.colIndexs.size()];
                int i = 0;
                for (final Integer colIndex : this.colIndexs) {
                    switch (metaData.getColumnType(colIndex)) {
                        case java.sql.Types.TIMESTAMP:
                            objArray[i] =  _rs.getTimestamp(colIndex);
                            break;
                        default:
                            objArray[i] =  _rs.getObject(colIndex);
                    }
                    i++;
                }
                object = objArray;
            } else if (this.colIndexs.size() > 0) {
                switch (metaData.getColumnType(this.colIndexs.get(0))) {
                    case java.sql.Types.TIMESTAMP:
                        object = _rs.getTimestamp(this.colIndexs.get(0));
                        break;
                    default:
                        object = _rs.getObject(this.colIndexs.get(0));
                }
            }
            this.objectList.add(object);
        }

        /**
         * Add an attribute name evaluated from an
         * <code>attribute[ATTRIBUTENAME]</code> part of an select statement.
         * @param _attrName name of an attribute
         */
        public void addAttributeSelectPart(final String _attrName)
        {
            this.attrName = _attrName;
        }

        /**
         * Add a classification name evaluated from an
         * <code>class[CLASSIFICATIONNAME]</code> part of an select statement.
         * @param _classificationName   name of the classification
         */
        public void addClassificationSelectPart(final String _classificationName)
        {
            this.selectParts.add(new ClassSelectPart(_classificationName));
        }

        /**
         * Add the name of the attribute the link must go to, evaluated from an
         * <code>linkTo[ATTRIBUTENAME]</code> part of an select statement.
         * @param _linkTo   name of the attribute the link must go to
         */
        public void addLinkToSelectPart(final String _linkTo)
        {
            Type type;
            // if a previous select exists it is based on the previous select,
            // else it is based on the basic table
            if (this.selectParts.size() > 0) {
                type = this.selectParts.get(this.selectParts.size() - 1).getType();
            } else {
                type = PrintQuery.this.instance.getType();
            }
            final LinkToSelectPart linkto = new LinkToSelectPart(_linkTo, type);
            this.selectParts.add(linkto);
        }

        /**
         * Add the name of the type and attribute the link comes from,
         * evaluated from an <code>linkTo[TYPENAME#ATTRIBUTENAME]</code>
         * part of an select statement.
         * @param _linkFrom   name of the attribute the link comes from
         */
        public void addLinkFromSelectPart(final String _linkFrom)
        {
            this.fromSelect = new FromSelect(_linkFrom);
        }

        /**
         * Method used to append to the from part of an sql statement.
         * @param _fromBldr builder to append to
         */
        public void append2SQLFrom(final StringBuilder _fromBldr)
        {
            for (final ISelectPart sel : this.selectParts) {
                this.tableIndex = sel.join(this, _fromBldr, this.tableIndex);
            }
        }

        /**
         * Method used to append to the select part of an sql statement.
         * @param _fromBldr builder to append to
         * @param _colIndex aactual column index
         * @return number of columns added in this part of the sql statement
         */
        public int append2SQLSelect(final StringBuilder _fromBldr, final int _colIndex)
        {
            int ret = 0;
            //in case that the OneSelect was instantiated for an attribute
            if (this.selectStmt == null) {
                for (final String colName : this.attribute.getSqlColNames()) {
                    _fromBldr.append(",T0.").append(colName);
                    this.colIndexs.add(_colIndex + ret);
                    ret++;
                }
            } else {
                Type type;
                // if a previous select exists it is based on the previous select,
                // else it is based on the basic table
                if (this.selectParts.size() > 0) {
                    type = this.selectParts.get(this.selectParts.size() - 1).getType();
                } else {
                    type = PrintQuery.this.instance.getType();
                }
                if ("id".equals(this.attrName)) {
                    this.attribute = type.getAttribute("ID");
                    _fromBldr.append(",T").append(this.tableIndex)
                        .append(".").append(type.getMainTable().getSqlColId());
                    this.colIndexs.add(_colIndex);
                    ret++;
                } else if ("oid".equals(this.attrName)) {
                    this.attribute = type.getAttribute("OID");
                    for (final String colName : this.attribute.getSqlColNames()) {
                        _fromBldr.append(",T").append(this.tableIndex).append(".").append(colName);
                        this.colIndexs.add(_colIndex + ret);
                        ret++;
                    }
                } else if (this.attrName != null) {
                    this.attribute = type.getAttribute(this.attrName);
                    for (final String colName : this.attribute.getSqlColNames()) {
                        _fromBldr.append(",T").append(this.tableIndex).append(".").append(colName);
                        this.colIndexs.add(_colIndex + ret);
                        ret++;
                    }
                }
            }
            return ret;
        }

        /**
         * Method to analyse the select statement. Meaning the the different
         * select parts will be added to {@link #selectParts}.
         */
        public void analyzeSelectStmt()
        {
            final Pattern pattern = Pattern.compile("(?<=\\[)[0-9a-zA-Z_]*(?=\\])");
            final Pattern linkfomPat = Pattern.compile("(?<=\\[)[0-9a-zA-Z_#:]*(?=\\])");

            final String[] parts = this.selectStmt.split("\\.");
            OneSelect currentSelect = this;
            for (final String part : parts) {
                if (part.startsWith("class")) {
                    final Matcher matcher = pattern.matcher(part);
                    if (matcher.find()) {
                        currentSelect.addClassificationSelectPart(matcher.group());
                    }
                } else if (part.startsWith("linkto")) {
                    final Matcher matcher = pattern.matcher(part);
                    if (matcher.find()) {
                        currentSelect.addLinkToSelectPart(matcher.group());
                    }
                } else if (part.startsWith("attribute")) {
                    final Matcher matcher = pattern.matcher(part);
                    if (matcher.find()) {
                        currentSelect.addAttributeSelectPart(matcher.group());
                    }
                } else if (part.startsWith("linkfrom")) {
                    final Matcher matcher = linkfomPat.matcher(part);
                    if (matcher.find()) {
                        currentSelect.addLinkFromSelectPart(matcher.group());
                        currentSelect = currentSelect.fromSelect.getMainOneSelect();
                    }
                } else {
                    currentSelect.addAttributeSelectPart(part);
                }
            }
        }

        /**
         * Method to get the Object for this OneSelect.
         * @return  object for this OneSelect
         * @throws EFapsException on error
         */
        public Object getObject() throws EFapsException
        {
            Object ret = null;
            if (this.attribute == null) {
                if (this.fromSelect.hasResult) {
                    ret = this.fromSelect.getMainOneSelect().getObject();
                }
            } else {
                final IAttributeType attrInterf = this.attribute.newInstance();
                ret = attrInterf.readValue(this.objectList);
            }
            return ret;
        }

        /**
         * Methdo return the instances this OneSelect has returned.
         * @return Collection of Insatcne
         */
        public List<Instance> getInstances()
        {
            final List<Instance> ret = new ArrayList<Instance>();
            if (this.attribute == null) {
                ret.addAll(this.fromSelect.getMainOneSelect().getInstances());
            } else {
                for (final Long id : this.idList) {
                    ret.add(Instance.get(this.attribute.getParent(), id.toString()));
                }
            }
            return ret;
        }

        /**
         * Getter method for instance variable {@link #attribute}.
         *
         * @return value of instance variable {@link #attribute}
         */
        public Attribute getAttribute()
        {
            Attribute ret;
            if (this.attribute == null) {
                ret = this.fromSelect.getMainOneSelect().getAttribute();
            } else {
                ret = this.attribute;
            }
            return ret;
        }

        /**
         * Getter method for instance variable {@link #fromSelect}.
         *
         * @return value of instance variable {@link #fromSelect}
         */
        public FromSelect getFromSelect()
        {
            return this.fromSelect;
        }

        /**
         * Method to determine if this OneSelect does return mor than one value.
         * @return true if more than one value is returned, else false
         */
        public boolean isMulitple() {
            boolean ret;
            if (this.attribute == null) {
                ret = this.fromSelect.getMainOneSelect().isMulitple();
            } else {
                ret = this.objectList.size() > 1;
            }
            return ret;
        }

        /**
         * Get a new table index and add the table to the map of existing table
         * indexes. The method calls in case that this OneSelect is related
         * to a FromSelect the method
         * {@link org.efaps.db.PrintQuery.FromSelect#getNewTableIndex(String, Integer)},
         * else in PrintQuery the method
         * {@link org.efaps.db.PrintQuery#getNewTableIndex(String, Integer)}.
         *
         * @param _tableName    tablename the index is wanted for
         * @param _relIndex     relation the table is used in
         * @return new index for the table
         */
        private Integer getNewTableIndex(final String _tableName, final Integer _relIndex)
        {
            int ret;
            if (this.attribute == null  && this.fromSelect != null) {
                ret = this.fromSelect.getNewTableIndex(_tableName, _relIndex);
            } else {
                ret = PrintQuery.this.getNewTableIndex(_tableName, _relIndex);
            }
            return ret;
        }

        /**
         * Method to get an table index from {@link #sqlTable2Index}.
         * Get a new table index and add the table to the map of existing table
         * indexes. The method calls in case that this OneSelect is related
         * to a FromSelect the method
         * {@link org.efaps.db.PrintQuery.FromSelect#getTableIndex(String, Integer)},
         * else in PrintQuery the method
         * {@link org.efaps.db.PrintQuery#getTableIndex(String, Integer)}
         * @param _tableName tablename the index is wanted for
         * @param _relIndex relation the table is used in
         * @return index of the table or null if not found
         */
        private Integer getTableIndex(final String _tableName, final int _relIndex)
        {
            Integer ret;
            if (this.attribute == null && this.fromSelect != null) {
                ret = this.fromSelect.getTableIndex(_tableName, _relIndex);
            } else {
                ret = PrintQuery.this.getTableIndex(_tableName, _relIndex);
            }
            return ret;
        }
    }

    /**
     * Interface used for the different Select parts.
     */
    public interface ISelectPart
    {
        /**
         * Method to join a table to the given from select statement.
         * @param _oneselect oneselect this select part must be joined to
         * @param _fromBldr StringBuilder containing the from select statement
         * @param _relIndex relation index
         * @return table index of the joint table
         */
        int join(final OneSelect _oneselect, final StringBuilder _fromBldr, final int _relIndex);

        /**
         * Method to get the Type the part belongs to.
         * @return type
         */
        Type getType();
    }

    /**
     * Select Part for <code>linkfrom[TYPERNAME#ATTRIBUTENAME]</code>.
     */
    public class FromSelect
    {
        /**
         * Name of the Attribute the link to is based on.
         */
        private final String attrName;

        /**
         * Type the {@link #attrName} belongs to.
         */
        private final Type type;

        /**
         * Mapping of Select statements to OneSelect.
         */
        private final List<PrintQuery.OneSelect> oneSelects = new ArrayList<PrintQuery.OneSelect>();

        /**
         * Mapping of sql tables to table index.
         * @see #tableIndex
         */
        private final Map<String, Integer> sqlTable2Index = new HashMap<String, Integer>();

        /**
         * Actual index of the table.
         */
        private int tableIndex;

        /**
         * Did this query return a result.
         */
        private boolean hasResult;

        /**
         * @param _linkFrom linkfrom element of the query
         */
        public FromSelect(final String _linkFrom)
        {
            final String[] linkfrom = _linkFrom.split("#");
            this.type = Type.get(linkfrom[0]);
            this.attrName = linkfrom[1];
            final OneSelect onsel = new OneSelect(_linkFrom);
            this.oneSelects.add(onsel);
            onsel.fromSelect = this;
            onsel.selectParts.add(new ISelectPart() {

                public Type getType()
                {
                    return PrintQuery.FromSelect.this.type;
                }

                public int join(final OneSelect _oneselect, final StringBuilder _fromBldr, final int _relIndex)
                {
                    // TODO Auto-generated method stub
                    return 0;
                }
            });
        }

        /**
         * Getter method for instance variable {@link #oneSelects}.
         *
         * @return value of instance variable {@link #oneSelects}
         */
        public List<PrintQuery.OneSelect> getOneSelects()
        {
            return this.oneSelects;
        }

        /**
         * @param _oneSelect OneSelect to be added
         */
        public void addOneSelect(final OneSelect _oneSelect)
        {
            this.oneSelects.add(_oneSelect);
        }

        /**
         * Method to get an table index from {@link #sqlTable2Index}.
         *
         * @param _tableName    tablename the index is wanted for
         * @param _relIndex     relation the table is used in
         * @return  index of the table or null if not found
         */
        private Integer getTableIndex(final String _tableName, final int _relIndex)
        {
            return this.sqlTable2Index.get(_relIndex + "__" + _tableName);
        }

        /**
         * Get a new table index and add the table to the map of existing table
         * indexes.
         * @param _tableName    tablename the index is wanted for
         * @param _relIndex     relation the table is used in
         * @return new index for the table
         */
        private Integer getNewTableIndex(final String _tableName, final Integer _relIndex)
        {
            this.tableIndex++;
            this.sqlTable2Index.put(_relIndex + "__" + _tableName, this.tableIndex);
            return this.tableIndex;
        }

        /**
         * Execute the from select.
         * @throws EFapsException on error
         */
        public void execute() throws EFapsException
        {
            this.hasResult = executeOneCompleteStmt(createSQLStatement(), this.oneSelects);
        }

        /**
         * Method to create on Statement out of the different parts.
         * @return StringBuilder containing the sql statement
         */
        private StringBuilder createSQLStatement()
        {
            final Attribute attr = this.type.getAttribute(this.attrName);
            final StringBuilder selBldr = new StringBuilder();
            selBldr.append("select T0.ID, T0.").append(attr.getSqlColNames().get(0));

            final StringBuilder fromBldr = new StringBuilder();
            fromBldr.append(" from ").append(this.type.getMainTable().getSqlTable()).append(" T0");

            // on a from  select only on table is the base
            this.oneSelects.get(0).append2SQLFrom(fromBldr);

            int colIndex = 3;
            for (final OneSelect oneSel : this.oneSelects) {
                colIndex += oneSel.append2SQLSelect(selBldr, colIndex);
            }

            final StringBuilder whereBldr = new StringBuilder();
            whereBldr.append(" where T0.").append(attr.getSqlColNames().get(0)).append("=")
                .append(PrintQuery.this.instance.getId());
            // in a subquery the type must also be set
            if (this.type.getMainTable().getSqlColType() != null) {
                whereBldr.append(" and T0.").append(this.type.getMainTable().getSqlColType()).append("=")
                .append(this.type.getId());
            }

            selBldr.append(fromBldr).append(whereBldr);

            if (PrintQuery.LOG.isDebugEnabled()) {
                PrintQuery.LOG.debug(selBldr.toString());
            }
            return selBldr;
        }

        /**
         * Getter method for instance variable {@link #oneSelect}.
         *
         * @return value of instance variable {@link #oneSelect}
         */
        public OneSelect getMainOneSelect()
        {
            return this.oneSelects.get(0);
        }

        /**
         * {@inheritDoc}
         */
        public Type getType()
        {
            return this.type;
        }
    }

    /**
     * Select Part for <code>linkto[ATTRIBUTENAME]</code>.
     */
    public class LinkToSelectPart implements PrintQuery.ISelectPart
    {
        /**
         * Name of the Attribute the link to is based on.
         */
        private final String attrName;

        /**
         * Type the {@link #attrName} belongs to.
         */
        private final Type type;

        /**
         * @param _attrName attribute name
         * @param _type     type
         */
        public LinkToSelectPart(final String _attrName, final Type _type)
        {
            this.attrName = _attrName;
            this.type = _type;
        }

        /**
         * {@inheritDoc}
         */
        public int join(final OneSelect _oneSelect, final StringBuilder _fromBldr, final int _relIndex)
        {
            final Attribute attr = this.type.getAttribute(this.attrName);
            Integer ret;
            final String tableName = attr.getLink().getMainTable().getSqlTable();
            ret = _oneSelect.getTableIndex(tableName, _relIndex);
            if (ret == null) {
                ret = _oneSelect.getNewTableIndex(tableName, _relIndex);
                _fromBldr.append(" left join ").append(tableName).append(" T").append(ret)
                    .append(" on T").append(_relIndex).append(".").append(attr.getSqlColNames().get(0))
                    .append("=T").append(ret).append(".ID");
            }
            return ret;
        }

        /**
         * {@inheritDoc}
         */
        public Type getType()
        {
            return this.type.getAttribute(this.attrName).getLink();
        }
    }

    /**
     * Select Part for <code>class[CLASSIFICATIONNAME]</code>.
     */
    public class ClassSelectPart implements PrintQuery.ISelectPart
    {

        /**
         * Classification this select part belongs to.
         */
        private final Classification classification;

        /**
         * @param _classification   classification
         */
        public ClassSelectPart(final String _classification)
        {
            this.classification = (Classification) Classification.get(_classification);
        }

        /**
         * {@inheritDoc}
         */
        public int join(final OneSelect _oneSelect, final StringBuilder _fromBldr, final int _relIndex)
        {
            Integer ret;
            final String tableName = this.classification.getMainTable().getSqlTable();
            ret = _oneSelect.getTableIndex(tableName, _relIndex);
            if (ret == null) {
                ret = _oneSelect.getNewTableIndex(tableName, _relIndex);
                _fromBldr.append(" left join ").append(tableName).append(" T").append(ret).append(" on T").append(
                                _relIndex).append(".ID=").append("T").append(ret).append(".").append(
                                this.classification.getLinkAttributeName());
            }
            return ret;
        }

        /**
         * {@inheritDoc}
         */
        public Type getType()
        {
            return this.classification;
        }
    }
}
