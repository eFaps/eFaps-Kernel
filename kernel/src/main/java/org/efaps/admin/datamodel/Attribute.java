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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.CacheReloadInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private final static String SQL_SELECT =
      "select " + "ID," + "NAME," + "DMTABLE," + "DMTYPE," + "DMATTRIBUTETYPE,"
          + "DMTYPELINK," + "SQLCOLUMN, " + "DEFAULTVAL "
          + "from V_ADMINATTRIBUTE";

  /**
   * This is the instance variable for the table, where attribute is stored.
   *
   * @see #getTable
   * @see #setTable
   */
  private SQLTable table = null;

  /**
   * Instance variable for the link to onther type.
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
   * @see #setAttributeType
   */
  private AttributeType attributeType = null;

  /**
   * The collection intance variables holds all unique keys, for which this
   * attribute belongs to.
   *
   * @see #getUniqueKeys
   * @see #setUniqueKeys
   */
  private Collection<UniqueKey> uniqueKeys = null;

  /**
   * The String holds the Defaultvalue for this Attribute
   *
   * @see #getDefaultValue()
   * @see #setDefaultValue(String)
   */
  private String defaultValue = null;

  /**
   * This is the constructor for class {@link Attribute}. Every instance of
   * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
   * identifier (parameter <i>_id</i>).
   *
   * @param _id
   *          id of the attribute
   * @param _name
   *          name of the instance
   * @param _sqlColNames
   *          name of the sql columns
   */
  protected Attribute(long _id, String _name, String _sqlColNames) {
    super(_id, null, _name);
    StringTokenizer tokens = new StringTokenizer(_sqlColNames, ",");
    while (tokens.hasMoreTokens()) {
      getSqlColNames().add(tokens.nextToken());
    }
  }

  /**
   * This is the constructor for class {@link Attribute}. Every instance of
   * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
   * identifier (parameter <i>_id</i>).<br/> This constructor is used for the
   * copy method (clone of an attribute instance).
   *
   * @param _id
   *          id of the attribute
   * @param _name
   *          name of the instance
   * @see #copy
   */
  protected Attribute(long _id, String _name) {
    super(_id, null, _name);
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
  public void addUniqueKey(UniqueKey _uniqueKey) {
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
    AttributeTypeInterface ret = getAttributeType().newInstance();
    ret.setAttribute(this);
    return ret;
  }

  /**
   * The method makes a clone of the current attribute instance.
   *
   * @return clone of current attribute instance
   */
  public Attribute copy() {
    Attribute ret = new Attribute(getId(), getName());
    ret.getSqlColNames().addAll(getSqlColNames());
    ret.setTable(getTable());
    ret.setLink(getLink());
    ret.setAttributeType(getAttributeType());
    ret.setUniqueKeys(getUniqueKeys());
    ret.getProperties().putAll(getProperties());
    ret.setDefaultValue(this.getDefaultValue());
    return ret;
  }

  /**
   * This is the setter method for instance variable {@table #table}.
   *
   * @param _table
   *          new instance of class {@table Table} to set for table
   * @see #table
   * @see #getTable
   */
  private void setTable(SQLTable _table) {
    this.table = _table;
  }

  /**
   * This is the getter method for instance variable {@table #table}.
   *
   * @return value of instance variable {@table #table}
   * @see #table
   * @see #setTable
   */
  public SQLTable getTable() {
    return this.table;
  }

  /**
   * This is the setter method for instance variable {@link #link}.
   *
   * @param _link
   *          new instance of class {@link Type} to set for link
   * @see #link
   * @see #getLink
   */
  private void setLink(Type _link) {
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
  void setParent(Type _parent) {
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
   * This is the setter method for instance variable {@link #attributeType}.
   *
   * @param _attributeType
   *          new value for instance variable {@link #attributeType}
   * @see #attributeType
   * @see #getAttributeType
   */
  protected void setAttributeType(AttributeType _attributeType) {
    this.attributeType = _attributeType;
  }

  /**
   * This is the getter method for instance variable {@link #attributeType}.
   *
   * @return value of instance variable {@link #attributeType}
   * @see #attributeType
   * @see #setAttributeType
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
  private void setUniqueKeys(Collection<UniqueKey> _uniqueKeys) {
    this.uniqueKeys = _uniqueKeys;
  }

  /**
   * This is the getter method for instance variable {@link #defaultValue}.
   *
   * @return
   */
  public String getDefaultValue() {
    return this.defaultValue;
  }

  /**
   * This is the setter method for instance variable {@link #defaultValue}.
   *
   * @param _defaultvalue
   */
  private void setDefaultValue(String _defaultvalue) {
    this.defaultValue = _defaultvalue;
  }

  // ///////////////////////////////////////////////////////////////////////////

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

        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next()) {
          long id = rs.getLong(1);
          String name = rs.getString(2).trim();
          long tableId = rs.getLong(3);
          long typeId = rs.getLong(4);
          long attrType = rs.getLong(5);
          long typeLinkId = rs.getLong(6);
          String sqlCol = rs.getString(7).trim();
          String defaultval = rs.getString(8);
          Type type = Type.get(typeId);

          log.debug("read attribute '" + type.getName() + "/" + name + "' "
              + "(id = " + id + ")");

          Attribute attr = new Attribute(id, name, sqlCol);
          attr.setTable(SQLTable.get(tableId));
          attr.setAttributeType(AttributeType.get(attrType));
          attr.setParent(type);
          if (defaultval != null) {
            attr.setDefaultValue(defaultval.trim());
          }
          UUID uuid = attr.getAttributeType().getUUID();
          if (uuid.equals(EFapsClassName.ATTRTYPE_LINK.uuid)
              || uuid.equals(EFapsClassName.ATTRTYPE_LINK_WITH_RANGES.uuid)) {
            Type linkType = Type.get(typeLinkId);
            attr.setLink(linkType);
            linkType.addLink(attr);
          } else if (uuid.equals(EFapsClassName.ATTRTYPE_CREATOR_LINK.uuid)) {
            Type linkType = Type.get("Admin_User_Person");
            attr.setLink(linkType);
            linkType.addLink(attr);
          } else if (uuid.equals(EFapsClassName.ATTRTYPE_MODIFIER_LINK.uuid)) {
            Type linkType = Type.get("Admin_User_Person");
            attr.setLink(linkType);
            linkType.addLink(attr);
          }
          /*
           * if ((attrType == 400) || (attrType == 401)) { Type linkType =
           * Type.get(typeLinkId); attr.setLink(linkType);
           * linkType.addLink(attr); } else if (attrType == 411) { Type linkType =
           * Type.get("Admin_User_Person"); attr.setLink(linkType);
           * linkType.addLink(attr); } else if (attrType == 412) { Type linkType =
           * Type.get("Admin_User_Person"); attr.setLink(linkType);
           * linkType.addLink(attr); } else if (attrType == 421) { Type linkType =
           * Type.get("Admin_LifeCycle_Status"); attr.setLink(linkType);
           * linkType.addLink(attr); }
           */
          type.addAttribute(attr);

          getCache().add(attr);

          attr.readFromDB4Properties();
        }
        rs.close();
      }
      finally {
        if (stmt != null) {
          stmt.close();
        }
      }
      con.commit();
    } catch (SQLException e) {
      throw new CacheReloadException("could not read roles", e);
    } catch (EFapsException e) {
      throw new CacheReloadException("could not read roles", e);
    }
    finally {
      if ((con != null) && con.isOpened()) {
        try {
          con.abort();
        } catch (EFapsException e) {
          throw new CacheReloadException("could not read roles", e);
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
  static public Attribute get(long _id) {
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
  static public Attribute get(String _name) {
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
    return new ToStringBuilder(this).append("attribute name",
        getParent().getName() + "/" + getName()).appendSuper(super.toString())
        .append("attributetype", getAttributeType().toString()).toString();
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
    public void add(Attribute _attr) {
      getCache4Id().put(new Long(_attr.getId()), _attr);
      getCache4Name().put(_attr.getParent().getName() + "/" + _attr.getName(),
          _attr);
    }
  }

}
