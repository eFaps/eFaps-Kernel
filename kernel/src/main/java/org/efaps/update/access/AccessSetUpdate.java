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

package org.efaps.update.access;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

import org.efaps.update.AbstractUpdate;

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

  private final static Set <Link> ALLLINKS = new HashSet < Link > ();  {
    ALLLINKS.add(LINK2ACCESSTYPE);
    ALLLINKS.add(LINK2DATAMODELTYPE);
    ALLLINKS.add(LINK2PERSON);
    ALLLINKS.add(LINK2ROLE);
    ALLLINKS.add(LINK2GROUP);
  }

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public AccessSetUpdate() {
    super("Admin_Access_AccessSet", ALLLINKS);
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

  public static class Definition extends DefinitionAbstract {
    
    /**
     * @param _accessType access type to add (defined with the name of the
     *                    access type)
     * @see #accessTypes
     */
    public void addAccessType(final String _accessType)  {
      addLink(LINK2ACCESSTYPE, _accessType);
    }

    /**
     * @param _dataModelType  data model type to add (defined with the name of
     *                        the data model type)
     * @see #dataModelTypes
     */
    public void addDataModelType(final String _dataModelType)  {
      addLink(LINK2DATAMODELTYPE, _dataModelType);
    }

    /**
     * @param _person person to add (defined with the name of the person)
     * @see #persons
     */
    public void addPerson(final String _person)  {
      addLink(LINK2PERSON, _person);
    }

    /**
     * @param _role role to add (defined with the name of the role)
     * @see #roles
     */
    public void addRole(final String _role)  {
      addLink(LINK2ROLE, _role);
    }

    /**
     * @param _group group to add (defined with the name of the group)
     * @see #groups
     */
    public void addGroup(final String _group)  {
      addLink(LINK2GROUP, _group);
    }
  }
}
