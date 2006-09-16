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
public class AccessSetUpdate extends AbstractUpdate  {

  /////////////////////////////////////////////////////////////////////////////
  // enums
  
  /**
   * The enum is used to define the links with all information needed to update
   * the link information between this access set and the related objects.
   */
  private enum Links  {
    /** Link to access types. */
    AccessTypes("Admin_Access_AccessSet2Type", 
                "AccessSetLink", 
                "Admin_Access_AccessType", "AccessTypeLink"),
    /** Link to data model types. */
    DataModelTypes("Admin_Access_AccessSet2DataModelType", 
                   "AccessSetLink", 
                   "Admin_DataModel_Type", "DataModelTypeLink"),
    /** Link to persons. */
    Persons("Admin_Access_AccessSet2UserAbstract", 
            "AccessSetLink", 
            "Admin_User_Person", "UserAbstractLink"),
    /** Link to roles. */
    Roles("Admin_Access_AccessSet2UserAbstract", 
          "AccessSetLink", 
          "Admin_User_Role", "UserAbstractLink"),
    /** Link to groups. */
    Groups("Admin_Access_AccessSet2UserAbstract", 
           "AccessSetLink", 
           "Admin_User_Group", "UserAbstractLink");

    /** Name of the link. */
    final String linkName;
    /** Name of the parent attribute in the link. */
    final String parentAttrName;
    /** Name of the child type */
    final String childTypeName;
    /** Name of the child attribute in the link. */
    final String childAttrName;
    
