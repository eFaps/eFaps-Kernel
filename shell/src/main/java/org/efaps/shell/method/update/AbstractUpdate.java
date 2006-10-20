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

package org.efaps.shell.method.update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public abstract class AbstractUpdate  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(AccessTypeUpdate.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The name of the data model type is store in this instance variable.
   */
  private final String dataModelType;

  /**
   * All known link types are set to this instance varaible.
   */
  private final Set < Link > allLinkTypes;

  /**
   * The univeral unique identifier of the object is stored in this instance
   * variable.
   *
   * @see #setUUID
   */
  private String uuid = null;
  
  /**
   * All definitions of versions are added to this list.
   */
  private final List < DefinitionAbstract > definitions 
                                  = new ArrayList < DefinitionAbstract > ();

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  protected AbstractUpdate(final String _dataModelType)  {
    this(_dataModelType, null);
  }

  /**
   *
   */
  protected AbstractUpdate(final String _dataModelType,
                           final Set < Link > _allLinkTypes)  {
    this.dataModelType = _dataModelType;
    this.allLinkTypes = _allLinkTypes;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Adds one definition of a update for a specific version to all definitions
   * in {@link #definitions}.
   *
   * @param _definition definition to add
   * @see #definitions
   */
  public void addDefinition(final DefinitionAbstract _definition)  {
    this.definitions.add(_definition);
  }

  /**
   * The instance method returns the eFaps instance representing the read XML
   * configuration. If not already get from the eFaps databasse, the 
   * information is read. If no instance exists in the database, a new one
   * is automatically created.
   * The method searchs for the given universal unique identifier in 
   * {@link #uuid} the instance in the eFaps database and stores the result
   * in {@link #instance}. If no object is found in eFaps, {@link #instance}
   * is set to <code>null</code>.
   * A new instance is created in the eFaps db for given univeral unique 
   * identifier in {@link #uuid}. The name of the access set is also the
   * universal unique identifier, because the name of access set is first 
   * updates in the version definition.<br/>
   * The new created object is stored as instance information in 
   * {@link #instance}.
   *
   * @todo description
   * @param _jexlContext  expression context used to evaluate 
   */
  public void updateInDB(final JexlContext _jexlContext) throws EFapsException,Exception {
    Instance instance = null;
    Insert insert = null;
    Context context = Context.getThreadContext();

    // search for the instance
    SearchQuery query = new SearchQuery();
    query.setQueryTypes(this.dataModelType);
    query.addWhereExprEqValue("UUID", this.uuid);
    query.addSelect("OID");
    query.executeWithoutAccessCheck();
    if (query.next())  {
      instance = new Instance((String) query.get("OID"));
    }
    query.close();

    // if no instance exists, a new insert must be done
    if (instance == null)  {
      insert = new Insert(context, this.dataModelType);
//      insert.add(context, "Name", this.uuid);
      insert.add(context, "UUID", this.uuid);
    }

    for (DefinitionAbstract def : this.definitions)  {
      if (insert == null)  {
        _jexlContext.getVars().put("exists", new Boolean(true));
      } else  {
        _jexlContext.getVars().put("exists", new Boolean(false));
      }
      Expression jexlExpr = ExpressionFactory.createExpression(def.mode);
      boolean exec = new Boolean(jexlExpr.evaluate(_jexlContext).toString());
      if (exec)  {
        def.updateInDB(instance, this.allLinkTypes, insert);
      }
      _jexlContext.getVars().remove("exists");
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * @see #uuid
   */
  public void setUUID(final String _uuid)  {
    this.uuid = _uuid;
  }
  
  /**
   * Returns a string representation with values of all instance variables.
   *
   * @return string representation of this abstract update
   */
  public String toString()  {
    return new ToStringBuilder(this).
      append("uuid",            this.uuid).
      append("definitions",     this.definitions).
      toString();
  }

  /////////////////////////////////////////////////////////////////////////////
  // static classes

  /**
   * The class is used to define the links with all information needed to 
   * update the link information between the object to update and the related
   * objects.
   *
   * @see #setLinksInDB
   */
  static protected class Link  {

    /** Name of the link. */
    private final String linkName;

    /**
     * Name of the parent attribute in the link. The parent attribute stores
     * the id of the objecto udpate.
     */
    private final String parentAttrName;

    /**
     * Name of the child type used to query for the given name to which a link
     * must be set.
     */
    private final String childTypeName;

    /** Name of the child attribute in the link. */
    private final String childAttrName;
    
    /**
     * Constructor used to initialise the instance variables.
     *
     * @param _linkName         name of the link itself
     * @param _parentAttrName   name of the parent attribute in the link
     * @param _childTypeName    name of the child type
     * @param _childAttrName    name of the child attribute in the link
     * @see #linkName
     * @see #parentAttrName
     * @see #childTypeName
     * @see #childAttrName
     */
    public Link(final String _linkName,
         final String _parentAttrName,
         final String _childTypeName, final String _childAttrName)  {
      this.linkName = _linkName;
      this.parentAttrName = _parentAttrName;
      this.childTypeName = _childTypeName;
      this.childAttrName = _childAttrName;
    }
  }

  /**
   *
   */
  protected abstract static class DefinitionAbstract  {
    
    /**
     * Name of the application for which this definition is defined.
     *
     * @see #setVersion
     */
    private String application = null;
     
    /**
     * Number of the global version of the application.
     *
     * @see #setVersion
     */
    private long globalVersion = 0;

    /**
     * Text of the local version of this definition.
     *
     * @see #setVersion
     */
    private String localVersion = null;

    /**
     * 
     *
     * @see #setVersion
     */
    private String mode = null;

    /**
     * Name of the access set for which this definition is defined.
     */
    private final Map < String, String > values 
        = new HashMap < String, String > ();

    /**
     *
     */
    private final Map < Link, Map < String, Map < String, String > > > links 
        = new HashMap < Link, Map < String, Map < String, String > > >();

    /**
     *
     */
    public void updateInDB(final Instance _instance,
                           final Set < Link > _allLinkTypes,
                           final Insert _insert) throws EFapsException, Exception  {
      Context context = Context.getThreadContext();
      Instance instance = _instance;

      if (_insert != null)  {
        _insert.add(context, "Revision", this.globalVersion  
                                         + "#" + this.localVersion);
        if (this.values.get("Name") == null)  {
          _insert.add(context, "Name", "-");
        }
        for (Map.Entry < String, String > entry : this.values.entrySet())  {
          _insert.add(context, entry.getKey(), entry.getValue());
        }
        _insert.executeWithoutAccessCheck();
        instance = _insert.getInstance();
      } else  {
        Update update = new Update(context, _instance);
        update.add(context, "Revision", this.globalVersion  
                                        + "#" + this.localVersion);
        for (Map.Entry < String, String > entry : this.values.entrySet())  {
          update.add(context, entry.getKey(), entry.getValue());
        }
        update.executeWithoutAccessCheck();
      }
      if (_allLinkTypes != null)  {
        for (Link linkType : _allLinkTypes)  {
          setLinksInDB(instance, linkType, this.links.get(linkType));
        }
      }
    }

    /**
     * Sets the links from this object to the given list of objects (with the 
     * object name) in the eFaps database.
     *
     * @param _instance   instance for which the access types must be set
     * @param _linkType   link to update
     * @param _objNames   string list of all object names to set for this 
     *                    object
     */
    protected void setLinksInDB(
                      final Instance _instance,
                      final Link _linkType,
                      final Map < String, Map < String, String > > _links)  
                                              throws EFapsException,Exception  {
                                                
      Context context = Context.getThreadContext();
      
      // get ids from current object
      Map < Long, String > currents = new HashMap < Long, String > ();
      SearchQuery query = new SearchQuery();
      query.setExpand(_instance, 
                      _linkType.linkName + "\\" + _linkType.parentAttrName);
      query.addSelect(_linkType.childAttrName + ".ID");
      query.addSelect("OID");
      query.addSelect(_linkType.childAttrName + ".Type");
      query.executeWithoutAccessCheck();
      while (query.next())  {
        Type type = (Type) query.get(_linkType.childAttrName + ".Type");
        if (_linkType.childTypeName.equals(type.getName()))  {
          currents.put((Long) query.get(_linkType.childAttrName + ".ID"),
                     (String) query.get("OID"));
        }
      }
      query.close();
  
      // get ids for target
      Map < Long, Map < String, String > > targets 
                              = new HashMap < Long, Map < String, String > > ();
      if (_links != null)  {
        for (Map.Entry < String, Map < String, String > > linkEntry 
                                                          : _links.entrySet())  {
          query = new SearchQuery();
          query.setQueryTypes(_linkType.childTypeName);
          query.addWhereExprEqValue("Name", linkEntry.getKey());
          query.addSelect("ID");
          query.executeWithoutAccessCheck();
          if (query.next())  {
            targets.put((Long) query.get("ID"), linkEntry.getValue());
          } else  {
  System.out.println(_linkType.childTypeName + " '" + linkEntry.getKey() + "' not found!");
          }
          query.close();
        }
      }
   
      // insert needed new links and update already existing
      for (Map.Entry < Long, Map < String, String > > target 
                                                        : targets.entrySet())  {
        if (currents.get(target.getKey()) == null)  {
          Insert insert = new Insert(context, _linkType.linkName);
          insert.add(context, _linkType.parentAttrName, "" + _instance.getId());
          insert.add(context, _linkType.childAttrName, "" + target.getKey());
          if (target.getValue() != null)  {
            for (Map.Entry < String, String > value 
                                                : target.getValue().entrySet())  {
              insert.add(context, value.getKey(), value.getValue());
            }
          }
          insert.executeWithoutAccessCheck();
        } else  {
          if (target.getValue() != null)  {
            Update update = new Update(context, currents.get(target.getKey()));
            for (Map.Entry < String, String > value 
                                                : target.getValue().entrySet())  {
              update.add(context, value.getKey(), value.getValue());
            }
            update.executeWithoutAccessCheck();
          }
          currents.remove(target.getKey());
        }
      }
  
      // remove unneeded current links to access types
      for (String oid : currents.values())  {   
        Delete del = new Delete(oid);
        del.executeWithoutAccessCheck();
      }
    }

    /**
     * The version information of this defintion is set.
     *
     * @param _application    name of the application for which the version is 
     *                        defined
     * @param _globalVersion  global version
     */
    public void setVersion(final String _application, 
                           final String _globalVersion,
                           final String _localVersion,
                           final String _mode)  {
      this.application = _application;
      this.globalVersion = Long.valueOf(_globalVersion);
      this.localVersion = _localVersion;
      this.mode = _mode;
    }
    
    /**
     * @param _link   link type
     * @param _name   name of the object which is linked to
     * @param _values values in the link itself (or null)
     */
    protected void addLink(final Link _link, 
                           final String _name, 
                           final Map < String, String > _values)  {
      Map < String, Map < String, String > > oneLink = this.links.get(_link);
      if (oneLink == null)  {
        oneLink = new HashMap < String, Map < String, String > > ();
        this.links.put(_link, oneLink);
      }
      oneLink.put(_name, _values);
    }

    /**
     * @param _link   link type
     * @param _name   name of the object which is linked to
     * @param _values values in the link itself (or null)
     */
    protected void addLink(final Link _link, 
                           final String _name, 
                           final String... _values)  {
      Map < String, String > valuesMap = null;
      if (_values.length > 0)  {
        valuesMap = new HashMap < String, String > ();
        for (int i = 0; i < _values.length; i+=2)  {
          valuesMap.put(_values[i], _values[i+1]);
        }
      }
      addLink(_link, _name, valuesMap);
    }
    
    /**
     * @param _name   name of the attribute
     * @param _value  value of the attribute
     * @see #values
     */
    protected void addValue(final String _name, final String _value)  {
      this.values.put(_name, _value);
    }
    
    /**
     * @param _name   name of the attribtue
     * @return value of the set attribute value in this definition
     * @see #values
     */
    protected String getValue(final String _name)  {
      return this.values.get(_name);
    }

    /**
     * 
     * @see #values
     */
    public void setName(final String _name)  {
      addValue("Name", _name);
    }
    
    /**
     * Returns a string representation with values of all instance variables
     * of a definition.
     *
     * @return string representation of this definition of an access type 
     *         update
     */
    public String toString()  {
      return new ToStringBuilder(this)
        .append("application",     this.application)
        .append("global version",  this.globalVersion)
        .append("local version",   this.localVersion)
        .append("mode",            this.mode)
        .append("values",          this.values)
        .append("links",           this.links)
       .toString();
    }
  }
}
