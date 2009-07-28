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

package org.efaps.db.print;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.AbstractPrintQuery;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.print.value.AbstractValueSelect;
import org.efaps.db.print.value.AttributeValueSelect;
import org.efaps.db.print.value.IDValueSelect;
import org.efaps.db.print.value.LabelValueSelect;
import org.efaps.db.print.value.OIDValueSelect;
import org.efaps.db.print.value.TypeValueSelect;
import org.efaps.db.print.value.UUIDValueSelect;
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
    private final List<ISelectPart> selectParts = new ArrayList<ISelectPart>();

    /**
     * FromSelect this OneSelect belong to.
     */
    private LinkFromSelect fromSelect;

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
     * PrintQuery this ONeSelct belongs to.
     */
    private final AbstractPrintQuery query;

    /**
     * Iterator for the objects.
     */
    private Iterator<Object> objectIterator;

    /**
     * Iterator for the ids.
     */
    private Iterator<Long> idIterator;

    /**
     * current id.
     */
    private Long currentId;

    /**
     * Currecnt object.
     */
    private Object currentObject;

    private AbstractValueSelect valueSelect;

    /**
     * @param _query        PrintQuery this OneSelect belongs to
     * @param _selectStmt selectStatement this OneSelect belongs to
     */
    public OneSelect(final AbstractPrintQuery _query, final String _selectStmt)
    {
        this.query = _query;
        this.selectStmt = _selectStmt;
    }

    /**
     * @param _attr attribute to be used in this OneSelect
     */
    public OneSelect(final AbstractPrintQuery _query, final Attribute _attr)
    {
        this.query = _query;
        this.selectStmt = null;
        this.valueSelect =  new AttributeValueSelect(_attr);
    }

    /**
     * Getter method for instance variable {@link #valueSelect}.
     *
     * @return value of instance variable {@link #valueSelect}
     */
    public AbstractValueSelect getValueSelect()
    {
        return this.valueSelect;
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
     * Getter method for instance variable {@link #selectParts}.
     *
     * @return value of instance variable {@link #selectParts}
     */
    public List<ISelectPart> getSelectParts()
    {
        return this.selectParts;
    }

    /**
     * Setter method for instance variable {@link #attribute}.
     * @param _attribute Attribute to set
     */
    public void setAttribute(final Attribute _attribute)
    {
        this.valueSelect = new AttributeValueSelect(_attribute);
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
        AbstractValueSelect tmpValueSelect;
        if (this.valueSelect ==  null) {
            tmpValueSelect = this.fromSelect.getMainOneSelect().getValueSelect();
        } else {
            tmpValueSelect = this.valueSelect;
        }

        if (tmpValueSelect.getColIndexs().size() > 1) {
            final Object[] objArray = new Object[tmpValueSelect.getColIndexs().size()];
            int i = 0;
            for (final Integer colIndex : tmpValueSelect.getColIndexs()) {
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
        } else if (tmpValueSelect.getColIndexs().size() > 0) {
            switch (metaData.getColumnType(tmpValueSelect.getColIndexs().get(0))) {
                case java.sql.Types.TIMESTAMP:
                    object = _rs.getTimestamp(tmpValueSelect.getColIndexs().get(0));
                    break;
                default:
                    object = _rs.getObject(tmpValueSelect.getColIndexs().get(0));
            }
        }
        this.objectList.add(object);
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

//            if ("id".equals(this.attrName)) {
//                this.attribute = type.getAttribute("ID");
//                _fromBldr.append(",T").append(this.tableIndex)
//                    .append(".").append(type.getMainTable().getSqlColId());
//                this.colIndexs.add(_colIndex);
//                ret++;
//            } else if ("oid".equals(this.attrName)) {
//                this.attribute = type.getAttribute("OID");
//                for (final String colName : this.attribute.getSqlColNames()) {
//                    _fromBldr.append(",T").append(this.tableIndex).append(".").append(colName);
//                    this.colIndexs.add(_colIndex + ret);
//                    ret++;
//                }

        Type type;
        if (this.selectParts.size() > 0) {
            type = this.selectParts.get(this.selectParts.size() - 1).getType();
        } else {
            type = this.query.getMainType();
        }
        int ret;
        if (this.valueSelect == null) {
            ret = this.fromSelect.getMainOneSelect().getValueSelect().append2SQLSelect(type, _fromBldr, this.tableIndex,
                                                                                       _colIndex);
        } else {
            ret = this.valueSelect.append2SQLSelect(type, _fromBldr, this.tableIndex, _colIndex);
        }

        return ret;
    }

    /**
     * Method to analyse the select statement. Meaning the the different
     * select parts will be added to {@link #selectParts}.
     * @throws EFapsException on error
     */
    public void analyzeSelectStmt() throws EFapsException
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
                    addValueSelect(new AttributeValueSelect(matcher.group()));
                }
            } else if (part.startsWith("linkfrom")) {
                final Matcher matcher = linkfomPat.matcher(part);
                if (matcher.find()) {
                    currentSelect.addLinkFromSelectPart(matcher.group());
                    currentSelect = currentSelect.fromSelect.getMainOneSelect();
                }
            } else if (part.equalsIgnoreCase("oid")) {
                addValueSelect(new OIDValueSelect());
            } else if (part.equalsIgnoreCase("type")) {
                addValueSelect(new TypeValueSelect());
            } else if (part.equalsIgnoreCase("label")) {
                addValueSelect(new LabelValueSelect());
            } else if (part.equalsIgnoreCase("id")) {
                addValueSelect(new IDValueSelect());
            } else if (part.equalsIgnoreCase("uuid")) {
                addValueSelect(new UUIDValueSelect());
            }
        }
    }


    /**
     * @param _valueSee
     * @throws EFapsException
     */
    private void addValueSelect(final AbstractValueSelect _valueSelect) throws EFapsException
    {
        if (this.valueSelect != null) {
            this.valueSelect.addChildValueSelect(_valueSelect);
        } else if (this.fromSelect != null && !(this.query instanceof LinkFromSelect)) {
            this.fromSelect.getMainOneSelect().addValueSelect(_valueSelect);
        } else {
            this.valueSelect = _valueSelect;
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
        if (this.valueSelect == null) {
            if (this.fromSelect.hasResult()) {
                ret = this.fromSelect.getMainOneSelect().getObject();
            }
        } else {
            // if the currentObject is not null it means that the values are
            // retrieved by iteration through the objectlist
            if (this.currentObject != null) {
                ret = this.valueSelect.getValue(this.currentObject);
            } else {
                ret = this.valueSelect.getValue(this.objectList);
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
        if (this.valueSelect == null) {
            ret.addAll(this.fromSelect.getMainOneSelect().getInstances());
        } else {
            for (final Long id : this.idList) {
                ret.add(Instance.get(this.valueSelect.getAttribute().getParent(), id.toString()));
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
        if (this.valueSelect == null) {
            ret = this.fromSelect.getMainOneSelect().getAttribute();
        } else {
            ret = this.valueSelect.getAttribute();
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
     * Setter method for instance variable {@link #fromSelect}.
     *
     * @param _fromSelect value for instance variable {@link #fromSelect}
     */
    public void setFromSelect(final LinkFromSelect _fromSelect)
    {
        this.fromSelect = _fromSelect;
    }

    /**
     * Method to determine if this OneSelect does return more than one value.
     * @return true if more than one value is returned, else false
     */
    public boolean isMulitple()
    {
        boolean ret;
        if (this.valueSelect == null) {
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
        if (this.valueSelect == null  && this.fromSelect != null) {
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
        if (this.valueSelect == null && this.fromSelect != null) {
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
