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
package org.efaps.admin.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.attributetype.DateTimeType;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Context;
import org.efaps.db.databases.information.ColumnInformation;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This is the class for the attribute description. The type description holds
 * information about creation of a new instance of a attribute with default
 * values.
 *
 * @author The eFaps Team
 *
 */
public class Attribute
    extends AbstractDataModelObject
{
    /**
     * ENUM used to access the different attribute types.
     */
    public enum AttributeTypeDef
    {
        /** Attribute type Link. */
        ATTRTYPE_LINK("440f472f-7be2-41d3-baec-4a2f0e4e5b31"),
        /** Attribute type Link with Ranges. */
        ATTRTYPE_LINK_WITH_RANGES("9d6b2e3e-68ce-4509-a5f0-eae42323a696"),
        /** Attribute type PersonLink. */
        ATTRTYPE_GROUP_LINK("a48538dd-5d9b-468f-a84f-bf42791eed66"),
        /** Attribute type PersonLink. */
        ATTRTYPE_PERSON_LINK("7b8f98de-1967-44e0-b174-027349868a61"),
        /** Attribute type Creator Link. */
        ATTRTYPE_CREATOR_LINK("76122fe9-8fde-4dd4-a229-e48af0fb4083"),
        /** Attribute type Modifier Link. */
        ATTRTYPE_MODIFIER_LINK("447a7c87-8395-48c4-b2ed-d4e96d46332c"),
        /** Attribute type Multi Line Array. */
        ATTRTYPE_MULTILINEARRAY("adb13c3d-9506-4da2-8d75-b54c76779c6c"),
        /** Attribute type Status. */
        ATTRTYPE_STATUS("0161bcdb-45e9-4839-a709-3a1c56f8a76a"),
        /** Attribute type Enum. */
        ATTRTYPE_ENUM("b7c6a324-5dec-425f-b778-fa8fabf80202"),
        /** Attribute type BitEnum. */
        ATTRTYPE_BITENUM("a9b1abde-d58d-4aea-8cdc-f2870111f1cd"),
        /** Attribute type BitEnum. */
        ATTRTYPE_JAXB("58817bd8-db76-4b40-8acd-18112fe96170"),
        /** Attribute type AssociationLink. */
        ATTRTYPE_ASSOC("0d296eba-0c1e-4b78-a2e3-01b1f4991cfe");

        /**
         * Stored the UUID for the given type.
         */
        private final UUID uuid;

        /**
         * Private Constructor.
         *
         * @param _uuid UUID to set
         */
        AttributeTypeDef(final String _uuid)
        {
            uuid = UUID.fromString(_uuid);
        }

        /**
         * @return the uuid
         */
        public UUID getUuid()
        {
            return uuid;
        }
    }

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Attribute.class);

    /**
     * SQL Statement to get attributes for at type.
     */
    private static final String SQL_TYPE = new SQLSelect()
                    .column("ID")
                    .column("NAME")
                    .column("TYPEID")
                    .column("DMTABLE")
                    .column("DMATTRIBUTETYPE")
                    .column("DMTYPELINK")
                    .column("PARENTSET")
                    .column("SQLCOLUMN")
                    .column("DEFAULTVAL")
                    .column("DIMENSION")
                    .column("CLASSNAME")
                    .from("V_ADMINATTRIBUTE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "DMTYPE").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL Statement to get an attribute.
     */
    private static final String SQL_ATTR = new SQLSelect()
                    .column("DMTYPE")
                    .from("V_ADMINATTRIBUTE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * Name of the Cache by Name.
     */
    private static String NAMECACHE = Attribute.class.getName() + ".Name";

    /**
     * Name of the Cache by ID.
     */
    private static String IDCACHE = Attribute.class.getName() + ".ID";

    /**
     * This is the instance variable for the table, where attribute is stored.
     *
     * @see #getTable
     */
    private final SQLTable sqlTable;

    /**
     * Instance variable for the link to another type  id..
     *
     * @see #getLink
     * @see #setLink
     */
    private Long link = null;

    /**
     * Instance variable for the parent type id.
     *
     * @see #getParent
     * @see #setParent
     */
    private final Long parent;

    /**
     * This instance variable stores the sql column name.
     *
     * @see #getSqlColName
     * @see #setSqlColName
     */
    private final ArrayList<String> sqlColNames = new ArrayList<>();

    /**
     * The instance variable stores the attribute type for this attribute.
     *
     * @see #getAttributeType
     */
    private final AttributeType attributeType;

    /**
     * The String holds the default value as string for this Attribute.
     *
     * @see #getDefaultValue
     */
    private final String defaultValue;

    /**
     * Is the attribute required? This means at minimum one part of the
     * attribute is not allowed to be a null value.
     *
     * @see #isRequired
     */
    private final boolean required;

    /**
     * The parent this attribute belongs to.
     */
    private AttributeSet parentSet;

    /**
     * Size of the attribute (for string). Precision of the attribute (for
     * decimal).
     */
    private final int size;

    /**
     * Scale of the attribute (for decimal).
     */
    private final int scale;

    /**
     * UUID of the dimension belonging to this attribute.
     */
    private final String dimensionUUID;

    /**
     * Holds the Attributes this Attribute depend on. A TreeMap is used to have
     * a fixed position of each attribute. (Needed e.g for printquery)
     */
    private Map<String, Attribute> dependencies;

    /**
     * Key of this Attribute. Consist of name of the Parent Type and name of the Attribute itself
     */
    private String key;

    /**
     * ClassName of this Attribute. Used only in case of
     * {@link org.efaps.admin.datamodel.attributetype.EnumType}
     * and
     * {@link org.efaps.admin.datamodel.attributetype.BitEnumType}.
     */
    private String className;

    /**
     * This is the constructor for class {@link Attribute}. Every instance of
     * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
     * identifier (parameter <i>_id</i>).
     *
     * @param _id id of the attribute
     * @param _parentId id of the parent type
     * @param _name name of the instance
     * @param _sqlColNames name of the SQL columns
     * @param _sqlTable table of this attribute
     * @param _attributeType type of this attribute
     * @param _defaultValue default value for this attribute
     * @param _dimensionUUID UUID of the Dimension
     * @throws EFapsException on error while retrieving column information from
     *             database
     */
    //CHECKSTYLE:OFF
    protected Attribute(final long _id,
                        final long _parentId,
                        final String _name,
                        final String _sqlColNames,
                        final SQLTable _sqlTable,
                        final AttributeType _attributeType,
                        final String _defaultValue,
                        final String _dimensionUUID)
    //CHECKSTYLE:ON
        throws EFapsException
    {
        super(_id, null, _name);
        sqlTable = _sqlTable;
        parent = _parentId;
        attributeType = _attributeType;
        defaultValue = _defaultValue != null ? _defaultValue.trim() : null;
        dimensionUUID = _dimensionUUID != null ? _dimensionUUID.trim() : null;
        // add SQL columns and evaluate if attribute is required
        boolean req = false;
        int sizeTemp = 0;
        int scaleTemp = 0;
        final StringTokenizer tok = new StringTokenizer(_sqlColNames.trim(), ",");
        while (tok.hasMoreTokens()) {
            final String colName = tok.nextToken().trim();
            getSqlColNames().add(colName);
            final ColumnInformation columInfo = sqlTable.getTableInformation().getColInfo(colName);
            if (columInfo == null) {
                throw new EFapsException(Attribute.class, "Attribute", _id, _name, _sqlTable, colName);
            }
            req |= !columInfo.isNullable();
            sizeTemp = columInfo.getSize();
            scaleTemp = columInfo.getScale();
        }
        size = sizeTemp;
        scale = scaleTemp;
        required = req;
    }

    /**
     * This is the constructor for class {@link Attribute}. Every instance of
     * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
     * identifier (parameter <i>_id</i>).<br/>
     * This constructor is used for the copy method (clone of an attribute
     * instance).
     *
     * @see #copy
     * @param _id id of the attribute
     * @param _name name of the instance
     * @param _sqlTable table of this attribute
     * @param _attributeType typer of this attribute
     * @param _defaultValue default value for this attribute
     * @param _dimensionUUID uuid of the dimension belnging to this attribute
     * @param _required is it required
     * @param _size Size
     * @param _scale Scale
     */
    // CHECKSTYLE:OFF
    private Attribute(final long _id,
                      final long _parentId,
                      final String _name,
                      final SQLTable _sqlTable,
                      final AttributeType _attributeType,
                      final String _defaultValue,
                      final String _dimensionUUID,
                      final boolean _required,
                      final int _size,
                      final int _scale)
    {
        // CHECKSTYLE:ON
        super(_id, null, _name);
        parent = _parentId;
        sqlTable = _sqlTable;
        attributeType = _attributeType;
        defaultValue = _defaultValue != null ? _defaultValue.trim() : null;
        required = _required;
        size = _size;
        scale = _scale;
        dimensionUUID = _dimensionUUID;
    }

    /**
     * This method returns <i>true</i> if a link exists. This is made with a
     * test of the return value of method {@link #getLink} on null.
     *
     * @return <i>true</i> if this attribute has a link, otherwise <i>false</i>
     */
    public boolean hasLink()
    {
        return link != null;
    }

    /**
     * The method makes a clone of the current attribute instance.
     * @param _parentId if of the parent type
     * @return clone of current attribute instance
     */
    protected Attribute copy(final long _parentId)
    {
        final Attribute ret = new Attribute(getId(), _parentId, getName(), sqlTable, attributeType,
                        defaultValue, dimensionUUID, required, size, scale);
        ret.getSqlColNames().addAll(getSqlColNames());
        ret.setLink(link);
        ret.setClassName(getClassName());
        ret.getProperties().putAll(getProperties());
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEvent(final EventType _eventtype,
                         final EventDefinition _eventdef)
        throws CacheReloadException
    {
        super.addEvent(_eventtype, _eventdef);
        for (final Type child : getParent().getChildTypes()) {
            final Attribute childAttr = child.getAttribute(getName());
            if (childAttr != null) {
                childAttr.addEvent(_eventtype, _eventdef);
            }
        }
    }

    /**
     * This is the getter method for instance variable {@link #sqlTable}.
     *
     * @return value of instance variable {@link #sqlTable}
     * @see #sqlTable
     */
    public SQLTable getTable()
    {
        return sqlTable;
    }

    /**
     * This is the setter method for instance variable {@link #link}.
     *
     * @param _link new instance of class {@link Long} to set for link
     * @see #link
     * @see #getLink
     */
    protected void setLink(final Long _link)
    {
        link = _link;
    }

    /**
     * This is the getter method for instance variable {@link #link}.
     *
     * @return value of instance variable {@link #link}
     * @throws CacheReloadException on erorr
     */
    public Type getLink()
        throws CacheReloadException
    {
        if (link == null) {
            Attribute.LOG.error("Access on Attribute Link without parent defintion: {}", this);
        }
        return Type.get(link);
    }

    /**
     * Getter method for the instance variable {@link #dependencies}.
     *
     * @return value of instance variable {@link #dependencies}
     * @throws CacheReloadException on error
     */
    public Map<String, Attribute> getDependencies()
        throws CacheReloadException
    {
        if (dependencies == null) {
            dependencies = new TreeMap<>();
            // in case of a rate attribute the dependencies to the currencies
            // must be given
            if (getProperties().containsKey("CurrencyAttribute4Rate")) {
                dependencies.put("CurrencyAttribute4Rate",
                                getParent().getAttribute(getProperties().get("CurrencyAttribute4Rate")));
                dependencies.put("TargetCurrencyAttribute4Rate",
                                getParent().getAttribute(getProperties().get("TargetCurrencyAttribute4Rate")));
            }
        }
        return dependencies;
    }

    /**
     * This is the getter method for instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     * @throws CacheReloadException on error
     */
    public Type getParent()
        throws CacheReloadException
    {
        return Type.get(parent);
    }

    /**
     * @return the parent Type id
     */
    public Long getParentId()
    {
        return parent;
    }

    /**
     * This is the getter method for instance variable {@link #parentSet}.
     *
     * @return value of instance variable {@link #parentSet}
     *
     */
    public AttributeSet getParentSet()
    {
        return parentSet;
    }

    /**
     * This is the setter method for instance variable {@link #parentSet}.
     *
     * @param _parentSet new instance of class {@link AttributeSet} to set
     */
    private void setParentSet(final AttributeSet _parentSet)
    {
        parentSet = _parentSet;
    }

    /**
     * This is the getter method for instance variable {@link #sqlColNames}.
     *
     * @return value of instance variable {@link #sqlColNames}
     * @see #sqlColNames
     */
    public ArrayList<String> getSqlColNames()
    {
        return sqlColNames;
    }

    /**
     * This is the getter method for instance variable {@link #attributeType}.
     *
     * @return value of instance variable {@link #attributeType}
     * @see #attributeType
     */
    public AttributeType getAttributeType()
    {
        return attributeType;
    }

    /**
     * This is the getter method for instance variable {@link #defaultValue}.
     *
     * @return value of instance variable {@link #defaultValue}
     * @see #defaultValue
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * This is the getter method for instance variable {@link #required}.
     *
     * @return value of instance variable {@link #required}
     * @see #required
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * Getter method for instance variable {@link #size}.
     *
     * @return value of instance variable {@link #size}
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Getter method for instance variable {@link #scale}.
     *
     * @return value of instance variable {@link #scale}
     */
    public int getScale()
    {
        return scale;
    }

    /**
     * Method to get the dimension related to this attribute.
     *
     * @return Dimension
     */
    public Dimension getDimension()
    {
        Dimension ret = null;
        try {
            ret = Dimension.get(UUID.fromString(dimensionUUID));
        } catch (final CacheReloadException e) {
            Attribute.LOG.error("Catched CacheReloadException", e);
        }
        return ret;
    }

    /**
     * Has this attribute an UoM.
     *
     * @return true id dimensionUUId!=null, else false
     */
    public boolean hasUoM()
    {
        return dimensionUUID != null;
    }

    /**
     * Prepares for given <code>_values</code> depending on this attribute the
     * <code>_insert</code> into the database.
     *
     * @param _insert SQL insert statement for related {@link #sqlTable}
     * @param _values values to insert
     * @throws SQLException if values could not be inserted
     */
    public void prepareDBInsert(final SQLInsert _insert,
                                final Object... _values)
        throws SQLException
    {
        Object[] tmp = _values;
        try {
            final List<Return> returns = executeEvents(EventType.UPDATE_VALUE, ParameterValues.CLASS, this,
                            ParameterValues.OTHERS, _values);
            for (final Return aRet : returns) {
                if (aRet.contains(ReturnValues.VALUES)) {
                    tmp = (Object[]) aRet.get(ReturnValues.VALUES);
                }
            }
        } catch (final EFapsException e) {
            throw new SQLException(e);
        }
        attributeType.getDbAttrType().prepareInsert(_insert, this, tmp);
    }

    /**
     * Prepares for given <code>_values</code> depending on this attribute the
     * <code>_update</code> into the database.
     *
     * @param _update SQL update statement for related {@link #sqlTable}
     * @param _values values to update
     * @throws SQLException if values could not be inserted
     */
    public void prepareDBUpdate(final SQLUpdate _update,
                                final Object... _values)
        throws SQLException
    {
        Object[] tmp = _values;
        try {
            final List<Return> returns = executeEvents(EventType.UPDATE_VALUE, ParameterValues.CLASS, this,
                            ParameterValues.OTHERS, _values);
            for (final Return aRet : returns) {
                if (aRet.contains(ReturnValues.VALUES)) {
                    tmp = (Object[]) aRet.get(ReturnValues.VALUES);
                }
            }
        } catch (final EFapsException e) {
            throw new SQLException(e);
        }
        attributeType.getDbAttrType().prepareUpdate(_update, this, tmp);
    }

    /**
     *
     * @param _objectList object list from the database
     * @return found value
     * @throws EFapsException if values could not be read from the
     *             <code>_objectList</code>
     */
    public Object readDBValue(final List<Object> _objectList)
        throws EFapsException
    {
        Object ret;
        if (attributeType.getDbAttrType() instanceof DateTimeType) {
            ret = ((DateTimeType) attributeType.getDbAttrType()).readDateTimeValue(this, _objectList);
        } else {
            ret = attributeType.getDbAttrType().readValue(this, _objectList);
        }
        final List<Return> returns = executeEvents(EventType.READ_VALUE, ParameterValues.CLASS, this,
                        ParameterValues.OTHERS, ret);
        for (final Return aRet : returns) {
            if (aRet.contains(ReturnValues.VALUES)) {
                ret = aRet.get(ReturnValues.VALUES);
            }
        }
        return ret;
    }

    public Object value(final List<Object> _objectList)
        throws EFapsException
    {
        Object ret = attributeType.getDbAttrType().readValue(this, _objectList);
        final List<Return> returns = executeEvents(EventType.READ_VALUE, ParameterValues.CLASS, this,
                        ParameterValues.OTHERS, ret);
        for (final Return aRet : returns) {
            if (aRet.contains(ReturnValues.VALUES)) {
                ret = aRet.get(ReturnValues.VALUES);
            }
        }
        return ret;
    }


    /**
     * @return the key for the DBProperties value
     */
    public String getLabelKey()
    {
        return getKey() + ".Label";
    }

    /**
     * @return the key for the DBProperties value
     */
    public String getKey()
    {
        if (key == null) {
            try {
                key = getParent().getName() + "/" + getName();
            } catch (final CacheReloadException e) {
                Attribute.LOG.error("Problems during reading of key for Attribute: {}", this);
            }
        }
        return key;
    }

    /**
     * Getter method for the instance variable {@link #className}.
     *
     * @return value of instance variable {@link #className}
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Setter method for instance variable {@link #className}.
     *
     * @param _className value for instance variable {@link #className}
     */
    protected void setClassName(final String _className)
    {
        className = _className;
    }

    /**
     * Method to initialize this Cache.
     *
     * @param _class clas that called this method
     * @throws CacheReloadException on error
     */
    public static void initialize(final Class<?> _class)
        throws CacheReloadException
    {
        if (InfinispanCache.get().exists(Attribute.NAMECACHE)) {
            InfinispanCache.get().<String, Attribute>getCache(Attribute.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Attribute>getCache(Attribute.NAMECACHE)
                            .addListener(new CacheLogListener(Attribute.LOG));
        }

        if (InfinispanCache.get().exists(Attribute.IDCACHE)) {
            InfinispanCache.get().<Long, Attribute>getCache(Attribute.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Attribute>getCache(Attribute.IDCACHE)
                            .addListener(new CacheLogListener(Attribute.LOG));
        }
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        Attribute.initialize(Attribute.class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Attribute}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Attribute}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static Attribute get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Attribute> cache = InfinispanCache.get().<Long, Attribute>getCache(Attribute.IDCACHE);
        if (!cache.containsKey(_id)) {
            Type.get(Attribute.getTypeID(_id));
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Attribute}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Attribute}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORE")
    public static Attribute get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, Attribute> cache = InfinispanCache.get().<String, Attribute>getCache(Attribute.NAMECACHE);
        if (!cache.containsKey(_name)) {
            final String[] nameParts = _name.split("/");
            if (nameParts != null && nameParts.length == 2) {
                Type.get(nameParts[0]);
            }
        }
        return cache.get(_name);
    }

    /**
     * @param _attr Attribute to be cached
     * @param _type Parent Type
     */
    private static void cacheAttribute(final Attribute _attr,
                                       final Type _type)
    {
        final Cache<String, Attribute> nameCache = InfinispanCache.get().<String, Attribute>getIgnReCache(
                        Attribute.NAMECACHE);
        if (_type != null) {
            nameCache.putIfAbsent(_type.getName() + "/" + _attr.getName(), _attr);
        } else {
            nameCache.putIfAbsent(_attr.getKey(), _attr);
        }

        final Cache<Long, Attribute> idCache = InfinispanCache.get().<Long, Attribute>getIgnReCache(
                        Attribute.IDCACHE);
        idCache.putIfAbsent(_attr.getId(), _attr);
    }

    /**
     * The instance method returns the string representation of this attribute.
     * The string representation of this attribute is the name of the type plus
     * slash plus name of this attribute. (Must not contain getKey()!!)
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).appendSuper(super.toString())
                        .append("attributetype", getAttributeType().toString())
                        .append("required", required).toString();
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof Attribute) {
            ret = ((Attribute) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return  Long.valueOf(getId()).intValue();
    }

    /**
     * @param _attrId   id of an attribute
     * @return the id of a Type
     * @throws CacheReloadException on error
     */
    protected static long getTypeID(final long _attrId)
        throws CacheReloadException
    {
        long ret = 0;
        Connection con = null;
        try {
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(Attribute.SQL_ATTR);
                stmt.setObject(1, _attrId);
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ret = rs.getLong(1);
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            throw new CacheReloadException("Cannot read a type for an attribute.", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("Cannot read a type for an attribute.", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
        return ret;
    }

    /**
     * @param _type Type the attributes are wanted for
     * @throws EFapsException on error
     */
    protected static void add4Type(final Type _type)
        throws EFapsException
    {
        Connection con = null;
        try {
            Context.getThreadContext();
            con = Context.getConnection();
            PreparedStatement stmt = null;
            final List<Object[]> values = new ArrayList<>();
            try {
                stmt = con.prepareStatement(Attribute.SQL_TYPE);
                stmt.setObject(1, _type.getId());
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    values.add(new Object[] {
                                    rs.getLong(1),
                                    rs.getString(2).trim(),
                                    rs.getLong(3),
                                    rs.getLong(4),
                                    rs.getLong(5),
                                    rs.getLong(6),
                                    rs.getLong(7),
                                    rs.getString(8),
                                    rs.getString(9),
                                    rs.getString(10),
                                    rs.getString(11)
                    });
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            con.close();

            final Map<Long, AttributeSet> id2Set = new HashMap<>();
            final Map<Attribute, Long> attribute2setId = new HashMap<>();
            final List<Attribute> attributes = new ArrayList<>();
            for (final Object[] row : values) {
                final long id = (Long) row[0];
                final String name = (String) row[1];
                final long typeAttrId = (Long) row[2];
                final long tableId = (Long) row[3];
                final long attrTypeId = (Long) row[4];
                final long typeLinkId = (Long) row[5];
                final long parentSetId = (Long) row[6];
                final String sqlCol = (String) row[7];
                final String defaultval = (String) row[8];
                final String dimensionUUID = (String) row[9];
                final String className = (String) row[10];

                Attribute.LOG.debug("read attribute '{}/{}' (id = {})", _type.getName(), name, id);

                if (Type.check4Type(typeAttrId, CIAdminDataModel.AttributeSet.uuid)) {
                    final AttributeSet set = new AttributeSet(id, _type, name, AttributeType.get(attrTypeId),
                                    sqlCol, tableId, typeLinkId, dimensionUUID);
                    id2Set.put(id, set);
                } else {
                    final Attribute attr = new Attribute(id, _type.getId(), name, sqlCol, SQLTable.get(tableId),
                                    AttributeType.get(attrTypeId), defaultval,
                                    dimensionUUID);

                    final UUID uuid = attr.getAttributeType().getUUID();
                    if (uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_LINK.getUuid())
                                    || uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_LINK_WITH_RANGES.getUuid())
                                    || uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_STATUS.getUuid())) {
                        attr.setLink(typeLinkId);
                        // in case of a PersonLink, CreatorLink or ModifierLink a link to Admin_User_Person
                        // must be set
                    } else if (uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_CREATOR_LINK.getUuid())
                                    || uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_MODIFIER_LINK.getUuid())
                                    || uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_PERSON_LINK.getUuid())) {
                        attr.setLink(Type.getId4UUID(CIAdminUser.Person.uuid));
                        // in case of a GroupLink, a link to Admin_User_Group must be set
                    } else if (uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_GROUP_LINK.getUuid())) {
                        attr.setLink(Type.getId4UUID(CIAdminUser.Group.uuid));
                        // in case of a Enum and BitEnum the className must be set
                    } else if (uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_ENUM.getUuid())
                                    || uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_BITENUM.getUuid())
                                    || uuid.equals(Attribute.AttributeTypeDef.ATTRTYPE_JAXB.getUuid())) {
                        if (className == null || className != null && className.isEmpty()) {
                            Attribute.LOG.error("An Attribute of Type Enum, BitEnum, Jaxb must have a className: {}",
                                            attr);
                        }
                        attr.setClassName(className.trim());
                    }
                    attr.readFromDB4Properties();

                    if (Type.check4Type(typeAttrId, CIAdminDataModel.AttributeSetAttribute.uuid)) {
                        attribute2setId.put(attr, parentSetId);
                    } else {
                        attributes.add(attr);
                        Attribute.cacheAttribute(attr, _type);
                    }
                }
            }
            // make connection between set and attributes
            for (final Entry<Attribute, Long> entry : attribute2setId.entrySet()) {
                final AttributeSet parentset = id2Set.get(entry.getValue());
                final Attribute childAttr = entry.getKey();
                parentset.addAttributes(false, childAttr);
                childAttr.setParentSet(parentset);
                // needed due to cluster serialization that does not update automatically
                Attribute.cacheAttribute(childAttr, parentset);
            }
            for (final AttributeSet set : id2Set.values()) {
                Type.cacheType(set);
            }

            _type.addAttributes(false, attributes.toArray(new Attribute[attributes.size()]));
        } catch (final SQLException e) {
            throw new CacheReloadException("Cannot read attributes.", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read attributes.", e);
            }
        }
    }
}
