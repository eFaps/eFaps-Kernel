/*
 * Copyright 2003-2007 The eFaps Team
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

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

import org.efaps.db.Context;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Form extends AbstractCollection {
  /**
   * The static variable defines the class name in eFaps.
   */
  public final static EFapsClassName EFAPS_CLASSNAME = EFapsClassName.FORM;
  /**
   *
   */
  public Form(final Long _id, final String _uuid, final String _name) {
    super(_id, _uuid, _name);
  }

  /**
   * The instance method returns the title of the form.
   *
   * @param _context
   *                context for this request
   * @return title of the form
   */
  public String getViewableName(final Context _context) {
    String title = "";
    final ResourceBundle msgs =
        ResourceBundle.getBundle("org.efaps.properties.AttributeRessource",
            _context.getLocale());
    try {
      title = msgs.getString("Form.Title." + getName());
    } catch (MissingResourceException e) {
    }
    return title;
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Form}.
   *
   * @param _id
   *                id to search in the cache
   * @return instance of class {@link Form}
   * @see #getCache
   */
  static public Form get(final long _id) {
    return getCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Form}.
   *
   * @param _name
   *                name to search in the cache
   * @return instance of class {@link Form}
   * @see #getCache
   */
  static public Form get(final String _name) {
    return getCache().get(_name);
  }

  /**
   * Returns for given parameter <i>_uuid</i> the instance of class
   * {@link Form}.
   *
   * @param _uuid
   *                UUID to search in the cache
   * @return instance of class {@link Form}
   * @see #getCache
   */
  static public Form get(final UUID _uuid) {
    return getCache().get(_uuid);
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static UserInterfaceObjectCache<Form> getCache() {
    return cache;
  }

  /**
   * Stores all instances of class {@link Form}.
   *
   * @see #getCache
   */
  static private UserInterfaceObjectCache<Form> cache =
      new UserInterfaceObjectCache<Form>(Form.class);
}
