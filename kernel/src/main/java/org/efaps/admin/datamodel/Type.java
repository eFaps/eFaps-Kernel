/*
 * Copyright 2006 The eFaps Team
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.TriggerEvent;
import org.efaps.admin.lifecycle.Policy;
import org.efaps.admin.lifecycle.Status;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.servlet.RequestHandler;

/**
 * This is the class for the type description. The type description holds
 * information about creation of a new instance of a type with default values.
 *
 * @author tmo
 * @version $Id$
 */
public class Type extends DataModelObject  {

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(Type.class);

  /**
   * This is the sql select statement to select all types from the database.
   */
  private static final String SQL_SELECT  = "select "
                                                + "ID,"
                                                + "NAME,"
                                                + "PARENTDMTYPE,"
                                                + "SQLCACHEEXPR "
                                              + "from V_ADMINTYPE";

  /**
   * This is the constructor for class Type. Every instance of class Type
   * must have a name (parameter <i>_name</i>).
   *
   * @param _name   name of the instance
   */
  protected Type(final long _id, final String _name)  {
    super(_id, _name);
  }

  /**
   * Add an attribute to this type and all child types of this type.
   *
   * @param _attribute  attribute to add
   */
  protected void addAttribute(final Attribute _attribute)  {
    _attribute.setParent(this);
    getAttributes().put(_attribute.getName(), _attribute);
    if (_attribute.getTable() != null)  {
      getTables().add(_attribute.getTable());
      _attribute.getTable().add(this);
      if (getMainTable() == null)  {
        if (_attribute.getTable().getMainTable() != null)  {
          setMainTable(_attribute.getTable().getMainTable());
        } else if (_attribute.getTable().getMainTable() == null)  {
          setMainTable(_attribute.getTable());
        }
      }
    }
    for (Type child : getChildTypes())  {
      if (child.getParentType().getId() == this.getId())  {
        child.addAttribute(_attribute.copy());
      }
    }
  }

  /**
   * Adds link from an attribute to this type.

   The link is also registered
   * under the name of all child types of the attribute.
   *
   * @param _attr attribute with the link to this type
   * @todo description of algorithm
   */
  protected void addLink(final Attribute _attr)  {
    getLinks().put(_attr.getParent().getName() + "\\" + _attr.getName(), _attr);
    for (Type type : _attr.getParent().getChildTypes())  {
      getLinks().put(type.getName() + "\\" + _attr.getName(), _attr);
    }
    for (Type child : getChildTypes())  {
      if (child.getParentType().getId() == this.getId())  {
        child.addLink(_attr);
      }
    }
  }

  /**
   * Returns for the given parameter <b>_name</b> the attribute.
   *
   * @param _name name of the attribute for this type to return
   * @return instance of class {@link Attribute}
   */
  public final Attribute getAttribute(final String _name)  {
    return getAttributes().get(_name);
  }

  /**
   * The instance method returns all attributes which are from the same
   * attribute type as the described with the parameter <i>_class</i>.
   *
   * @param _class  searched attribute type
   * @return all attributes assigned from parameter <i>_class</i>
   */
  public final Set < Attribute > getAttributes(final Class _class)  {
    Set < Attribute > ret = new HashSet < Attribute > ();
    for (Attribute attr : getAttributes().values())  {
      if (attr.getAttributeType().getClassRepr().isAssignableFrom(_class))  {
        ret.add(attr);
      }
    }
    return ret;
  }

  /**
   * If a hashtable instance for the cache is given, a <i>true</i> is returned,
   * that this type is cacheable, otherwise a <i>false</i> is returned.
   *
   * @return <i>true</i> if type is cacheable, otherwise <i>false</i>
   */
  public final boolean isCacheable()  {
    boolean ret = false;

    if (getCache() != null)  {
      ret = true;
    }
    return ret;
  }

  /**
   * Search in the cache for the object with the given <i>_id</i> and returns
   * this Object.
   *
   * @return cache object for given parameter <i>_id</i>
   * @param _id
   */
  public CacheInterface getCacheObject(final long _id)  {
    return getCache().get(_id);
  }

  /**
   * Add a policy to this type.
   *
   * @see #policies
   * @see #getPolicies
   */
  public void addPolicy(final Policy _policy)  {
    synchronized (getPolicies())  {
      getPolicies().add(_policy);
    }
  }

