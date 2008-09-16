/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.admin.datamodel;

import static org.efaps.admin.EFapsClassNames.ATTRTYPE_CREATOR_LINK;
import static org.efaps.admin.EFapsClassNames.ATTRTYPE_LINK;
import static org.efaps.admin.EFapsClassNames.ATTRTYPE_LINK_WITH_RANGES;
import static org.efaps.admin.EFapsClassNames.ATTRTYPE_MODIFIER_LINK;
import static org.efaps.admin.EFapsClassNames.DATAMODEL_ATTRIBUTESET;
import static org.efaps.admin.EFapsClassNames.DATAMODEL_ATTRIBUTESETATTRIBUTE;
import static org.efaps.admin.EFapsClassNames.USER_PERSON;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.CacheReloadInterface;

/**
 * This is the class for the type description. The type description holds
 * information about creation of a new instance of a type with default values.
 *
 * @author tmo
 * @version $Id$
 */
public class Attribute extends AbstractDataModelObject {

  /**
   * Logging instance used in this class.
   */
  private final static Logger log = LoggerFactory.getLogger(Attribute.class);

  /**
   * Stores all instances of attribute.
   *
   * @see #get
   */
  private static AttributeCache attributeCache = new AttributeCache();

  /**
   * This is the sql select statement to select all types from the database.
   */
  private final static String SQL_SELECT
      = "select ID,"
             + "NAME,"
             + "TYPEID,"
             + "DMTABLE,"
             + "DMTYPE,"
             + "DMATTRIBUTETYPE,"
             + "DMTYPELINK,"
             + "PARENTSET,"
             + "SQLCOLUMN,"
             + "DEFAULTVAL "
       + "from V_ADMINATTRIBUTE";

  /**
   * This is the instance variable for the table, where attribute is stored.
   *
   * @see #getTable
   */
  private final SQLTable sqlTable;

  /**
   * Instance variable for the link to another type.
   *
   * @see #getLink
   * @see #setLink
   */
  private Type link = null;

  /**
   * Instance variable for the parent type.
   *
   * @see #getParent
   * @see #setParent
   */
  private Type parent = null;

  /**
   * This instance variable stores the sql column name.
   *
   * @see #getSqlColName
   * @see #setSqlColName
   */
  private final ArrayList<String> sqlColNames = new ArrayList<String>();

  /**
   * The instance variable stores the attribute type for this attribute.
   *
   * @see #getAttributeType
   */
  private final AttributeType attributeType;

  /**
   * The collection intance variables holds all unique keys, for which this
   * attribute belongs to.
   *
   * @see #getUniqueKeys
   * @see #setUniqueKeys
   */
  private Collection<UniqueKey> uniqueKeys = null;

  /**
   * The String holds the default value as string for this Attribute
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

  private AttributeSet parentSet;

  /**
   * This is the constructor for class {@link Attribute}. Every instance of
   * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
   * identifier (parameter <i>_id</i>).
   *
   * @param _id           id of the attribute
   * @param _name         name of the instance
   * @param _sqlColNames  name of the SQL columns
   */
  protected Attribute(final long _id,
                      final String _name,
                      final String _sqlColNames,
                      final SQLTable _sqlTable,
                      final AttributeType _attributeType,
                      final String _defaultValue)
  {
    super(_id, null, _name);
    this.sqlTable = _sqlTable;
    this.attributeType = _attributeType;
    this.defaultValue = (_defaultValue != null)
                        ? _defaultValue.trim()
                        : null;
    // add SQL columns and evaluate if attribute is required
    boolean required = false;
    final StringTokenizer tokens = new StringTokenizer(_sqlColNames.trim(), ",");
    while (tokens.hasMoreTokens()) {
      final String colName = tokens.nextToken().trim();
      getSqlColNames().add(colName);
      required |= !this.sqlTable.getTableInformation().getColInfo(colName).isNullable();
    }
    this.required = required;
  }

