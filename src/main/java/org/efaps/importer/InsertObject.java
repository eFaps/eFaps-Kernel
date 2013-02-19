/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.importer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class presents the main Object for the import of Data into eFaps and
 * the connected Database.</p>
 * <p>Every &quot;insert object&quot; can be a child to an other &quot;insert
 * object&quot;, so that a parent-child hierarchy can be constructed. The first
 * &quot;insert object&quot; must be the child to a
 * {@link RootObject root object}.</p>
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class InsertObject
    extends AbstractObject
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InsertObject.class);

    /**
     *Name of the Type of the current insert object.
     */
    private String type;

    /**
     * Map containing all Attributes of this insert object.
     */
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * Name of the attribute, which presents the parent-child-relation for this
     * insert object.
     */
    private String parentAttribute;

    /**
     * Contains all children of this insert object.
     */
    private final Map<String, List<AbstractObject>> childs = new HashMap<String, List<AbstractObject>>();

    /**
     * Contains the id for this insert object.
     */
    private long id;

    /**
     * Contains all links to {@link ForeignObject foreign objects} of this
     * insert object.
     */
    private final Set<ForeignObject> links = new HashSet<ForeignObject>();

    /**
     * Contains all Attributes which are defined as unique for this insert
     * object.
     */
    private final Set<String> uniqueAttributes = new HashSet<String>();

    /**
     * Contains the checkin object if this insert object contains one.
     */
    private CheckinObject checkInObject;

    /**
     * Default constructor.
     */
    public InsertObject()
    {
    }

    /**
     * Constructor used by
     * {@link InsertObjectBuilder#createObject(org.xml.sax.Attributes)}.
     *
     * @param _type     type of the insert object
     */
    public InsertObject(final String _type)
    {
        InsertObject.LOG.info("Creating new " + _type);
        setType(_type);
    }

    /**
     * Defines the type of this insert object.
     *
     * @param _type     type of this insert object
     */
    public void setType(final String _type)
    {
        this.type = _type;
    }

    /**
     * Adds an Attribute to the {@link #attributes} of this insert object and
     * in the case that the parameter <code>_unique</code> equals <i>true</i>
     * the attribute will also be added to the {@link #uniqueAttributes}.
     *
     * @param _name     name of the attribute
     * @param _value    value of the attribute
     * @param _unique   if <i>true</i> the attribute will be added to the
     *                  {@link #uniqueAttributes} of this insert object
     */
    public void addAttribute(final String _name,
                             final String _value,
                             final String _unique)
    {
        this.attributes.put(_name, _value.trim());
        if (_unique != null && _unique.equals("true")) {
            this.uniqueAttributes.add(_name);
        }
    }

    /**
     * Defines the {@link #parentAttribute parent attribute} of this insert
     * object. If the parameter <code>_unique</code> equals <i>true</i> the
     * attribute will also be added to the {@link #uniqueAttributes}.
     *
     * @param _parentAttribute  name of the Attribute
     * @param _unique           if <i>true</i> the attribute will be also added
     *                          to the {@link #uniqueAttributes}
     */
    public void setParentAttribute(final String _parentAttribute,
                                   final String _unique)
    {
        this.parentAttribute = _parentAttribute;
        if ((_unique != null) && "true".equals(_unique)) {
            this.uniqueAttributes.add(_parentAttribute);
        }
    }

    /**
     * Adds a child to this insert object separated for the different types. If
     * the type of the insert object is also an order object, the children will
     * be sorted.
     *
     * @param _object   child to be added
     */
    public void addChild(final AbstractObject _object)
    {
        List<AbstractObject> list = this.childs.get(_object.getType());
        if (list == null) {
            list = new ArrayList<AbstractObject>();
            this.childs.put(_object.getType(), list);
            list.add(_object);
        } else {
            list.add(_object);
            if (RootObject.getOrder(_object.getType()) != null) {
                final TreeSet<AbstractObject> treeSet
                    = new TreeSet<AbstractObject>(RootObject.getOrder(_object.getType()));
                treeSet.addAll(list);
                list.clear();
                list.addAll(treeSet);
            }
        }
    }

    /**
     * Checks if {@link #childs} is not empy meaning that this insert object
     * has children.
     *
     * @return <i>true</i> if this insert object has children; otherwise
     *         <i>false</i>
     * @see #childs
     */
    @Override
    public boolean hasChilds()
    {
        return !this.childs.isEmpty();
    }

    /**
     * Links given foreign <code>_object</code> to this insert object.
     *
     * @param _object   foreign object to link
     * @see #links
     */
    public void addLink(final ForeignObject _object)
    {
        this.links.add(_object);
    }

    /**
     * Adds the attribute with <code>_name</code> to the
     * {@link #uniqueAttributes} if <code>_unique</code> is equals true.
     *
     * @param _unique   the attribute will be added if equals "true"
     * @param _name     name of the attribute
     * @see #uniqueAttributes
     */
    public void addUniqueAttribute(final String _unique,
                                   final String _name)
    {
        if ((_unique != null) && "true".equals(_unique)) {
            this.uniqueAttributes.add(_name);
        }
    }

    /**
     * Defines the new <code>_id</code> of this insert object.
     *
     * @param _id   new id of the insert object
     * @see #id
     */
    @Override
    public void setID(final long _id)
    {
        this.id = _id;
    }

    @Override
    public void dbAddChilds()
    {
        Long newId = null;
        boolean noInsert = false;
        for (final List<AbstractObject> list : this.childs.values()) {
            for (final AbstractObject object : list) {
                noInsert = false;
                InsertObject.LOG.info("adding Child: {}", object.getType());
                InsertObject.LOG.debug("this: {}", toString());
                InsertObject.LOG.debug("Cild: {}", object.toString());
                try {
                    if (object.getUniqueAttributes().size() > 0) {
                        final QueryBuilder queryBldr = new QueryBuilder(Type.get(object.getType()));
                        for (final String element : object.getUniqueAttributes()) {
                            if (object.getAttributes().get(element) != null) {
                                queryBldr.addWhereAttrEqValue(element, object.getAttributes().get(element).toString());
                            }
                            if (object.getParrentAttribute() != null
                                    && object.getParrentAttribute().equals(element)) {
                                queryBldr.addWhereAttrEqValue(element, this.id);
                            }
                            for (final ForeignObject link : object.getLinks()) {
                                if (link.getLinkAttribute().equals(element)) {
                                    final String foreignValue = link.dbGetValue();
                                    if (foreignValue != null) {
                                        queryBldr.addWhereAttrEqValue(element, foreignValue);
                                    } else {
                                        noInsert = true;
                                    }
                                }
                            }
                        }
                        final InstanceQuery query = queryBldr.getQuery();
                        query.executeWithoutAccessCheck();
                        if (query.next() && !noInsert) {
                            newId = object.dbUpdateOrInsert(this, query.getCurrentValue().getId());
                        } else {
                            if (noInsert && !object.hasChilds()) {
                                InsertObject.LOG.error("skipt: " + object.toString());
                            } else {
                                newId = object.dbUpdateOrInsert(this, 0);
                            }
                        }
                    } else {
                        newId = object.dbUpdateOrInsert(this, 0);
                    }
                    object.setID(newId);

                    if (object.isCheckinObject()) {
                        object.dbCheckObjectIn();
                    }
                } catch (final EFapsException e) {
                    InsertObject.LOG.error("dbAddChilds() " + toString(), e);
                  //CHECKSTYLE:OFF
                } catch (final Exception e) {
                  //CHECKSTYLE:ON
                    InsertObject.LOG.error("dbAddChilds() " + toString(), e);
                }
            }
        }

        for (final List<AbstractObject> list : this.childs.values()) {
            for (final AbstractObject object : list) {
                object.dbAddChilds();
            }
        }
    }

    /**
     * Method to create or update an object.
     *
     * @param _parent   parent object of this object
     * @param _id       id of the object to be updated, if empty string "" is
     *                  given an insert will be made
     * @return string with the id of the new or updated object,
     *         <code>null</code> if the creation of the new object was skipped,
     *         because of a foreign object was not found
     */
    @Override
    public long dbUpdateOrInsert(final AbstractObject _parent,
                                 final long _id)
    {
        Boolean noInsert = false;
        Long newId = null;
        try {
            Update upIn;
            if (0 != _id)  {
                upIn = new Update(Type.get(this.type), _id);
            } else {
                upIn = new Insert(this.type);
            }
            for (final Entry<String, Object> element : getAttributes().entrySet()) {
                if (element.getValue() instanceof DateTime) {
                    upIn.add(element.getKey().toString(), (DateTime) element.getValue());
                } else {
                    upIn.add(element.getKey().toString(), element.getValue().toString());
                }
            }
            if (getParrentAttribute() != null) {
                upIn.add(getParrentAttribute(), _parent.getID());
            }
            for (final ForeignObject link : getLinks()) {
                final String foreignValue = link.dbGetValue();
                if (foreignValue != null) {
                    upIn.add(link.getLinkAttribute(), foreignValue);
                } else {
                    noInsert = true;
                    InsertObject.LOG.error("skipt: " + toString());
                }
            }
            if (!noInsert) {
                upIn.executeWithoutAccessCheck();
                newId = upIn.getId();
                upIn.close();
            }
        } catch (final EFapsException e) {
            InsertObject.LOG.error("dbUpdateOrInsert() " + toString(), e);
            newId = null;
            //CHECKSTYLE:OFF
        } catch (final Exception e) {
          //CHECKSTYLE:ON
            InsertObject.LOG.error("dbUpdateOrInsert() " + toString(), e);
            newId = null;
        }
        return newId;
    }

    /**
     * Returns the {@link #id} of the insert object.
     *
     * @return id of the insert object
     * @see #id
     */
    @Override
    public long getID()
    {
        return this.id;
    }

    /**
     * Returns the {@link #type} of the insert object.
     *
     * @return type of insert object
     * @see #type
     */
    @Override
    public String getType()
    {
        return this.type;
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        for (final Entry<String, Object> element : this.attributes.entrySet()) {
            final Attribute attribute = Type.get(this.type).getAttribute(element.getKey().toString());
// TODO das ist nur ein
// hack damit CreatedType als DateTimeType behandelt werden kann
            if (attribute.getAttributeType().getClassRepr().getName().equals(
                    "org.efaps.admin.datamodel.attributetype.DateTimeType")
                || attribute.getAttributeType().getClassRepr().getName().equals(
                    "org.efaps.admin.datamodel.attributetype.CreatedType")) {
                final DateTimeFormatter fmt;
                if (RootObject.getDateFormat() == null) {
                    fmt = ISODateTimeFormat.dateTime();
                } else {
                    fmt = DateTimeFormat.forPattern(RootObject.getDateFormat());
                }
                final DateTime date = fmt.parseDateTime(element.getValue().toString());
                this.attributes.put(element.getKey(), date);
            }
        }
        return this.attributes;
    }

    @Override
    public Object getAttribute(final String _attribute)
    {
        return this.attributes.get(_attribute);
    }

    @Override
    public String getParrentAttribute()
    {
        return this.parentAttribute;
    }

    /**
     * Returns the set of all {@link #links}.
     *
     * @return all links
     * @see #links
     */
    @Override
    public Set<ForeignObject> getLinks()
    {
        return this.links;
    }

    /**
     * Returns the set of all {@link #uniqueAttributes unique attributes}.
     *
     * @return set of all unique attributes
     * @see #uniqueAttributes
     */
    @Override
    public Set<String> getUniqueAttributes()
    {
        return this.uniqueAttributes;
    }

    /**
     * Initializes {@link #checkInObject} with a new instance depending on the
     * file <code>_name</code> and the <code>_url</code> of the check in
     * object.
     *
     * @param _name     name of the check in object
     * @param _url      URL to the File of the check in object
     * @see #checkInObject
     */
    public void setCheckinObject(final String _name,
                                 final String _url)
    {
        this.checkInObject = new CheckinObject(_name, _url);
    }

    /**
     * Checks if a check in object for this insert object is defined.
     *
     * @return <i>true</i> if a check in object exists (meaning a file must be
     *         checked in); otherwise <i>false</i>
     */
    @Override
    public boolean isCheckinObject()
    {
        return this.checkInObject != null;
    }

    @Override
    public void dbCheckObjectIn()
    {
        final Checkin checkin = new Checkin(Instance.get(Type.get(this.type), this.id));
        try {
            checkin.executeWithoutAccessCheck(this.checkInObject.getName(), this.checkInObject.getInputStream(), -1);
        } catch (final EFapsException e) {
            InsertObject.LOG.error("checkObjectin() " + toString(), e);
        }
    }

    /**
     * Returns the link representation for this insert object including the
     * {@link #type}, {@link #parentAttribute parent attribute} and all
     * {@link #links}.
     *
     * @return string representation of this class
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("type", this.type)
            .append("parent attribute", this.parentAttribute)
            .append("links", this.links.toString())
            .toString();
    }

    /**
     * Class to store the information, needed to check in a insert object.
     */
    public class CheckinObject
    {
        /**
         * Contains the file name of the check in object.
         */
        private String name = null;

        /**
         * Contains the URL to the file.
         */
        private String url = null;

        /**
         * Constructor setting the {@link #name file name} and the
         * {@link #url URL} of the check in object.
         *
         * @param _name     file name of the check in object
         * @param _url      URL to the File
         */
        public CheckinObject(final String _name,
                             final String _url)
        {
            this.name = _name.trim();
            this.url = _url.trim();
        }

        /**
         * Returns the file name of the check in object.
         *
         * @return file name of the check in object
         * @see #name
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Returns the {@link #url URL} of the check in object.
         *
         * @return URL to the file
         * @see #url
         */
        public String getURL()
        {
            return this.url;
        }

        /**
         * Returns an input stream of the file to check in.
         *
         * @return input stream of the file
         */
        public InputStream getInputStream()
        {
            InputStream inputstream;
            try {
                inputstream = new FileInputStream(this.url);
            } catch (final FileNotFoundException e) {
                InsertObject.LOG.error("getInputStream()", e);
                inputstream = null;
            }
            return inputstream;
        }
    }
}
