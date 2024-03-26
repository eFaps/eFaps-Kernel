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
package org.efaps.db.print;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.AbstractPrintQuery;
import org.efaps.db.Instance;
import org.efaps.db.print.value.AbstractValueSelect;
import org.efaps.db.print.value.AttributeValueSelect;
import org.efaps.db.print.value.BaseValueSelect;
import org.efaps.db.print.value.ClassificationValueSelect;
import org.efaps.db.print.value.EsjpValueSelect;
import org.efaps.db.print.value.FormatValueSelect;
import org.efaps.db.print.value.IDValueSelect;
import org.efaps.db.print.value.InstanceValueSelect;
import org.efaps.db.print.value.KeyValueSelect;
import org.efaps.db.print.value.LabelValueSelect;
import org.efaps.db.print.value.LengthValueSelect;
import org.efaps.db.print.value.NameValueSelect;
import org.efaps.db.print.value.OIDValueSelect;
import org.efaps.db.print.value.StatusValueSelect;
import org.efaps.db.print.value.TypeValueSelect;
import org.efaps.db.print.value.UUIDValueSelect;
import org.efaps.db.print.value.UoMValueSelect;
import org.efaps.db.print.value.ValueValueSelect;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used as a part of one complete statement to be executed
 * against the eFaps database. It will normally return the object for one
 * attribute. The OneSelect can consist from only one attribute or of a
 * series of PrintQuery.ISelectPart
 *
 * @author The eFaps Team
 */