  /**
   * This is the constructor for class {@link Attribute}. Every instance of
   * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
   * identifier (parameter <i>_id</i>).<br/> This constructor is used for the
   * copy method (clone of an attribute instance).
   *
   * @param _id     id of the attribute
   * @param _name   name of the instance
   * @see #copy
   */
  private Attribute(final long _id,
                    final String _name,
                    final SQLTable _sqlTable,
                    final AttributeType _attributeType,
                    final String _defaultValue,
                    final boolean _required)
  {
    super(_id, null, _name);
    this.sqlTable = _sqlTable;
    this.attributeType = _attributeType;
    this.defaultValue = (_defaultValue != null)
                        ? _defaultValue.trim()
                        : null;
    this.required = _required;
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This method returns <i>true</i> if a link exists. This is made with a test
   * of the return value of method {@link #getLink} on null.
   *
   * @return <i>true</i> if this attribute has a link, otherwise <i>false</i>
   */
  public boolean hasLink() {
    boolean ret = false;
    if (getLink() != null) {
      ret = true;
    }
    return ret;
  }


  /**
   * A unique key can added to this attribute instance. If no unique key is
   * added before, the instance variable {@link #uniqueKeys} is initialised.
   *
   * @param _uniqueKey
   *          unique key to add to this attribute
   * @see #uniqueKeys
   */
  public void addUniqueKey(final UniqueKey _uniqueKey) {
    if (getUniqueKeys() == null) {
      setUniqueKeys(new HashSet<UniqueKey>());
    }
    getUniqueKeys().add(_uniqueKey);
  }

  /**
   * Creates a new instance of this attribute from type {@link #attributeType}.
   *
   * @return new created instance of this attribute
   */
  public AttributeTypeInterface newInstance() throws EFapsException {
    final AttributeTypeInterface ret = getAttributeType().newInstance();
    ret.setAttribute(this);
    return ret;
  }

  /**
   * The method makes a clone of the current attribute instance.
   *
   * @return clone of current attribute instance
   */
  public Attribute copy() {
    final Attribute ret = new Attribute(getId(),
                                  getName(),
                                  this.sqlTable,
                                  this.attributeType,
                                  this.defaultValue,
                                  this.required);
    ret.getSqlColNames().addAll(getSqlColNames());
    ret.setLink(getLink());
    ret.setUniqueKeys(getUniqueKeys());
    ret.getProperties().putAll(getProperties());
    return ret;
  }

  /**
   * This is the getter method for instance variable {@link #sqlTable}.
   *
   * @return value of instance variable {@link #sqlTable}
   * @see #sqlTable
   */
  public SQLTable getTable() {
    return this.sqlTable;
  }

  /**
   * This is the setter method for instance variable {@link #link}.
   *
   * @param _link
   *          new instance of class {@link Type} to set for link
   * @see #link
   * @see #getLink
   */
  protected void setLink(final Type _link) {
    this.link = _link;
  }

  /**
   * This is the getter method for instance variable {@link #link}.
   *
   * @return value of instance variable {@link #link}
   * @see #link
   * @see #setLink
   */
  public Type getLink() {
    return this.link;
  }

  /**
   * This is the setter method for instance variable {@link #parent}.
   *
   * @param _parent
   *          new instance of class {@link Type} to set for parent
   * @see #parent
   * @see #getParent
   */
  void setParent(final Type _parent) {
    this.parent = _parent;
  }

  /**
   * This is the getter method for instance variable {@link #parent}.
   *
   * @return value of instance variable {@link #parent}
   * @see #parent
   * @see #setParent
   */
  public Type getParent() {
    return this.parent;
  }

  public AttributeSet getParentSet() {
    return this.parentSet;
  }

  private void setParentSet(final AttributeSet _parentSet) {
    this.parentSet = _parentSet;
  }

  /**
   * This is the getter method for instance variable {@link #sqlColNames}.
   *
   * @return value of instance variable {@link #sqlColNames}
   * @see #sqlColNames
   */
  public ArrayList<String> getSqlColNames() {
    return this.sqlColNames;
  }

  /**
   * This is the getter method for instance variable {@link #attributeType}.
   *
   * @return value of instance variable {@link #attributeType}
   * @see #attributeType
   */
  public AttributeType getAttributeType() {
    return this.attributeType;
  }

  /**
   * This is the getter method for instance variable {@link #uniqueKeys}.
   *
   * @return value of instance variable {@link #uniqueKeys}
   * @see #uniqueKeys
   */
  public Collection<UniqueKey> getUniqueKeys() {
    return this.uniqueKeys;
  }

  /**
   * This is the setter method for instance variable {@link #uniqueKeys}.
   *
   * @param _uniqueKeys
   *          new value for instance variable {@link #uniqueKeys}
   * @see #uniqueKeys
   * @see #getUniqueKeys
   */
  private void setUniqueKeys(final Collection<UniqueKey> _uniqueKeys) {
    this.uniqueKeys = _uniqueKeys;
  }

  /**
   * This is the getter method for instance variable {@link #defaultValue}.
   *
   * @return value of instance variable {@link #defaultValue}
   * @see #defaultValue
   */
  public String getDefaultValue() {
    return this.defaultValue;
  }


  /**
   * This is the getter method for instance variable {@link #required}.
   *
   * @return value of instance variable {@link #required}
   * @see #required
   */
  public boolean isRequired()
  {
    return this.required;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Initialise the cache of types.
   */
  protected static void initialise() throws CacheReloadException {
    ConnectionResource con = null;
    try {
      con = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;
      try {
        stmt = con.getConnection().createStatement();
        final Map<Long,AttributeSet> id2Set = new HashMap<Long, AttributeSet>();
        final Map<Attribute,Long> attribute2setId = new HashMap<Attribute,Long>();

        final ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next()) {
          final long id = rs.getLong(1);
          final String name = rs.getString(2);
          final long typeAttrId = rs.getLong(3);
          final long tableId = rs.getLong(4);
          final long typeId = rs.getLong(5);
          final long attrTypeId = rs.getLong(6);
          final long typeLinkId = rs.getLong(7);
          final long parentSetId = rs.getLong(8);
          final String sqlCol = rs.getString(9);
          final String defaultval = rs.getString(10);
          final Type type = Type.get(typeId);

          log.debug("read attribute '" + type.getName() + "/" + name + "' "
              + "(id = " + id + ")");

          final Type typeAttr = Type.get(typeAttrId);

          if (typeAttr.getUUID().equals(DATAMODEL_ATTRIBUTESET.uuid)) {
            final AttributeSet set = new AttributeSet(id,
                                                      type,
                                                      name,
                                                      AttributeType.get(attrTypeId),
                                                      sqlCol,
                                                      tableId,
                                                      typeLinkId);
            id2Set.put(id, set);
          } else if (typeAttr.getUUID().equals(DATAMODEL_ATTRIBUTESETATTRIBUTE.uuid)) {
            final AttributeSet parentset =  (AttributeSet) Type.get(parentSetId);
            final Attribute attr = new Attribute(id, name, sqlCol, SQLTable
                .get(tableId), AttributeType.get(attrTypeId), defaultval);
            parentset.addAttribute(attr);
            getCache().add(attr);
            attr.readFromDB4Properties();
            attr.setParentSet(parentset);
          } else {
            final Attribute attr = new Attribute(id, name, sqlCol, SQLTable
                .get(tableId), AttributeType.get(attrTypeId), defaultval);
            attr.setParent(type);
            final UUID uuid = attr.getAttributeType().getUUID();
            if (uuid.equals(ATTRTYPE_LINK.uuid)
                || uuid.equals(ATTRTYPE_LINK_WITH_RANGES.uuid)) {
              final Type linkType = Type.get(typeLinkId);
              attr.setLink(linkType);
              linkType.addLink(attr);
            } else if (uuid.equals(ATTRTYPE_CREATOR_LINK.uuid)) {
              final Type linkType = Type.get(USER_PERSON);
              attr.setLink(linkType);
              linkType.addLink(attr);
            } else if (uuid.equals(ATTRTYPE_MODIFIER_LINK.uuid)) {
              final Type linkType = Type.get(USER_PERSON);
              attr.setLink(linkType);
              linkType.addLink(attr);
            }
            type.addAttribute(attr);

            getCache().add(attr);

            attr.readFromDB4Properties();
          }

        }
        rs.close();

        for (final Entry<Attribute,Long> entry : attribute2setId.entrySet()){
          final AttributeSet parentset = id2Set.get(entry.getValue());
          final Attribute childAttr = entry.getKey();
          parentset.addAttribute(childAttr);
        }
      }
      finally {
        if (stmt != null) {
          stmt.close();
        }
      }
      con.commit();



    } catch (final SQLException e) {
      throw new CacheReloadException("could not read attributes", e);
    } catch (final EFapsException e) {
      throw new CacheReloadException("could not read attributes", e);
    }
    finally {
      if ((con != null) && con.isOpened()) {
        try {
          con.abort();
        } catch (final EFapsException e) {
          throw new CacheReloadException("could not read attributes", e);
        }
      }
    }
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link Attribute}.
   *
   * @param _id
   *          id to search in the cache
   * @return instance of class {@link Attribute}
   * @see #getCache
   */
  static public Attribute get(final long _id) {
    return getCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Attribute}.
   *
   * @param _name
   *          name to search in the cache
   * @return instance of class {@link Attribute}
   * @see #getCache
   */
  static public Attribute get(final String _name) {
    return getCache().get(_name);
  }

  /**
   * Static getter method for the attribute hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static AttributeCache getCache() {
    return attributeCache;
  }

  /**
   * The instance method returns the string representation of this attribute.
   * The string representation of this attribute is the name of the type plus
   * slash plus name of this attribute.
   *
   * @see #name
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("attribute name", getParent().getName() + "/" + getName())
            .append("attributetype", getAttributeType().toString())
            .append("required", this.required)
            .toString();
  }

  static protected class AttributeCache extends Cache<Attribute> {

    private AttributeCache() {
      super(new CacheReloadInterface() {
        public int priority() {
          return CacheReloadInterface.Priority.Attribute.number;
        };

        public void reloadCache() throws CacheReloadException {
          Attribute.initialise();
        };
      });
    }

    /*
     * private Attribute readAttribute(Context _context, String _name) throws
     * Exception { int index = _name.indexOf("/"); String typeName =
     * _name.substring(0, index); String name = _name.substring(index+1);
     * System.out.println("typeName="+typeName+":name="+name); Type type =
     * Type.get(typeName); if (type==null) { throw new Exception("can not found
     * attribute '"+_name+"'"); } return readAttribute4Statement(_context,
     * "select "+ "ABSTRACT.ID,"+ "ABSTRACT.NAME,"+ "DMATTRIBUTE.DMTABLE,"+
     * "DMATTRIBUTE.DMTYPE,"+ "DMATTRIBUTE.DMATTRIBUTETYPE,"+
     * "DMATTRIBUTE.DMTYPELINK,"+ "DMATTRIBUTE.SQLCOLUMN "+ "from
     * ABSTRACT,DMATTRIBUTE "+ "where ABSTRACT.NAME='"+name+"' and
     * ABSTRACT.ID=DMATTRIBUTE.ID and DMATTRIBUTE.DMTYPE="+type.getId() ); }
     * private Attribute readAttribute(Context _context, long _id) throws
     * Exception { return readAttribute4Statement(_context, "select "+
     * "ABSTRACT.ID,"+ "ABSTRACT.NAME,"+ "DMATTRIBUTE.DMTABLE,"+
     * "DMATTRIBUTE.DMTYPE,"+ "DMATTRIBUTE.DMATTRIBUTETYPE,"+
     * "DMATTRIBUTE.DMTYPELINK,"+ "DMATTRIBUTE.SQLCOLUMN "+ "from
     * ABSTRACT,DMATTRIBUTE "+ "where ABSTRACT.ID="+_id+" and
     * ABSTRACT.ID=DMATTRIBUTE.ID" ); } private Attribute
     * readAttribute4Statement(Context _context, String _statement) throws
     * Exception { Attribute attr = null; Statement stmt =
     * _context.getConnection().createStatement(); try { ResultSet rs =
     * stmt.executeQuery(_statement); while (rs.next()) { long id =
     * rs.getLong(1); String name = rs.getString(2); long tableId =
     * rs.getLong(3); long typeId = rs.getLong(4); long attrType =
     * rs.getLong(5); long typeLinkId = rs.getLong(6); String sqlCol =
     * rs.getString(7); Type type = Type.get(typeId); attr = new Attribute(id,
     * name, sqlCol); attr.setTable(Table.get(_context, tableId));
     * attr.setAttributeType(AttributeType.get(attrType));
     * type.addAttribute(attr); this.add(attr); if (attrType==400 ||
     * attrType==401) { Type linkType = Type.get(typeLinkId);
     * attr.setLink(linkType); linkType.addLink(attr); } else if (attrType==411) {
     * Type linkType = Type.get("Admin_User_Person"); attr.setLink(linkType);
     * linkType.addLink(attr); } else if (attrType==412) { Type linkType =
     * Type.get("Admin_User_Person"); attr.setLink(linkType);
     * linkType.addLink(attr); } else if (attrType==421) { Type linkType =
     * Type.get("Admin_LifeCycle_Status"); attr.setLink(linkType);
     * linkType.addLink(attr); } attr.readFromDB4Properties(_context); }
     * rs.close(); } catch (Exception e) { e.printStackTrace(); } finally {
     * stmt.close(); } return attr; }
     */

    /**
     * Add a new object implements the {@link CacheInterface} to the hashtable.
     * This is used from method {@link #get(long)} and {@link #get(String) to
     * return the cache object for an id or a string out of the cache.
     *
     * @param _cacheObj
     *          cache object to add
     * @see #get
     */
    // protected void add(CacheInterface _cacheObj) {
    @Override
    public void add(final Attribute _attr) {
      getCache4Id().put(new Long(_attr.getId()), _attr);
      getCache4Name().put(_attr.getParent().getName() + "/" + _attr.getName(),
          _attr);
    }
  }

}
