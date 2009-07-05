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
 * TODO description!
 * TODO .oid
 * TODO .type
 * TODO .linkfrom[]
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
     * Instance this PrintQUery is based on.
     */
    private final Instance instance;

    /**
     * Mapping of Select statements to OneSelect.
     */
    private final Map<String, OneSelect> selectStmt2OneSelect = new HashMap<String, OneSelect>();

    /**
     * Mapping of attributes to OneSelect.
     */
    private final Map<Attribute, OneSelect> attr2OneSelect = new HashMap<Attribute, OneSelect>();

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
            addAttribute(type.getAttribute(attrName));
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
            this.attr2OneSelect.put(attr, oneselect);
        }
        return this;
    }

    /**
     * Get the object returned by the given name of an attribute.
     *
     * @param _attributeName name of the attribute the object is wanted for
     * @return object for the select statement
     * @throws EFapsException on error
     */
    public Object getAttribute(final String _attributeName)
            throws EFapsException
    {
        final Type type = this.instance.getType();
        return getAttribute(type.getAttribute(_attributeName));
    }

    /**
     * Get the object returned by the given Attribute.
     *
     * @param _attribute the object is wanted for
     * @return object for the select statement
     * @throws EFapsException on error
     */
    public Object getAttribute(final Attribute _attribute)
            throws EFapsException
    {
        final OneSelect oneselect = this.attr2OneSelect.get(_attribute);
        return oneselect.getObject();
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
     *
     * @param _key  key for an expression the object is wanted for
     * @return object for the expression
     * @throws EFapsException allways
     */
    public Object getExpression(final String _key)
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
     * @param _selectStmt select statement the object is wanted for
     * @return object for the select statement
     * @throws EFapsException on error
     */
    public Object getSelect(final String _selectStmt)
            throws EFapsException
    {
        final OneSelect oneselect = this.selectStmt2OneSelect.get(_selectStmt);
        return oneselect.getObject();
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
        return executeOneCompleteStmt(createSQLStatement());
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
     * @param _complStmt complete statement instance to execute
     * @return true if the query contains values, else false
     * @throws EFapsException on error
     */
    private boolean executeOneCompleteStmt(final StringBuilder _complStmt)
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
                for (final OneSelect onesel : this.allSelects) {
                    onesel.setObject(rs);
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
         * List of column indexes the values have in the ResultSet returned
         * from the eFaps database.
         */
        private final List<Integer> colIndexs = new ArrayList<Integer>();

        /**
         * List of objects retrieved from the ResultSet returned
         * from the eFaps database.
         */
        private final List<Object> objectList = new ArrayList<Object>();

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
         * Set the Object for this OneSelect.
         *
         * @param _rs ResultSet from the eFaps database
         * @throws SQLException on error
         */
        public void setObject(final ResultSet _rs) throws SQLException
        {
            final ResultSetMetaData metaData = _rs.getMetaData();

            for (final Integer colIndex : this.colIndexs) {
                switch (metaData.getColumnType(colIndex)) {
                    case java.sql.Types.TIMESTAMP:
                        this.objectList.add(_rs.getTimestamp(colIndex));
                        break;
                    default:
                        this.objectList.add(_rs.getObject(colIndex));
                }
            }
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
         * Method used to append to the from part of an sql statement.
         * @param _fromBldr builder to append to
         */
        public void append2SQLFrom(final StringBuilder _fromBldr)
        {
            for (final ISelectPart sel : this.selectParts) {
                this.tableIndex = sel.join(_fromBldr, this.tableIndex);
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
            //in case that the OneSelct was instantiated for an attribute
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
                } else {
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
            final Pattern pattern = Pattern.compile("(?<=\\[)[a-zA-Z_]*(?=\\])");
            final String[] parts = this.selectStmt.split("\\.");

            for (final String part : parts) {
                if (part.startsWith("class")) {
                    final Matcher matcher = pattern.matcher(part);
                    matcher.find();
                    addClassificationSelectPart(matcher.group());
                } else if (part.startsWith("linkto")) {
                    final Matcher matcher = pattern.matcher(part);
                    matcher.find();
                    addLinkToSelectPart(matcher.group());
                } else if (part.startsWith("attribute")) {
                    final Matcher matcher = pattern.matcher(part);
                    matcher.find();
                    addAttributeSelectPart(matcher.group());
                } else {
                    addAttributeSelectPart(part);
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
            final IAttributeType attrInterf = this.attribute.newInstance();
            return attrInterf.readValue(this.objectList);
        }
    }

    /**
     * Interface used for the different Select parts.
     */
    public interface ISelectPart
    {
        /**
         * Method to join a table to the given from select statement.
         * @param _fromBldr StringBuilder containing the from select statement
         * @param _relIndex relation index
         * @return table index of the joint table
         */
        int join(final StringBuilder _fromBldr, final int _relIndex);

        /**
         * Method to get the TYpe the part belongs to.
         * @return type
         */
        Type getType();
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
         * @see org.efaps.db.PrintQuery.ISelectPart#join(java.lang.StringBuilder)
         * @param _fromBldr StringBuilder containing the from select statement
         * @param _relIndex relation index
         * @return table index of the joint table
         */
        public int join(final StringBuilder _fromBldr, final int _relIndex)
        {
            final Attribute attr = this.type.getAttribute(this.attrName);
            Integer ret;
            final String tableName = attr.getLink().getMainTable().getSqlTable();
            ret = getTableIndex(tableName, _relIndex);
            if (ret == null) {
                ret = getNewTableIndex(tableName, _relIndex);
                _fromBldr.append(" left join ").append(tableName).append(" T").append(ret)
                    .append(" on T").append(_relIndex).append(".").append(attr.getSqlColNames().get(0))
                    .append("=T").append(ret).append(".ID");
            }
            return ret;
        }

        /**
         * @see org.efaps.db.PrintQuery.ISelectPart#getType()
         * @return Type this LinkTo links to
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
         * @see org.efaps.db.PrintQuery.ISelectPart#join(java.lang.StringBuilder)
         * @param _fromBldr StringBuilder containing the from select statement
         * @param _relIndex relation index
         * @return table index of the joint table
         */
        public int join(final StringBuilder _fromBldr, final int _relIndex)
        {
            Integer ret;
            final String tableName = this.classification.getMainTable().getSqlTable();
            ret = getTableIndex(tableName, _relIndex);
            if (ret == null) {
                ret = getNewTableIndex(tableName, _relIndex);
                _fromBldr.append(" left join ").append(tableName).append(" T").append(ret).append(" on T").append(
                                _relIndex).append(".ID=").append("T").append(ret).append(".").append(
                                this.classification.getLinkAttributeName());
            }
            return ret;
        }

        /**
         * @see org.efaps.db.PrintQuery.ISelectPart#getType()
         * @return the classification
         */
        public Type getType()
        {
            return this.classification;
        }

    }
}
