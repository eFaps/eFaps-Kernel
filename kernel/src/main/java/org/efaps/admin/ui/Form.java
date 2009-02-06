/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.admin.ui;

import static org.efaps.admin.EFapsClassNames.FORM;

import java.util.UUID;

import org.efaps.admin.EFapsClassNames;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Form extends AbstractCollection {

  /**
   * The static variable defines the class name in eFaps.
   */
  public final static EFapsClassNames EFAPS_CLASSNAME = FORM;

  private static final FormCache CACHE = new FormCache();

  /**
   *
   */
  public Form(final Long _id, final String _uuid, final String _name) {
    super(_id, _uuid, _name);
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Form}.
   *
   * @param _id
   *                id to search in the cache
   * @return instance of class {@link Form}
   * @throws CacheReloadException
   * @see #getCache
   */
  public static Form get(final long _id) {
    return CACHE.get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Form}.
   *
   * @param _name
   *                name to search in the cache
   * @return instance of class {@link Form}
   * @throws CacheReloadException
   * @see #getCache
   */
  public static Form get(final String _name) {
    return CACHE.get(_name);
  }

  /**
   * Returns for given parameter <i>_uuid</i> the instance of class
   * {@link Form}.
   *
   * @param _uuid
   *                UUID to search in the cache
   * @return instance of class {@link Form}
   * @throws CacheReloadException
   * @see #getCache
   */
  public static Form get(final UUID _uuid)  {
    return CACHE.get(_uuid);
  }

  /**
   * Static getter method for the type hashtable {@link #CACHE}.
   *
   * @return value of static variable {@link #CACHE}
   */
  protected static UserInterfaceObjectCache<Form> getCache() {
    return CACHE;
  }

  private static class FormCache extends UserInterfaceObjectCache<Form> {

    protected FormCache() {
      super(Form.class);
    }
  }
}
