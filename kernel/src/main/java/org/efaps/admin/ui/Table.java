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

package org.efaps.admin.ui;

import static org.efaps.admin.EFapsClassNames.TABLE;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

import org.efaps.admin.EFapsClassNames;
import org.efaps.db.Context;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Table extends AbstractCollection implements Cloneable {

  /**
   * The static variable defines the class name in eFaps.
   */
  static public EFapsClassNames EFAPS_CLASSNAME = TABLE;

  /**
   * Stores all instances of class {@link Table}.
   *
   * @see #getCache
   */
  static private UserInterfaceObjectCache<Table> cache =
      new UserInterfaceObjectCache<Table>(Table.class);

  /**
   * This is the constructor to set the id and the name.
   *
   * @param _id     id of the new table
   * @param _name   name of the new table
   */
  public Table(final Long _id,
               final String _uuid,
               final String _name)
  {
    super(_id, _uuid, _name);
  }

  /**
   * The instance method returns the title of the table.
   *
   * @param _context    context for this request
   * @return title of the form
   */
  public String getViewableName(final Context _context)
  {
    String title = "";
    ResourceBundle msgs =
        ResourceBundle.getBundle("org.efaps.properties.AttributeRessource",
            _context.getLocale());
    try {
      title = msgs.getString("Table.Title." + getName());
    } catch (MissingResourceException e) {
    }
    return title;
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates and returns a copy of this table object.
   */
  public Table cloneTable()
  {
    Table ret = null;
    try {
      ret = (Table) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return ret;
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Table}.
   *
   * @param _id     id to search in the cache
   * @return instance of class {@link Table}
   * @see #getCache
   */
  static public Table get(final long _id)
  {
    return getCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Table}.
   *
   * @param _name   name to search in the cache
   * @return instance of class {@link Table}
   * @see #getCache
   */
  static public Table get(final String _name)
  {
    return getCache().get(_name);
  }

  /**
   * Returns for given parameter <i>UUID</i> the instance of class
   * {@link Table}.
   *
   * @param _uuid   UUID to search in the cache
   * @return instance of class {@link Table}
   * @see #getCache
   */
  public static Table get(final UUID _uuid)
  {
    return getCache().get(_uuid);
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  protected static UserInterfaceObjectCache<Table> getCache()
  {
    return cache;
  }
}
