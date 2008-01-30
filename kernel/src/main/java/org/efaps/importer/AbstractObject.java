/*
 * Copyright 2003 - 2008 The eFaps Team
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

package org.efaps.importer;

import java.util.Map;
import java.util.Set;

/**
 * Abstract Class for Importing Objects into the Database connected to eFaps
 *
 * @author jmox
 * @version $Id$
 *
 */
public abstract class AbstractObject {

  /**
   * get the Links of the Object
   *
   * @return Set with the ForeignObjects connected to this Object
   */
  public abstract Set<ForeignObject> getLinks();

  /**
   * get the Type of the Object
   *
   * @return the Type of the Object
   */
  public abstract String getType();

  /**
   * Get the Value of an Attribute
   *
   * @param _attribute
   *          Attribute of wich the Value will be returned
   * @return Object with the Value, null if Attribute das not exist
   */
  public abstract Object getAttribute(final String _attribute);

  /**
   * get the Map with all Attributes
   *
   * @return Map containing all Attributes
   */
  public abstract Map<String, Object> getAttributes();

  /**
   * get the ID of the Object
   *
   * @return Strng with the ID of the Object
   */
  public abstract String getID();

  /**
   * sets the ID of the Object
   *
   * @param _id
   */
  public abstract void setID(String _id);

  /**
   * get the Attribute wich contains the Parent-Child-Relation
   *
   * @return String with the Name of the Attribute
   */
  public abstract String getParrentAttribute();

  /**
   * has the Object a CheckinObject
   *
   * @return true if so, false if not
   */
  public abstract boolean isCheckinObject();

  /**
   * get the Attributes defined as Unique
   *
   * @return Set of the unique Attributes
   */
  public abstract Set<String> getUniqueAttributes();

  /**
   * has the Object Childs?
   *
   * @return true if the Object has Childs, otherwise false
   */
  public abstract boolean hasChilds();

  /**
   * Check the Object into the Database
   */
  public abstract void dbCheckObjectIn();

  /**
   * Method that executes the Insert into the Database
   */
  public abstract void dbAddChilds();

  /**
   * Method to Create the Update or Insert of the Datebase
   *
   * @param _parent
   *          Parent-Object of this Object
   * @param _ID
   *          Id of the Object to be updated, if "" is given a Insert will be
   *          made
   * @return String with the ID of the new or updated Object
   */
  public abstract String dbUpdateOrInsert(final AbstractObject _parent,
                                          final String _ID);
}