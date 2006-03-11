/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.admin.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 *
 */
public class Form extends Collection  {

  /**
   * The static variable defines the class name in eFaps.
   */
  static public EFapsClassName EFAPS_CLASSNAME = EFapsClassName.FORM;

  /**
   *
   */
  public Form(Long _id, String _name)  {
    super(_id, _name);
  }

  /**
   * The instance  method returns the title of the form.
   *
   * @param _context  context for this request
   * @return title of the form
   */
  public String getViewableName(Context _context)  {
    String title = "";
    ResourceBundle msgs = ResourceBundle.getBundle("org.efaps.properties.AttributeRessource", _context.getLocale());
    try  {
      title = msgs.getString("Form.Title."+getName());
    } catch (MissingResourceException e)  {
    }
    return title;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Form}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Form}
   * @see #getCache
   */
  static public Form get(Context _context, long _id) throws EFapsException  {
    Form form = (Form)getCache().get(_id);
    if (form == null)  {
      form = getCache().read(_context, _id);
    }
    return form;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Form}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Form}
   * @see #getCache
   */
  static public Form get(Context _context, String _name) throws EFapsException  {
    Form form = (Form)getCache().get(_name);
    if (form == null)  {
      form = getCache().read(_context, _name);
    }
    return form;
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static UserInterfaceObjectCache<Form> getCache()  {
    return cache;
  }

  /**
   * Stores all instances of class {@link Form}.
   *
   * @see #getCache
   */
  static private UserInterfaceObjectCache<Form> cache = new UserInterfaceObjectCache<Form>(Form.class);
}
