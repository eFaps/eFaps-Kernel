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

package org.efaps.db.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.AbstractPrintQuery;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.util.EFapsException;

/**
 * This class is used as a part of one complete statement to be executed
 * against the eFaps database. It will normally return the object for one
 * attribute. The OneSelect can consist from only one attribute or of a
 * series of {@link PrintQuery.ISelectPart}
 *
 * @author The eFaps Team
 * @version $Id$
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
    final List<ISelectPart> selectParts = new ArrayList<ISelectPart>();

    /**
     * FromSelect this OneSelect belong to.
     */
    LinkFromSelect fromSelect;

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

    private AbstractPrintQuery query;

    private Iterator<Object> objectIterator;

    private Iterator<Long> idIterator;

    private Long currentId;

    private Object currentObject;

    /**
     * @param _selectStmt selectStatement this OneSelect belongs to
     * @param _subQuery
     * @param tableIndex
     */
    public OneSelect(final AbstractPrintQuery _query, final String _selectStmt)
    {
        this.query = _query;
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
     * Getter method for instance variable {@link #selectStmt}.
     *
     * @return value of instance variable {@link #selectStmt}
     */
    public String getSelectStmt()
    {
        return this.selectStmt;
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
            type = this.query.getMainType();
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
        this.fromSelect = new LinkFromSelect(_linkFrom);
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
                type = this.query.getMainType();
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
            if (this.fromSelect.hasResult()) {
                ret = this.fromSelect.getMainOneSelect().getObject();
            }
        } else {
            final IAttributeType attrInterf = this.attribute.newInstance();
            // if the currentObject is not null it means that the values are
            // retrieved by iteration through the objectlist
            if (this.currentObject != null) {
                final ArrayList<Object> tempList = new ArrayList<Object>();
                tempList.add(this.currentObject);
                ret = attrInterf.readValue(tempList);
            } else {
                ret = attrInterf.readValue(this.objectList);
            }
        }
        return ret;
    }

    /**
     * Method returns the instances this OneSelect has returned.
     *
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
    public LinkFromSelect getFromSelect()
    {
        return this.fromSelect;
    }

    /**
     * Method to determine if this OneSelect does return more than one value.
     * @return true if more than one value is returned, else false
     */
    public boolean isMulitple()
    {
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
    Integer getNewTableIndex(final String _tableName, final Integer _relIndex)
    {
        int ret;
        if (this.attribute == null  && this.fromSelect != null) {
            ret = this.fromSelect.getNewTableIndex(_tableName, _relIndex);
        } else {
            ret = this.query.getNewTableIndex(_tableName, _relIndex);
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
    Integer getTableIndex(final String _tableName, final int _relIndex)
    {
        Integer ret;
        if (this.attribute == null && this.fromSelect != null) {
            ret = this.fromSelect.getTableIndex(_tableName, _relIndex);
        } else {
            ret = this.query.getTableIndex(_tableName, _relIndex);
        }
        return ret;
    }

    /**
     * @return
     *
     */
    public boolean next()
    {
        boolean ret = false;
        if (this.objectIterator == null) {
            this.objectIterator = this.objectList.iterator();
        }
        if (this.idIterator == null) {
            this.idIterator = this.idList.iterator();
        }
        if (this.objectIterator.hasNext()) {
            this.currentObject = this.objectIterator.next();
            this.currentId = this.idIterator.next();
            ret = true;
        }
        return ret;
    }
}
