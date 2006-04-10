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

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 *
 */
public class Command extends CommandAbstract  {

  /**
   * The static variable defines the class name in eFaps.
   */
  public static EFapsClassName EFAPS_CLASSNAME = EFapsClassName.COMMAND;

  /**
   * Constructor to set the id and name of the command object.
   *
   * @param _id   id  of the command to set
   * @param _name name of the command to set
   */
  public Command(Long _id, String _name)  {
    super(_id, _name);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Command}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Command}
   * @see #getCache
   */
  static public Command get(Context _context, long _id) throws EFapsException  {
    Command command = (Command)getCache().get(_id);
    if (command == null)  {
      command = getCache().read(_context, _id);
    }
    return command;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Command}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Command}
   * @see #getCache
   */
  static public Command get(Context _context, String _name) throws EFapsException  {
    Command command = (Command)getCache().get(_name);
    if (command == null)  {
      command = getCache().read(_context, _name);
    }
    return command;
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static UserInterfaceObjectCache<Command> getCache()  {
    return cache;
  }

  /**
   * Stores all instances of class {@link Command}.
   *
   * @see #getCache
   */
  static private UserInterfaceObjectCache<Command> cache = new UserInterfaceObjectCache<Command>(Command.class);
}