  /**
   *
   */
  public void readCache(final Context _context, final String _cacheExpr) throws SQLException  {
//    Cache cache = new Cache(_context.getConnection(), getTableName(), _cacheExpr);
//    setCache(cache);
  }

  /**
   * Returns the name of the type.
   *
   * @param _context
   * @see #getName
   */
  public String getViewableName(final Context _context)  {
    return getName();
  }

  /**
   * Tests, if this type is kind of the type in the parameter (question is, is
   * this type a child of the parameter type).
   *
   * @param _type type to test for parent
   * @return true if this type is a child, otherwise false
   */
  public boolean isKindOf(final Type _type)  {
    boolean ret = false;
    Type type = this;
    while ((type != null) && (type.getId() != _type.getId()))  {
      type = type.getParentType();
    }
    if ((type != null) && (type.getId() == _type.getId()))  {
      ret = true;
    }
    return ret;
  }

  /**
   * Checks if the current type holds the property with the given name. If not,
   * the value of the property of the parent type (see {@link #getParentType})
   * is returned (if a parent type exists).
   *
   * @see org.efaps.admin.AdminObject#getProperty
   * @param _name     name of the property (key)
   * @return value of the property with the given name / key.
   */
  public String getProperty(final String _name)  {
    String value = super.getProperty(_name);
    if ((value == null) && (getParentType() != null))  {
      value = getParentType().getProperty(_name);
    }
    return value;
  }

