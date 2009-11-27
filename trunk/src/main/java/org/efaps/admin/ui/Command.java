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

import static org.efaps.admin.EFapsClassNames.COMMAND;

import java.util.UUID;

import org.efaps.admin.EFapsClassNames;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Command extends AbstractCommand {

  /**
   * The static variable defines the class name in eFaps.
   */
  public static final EFapsClassNames EFAPS_CLASSNAME = COMMAND;

  private static final CommandCache CACHE = new CommandCache();

  /**
   * Constructor to set the id and name of the command object.
   *
   * @param _id
   *                id of the command to set
   * @param _name
   *                name of the command to set
   */
  public Command(final Long _id, final String _uuid, final String _name) {
    super(_id, _uuid, _name);
  }


  /**
   * Static getter method for the type hashtable {@link #CACHE}.
   *
   * @return value of static variable {@link #CACHE}
   */
  protected static UserInterfaceObjectCache<Command> getCache() {
    return CACHE;
  }
  /**
   * Returns for given parameter <i>UUID</i> the instance of class
   * {@link Command}.
   *
   * @param _uuid
   *                UUID to search in the cache
   * @return instance of class {@link Command}
   * @see #getCache
   */
  public static Command get(final UUID _uuid) {
    return CACHE.get(_uuid);
  }
  /**
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link Command}.
   *
   * @param _id
   *                id to search in the cache
   * @return instance of class {@link Command}
   * @throws CacheReloadException
   * @see #getCache
   */
  static public Command get(final long _id)  {
    return CACHE.get(_id);
  }



  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Command}.
   *
   * @param _name
   *                name to search in the cache
   * @return instance of class {@link Command}
   * @throws CacheReloadException
   * @see #getCache
   */
  static public Command get(final String _name) {
    return CACHE.get(_name);
  }

  private static class CommandCache extends UserInterfaceObjectCache<Command> {

    protected CommandCache() {
      super(Command.class);
    }
  }
}
