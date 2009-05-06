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

package org.efaps.admin.datamodel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.Form;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;

/**
 * This is the class for the type description. The type description holds
 * information about creation of a new instance of a type with default values.
 *
 * @author tmo
 * @version $Id$
 */
public class Type extends AbstractDataModelObject {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Type.class);

  /**
   * This is the sql select statement to select all types from the database.
   *
   * @see #initialise
   */
  private static final String SQL_SELECT =
      "   select "
          + "  ID,"
          + "  UUID,"
          + "  NAME,"
          + "  ABSTRACT,"
          + "  PARENTDMTYPE,"
          + "  SQLCACHEEXPR "
          + "from V_ADMINTYPE";

  /**
   * Stores all instances of type.
   *
   * @see #get
   */
  private static TypeCache CACHE = new TypeCache();

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Instance variable for the parent type from which this type is derived.
   *
   * @see #getParentType
   * @see #setParentType
   */
  private Type parentType = null;

  /**
   * Instance variable for the id of parent type from which this type is
   * derived. Also the parent ids from the parent is stored.
   *
   * @see #getParentTypeId
   * @see #setParentTypeId
   */
  // private String parentTypeIds = "";
  /**
   * Instance variable for all child types derived from this type.
   *
   * @see #getChildTypes
   */
  private final Set<Type> childTypes = new HashSet<Type>();

  /**
   * The instance variables stores all attributes for this type object.
   *
   * @see #getAttributes()
   * @see #add(Attribute)
   * @see #getAttribute
   * @see #getAttributes(Class)
   */
  private final Map<String, Attribute> attributes =
      new HashMap<String, Attribute>();

  /**
   * Instance of a HashSet to store all needed tables for this type. The tables
   * are automatically added via the method {@link #add(Attribute)}.
   *
   * @see #add(Attribute)
   * @see #getTables
   */
  private final Set<SQLTable> tables = new HashSet<SQLTable>();

  /**
   * The instance variable stores the main table, which must be inserted first.
   * In the main table stands also the select statement to get a new id. The
   * value is automatically set with method {@link #add(Attribute)}.
   *
   * @see Table.mainTable
   * @see #add(Attribute)
   * @see #getMainTable
   * @see #setMainTable
   */
  private SQLTable mainTable = null;

  /**
   * This instance variable stores the form for the viewing mode.
   *
   * @see #getFormView
   * @see #setFormView
   */
  private final Form formView = null;

  /**
   * This instance variable stores the form for the editing mode.
   *
   * @see #getFormEdit
   * @see #setFormEdit
   */
  private final Form formEdit = null;

  /**
   * This instance variable stores the form for the creating mode.
   *
   * @see #getFormCreate
   * @see #setFormCreate
   */
  private final Form formCreate = null;

  /**
   * The instance variable stores all unique keys of this type instance.
   *
   * @see #getUniqueKeys
   * @see #setUniqueKeys
   */
  private Collection<UniqueKey> uniqueKeys = null;

  /**
   * All attributes which are used as links are stored in this map.
   *
   * @see #getLinks
   */
  private final Map<String, Attribute> links = new HashMap<String, Attribute>();

  /**
   * All access sets which are assigned to this type are store in this instance
   * variable.
   *
   * @see #addAccessSet
   * @see #getAccessSets
   */
  private final Set<AccessSet> accessSets = new HashSet<AccessSet>();

  /**
   * Stores all type of events which are allowed to fire on this type.
   *
   * @see #setLinkProperty
   */
  private final Set<Type> allowedEventTypes = new HashSet<Type>();

  /**
   * Id of the store for this type.
   */
  private long storeId;

  /**
   * This is the constructor for class Type. Every instance of class Type must
   * have a name (parameter <i>_name</i>).
   *
   * @param _id     id of th type
   * @param _uuid   universal unique identifier
   * @param _name   name of the type name of the instance
   * @throws CacheReloadException on error
   */
  protected Type(final long _id, final String _uuid, final String _name)
      throws CacheReloadException {
    super(_id, _uuid, _name);
    addAttribute(new Attribute(0,
                               "Type",
                               "",
                               (SQLTable) null,
                               AttributeType.get("Type"),
                               null));
  }

  /**
   * Add an attribute to this type and all child types of this type.
   *
   * @param _attribute
   *                attribute to add
   */
  protected void addAttribute(final Attribute _attribute) {
    _attribute.setParent(this);
    getAttributes().put(_attribute.getName(), _attribute);
    if (_attribute.getTable() != null) {
      getTables().add(_attribute.getTable());
      _attribute.getTable().add(this);
      if (getMainTable() == null) {
        if (_attribute.getTable().getMainTable() != null) {
          setMainTable(_attribute.getTable().getMainTable());
        } else if (_attribute.getTable().getMainTable() == null) {
          setMainTable(_attribute.getTable());
        }
      }
    }
    for (final Type child : getChildTypes()) {
      if (child.getParentType().getId() == getId()) {
        child.addAttribute(_attribute.copy());
      }
    }
  }

  /**
   * Adds link from an attribute to this type. The link is also registered
   * under the name of all child types of the attribute.
   *
   * @param _attr   attribute with the link to this type
   * @todo description of algorithm
   */
  protected void addLink(final Attribute _attr) {
    getLinks().put(_attr.getParent().getName() + "\\" + _attr.getName(), _attr);
    for (final Type type : _attr.getParent().getChildTypes()) {
      getLinks().put(type.getName() + "\\" + _attr.getName(), _attr);
    }
    for (final Type child : getChildTypes()) {
      if (child.getParentType().getId() == getId()) {
        child.addLink(_attr);
      }
    }
  }

  /**
   * Returns for the given parameter <b>_name</b> the attribute.
   *
   * @param _name
   *                name of the attribute for this type to return
   * @return instance of class {@link Attribute}
   */
  public final Attribute getAttribute(final String _name) {
    return getAttributes().get(_name);
  }

  /**
   * The instance method returns all attributes which are from the same
   * attribute type as the described with the parameter <i>_class</i>.
   *
   * @param _class
   *                searched attribute type
   * @return all attributes assigned from parameter <i>_class</i>
   */
  public final Set<Attribute> getAttributes(final Class<?> _class) {
    final Set<Attribute> ret = new HashSet<Attribute>();
    for (final Attribute attr : getAttributes().values()) {
      if (attr.getAttributeType().getClassRepr().isAssignableFrom(_class)) {
        ret.add(attr);
      }
    }
    return ret;
  }

  /**
   * Tests, if this type is kind of the type in the parameter (question is, is
   * this type a child of the parameter type).
   *
   * @param _type   type to test for parent
   * @return true if this type is a child, otherwise false
   */
  public boolean isKindOf(final Type _type) {
    boolean ret = false;
    Type type = this;
    while ((type != null) && (type.getId() != _type.getId())) {
      type = type.getParentType();
    }
    if ((type != null) && (type.getId() == _type.getId())) {
      ret = true;
    }
    return ret;
  }

  /**
   * Checks if the current type holds the property with the given name. If not,
   * the value of the property of the parent type (see {@link #getParentType})
   * is returned (if a parent type exists).
   *
   * @param _name   name of the property (key)
   * @return value of the property with the given name / key.
   * @see org.efaps.admin.AbstractAdminObject#getProperty
   */
  @Override
  public String getProperty(final String _name) {
    String value = super.getProperty(_name);
    if ((value == null) && (getParentType() != null)) {
      value = getParentType().getProperty(_name);
    }
    return value;
  }

  /**
   * The method overrides the original method 'toString' and returns information
   * about this type instance.
   *
   * @return name of the user interface object
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("parentType",
                       getParentType() != null ? getParentType().getName() : "")
                .append("uniqueKey", getUniqueKeys())
                .toString();
  }

  /**
   * Checks, if the current context user has all access defined in the list of
   * access types for the given instance.
   *
   * @param _instance     instance for which the access must be checked
   * @param _accessType   list of access types which must be checked
   * @throws EFapsException on error
   * @return true if user has access, else false
   */
  public boolean hasAccess(final Instance _instance,
                           final AccessType _accessType)
      throws EFapsException {
    boolean hasAccess = true;
    final List<EventDefinition> events = super.getEvents(EventType.ACCESSCHECK);
    if (events != null) {
      final Parameter parameter = new Parameter();
      parameter.put(ParameterValues.INSTANCE, _instance);
      parameter.put(ParameterValues.ACCESSTYPE, _accessType);

      for (final EventDefinition event : events) {
        final Return ret = event.execute(parameter);
        hasAccess = ret.get(ReturnValues.TRUE) != null;
      }
    }
    return hasAccess;
  }

  /**
   * A new access set is assigned to this type instance.
   *
   * @param _accessSet    new access to assign to this type instance
   * @see #accessSets
   */
  public void addAccessSet(final AccessSet _accessSet) {
    this.accessSets.add(_accessSet);
  }

  /**
   * This is the getter method for instance variable {@link #accessSets}.
   *
   * @return value of instance variable {@link #accessSets}
   * @see #accessSets
   */
  public Set<AccessSet> getAccessSets() {
    return this.accessSets;
  }

  /**
   *
   * Sets the link properties for this object.
   *
   * @param _linkType   type of the link property
   * @param _toId       to id
   * @param _toType     to type
   * @param _toName     to name
   * @throws Exception on error
   */
  @Override
  protected void setLinkProperty(final EFapsClassNames _linkType,
                                 final long _toId,
                                 final EFapsClassNames _toType,
                                 final String _toName)
      throws Exception {
    switch (_linkType) {
      case DATAMODEL_TYPEEVENTISALLOWEDFOR:
        final Type eventType = get(_toId);
        this.allowedEventTypes.add(eventType);
        break;
      case DATAMODEL_TYPE2STORE:
        this.storeId = _toId;
        break;
      default:
        super.setLinkProperty(_linkType, _toId, _toType, _toName);
    }
  }

  /**
   * The instance method sets a new property value.
   *
   * @param _name   name of the property
   * @param _value  value of the property
   * @see #addUniqueKey
   * @throws CacheReloadException on error
   */
  @Override
  protected void setProperty(final String _name,
                             final String _value)
      throws CacheReloadException {
    if (_name.startsWith("UniqueKey")) {
      addUniqueKey(_value);
    } else {
      super.setProperty(_name, _value);
    }
  }

  /**
   * First, the instance method initialize the set of unique keys
   * ({@link #uniqueKeys}) if needed. The a new unique key is created and added
   * to the list of unique keys in {@link #uniqueKeys}.
   *
   * @param _attrList
   *                string with comma separated list of attribute names
   * @see #setProperty
   */
  private void addUniqueKey(final String _attrList) {
    if (getUniqueKeys() == null) {
      setUniqueKeys(new HashSet<UniqueKey>());
    }
    getUniqueKeys().add(new UniqueKey(this, _attrList));
  }

  /**
   * Add a new child type for this type. All sub child types of the child type
   * are also defined as child type of this type.<br/> Also for all parent
   * types (of this type), the child type (with sub child types) are added.
   *
   * @param _childType child type to add
   * @see #childTypes
   */
  protected void addChildType(final Type _childType) {
    Type parent = this;
    while (parent != null) {
      parent.getChildTypes().add(_childType);
      parent.getChildTypes().addAll(_childType.getChildTypes());
      parent = parent.getParentType();
    }
  }

  /**
   * For the given type it is tested if a store is defined for the type.
   *
   * @return <i>true</i> if a store resource is defined for the type, otherwise
   *         <i>false</i> is returned
   */
  public boolean hasStore() {
    return getStoreId() > 0 ? true : false;
  }

  /**
   * This is the getter method for instance variable {@link #parentType}.
   *
   * @return value of instance variable {@link #parentType}
   * @see #parentType
   * @see #setParentType
   */
  public Type getParentType() {
    return this.parentType;
  }

  /**
   * Setter method for instance variable {@link #parentType}.
   *
   * @param _parentType parent to set
   */
  protected void setParentType(final Type _parentType) {
    this.parentType = _parentType;
  }

  /**
   * This is the getter method for instance variable {@link #childTypes}.
   *
   * @return value of instance variable {@link #childTypes}
   * @see #childTypes
   */
  public Set<Type> getChildTypes() {
    return this.childTypes;
  }

  /**
   * This is the getter method for instance variable {@link #attributes}.
   *
   * @return value of instance variable {@link #attributes}
   * @see #attributes
   */
  public Map<String, Attribute> getAttributes() {
    return this.attributes;
  }

  /**
   * This is the getter method for instance variable {@link #tables}.
   *
   * @return value of instance variable {@link #tables}
   * @see #tables
   */
  public Set<SQLTable> getTables() {
    return this.tables;
  }

  /**
   * This is the getter method for instance variable {@link #mainTable}.
   *
   * @return value of instance variable {@link #mainTable}
   * @see #setMainTable
   * @see #mainTable
   */
  public SQLTable getMainTable() {
    return this.mainTable;
  }

  /**
   * This is the setter method for instance variable {@link #mainTable}.
   *
   * @param _mainTable
   *                new value for instance variable {@link #mainTable}
   * @see #getMainTable
   * @see #mainTable
   */
  private void setMainTable(final SQLTable _mainTable) {
    this.mainTable = _mainTable;
  }

  /**
   * This is the getter method for instance variable {@link #formView}.
   *
   * @return value of instance variable {@link #formView}
   * @see #setFormView
   * @see #formView
   */
  public Form getFormView() {
    return this.formView;
  }

  /**
   * This is the getter method for instance variable {@link #formEdit}.
   *
   * @return value of instance variable {@link #formEdit}
   * @see #setFormEdit
   * @see #formEdit
   */
  public Form getFormEdit() {
    return this.formEdit;
  }

  /**
   * This is the getter method for instance variable {@link #formCreate}.
   *
   * @return value of instance variable {@link #formCreate}
   * @see #setFormCreate
   * @see #formCreate
   */
  public Form getFormCreate() {
    return this.formCreate;
  }

  /**
   * This is the getter method for instance variable {@link #uniqueKeys}.
   *
   * @return value of instance variable {@link #uniqueKeys}
   * @see #setUniqueKeys
   * @see #uniqueKeys
   */
  public Collection<UniqueKey> getUniqueKeys() {
    return this.uniqueKeys;
  }

  /**
   * This is the setter method for instance variable {@link #uniqueKeys}.
   *
   * @param _uniqueKeys
   *                new value for instance variable {@link #uniqueKeys}
   * @see #getUniqueKeys
   * @see #uniqueKeys
   */
  private void setUniqueKeys(final Collection<UniqueKey> _uniqueKeys) {
    this.uniqueKeys = _uniqueKeys;
  }

  /**
   * This is the getter method for instance variable {@link #links}.
   *
   * @return value of instance variable {@link #links}
   * @see #links
   */
  public Map<String, Attribute> getLinks() {
    return this.links;
  }

  /**
   * This is the getter method for instance variable {@link #allowedEventTypes}.
   *
   * @return value of instance variable {@link #allowedEventTypes}
   * @see #allowedEventTypes
   */
  public Set<Type> getAllowedEventTypes() {
    return this.allowedEventTypes;
  }

  /**
   * Getter method for instance variable {@link #storeId}.
   *
   * @return value of instance variable {@link #storeId}
   */
  public long getStoreId() {
    final long ret;
    if (this.storeId == 0 && this.parentType != null) {
      ret = this.parentType.getStoreId();
    } else {
      ret = this.storeId;
    }
    return ret;
  }

  /**
   *  Method to initialize the Cache of this CacheObjectInterface.
   * @param _class class that called the method
   * @throws CacheReloadException on error
   */
  public static void initialize(final Class<?> _class)
      throws CacheReloadException {
    CACHE.initialize(_class);
    // initialize properties and links
    for (final Type type : CACHE.getCache4Id().values())  {
      type.readFromDB4Properties();
      type.readFromDB4Links();
    }
  }

  /**
   * Method to initialize the Cache of this CacheObjectInterface.
   *
   * @throws CacheReloadException on error
   */
  public static void initialize() throws CacheReloadException {
    Type.initialize(Type.class);
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Type}.
   *
   * @param _id id of the type to get
   * @return instance of class {@link Type}
   * @throws CacheReloadException
   */
  public static Type get(final long _id) {
    return CACHE.get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Type}.
   * @param _name name of the type to get
   * @return instance of class {@link Type}
   * @throws CacheReloadException
   */
  public static Type get(final String _name) {
    return CACHE.get(_name);
  }

  /**
   * Returns for given parameter <i>_uuid</i> the instance of class
   * {@link Type}.
   * @param _uuid uuid of the type to get
   * @return instance of class {@link Type}
   * @throws CacheReloadException
   */
  public static Type get(final UUID _uuid) {
    return CACHE.get(_uuid);
  }

  /**
   * Returns for given parameter <i>_className</i> the instance of class
   * {@link Type}.
   * @param _className classname of the type to get
   * @return instance of class {@link Type}
   * @throws CacheReloadException
   */
  public static Type get(final EFapsClassNames _className) {
    return CACHE.get(_className.getUuid());
  }

  /**
   * Static getter method for the type hashtable {@link #CACHE}.
   *
   * @return value of static variable {@link #CACHE}
   */
  static Cache<Type> getTypeCache() {
    return CACHE;
  }

  /**
   * Cahcwe for Types.
   */
  private static class TypeCache extends Cache<Type> {

    /**
     * @see org.efaps.util.cache.Cache#readCache(java.util.Map,
     *  java.util.Map, java.util.Map)
     * @param _cache4Id   Cache for id
     * @param _cache4Name Cache for name
     * @param _cache4UUID Cache for UUID
     * @throws CacheReloadException  on error during reading
     */
    @Override
    protected void readCache(final Map<Long, Type> _cache4Id,
                             final Map<String, Type> _cache4Name,
                             final Map<UUID, Type> _cache4UUID)
        throws CacheReloadException {
      ConnectionResource con = null;
      try {
        // to store parent informations
        final Map<Long, Long> parents = new HashMap<Long, Long>();
        // to store all read types
        final Set<Type> allTypes = new HashSet<Type>();

        con = Context.getThreadContext().getConnectionResource();

        Statement stmt = null;
        try {

          stmt = con.getConnection().createStatement();

          final ResultSet rs = stmt.executeQuery(SQL_SELECT);
          while (rs.next()) {
            final long id = rs.getLong(1);
            final String uuid = rs.getString(2).trim();
            final String name = rs.getString(3).trim();
            final boolean abstractType = rs.getBoolean(4);
            final long parentTypeId = rs.getLong(5);
            String sqlCacheExpr = rs.getString(6);
            sqlCacheExpr = sqlCacheExpr != null ? sqlCacheExpr.trim() : null;
            if (LOG.isDebugEnabled()) {
              LOG.debug("read type '" + name + "' (id = " + id + ")");
            }
            final Type type = new Type(id, uuid, name);
            type.setAbstractType(abstractType);
            _cache4Id.put(type.getId(), type);
            _cache4Name.put(type.getName(), type);
            _cache4UUID.put(type.getUUID(), type);

            allTypes.add(type);

            if (parentTypeId != 0) {
              parents.put(id, parentTypeId);
            }
          }
          rs.close();
        } finally {
          if (stmt != null) {
            stmt.close();
          }
        }
        // initialize parents
        for (final Map.Entry<Long, Long> entry : parents.entrySet()) {
          final Type child = _cache4Id.get(entry.getKey());
          final Type parent = _cache4Id.get(entry.getValue());
  // TODO: test if loop
          if (child.getId() == parent.getId()) {
            throw new CacheReloadException(
                "child and parent type is equal!child is " + child);
          }
          child.parentType = parent;
          parent.addChildType(child);
        }
        con.commit();
      } catch (final SQLException e) {
        throw new CacheReloadException("could not read types", e);
      } catch (final EFapsException e) {
        throw new CacheReloadException("could not read types", e);
      } finally {
        if ((con != null) && con.isOpened()) {
          try {
            con.abort();
          } catch (final EFapsException e) {
            throw new CacheReloadException("could not read types", e);
          }
        }
      }
    }
  }
}
