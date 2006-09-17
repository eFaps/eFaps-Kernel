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
  private String dataModelType;

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

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  protected AbstractUpdate(final String _dataModelType)  {
    this.dataModelType = _dataModelType;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The instance method returns the eFaps instance representing the read XML
   * configuration. If not already get from the eFaps databasse, the 
   * information is read. If no instance exists in the database, a new one
   * is automatically created.
   *
   * @return eFaps instance
   * @see #instance
   * @see #readInstanceFromDB
   * @see #createInstanceInDB
   * @todo remove throwing of Exception
   */
  protected Instance getInstance() throws EFapsException,Exception  {
    if (this.instance == null)  {
      readInstanceFromDB();
      if (this.instance == null)  {
        createInstanceInDB();
      }
    }
    return this.instance;
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
    query.setQueryTypes(context, this.dataModelType);
    query.addWhereExprEqValue(context, "UUID", this.uuid);
    query.addSelect(context, "OID");
    query.executeWithoutAccessCheck();
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
    Insert insert = new Insert(context, this.dataModelType);
    insert.add(context, "Name", this.uuid);
    insert.add(context, "UUID", this.uuid);
    insert.executeWithoutAccessCheck();
    this.instance = insert.getInstance();
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
                              final Link _linkType,
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
      Delete del = new Delete(oid);
      del.executeWithoutAccessCheck();
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
    Link(final String _linkName,
         final String _parentAttrName,
         final String _childTypeName, final String _childAttrName)  {
      this.linkName = _linkName;
      this.parentAttrName = _parentAttrName;
      this.childTypeName = _childTypeName;
      this.childAttrName = _childAttrName;
    }
  }
}
