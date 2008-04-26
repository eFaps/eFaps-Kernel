/*
 * Copyright 2003-2008 The eFaps Team
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

import org.efaps.admin.AbstractAdminObject;

/**
 * @author tmo
 * @version $Id$
 */
public abstract class DataModelObject extends AbstractAdminObject  {

  /**
   * Constructor to set the id and name of the data model object.
   *
   * @param _id         id to set
   * @param _uuid       universal unique identifier
   * @param _name name  to set
   */
  protected DataModelObject(final long _id,
                            final String _uuid,
                            final String _name)  {
    super(_id, _uuid, _name);
  }
}