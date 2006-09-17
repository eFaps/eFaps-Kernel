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
 * @todo description
 */
public class AccessSetUpdate extends AbstractUpdate  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(AccessSetUpdate.class);

  /** Link to access types. */
  private final static Link LINK2ACCESSTYPE
                    = new Link("Admin_Access_AccessSet2Type", 
                               "AccessSetLink", 
                               "Admin_Access_AccessType", "AccessTypeLink");

  /** Link to data model types. */
  private final static Link LINK2DATAMODELTYPE
                    = new Link("Admin_Access_AccessSet2DataModelType", 
                               "AccessSetLink", 
                               "Admin_DataModel_Type", "DataModelTypeLink");

  /** Link to persons. */
  private final static Link LINK2PERSON
                    = new Link("Admin_Access_AccessSet2UserAbstract", 
                               "AccessSetLink", 
                               "Admin_User_Person", "UserAbstractLink");

  /** Link to roles. */
  private final static Link LINK2ROLE
                    = new Link("Admin_Access_AccessSet2UserAbstract", 
                               "AccessSetLink", 
                               "Admin_User_Role", "UserAbstractLink");

  /** Link to groups. */
  private final static Link LINK2GROUP
                    = new Link("Admin_Access_AccessSet2UserAbstract", 
                               "AccessSetLink", 
                               "Admin_User_Group", "UserAbstractLink");

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
      setLinksInDB(instance, LINK2ACCESSTYPE,    def.accessTypes);
      setLinksInDB(instance, LINK2DATAMODELTYPE, def.dataModelTypes);
      setLinksInDB(instance, LINK2PERSON,        def.persons);
      setLinksInDB(instance, LINK2ROLE,          def.roles);
      setLinksInDB(instance, LINK2GROUP,         def.groups);
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