  /**
   * The method overrides the original method 'toString' and returns
   * information about this type instance.
   *
   * @return name of the user interface object
   */
  public String toString()  {
    return new ToStringBuilder(this).
      appendSuper(super.toString()).
      append("parentType", getParentType() != null ? getParentType().getName() : "").
      append("uniqueKey", getUniqueKeys()).
      toString();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method sets a new property value.
   *
   * @param _context  context for this request
   * @param _name     name of the property
   * @param _value    value of the property
   * @see #addUniqueKey
   * @see #setViewAttribute
   */
  protected void setProperty(final Context _context, final String _name, final String _value) throws Exception  {
    if (_name.startsWith("Icon"))  {
      setIcon(RequestHandler.replaceMacrosInUrl(_value));
    } else if (_name.startsWith("Tree"))  {
      setTreeMenuName(_value);
    } else if (_name.startsWith("UniqueKey"))  {
      addUniqueKey(_value);
    } else if (_name.equals("ViewAttribute"))  {
//      setViewAttribute(_context, _value);
    } else  {
      super.setProperty(_context, _name, _value);
    }
  }

  /**
   * First, the instance method initiliase the set of unique keys
   * ({@link #uniqueKeys}) if needed. The a new unique key is created and
   * added to the list of unique keys in {@link #uniqueKeys}.
   *
   * @param _attrList string with comma separated list of attribute names
   * @see #setProperty
   */
  private void addUniqueKey(final String _attrList)  {
    if (getUniqueKeys() == null)  {
      setUniqueKeys(new HashSet < UniqueKey > ());
    }
    getUniqueKeys().add(new UniqueKey(this, _attrList));
  }

  /**
   * The view attribute represents this type is set. First, the attribute name
   * is test, if it exists directly at this type (with {@link #getAttribute}).
   * If not, the attribute name is test with
   * {@link Attribute.get(Context,String)}.<br/>
   * So the view attribute can include the type name or can be without the
   * type name.
   *
   * @param _context  context for this request
   * @param _name     name of the attribute
   * @see #viewAttribute
   * @see #setProperty
   */
  private void setViewAttribute(final Context _context, final String _name) throws Exception  {
/*    Attribute attr = getAttribute(_name);
    if (attr == null)  {
      attr = Attribute.get(_context, _name);
    }
    setViewAttribute(attr);
*/
  }

  /**
   * Add a new child type for this type. All sub child types of the child type
   * are also defined as child type of this type.<br/>
   * Also for all parent types (of this type), the child type (with sub child
   * types) are added.
   *
   * @param _childType  child type to add
   * @see #childTypes
   */
  private void addChildType(final Type _childType)  {
//    for (Attribute linkAttr: getLinks().values())  {
//      _childType.addLink(linkAttr);
//    }
//    for (Type subChildType : _childType.getChildTypes())  {
//      getChildTypes().add(subChildType);
//      for (Attribute linkAttr: getLinks().values())  {
//        subChildType.addLink(linkAttr);
//      }
//    }
    Type parent = this;
    while (parent != null)  {
      parent.getChildTypes().add(_childType);
      parent.getChildTypes().addAll(_childType.getChildTypes());
//System.out.println("childTypes("+parent.getName()+")="+parent.getChildTypes());
      parent = parent.getParentType();
    }
  }

  /**
   * Adds a new trigger event to this type.
   *
   * @param _triggerEvent   trigger class name to add
   * @param _eventDef       event defition to add
   */
  public void addTrigger(final TriggerEvent _triggerEvent, final EventDefinition _eventDef)  {
    List < EventDefinition > events = this.trigger.get(_triggerEvent);
    if (events == null)  {
      events = new ArrayList < EventDefinition > ();
      this.trigger.put(_triggerEvent, events);
    }
    int pos = 0;
    for (EventDefinition cur : events)  {
      if (_eventDef.getIndexPos() > cur.getIndexPos())  {
        break;
      }
      pos++;
    }
    events.add(pos, _eventDef);
  }

  /**
   * Returns the ordered list of triggers assigned to this type instance.
   *
   * @param _triggerEvent   trigger class name for which the triggers
   *                        should returned
   */
  public List < EventDefinition > getTrigger(final TriggerEvent _triggerEvent)  {
    return this.trigger.get(_triggerEvent);
  }

  /**
   * For the given type it is tested if a store is defined for the type.
   *
   * @param _type type to test
   * @return <i>true</i> if a store resource is defined for the type, otherwise
   *         <i>false</i> is returned
   */
  public boolean hasStoreResource()  {
    return getProperty("StoreResource") != null ? true : false;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method reads all related policies for this type are for a
   * parent type (because policies could be derived!).
   *
   * @param _context  eFaps context for this request
   */
/*  private void readDBPolicies(Context _context) throws Exception  {
    Statement stmt = _context.getConnection().createStatement();
    try  {
      ResultSet rs = stmt.executeQuery(
          "select LCPOLICY "+
              "from DMTYPE2POLICY "+
              "where DMTYPE in ("+getParentTypeIds()+")"
      );
      while (rs.next())  {
        addPolicy(Policy.get(_context, rs.getLong(1)));
      }
      rs.close();
    } catch (Exception e)  {
e.printStackTrace();
throw e;
    } finally  {
      stmt.close();
    }
  }
*/

  /////////////////////////////////////////////////////////////////////////////

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
  private String parentTypeIds = "";

  /**
   * Instance variable for all child types derived from this type.
   *
   * @see #getChildTypes
   */
  private Set < Type > childTypes = new HashSet < Type > ();

  /**
   * The instance variables stores all attributes for this type object.
   *
   * @see #getAttributes()
   * @see #add(Attribute)
   * @see #getAttribute
   * @see #getAttributes(Class)
   */
  private Map < String,Attribute > attributes = new Hashtable < String, Attribute > ();

  /**
   * Cache of the business objects of this type.
   *
   * @see #getCache
   * @see #setCache
   */
  private Cache cache = null;

  /**
   * Instance of a HashSet to store all possible policies for this type.
   *
   * @see #addPolicy
   * @see #getPolicies
   */
  private Set < Policy > policies = new HashSet < Policy > ();

  /**
   * Instance of a HashSet to store all needed tables for this type. The
   * tables are automatically added via the method {@link #add(Attribute)}.
   *
   * @see #add(Attribute)
   * @see #getTables
   */
  private Set < SQLTable > tables = new HashSet < SQLTable > ();

  /**
   * The instance variable stores the main table, which must be inserted
   * first. In the main table stands also the select statement to get a new
   * id. The value is automatically set with method {@link #add(Attribute)}.
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
  private Form formView = null;

  /**
   * This instance variable stores the form for the editing mode.
   *
   * @see #getFormEdit
   * @see #setFormEdit
   */
  private Form formEdit = null;

  /**
   * This instance variable stores the form for the creating mode.
   *
   * @see #getFormCreate
   * @see #setFormCreate
   */
  private Form formCreate = null;

  /**
   * This instance variable stores the standard type menu.
   *
   * @see #getTreeMenu
   * @see #setTreeMenu
   */
  private Menu treeMenu = null;

  /**
   * Th instance variable stores the attribute used to represent this type.
   *
   * @see #getViewAttribute
   * @see #setViewAttribute
   */
  private Attribute viewAttribute = null;

  /**
   * The instance variable stores all unique keys of this type instance.
   *
   * @see #getUniqueKeys
   * @see #setUniqueKeys
   */
  private Collection < UniqueKey > uniqueKeys = null;

  /**
   * The type icon is stored in this instance variable.
   */
  private String icon = null;

  /**
   * All attributes which are used as links are stored in this map.
   *
   * @see #getLinks
   */
  private Map < String, Attribute > links = new HashMap < String, Attribute > ();

  /**
   * All triggers for this type are stored in this map.
   */
  private final Map < TriggerEvent, List < EventDefinition > > trigger = new HashMap <  TriggerEvent, List < EventDefinition > > ();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #parentType}. Only
   * the DB reader is allowed to set this!
   *
   * @param _tableName new value for instance variable {@link #parentType}
   * @see #parentType
   * @see #getParentType
   */
  private void setParentType(final Type _parentType)  {
    this.parentType = _parentType;
  }

  /**
   * This is the getter method for instance variable {@link #parentType}.
   *
   * @return value of instance variable {@link #parentType}
   * @see #parentType
   * @see #setParentType
   */
  public Type getParentType()  {
    return this.parentType;
  }

  /**
   * This is the setter method for instance variable {@link #parentTypeIds}. Only
   * the DB reader is allowed to set this!
   *
   * @param _tableName new value for instance variable {@link #parentTypeIds}
   * @see #parentTypeIds
   * @see #getParentTypeIds
   */
  private void setParentTypeIds(final String _parentTypeIds)  {
    this.parentTypeIds = _parentTypeIds;
  }

  /**
   * This is the getter method for instance variable {@link #parentTypeIds}.
   * The method is only used from this class, because the parent types are not
   * read when this information is needed.
   *
   * @return value of instance variable {@link #parentTypeIds}
   * @see #parentTypeIds
   * @see #setParentTypeIds
   */
  private String getParentTypeIds()  {
    return this.parentTypeIds;
  }

  /**
   * This is the getter method for instance variable {@link #childTypes}.
   *
   * @return value of instance variable {@link #childTypes}
   * @see #childTypes
   */
  public Set < Type > getChildTypes()  {
    return this.childTypes;
  }

  /**
   * This is the getter method for instance variable {@link #attributes}.
   *
   * @return value of instance variable {@link #attributes}
   * @see #attributes
   */
  public Map < String, Attribute > getAttributes()  {
    return this.attributes;
  }

  /**
   * This is the getter method for instance variable {@link #cache}.
   *
   * @return value of instance variable {@link #cache}
   * @see #setCache
   * @see #cache
   */
  public Cache getCache()  {
    return this.cache;
  }

  /**
   * This is the setter method for instance variable {@link #cache}.
   *
   * @param _cache new value for instance variable {@link #cache}
   * @see #getCache
   * @see #cache
   */
  private void setCache(final Cache _cache)  {
    this.cache = _cache;
  }

  /**
   * This is the getter method for instance variable {@link #policies}.
   *
   * @return value of instance variable {@link #policies}
   * @see #addPolicy
   * @see #policies
   */
  public Set < Policy > getPolicies()  {
    return this.policies;
  }

  /**
   * This is the getter method for instance variable {@link #tables}.
   *
   * @return value of instance variable {@link #tables}
   * @see #addPolicy
   * @see #tables
   */
  public Set < SQLTable > getTables()  {
    return this.tables;
  }

  /**
   * This is the getter method for instance variable {@link #mainTable}.
   *
   * @return value of instance variable {@link #mainTable}
   * @see #setMainTable
   * @see #mainTable
   */
  public SQLTable getMainTable()  {
    return this.mainTable;
  }

  /**
   * This is the setter method for instance variable {@link #mainTable}.
   *
   * @param _mainTable new value for instance variable {@link #mainTable}
   * @see #getMainTable
   * @see #mainTable
   */
  private void setMainTable(final SQLTable _mainTable)  {
    this.mainTable = _mainTable;
  }

  /**
   * This is the getter method for instance variable {@link #formView}.
   *
   * @return value of instance variable {@link #formView}
   * @see #setFormView
   * @see #formView
   */
  public Form getFormView()  {
    return this.formView;
  }

  /**
   * This is the setter method for instance variable {@link #formView}.
   *
   * @param _formView new value for instance variable {@link #formView}
   * @see #getFormView
   * @see #formView
   */
  private void setFormView(final Form _formView)  {
    this.formView = _formView;
  }

  /**
   * This is the getter method for instance variable {@link #formEdit}.
   *
   * @return value of instance variable {@link #formEdit}
   * @see #setFormEdit
   * @see #formEdit
   */
  public Form getFormEdit()  {
    return this.formEdit;
  }

  /**
   * This is the setter method for instance variable {@link #formEdit}.
   *
   * @param _formEdit new value for instance variable {@link #formEdit}
   * @see #getFormEdit
   * @see #formEdit
   */
  private void setFormEdit(final Form _formEdit)  {
    this.formEdit = _formEdit;
  }

  /**
   * This is the getter method for instance variable {@link #formCreate}.
   *
   * @return value of instance variable {@link #formCreate}
   * @see #setFormCreate
   * @see #formCreate
   */
  public Form getFormCreate()  {
    return this.formCreate;
  }

  /**
   * This is the setter method for instance variable {@link #formCreate}.
   *
   * @param _formCreate new value for instance variable {@link #formCreate}
   * @see #getFormCreate
   * @see #formCreate
   */
  private void setFormCreate(final Form _formCreate)  {
    this.formCreate = _formCreate;
  }

String treeMenuName = null;
private void setTreeMenuName(final String _treeMenuName)  {
  this.treeMenuName = _treeMenuName;
}
public String getTreeMenuName()  {
  return this.treeMenuName;
}

public Menu getTreeMenu() throws Exception  {
  if ((this.treeMenu == null) && (getTreeMenuName() != null))  {
    this.treeMenu = Menu.get(getTreeMenuName());
  }
  return this.treeMenu;
}


  /**
   * This is the getter method for instance variable {@link #viewAttribute}.
   *
   * @return value of instance variable {@link #viewAttribute}
   * @see #setViewAttribute
   * @see #viewAttribute
   */
  public Attribute getViewAttribute()  {
    return this.viewAttribute;
  }

  /**
   * This is the setter method for instance variable {@link #viewAttribute}.
   *
   * @param _viewAttribute new value for instance variable {@link #viewAttribute}
   * @see #getViewAttribute
   * @see #viewAttribute
   */
  private void setViewAttribute(final Attribute _viewAttribute)  {
    this.viewAttribute = _viewAttribute;
  }

  /**
   * This is the getter method for instance variable {@link #uniqueKeys}.
   *
   * @return value of instance variable {@link #uniqueKeys}
   * @see #setUniqueKeys
   * @see #uniqueKeys
   */
  public Collection < UniqueKey > getUniqueKeys()  {
    return this.uniqueKeys;
  }

  /**
   * This is the setter method for instance variable {@link #uniqueKeys}.
   *
   * @param _uniqueKeys new value for instance variable {@link #uniqueKeys}
   * @see #getUniqueKeys
   * @see #uniqueKeys
   */
  private void setUniqueKeys(final Collection < UniqueKey > _uniqueKeys)  {
    this.uniqueKeys = _uniqueKeys;
  }

  /**
   * This is the getter method for instance variable {@link #icon}.
   *
   * @return value of instance variable {@link #icon}
   * @see #setIcon
   * @see #icon
   */
  public String getIcon()  {
    return this.icon;
  }

  /**
   * This is the setter method for instance variable {@link #icon}.
   *
   * @param _icon new value for instance variable {@link #icon}
   * @see #getIcon
   * @see #icon
   */
  private void setIcon(final String _icon)  {
    this.icon = _icon;
  }

  /**
   * This is the getter method for instance variable {@link #links}.
   *
   * @return value of instance variable {@link #links}
   * @see #links
   */
  public Map < String, Attribute > getLinks()  {
    return this.links;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Initialise the cache of types.
   *
   * @param _context  eFaps context for this request
   */
  public static void initialise(final Context _context) throws Exception  {
    ConnectionResource con = null;
    try  {
      Map < Long, Long > parents = new HashMap < Long, Long > ();

      con = _context.getConnectionResource();

      Statement stmt = null;
      try  {

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next())  {
          long id =             rs.getLong(1);
          String name =         rs.getString(2).trim();
          long parentTypeId =   rs.getLong(3);
          String sqlCacheExpr = rs.getString(4);
          sqlCacheExpr = sqlCacheExpr != null ? sqlCacheExpr.trim() : null;

          LOG.debug("read type '" + name + "' (id = " + id + ")");

          Type type = new Type(id, name);
          if (type.getName().equals(EFapsClassName.USER_PERSON.name))  {
            type.setCache(Person.getCache());
          } else if (type.getName().equals(EFapsClassName.USER_ROLE.name))  {
            type.setCache(Role.getCache());
          } else if (type.getName().equals(EFapsClassName.LIFECYCLE_POLICY.name))  {
            type.setCache(Policy.getCache());
          } else if (type.getName().equals(EFapsClassName.LIFECYCLE_STATUS.name))  {
            type.setCache(Status.getCache());
          }

          getTypeCache().add(type);

//type.readDBPolicies(_context);
type.readFromDB4Properties(_context);

          if (parentTypeId != 0)  {
            parents.put(id , parentTypeId);
          }
        }
        rs.close();

      } finally  {
        if (stmt != null)  {
          stmt.close();
        }
      }

      // initialise parents
      for (Map.Entry < Long, Long > entry : parents.entrySet())  {
        Type child  = Type.get(entry.getKey());
        Type parent = Type.get(entry.getValue());

        child.setParentType(parent);
        parent.addChildType(child);
      }

      con.commit();

    } finally  {
      if ((con != null) && con.isOpened())  {
        con.abort();
      }
    }
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Type}.
   *
   * @return instance of class {@link Type}
   */
  public static Type get(final long _id) throws Exception  {
    Type type = getTypeCache().get(_id);
    return type;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class {@link Type}.
   *
   * @return instance of class {@link Type}
   */
  public static Type get(final String _name) throws Exception  {
    Type type = getTypeCache().get(_name);
    return type;
  }

  /**
   * Static getter method for the type hashtable {@link #typeCache}.
   *
   * @return value of static variable {@link #typeCache}
   */
  static Cache < Type > getTypeCache()  {
    return typeCache;
  }

  /**
   * Stores all instances of type.
   *
   * @see #get
   */
  private static Cache < Type > typeCache = new Cache < Type > ();

  /////////////////////////////////////////////////////////////////////////////

/*  static protected class TypeCache extends Cache<Type>  {

    private Type readType4Statement(Context _context, String _statement) throws Exception  {
      Type type = null;
      Statement stmt = _context.getConnection().createStatement();
      try  {
        ResultSet rs = stmt.executeQuery(_statement);
        while (rs.next())  {
          long id =               rs.getLong(1);
          String name =           rs.getString(2).trim();
          long parentTypeId =     rs.getLong(3);
          String sqlCacheExpr =   rs.getString(4);
          sqlCacheExpr = (sqlCacheExpr!=null ? sqlCacheExpr.trim() : null);

          type = new Type(id, name);
if (id==1000)  {
  type.setCache(Person.getCache());
} else if (id==1100)  {
  type.setCache(Role.getCache());
} else if (id==1200)  {
  type.setCache(Policy.getCache());
} else if (id==1220)  {
  type.setCache(Status.getCache());
}

Attribute attr = new Attribute(0, "Type");
attr.setAttributeType(AttributeType.get("Type"));
type.add(attr);

          this.add(type);
          if (parentTypeId!=0)  {
            type.setParentType(Type.get(_context, parentTypeId));
            type.getParentType().addChildType(type);
          }
          if (sqlCacheExpr!=null)  {
//System.out.println("type.readCache for '"+name+"'");
            type.readCache(_context, sqlCacheExpr);
          }

          type.readDBParentIds(_context);
          type.readDBAttributes(_context);
          type.readDBPolicies(_context);
          type.readFromDB4Properties(_context);
          type.readDBChilds(_context);
          type.readDBLinks(_context);
        }
        rs.close();
      } catch (Exception e)  {
e.printStackTrace();
      } finally  {
        stmt.close();
      }
      return type;
    }
  }
*/
}