    /** 
    */
    Links(final String _linkName,
          final String _parentAttrName,
          final String _childTypeName, final String _childAttrName)  {
      this.linkName = _linkName;
      this.parentAttrName = _parentAttrName;
      this.childTypeName = _childTypeName;
      this.childAttrName = _childAttrName;
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(AccessSetUpdate.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * All definitions of versions are added to this list.
   */
  private List < Definition > definitions = new ArrayList < Definition > ();
 
  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public AccessSetUpdate() {
    super("Admin_Access_AccessSet");
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
  public void addDefinition(final Definition _definition)  {
    this.definitions.add(_definition);
  }

  public void updateInDB() throws EFapsException,Exception {
    Context context = Context.getThreadContext();

    Instance instance = getInstance();
    for (Definition def : this.definitions)  {
      Update update = new Update(context, instance);
      update.add(context, "Name", def.name);
      update.add(context, "Revision", def.globalVersion 
                                      + "#" + def.localVersion);
      update.executeWithoutAccessCheck();
      setLinksInDB(instance, Links.AccessTypes, def.accessTypes);
      setLinksInDB(instance, Links.DataModelTypes, def.dataModelTypes);
      setLinksInDB(instance, Links.Persons, def.persons);
      setLinksInDB(instance, Links.Roles, def.roles);
      setLinksInDB(instance, Links.Groups, def.groups);
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
  protected void setLinksInDB(final Instance _instance,
                              final Links _linkType,
                              final List < String > _objNames)  
                                            throws EFapsException,Exception  {
                                              
    Context context = Context.getThreadContext();
    
    // get ids from current object
    Map < Long, String > currents = new HashMap < Long, String > ();
    SearchQuery query = new SearchQuery();
    query.setExpand(context, 
                    _instance, 
                    _linkType.linkName + "\\" + _linkType.parentAttrName);
    query.addSelect(context, _linkType.childAttrName + ".ID");
    query.addSelect(context, "OID");
    query.addSelect(context, _linkType.childAttrName + ".Type");
    query.executeWithoutAccessCheck();
    while (query.next())  {
      Type type = (Type) query.get(context, _linkType.childAttrName + ".Type");
      if (_linkType.childTypeName.equals(type.getName()))  {
        currents.put((Long) query.get(context, _linkType.childAttrName + ".ID"),
                   (String) query.get(context, "OID"));
      }
    }
    query.close();

    // get ids for target
    Set < Long > targets = new HashSet < Long > ();
    for (String objName : _objNames)  {
      query = new SearchQuery();
      query.setQueryTypes(context, _linkType.childTypeName);
      query.addWhereExprEqValue(context, "Name", objName);
      query.addSelect(context, "ID");
      query.executeWithoutAccessCheck();
      if (query.next())  {
        targets.add((Long) query.get(context, "ID"));
        
      } else  {
System.out.println(_linkType.childTypeName + " '" + objName + "' not found!");
      }
      query.close();
    }

    // insert needed new links
    for (Long target : targets)  {
      if (currents.get(target) == null)  {
        Insert insert = new Insert(context, _linkType.linkName);
        insert.add(context, _linkType.parentAttrName, "" + _instance.getId());
        insert.add(context, _linkType.childAttrName, "" + target);
        insert.executeWithoutAccessCheck();
      } else  {
        currents.remove(target);
      }
    }

    // remove unneeded current links to access types
    for (String oid : currents.values())  {   
      Delete del = new Delete(context, oid);
      del.execute(context);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * Returns a string representation with values of all instance variables.
   *
   * @return string representation of this access set update
   */
  public String toString()  {
    return new ToStringBuilder(this).
      appendSuper(super.toString()).
      append("definitions",     this.definitions).
      toString();
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  public static AccessSetUpdate readXMLFile(final String _fileName) throws IOException  {
//    } catch (IOException e)  {
//      LOG.error("could not open file '" + _fileName + "'", e);
    return readXMLFile(new File(_fileName));
  }

  public static AccessSetUpdate readXMLFile(final File _file) throws IOException  {
    AccessSetUpdate ret = null;

    try  {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("access-set", AccessSetUpdate.class);

      digester.addCallMethod("access-set/uuid", "setUUID", 1);
      digester.addCallParam("access-set/uuid", 0);

      digester.addObjectCreate("access-set/definition", Definition.class);
      digester.addSetNext("access-set/definition", "addDefinition");

      digester.addCallMethod("access-set/definition/version", "setVersion", 4);
      digester.addCallParam("access-set/definition/version/application", 0);
      digester.addCallParam("access-set/definition/version/global", 1);
      digester.addCallParam("access-set/definition/version/local", 2);
      digester.addCallParam("access-set/definition/version/mode", 3);
      
      digester.addCallMethod("access-set/definition/name", "setName", 1);
      digester.addCallParam("access-set/definition/name", 0);

      digester.addCallMethod("access-set/definition/access-type", "addAccessType", 1);
      digester.addCallParam("access-set/definition/access-type", 0);

      digester.addCallMethod("access-set/definition/type", "addDataModelType", 1);
      digester.addCallParam("access-set/definition/type", 0);

      digester.addCallMethod("access-set/definition/person", "addPerson", 1);
      digester.addCallParam("access-set/definition/person", 0);

      digester.addCallMethod("access-set/definition/role", "addRole", 1);
      digester.addCallParam("access-set/definition/role", 0);

      digester.addCallMethod("access-set/definition/group", "addGroup", 1);
      digester.addCallParam("access-set/definition/group", 0);

      ret = (AccessSetUpdate) digester.parse(_file);
    } catch (SAXException e)  {
e.printStackTrace();
      //      LOG.error("could not read file '" + _fileName + "'", e);
    }
System.out.println("ret="+ret);
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition  {
    
    ///////////////////////////////////////////////////////////////////////////
    // instance variables

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
    private String name = null;

    /**
     * List of all access type to which this access set is assigned to.
     */
    private final List < String > accessTypes = new ArrayList < String > ();

    /**
     * List of all data model types which are assigned to this access set.
     */
    private final List < String > dataModelTypes = new ArrayList < String > ();

    /**
     * List of all person which are assigned to this access set.
     */
    private final List < String > persons = new ArrayList < String > ();

    /**
     * List of all roles which are assigned to this access set.
     */
    private final List < String > roles = new ArrayList < String > ();

    /**
     * List of all groups which are assigned to this access set.
     */
    private final List < String > groups = new ArrayList < String > ();

    ///////////////////////////////////////////////////////////////////////////
    // instance methods

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
     * @param _accessType access type to add (defined with the name of the
     *                    access type)
     * @see #accessTypes
     */
    public void addAccessType(final String _accessType)  {
      this.accessTypes.add(_accessType);
    }

    /**
     * @param _dataModelType  data model type to add (defined with the name of
     *                        the data model type)
     * @see #dataModelTypes
     */
    public void addDataModelType(final String _dataModelType)  {
      this.dataModelTypes.add(_dataModelType);
    }

    /**
     * @param _person person to add (defined with the name of the person)
     * @see #persons
     */
    public void addPerson(final String _person)  {
      this.persons.add(_person);
    }

    /**
     * @param _role role to add (defined with the name of the role)
     * @see #roles
     */
    public void addRole(final String _role)  {
      this.roles.add(_role);
    }

    /**
     * @param _group group to add (defined with the name of the group)
     * @see #groups
     */
    public void addGroup(final String _group)  {
      this.groups.add(_group);
    }

    /**
     *
     * @param _name name of the access set (for this version definition)
     * @see #name
     */
    public void setName(final String _name)  {
      this.name = _name;
    }

    /**
     * Returns a string representation with values of all instance variables
     * of a definition.
     *
     * @return string representation of this definition of an access set update
     */
    public String toString()  {
      return new ToStringBuilder(this).
        append("application",     this.application).
        append("global version",  this.globalVersion).
        append("local version",   this.localVersion).
        append("mode",            this.mode).
        append("name",            this.name).
        append("access types",    this.accessTypes).
        append("persons",         this.persons).
        append("roles",           this.roles).
        append("groups",          this.groups).
        toString();
    }
  }
}
