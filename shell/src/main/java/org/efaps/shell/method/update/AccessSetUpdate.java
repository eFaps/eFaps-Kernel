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
 */
public class AccessSetUpdate  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(AccessSetUpdate.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The univeral unique identifier of the object is stored in this instance
   * variable.
   *
   * @see #setUUID
   */
  private String uuid = null;
  
  /**
   * The instance of the object in the eFaps database is stored in this 
   * instance variable..
   */
  private Instance instance = null;

  /**
   * All definitions of versions are added to this list.
   */
  private List < Definition > definitions = new ArrayList < Definition > ();
 
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

    readInstanceFromDB();
    if (this.instance == null)  {
      createInstanceInDB();
    }
    for (Definition def : this.definitions)  {
      Update update = new Update(context, this.instance);
      update.add(context, "Name", def.name);
      update.add(context, "Revision", def.globalVersion 
                                      + "#" + def.localVersion);
      update.execute(context);
      setAccessTypesInDB(def.accessTypes);
    }
  }

  /**
   * Sets for this access set in the eFaps database the given access types. 
   *
   * @param _accessType string list of all access types to set for this 
   *                    access set
   */
  protected void setAccessTypesInDB(final List < String > _accessTypes)  
                                            throws EFapsException,Exception  {
                                              
    Context context = Context.getThreadContext();
    
    // get ids from current access types
    Map < Long, String > currents = new HashMap < Long, String > ();
    SearchQuery query = new SearchQuery();
    query.setExpand(context, 
                    this.instance, 
                    "Admin_Access_AccessSet2Type\\AccessSetLink");
    query.addSelect(context, "AccessTypeLink.ID");
    query.addSelect(context, "OID");
    query.execute(context);
    while (query.next())  {
      currents.put((Long) query.get(context, "AccessTypeLink.ID"),
                   (String) query.get(context, "OID"));
    }
    query.close();

    // get ids for target access types
    Set < Long > targets = new HashSet < Long > ();
    for (String accessType : _accessTypes)  {
      query = new SearchQuery();
      query.setQueryTypes(context, "Admin_LifeCycle_AccessType");
      query.addWhereExprEqValue(context, "Name", accessType);
      query.addSelect(context, "ID");
      query.execute(context);
      if (query.next())  {
        targets.add((Long) query.get(context, "ID"));
        
      } else  {
System.out.println("Access Type '" + accessType + "' not found!");
      }
      query.close();
    }

    // insert needed new links to access types
    for (Long target : targets)  {
      if (currents.get(target) == null)  {
        Insert insert = new Insert(context, "Admin_Access_AccessSet2Type");
        insert.add(context, "AccessSetLink", "" + this.instance.getId());
        insert.add(context, "AccessTypeLink", "" + target);
        insert.execute(context);
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

  /**
   * The method searchs for the given universal unique identifier in 
   * {@link #uuid} the instance in the eFaps database and stores the result
   * in {@link #instance}. If no object is found in eFaps, {@link #instance}
   * is set to <code>null</code>.
   *
   * @see #instance
   * @see #uuid
   * @todo remove throwing of Exception
   */
  protected void readInstanceFromDB() throws EFapsException,Exception  {
    Context context = Context.getThreadContext();
    SearchQuery query = new SearchQuery();
    query.setQueryTypes(context, "Admin_Access_AccessSet");
    query.addWhereExprEqValue(context, "UUID", this.uuid);
    query.addSelect(context, "OID");
    query.execute(context);
    if (query.next())  {
      this.instance = new Instance(context, 
                                   (String) query.get(context, "OID"));
    } else  {
      this.instance = null;
    }
    query.close();
  }
  
  /**
   * A new instance is created in the eFaps db for given univeral unique 
   * identifier in {@link #uuid}. The name of the access set is also the
   * universal unique identifier, because the name of access set is first 
   * updates in the version definition.<br/>
   * The new created object is stored as instance information in 
   * {@link #instance}.
   *
   * @see #uuid
   * @see #instance
   * @todo remove throwing of Exception
   */
  protected void createInstanceInDB() throws EFapsException, Exception  {
    Context context = Context.getThreadContext();
    Insert insert = new Insert(context, "Admin_Access_AccessSet");
    insert.add(context, "Name", this.uuid);
    insert.add(context, "UUID", this.uuid);
    insert.execute(context);
    this.instance = insert.getInstance();
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
   *
   */
  public String toString()  {
    return new ToStringBuilder(this).
      append("uuid",            this.uuid).
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
    private List < String > accessTypes = new ArrayList < String > ();

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
     * @see #accessTypes
     */
    public void addAccessType(final String _accessType)  {
      this.accessTypes.add(_accessType);
    }

    /**
     * 
     * @see #name
     */
    public void setName(final String _name)  {
      this.name = _name;
    }

    /**
     *
     */
    public String toString()  {
      return new ToStringBuilder(this).
        append("application",     this.application).
        append("global version",  this.globalVersion).
        append("local version",   this.localVersion).
        append("mode",            this.mode).
        append("name",            this.name).
        append("access types",    this.accessTypes).
        toString();
    }
  }
}
