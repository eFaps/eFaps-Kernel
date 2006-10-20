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
import java.util.List;

import org.efaps.db.Instance;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class CollectionResource extends AbstractResource  {
  
  /////////////////////////////////////////////////////////////////////////////
  // constructor / desctructors
  
  public CollectionResource(final WebDAVInterface _webDAVImpl,
                            final String _name,
                            final Instance _instance,
                            final Date _created,
                            final Date _modified,
                            final String _description)  {
    super(_webDAVImpl, _name, _instance, _created, _modified, _description);
  }
  
  /**
   * Deletes this collection resource.
   *
   * @return <i>true</i> if deleted, otherwise <i>false</i>
   */
  public boolean delete()  {
    return getWebDAVImpl().deleteCollection(this);
  }

  /**
   * @return sub collections and sources for this collection
   */
  public List < AbstractResource > getSubs()  {
    return getWebDAVImpl().getSubs(this);
  }

  public CollectionResource getCollection(final String _name)  {
    return getWebDAVImpl().getCollection(this, _name);
  }

  /**
   * @return <i>true</i> if created, otherwise <i>false</i>
   */
  public boolean createCollection(final String _name)  {
    return getWebDAVImpl().createCollection(this, _name);
  }


  public SourceResource getSource(final String _name)  {
    return getWebDAVImpl().getSource(this, _name);
  }

  public boolean createSource(final String _name)  {
    return getWebDAVImpl().createSource(this, _name);
  }

}
