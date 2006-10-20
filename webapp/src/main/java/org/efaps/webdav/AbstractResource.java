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

package org.efaps.webdav;

import java.util.Date;

import org.efaps.db.Instance;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public abstract class AbstractResource  {
  
  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * WebDAV implementation to get information for this resource.
   */
  private final WebDAVInterface webDAVImpl;

  /**
   * Name of the resource.
   *
   * @see #getName
   */
  private final String name;

  /**
   * The instance represented from this resource is stored in this instance 
   * variable.
   *
   * @see #getInstance
   */
  private final Instance instance;

  /**
   * When is the resource created.
   *
   * @see #getCreated
   */
  private final Date created;

  /**
   * When is the resource last modified.
   *
   * @see #getModified
   */
  private final Date modified;

  /**
   * Description / display name of the resource.
   *
   * @see #getDescription
   */
  private final String description;

  /////////////////////////////////////////////////////////////////////////////
  // constructor / desctructors
  
  protected AbstractResource(final WebDAVInterface _webDAVImpl,
                             final String _name,
                             final Instance _instance,
                             final Date _created,
                             final Date _modified,
                             final String _description)  {
    this.webDAVImpl = _webDAVImpl;
    this.name = _name;
    this.instance = _instance;
    this.created = _created;
    this.modified = _modified;
    this.description = _description;
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter / setter methods for instance variables

  /**
   * Deletes this resource.
   *
   * @return <i>true</i> if deleted, otherwise <i>false</i>
   */
  public abstract boolean delete();

  /////////////////////////////////////////////////////////////////////////////
  // getter / setter methods for instance variables

  /**
   * This is the getter method for instance variable {@table #webDAVImpl}.
   *
   * @return value of instance variable {@table #webDAVImpl}
   * @see #webDAVImpl
   */
  public WebDAVInterface getWebDAVImpl()  {
    return this.webDAVImpl;
  }

  /**
   * This is the getter method for instance variable {@table #name}.
   *
   * @return value of instance variable {@table #name}
   * @see #name
   */
  public String getName()  {
    return this.name;
  }

  /**
   * This is the getter method for instance variable {@table #instance}.
   *
   * @return value of instance variable {@table #instance}
   * @see #instance
   */
  public Instance getInstance()  {
    return this.instance;
  }

  /**
   * This is the getter method for instance variable {@table #created}.
   *
   * @return value of instance variable {@table #created}
   * @see #created
   */
  public Date getCreated()  {
    return this.created;
  }

  /**
   * This is the getter method for instance variable {@table #modified}.
   *
   * @return value of instance variable {@table #modified}
   * @see #modified
   */
  public Date getModified()  {
    return this.modified;
  }

  /**
   * This is the getter method for instance variable {@table #description}.
   *
   * @return value of instance variable {@table #description}
   * @see #description
   */
  public String getDescription()  {
    return this.description;
  }
}