public class OneSelect
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OneSelect.class);

    /**
     * The select this OneSelect belongs to.
     */
    private final String selectStmt;

    /**
     * List of select parts.
     */
    private final List<ISelectPart> selectParts = new ArrayList<>();

    /**
     * FromSelect this OneSelect belong to.
     */
    private LinkFromSelect fromSelect;

    /**
     * List of objects retrieved from the ResultSet returned
     * from the eFaps database. It represent one row in a result set.
     */
    private final List<Object> objectList = new ArrayList<>();


    /**
     * List of ids retrieved from the ResultSet returned
     * from the eFaps database. It represent one row in a result set.
     */
    private final List<Long> idList = new ArrayList<>();

    /**
     * If this OneSelect is a FromSelect the relation Ids are stored in this
     * List.
     */
    private final List<Long> relIdList = new ArrayList<>();

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

    /**
     * Valueselect of this oneselect.
     */
    private AbstractValueSelect valueSelect;

    /**
     * @param _query        PrintQuery this OneSelect belongs to
     * @param _selectStmt selectStatement this OneSelect belongs to
     */
    public OneSelect(final AbstractPrintQuery _query,
                     final String _selectStmt)
    {
        this.query = _query;
        this.selectStmt = _selectStmt;
    }

    /**
     * @param _query    AbstractPrintQuery this OneSelect belongs to
     * @param _attr     attribute to be used in this OneSelect
     */
    public OneSelect(final AbstractPrintQuery _query,
                     final Attribute _attr)
    {
        this.query = _query;
        this.selectStmt = null;
        this.valueSelect =  new AttributeValueSelect(this, _attr);
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
     * Setter method for instance variable {@link #valueSelect}.
     *
     * @param _valueSelect value for instance variable {@link #valueSelect}
     */
    public void setValueSelect(final AbstractValueSelect _valueSelect)
    {
        this.valueSelect = _valueSelect;
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
        this.valueSelect = new AttributeValueSelect(this, _attribute);
    }

    /**
     * Add an Object for this OneSelect.
     *
     * @param _row Objects from the eFaps database
     * @throws SQLException on error
     */
    public void addObject(final Object[] _row)
        throws SQLException
    {
        // store the ids also
        this.idList.add((Long) _row[0]);

        if (getFromSelect() != null) {
            final int column = "id".equals(this.valueSelect.getValueType())
                            ? this.valueSelect.getColIndexs().get(0) : 2;
            this.relIdList.add((Long) _row[column - 1]);
            // this means that it is a chained LinkFromSelect, but exclude
            // AttributeSets
            if (!getSelectParts().isEmpty() && getSelectParts().get(0) instanceof LinkFromSelect.LinkFromSelectPart
                            && !(((LinkFromSelect.LinkFromSelectPart) getSelectParts().get(0)).getType()
                                            instanceof AttributeSet)) {
                this.idList.set(this.idList.size() - 1, (Long) _row[1]);
            }
        }
        Object object = null;
        final AbstractValueSelect tmpValueSelect;
        if (this.valueSelect == null) {
            tmpValueSelect = this.fromSelect.getMainOneSelect().getValueSelect();
        } else {
            tmpValueSelect = this.valueSelect;
        }
        if (tmpValueSelect.getParentSelectPart() != null) {
            tmpValueSelect.getParentSelectPart().addObject(_row);
        }

        if (tmpValueSelect.getColIndexs().size() > 1) {
            final Object[] objArray = new Object[tmpValueSelect.getColIndexs().size()];
            int i = 0;
            for (final Integer colIndex : tmpValueSelect.getColIndexs()) {
                objArray[i] = _row[colIndex - 1];
                i++;
            }
            object = objArray;
        } else if (tmpValueSelect.getColIndexs().size() > 0) {
            object = _row[tmpValueSelect.getColIndexs().get(0) - 1];
        }
        this.objectList.add(object);
    }

    /**
     * Add a classification name evaluated from an
     * <code>class[CLASSIFICATIONNAME]</code> part of an select statement.
     * @param _classificationName   name of the classification
     * @throws CacheReloadException on error
     */
    public void addClassificationSelectPart(final String _classificationName)
        throws CacheReloadException
    {
        this.selectParts.add(new ClassSelectPart(_classificationName));
    }

    /**
     * Add the name of the attribute the link must go to, evaluated from an
     * <code>linkTo[ATTRIBUTENAME]</code> part of an select statement.
     *
     * @param _linkTo   name of the attribute the link must go to
     * @throws EFapsException the e faps exception
     */
    public void addLinkToSelectPart(final String _linkTo)
        throws EFapsException
    {
        final Type type;
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
     * Add the select part to connect the general store.
     *
     * @throws EFapsException the e faps exception
     */
    public void addFileSelectPart()
        throws EFapsException
    {
        final Type type;
        // if a previous select exists it is based on the previous select,
        // else it is based on the basic table
        if (this.selectParts.size() > 0) {
            type = this.selectParts.get(this.selectParts.size() - 1).getType();
        } else {
            type = this.query.getMainType();
        }
        final FileSelectPart linkto = new FileSelectPart(type);
        this.selectParts.add(linkto);
    }

    /**
     * Add the select part to connect the general instances.
     *
     * @throws EFapsException the e faps exception
     */
    public void addGenInstSelectPart()
        throws EFapsException
    {
        final Type type;
        // if a previous select exists it is based on the previous select,
        // else it is based on the basic table
        if (this.selectParts.size() > 0) {
            type = this.selectParts.get(this.selectParts.size() - 1).getType();
        } else {
            type = this.query.getMainType();
        }
        final GenInstSelectPart linkto = new GenInstSelectPart(type);
        this.selectParts.add(linkto);
    }

    /**
     * Add the name of the attribute the link must go to, evaluated from an
     * <code>attributeSet[ATTRIBUTESETNAME]</code> part of an select statement.
     *
     * @param _attributeSet   name of the attribute the link must go to
     * @throws EFapsException the e faps exception
     */
    public void addAttributeSetSelectPart(final String _attributeSet)
        throws EFapsException
    {
        addAttributeSetSelectPart(_attributeSet, null);
    }

    /**
     * Adds the attribute set select part.
     *
     * @param _attributeSet the attribute set
     * @param _where the where
     * @throws EFapsException on error
     */
    public void addAttributeSetSelectPart(final String _attributeSet,
                                          final String _where)
        throws EFapsException
    {
        final Type type;
        // if a previous select exists it is based on the previous select,
        // else it is based on the basic table
        if (this.selectParts.size() > 0) {
            type = this.selectParts.get(this.selectParts.size() - 1).getType();
        } else {
            type = this.query.getMainType();
        }

        try {
            final AttributeSet set = AttributeSet.find(type.getName(), _attributeSet);
            final String linkFrom = set.getName() + "#" + set.getAttributeName();
            this.fromSelect = new LinkFromSelect(linkFrom, getQuery().isCacheEnabled() ? getQuery().getKey() : null);
            this.fromSelect.addWhere(_where);
        } catch (final CacheReloadException e) {
            OneSelect.LOG.error("Could not find AttributeSet for Type: {}, attribute: {}", type.getName(),
                            _attributeSet);
        }
    }

    /**
     * Add the name of the type and attribute the link comes from,
     * evaluated from an <code>linkTo[TYPENAME#ATTRIBUTENAME]</code>
     * part of an select statement.
     * @param _linkFrom   name of the attribute the link comes from
     * @throws CacheReloadException on erro
     */
    public void addLinkFromSelectPart(final String _linkFrom)
        throws CacheReloadException
    {
        this.fromSelect = new LinkFromSelect(_linkFrom, getQuery().isCacheEnabled() ? getQuery().getKey() : null);
    }

    /**
     * Method used to append to the from part of an SQL statement.
     *
     * @param _select   SQL select wrapper
     * @throws EFapsException on error
     */
    public void append2SQLFrom(final SQLSelect _select)
        throws EFapsException
    {
        // for attributes it must be evaluated if the attribute is inside a child table
        if (this.valueSelect != null && "attribute".equals(this.valueSelect.getValueType())) {
            final Type type;
            if (this.selectParts.size() > 0) {
                type = this.selectParts.get(this.selectParts.size() - 1).getType();
            } else {
                type = this.query.getMainType();
            }
            Attribute attr = this.valueSelect.getAttribute();
            if (attr == null) {
                attr = type.getAttribute(((AttributeValueSelect) this.valueSelect).getAttrName());
            }
            // if the attr is still null that means that the type does not have this attribute, so last
            // chance to find the attribute is to search in the child types
            if (attr == null) {
                for (final Type childType : type.getChildTypes()) {
                    attr = childType.getAttribute(((AttributeValueSelect) this.valueSelect).getAttrName());
                    if (attr != null) {
                        ((AttributeValueSelect) this.valueSelect).setAttribute(attr);
                        break;
                    }
                }
            }
            if (attr != null && attr.getTable() != null && !attr.getTable().equals(type.getMainTable())) {
                final ChildTableSelectPart childtable = new ChildTableSelectPart(type, attr.getTable());
                this.selectParts.add(childtable);
            }
        }
        for (final ISelectPart sel : this.selectParts) {
            this.tableIndex = sel.join(this, _select, this.tableIndex);
        }
    }

    /**
     * Method used to append to the select part of an SQL statement.
     *
     * @param _select       SQL select statement
     * @param _colIndex     actual column index
     * @return number of columns added in this part of the SQL statement
     * @throws EFapsException on error
     */
    public int append2SQLSelect(final SQLSelect _select,
                                final int _colIndex)
        throws EFapsException
    {
        final Type type;
        if (this.selectParts.size() > 0) {
            type = this.selectParts.get(this.selectParts.size() - 1).getType();
        } else {
            type = this.query.getMainType();
        }
        final int ret;
        if (this.valueSelect == null) {
            ret = this.fromSelect.getMainOneSelect().getValueSelect().append2SQLSelect(type, _select, this.tableIndex,
                                                                                       _colIndex);
        } else {
            ret = this.valueSelect.append2SQLSelect(type, _select, this.tableIndex, _colIndex);
        }

        return ret;
    }

    /**
     * Method used to append to the where part of an SQL statement.
     *
     * @param _select   SQL select wrapper
     * @throws EFapsException on error
     */
    public void append2SQLWhere(final SQLSelect _select)
        throws EFapsException
    {
        for (final ISelectPart part : this.selectParts) {
            part.add2Where(this, _select);
        }
    }

    /**
     * Method to analyse the select statement. Meaning the the different
     * select parts will be added to {@link #selectParts}.
     * @throws EFapsException on error
     */
    public void analyzeSelectStmt()
        throws EFapsException
    {
        final Pattern mainPattern = Pattern.compile("[^.]+");
        final Pattern attrPattern = Pattern.compile("(?<=\\[)[0-9a-zA-Z_]*(?=\\])");
        final Pattern esjpPattern = Pattern.compile("(?<=\\[)[\\w\\d\\s,.\"]*(?=\\])");
        final Pattern linkfomPat = Pattern.compile("(?<=\\[)[0-9a-zA-Z_#:]*(?=\\])");
        final Pattern formatPat = Pattern.compile("(?<=\\[).*(?=\\])");
        final Pattern attrSetPattern = Pattern.compile("(?<=\\[)[0-9a-zA-Z_]+");
        final Pattern attrSetWherePattern = Pattern.compile("(?<=\\|).+(?=\\])");

        final Matcher mainMatcher = mainPattern.matcher(this.selectStmt);

        OneSelect currentSelect = this;
        while (mainMatcher.find()) {
            final String part = mainMatcher.group();
            if (part.startsWith("class[")) {
                final Matcher matcher = attrPattern.matcher(part);
                if (matcher.find()) {
                    currentSelect.addClassificationSelectPart(matcher.group());
                }
            } else if (part.startsWith("linkto")) {
                final Matcher matcher = attrPattern.matcher(part);
                if (matcher.find()) {
                    currentSelect.addLinkToSelectPart(matcher.group());
                }
            } else if (part.startsWith("attributeset")) {
                final Matcher matcher = attrSetPattern.matcher(part);
                if (matcher.find()) {
                    currentSelect.addValueSelect(new IDValueSelect(currentSelect));
                    final Matcher whereMatcher = attrSetWherePattern.matcher(part);
                    if (whereMatcher.find()) {
                        currentSelect.addAttributeSetSelectPart(matcher.group(), whereMatcher.group());
                    } else {
                        currentSelect.addAttributeSetSelectPart(matcher.group());
                    }
                    currentSelect = currentSelect.fromSelect.getMainOneSelect();
                }
            } else if (part.startsWith("attribute")) {
                final Matcher matcher = attrPattern.matcher(part);
                if (matcher.find()) {
                    currentSelect.addValueSelect(new AttributeValueSelect(currentSelect, matcher.group()));
                }
            } else if (part.startsWith("linkfrom")) {
                final Matcher matcher = linkfomPat.matcher(part);
                if (matcher.find()) {
                    currentSelect.addValueSelect(new IDValueSelect(currentSelect));
                    currentSelect.addLinkFromSelectPart(matcher.group());
                    currentSelect = currentSelect.fromSelect.getMainOneSelect();
                }
            } else if (part.startsWith("format")) {
                final Matcher matcher = formatPat.matcher(part);
                if (matcher.find()) {
                    currentSelect.addValueSelect(new FormatValueSelect(currentSelect, matcher.group()));
                }
            } else if (part.startsWith("esjp[")) {
                final String esjpPart = this.selectStmt.substring(this.selectStmt.indexOf("esjp["));
                final Matcher matcher = esjpPattern.matcher(esjpPart);
                if (matcher.find()) {
                    currentSelect.addValueSelect(new EsjpValueSelect(currentSelect, matcher.group()));
                }
            }  else if (part.startsWith("exec")) {
                final String execPart = this.selectStmt.substring(this.selectStmt.indexOf("exec"));
                currentSelect.addValueSelect(new EsjpValueSelect(currentSelect, execPart));
            } else {
                switch (part) {
                    case "oid":
                        currentSelect.addValueSelect(new OIDValueSelect(currentSelect));
                        break;
                    case "type":
                        currentSelect.addValueSelect(new TypeValueSelect(currentSelect));
                        break;
                    case "instance":
                        currentSelect.addValueSelect(new InstanceValueSelect(currentSelect));
                        break;
                    case "label":
                        currentSelect.addValueSelect(new LabelValueSelect(currentSelect));
                        break;
                    case "id":
                        currentSelect.addValueSelect(new IDValueSelect(currentSelect));
                        break;
                    case "uuid":
                        currentSelect.addValueSelect(new UUIDValueSelect(currentSelect));
                        break;
                    case "name":
                        currentSelect.addValueSelect(new NameValueSelect(currentSelect));
                        break;
                    case "class":
                        currentSelect.addValueSelect(new ClassificationValueSelect(currentSelect));
                        break;
                    case "value":
                        currentSelect.addValueSelect(new ValueValueSelect(currentSelect));
                        break;
                    case "base":
                        currentSelect.addValueSelect(new BaseValueSelect(currentSelect));
                        break;
                    case "uom":
                        currentSelect.addValueSelect(new UoMValueSelect(currentSelect));
                        break;
                    case "file":
                        currentSelect.addFileSelectPart();
                        break;
                    case "length":
                        currentSelect.addValueSelect(new LengthValueSelect(currentSelect));
                        break;
                    case "status":
                        currentSelect.addValueSelect(new StatusValueSelect(currentSelect));
                        break;
                    case "key":
                        currentSelect.addValueSelect(new KeyValueSelect(currentSelect));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * @param _valueSelect AbstractValueSelect to add
     * @throws EFapsException on error
     */
    private void addValueSelect(final AbstractValueSelect _valueSelect)
        throws EFapsException
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
     * Get an object from a "n to 1" Relation.Therefore the given object
     * is used to filter only the valid values from the by the Database
     * returned objects.
     * @param _object Object used as filter (must be an <code>Long</code> Id)
     * @return  Object
     * @throws EFapsException on error
     */
    private Object getObject(final Object _object)
        throws EFapsException
    {
        final Object ret;
        // inside a fromobject the correct value must be set
        if (this.fromSelect != null && _object instanceof Number) {
            final List<Object> tmpList = new ArrayList<>();
            final Long id = ((Number) _object).longValue();
            Iterator<Long> relIter = this.relIdList.iterator();
            // chained linkfroms
            if (!getSelectParts().isEmpty() && getSelectParts().get(0) instanceof LinkFromSelect.LinkFromSelectPart
                            && !(((LinkFromSelect.LinkFromSelectPart) getSelectParts().get(0)).getType()
                                            instanceof AttributeSet)) {
                relIter = this.idList.iterator();
            } else {
                relIter = this.relIdList.iterator();
            }
            final Iterator<Object> objIter = this.objectList.iterator();
            while (relIter.hasNext()) {
                final Long rel = relIter.next();
                final Object obj = objIter.next();
                if (rel.equals(id)) {
                    tmpList.add(obj);
                }
            }
            if (this.valueSelect == null) {
                final List<Object> retTmp = new ArrayList<>();
                for (final Object obj : tmpList) {
                    retTmp.add(this.fromSelect.getMainOneSelect().getObject(obj));
                }
                ret = retTmp.size() > 0 ? retTmp.size() > 1 ? retTmp : retTmp.get(0) : null;
            } else {
                ret = this.valueSelect.getValue(tmpList);
            }
        } else {
            ret = this.getObject();
        }
        return ret;
    }

    /**
     * Method to get the Object for this OneSelect.
     * @return  object for this OneSelect
     * @throws EFapsException on error
     */
    public Object getObject()
        throws EFapsException
    {
        Object ret = null;
        if (this.valueSelect == null) {
            // if the fromSelect has data
            if (this.fromSelect.hasResult()) {
                // and there are more than one id the current object must not be null
                if (this.idList.size() > 1 && this.currentObject != null) {
                    ret = this.fromSelect.getMainOneSelect().getObject(this.currentObject);
                // or if there is only one id the first objectvalue must not be null
                } else if (this.idList.size() == 1 && this.objectList.get(0) != null) {
                    ret = this.fromSelect.getMainOneSelect().getObject(this.currentObject);
                }
            }
        } else {
            // if the currentObject is not null it means that the values are
            // retrieved by iteration through the object list
            if (this.currentId != null) {
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
     * @return Collection of Instances
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public List<Instance> getInstances()
        throws EFapsException
    {
        final List<Instance> ret = new ArrayList<>();
        // no value select means, that the from select must be asked
        if (this.valueSelect == null) {
            ret.addAll(this.fromSelect.getMainOneSelect().getInstances());
        } else {
            // if an oid select was given the oid is evaluated
            if ("oid".equals(this.valueSelect.getValueType())) {
                for (final Object object : this.objectList) {
                    final Instance inst = Instance.get((String) this.valueSelect.getValue(object));
                    if (inst.isValid()) {
                        ret.add(inst);
                    }
                }
            } else {
                final List<Long> idTmp;
                if (this.valueSelect.getParentSelectPart() != null
                                && this.valueSelect.getParentSelectPart() instanceof LinkToSelectPart) {
                    idTmp = (List<Long>) this.valueSelect.getParentSelectPart().getObject();
                } else {
                    idTmp = this.idList;
                }
                for (final Long id : idTmp) {
                    if (id != null) {
                        ret.add(Instance.get(this.valueSelect.getAttribute().getParent(), String.valueOf(id)));
                    }
                }
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
        final Attribute ret;
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
     * @param _object Object used as filter (must be an <code>Long</code> Id)
     * @return true if more than one value is returned, else false
     * @throws EFapsException on error
     */
    private boolean isMultiple(final Object _object)
        throws EFapsException
    {
        final boolean ret;
        if (_object instanceof Long && this.fromSelect != null) {
            final Object object = this.getObject(_object);
            if (object instanceof List<?>) {
                ret = true;
            } else {
                ret = false;
            }
        } else {
            ret = this.isMultiple();
        }
        return ret;
    }

    /**
     * Method to determine if this OneSelect does return more than one value.
     * @return true if more than one value is returned, else false
     * @throws EFapsException on error
     */
    public boolean isMultiple()
        throws EFapsException
    {
        final boolean ret;
        if (this.valueSelect == null) {
            ret = this.fromSelect.getMainOneSelect().isMultiple(this.currentObject);
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
     * @param _column       name of the column, used for the relation
     * @param _relIndex     relation the table is used in
     * @param _clazzId      optional id of the classification
     * @return new index for the table
     */
    public Integer getNewTableIndex(final String _tableName,
                                    final String _column,
                                    final Integer _relIndex,
                                    final Long _clazzId)
    {
        final int ret;
        if (this.valueSelect == null && this.fromSelect != null) {
            ret = this.fromSelect.getNewTableIndex(_tableName, _column, _relIndex, _clazzId);
        } else {
            ret = this.query.getNewTableIndex(_tableName, _column, _relIndex, _clazzId);
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
     * @param _column       name of the column, used for the relation
     * @param _relIndex     relation the table is used in
     * @param _clazzId      optional id of the classification
     * @return index of the table or null if not found
     */
    public Integer getTableIndex(final String _tableName,
                                 final String _column,
                                 final int _relIndex,
                                 final Long _clazzId)
    {
        final Integer ret;
        if (this.valueSelect == null && this.fromSelect != null) {
            ret = this.fromSelect.getTableIndex(_tableName, _column, _relIndex, _clazzId);
        } else {
            ret = this.query.getTableIndex(_tableName, _column, _relIndex, _clazzId);
        }
        return ret;
    }

    /**
     * @return true if next, else false
     * @throws EFapsException on error
     */
    public boolean next()
        throws EFapsException
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
            for (final ISelectPart part : getSelectParts()) {
                part.next();
            }
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #query}.
     *
     * @return value of instance variable {@link #query}
     */
    public AbstractPrintQuery getQuery()
    {
        return this.query;
    }

    /**
     * Getter method for the instance variable {@link #objectList}.
     *
     * @return value of instance variable {@link #objectList}
     */
    public List<Object> getObjectList()
    {
        return this.objectList;
    }

    /**
     * Method sorts the object and idList.
     * @param _targetList   list of instances
     * @param _currentList  list if instances how it is sorted now
     */
    public void sortByInstanceList(final List<Instance> _targetList,
                                   final Map<Instance, Integer> _currentList)
    {
        final List<Long> idListNew = new ArrayList<>();
        final List<Object> objectListNew = new ArrayList<>();
        for (final Instance instance : _targetList) {
            if (_currentList.containsKey(instance)) {
                final Integer i = _currentList.get(instance);
                idListNew.add(this.idList.get(i));
                objectListNew.add(this.objectList.get(i));
            }
        }
        this.idList.clear();
        this.idList.addAll(idListNew);
        this.objectList.clear();
        this.objectList.addAll(objectListNew);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
