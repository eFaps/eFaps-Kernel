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

package org.efaps.util.cache;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public interface CacheReloadInterface  {

  /**
   * The enum defines the priority numbers for the caches used to initialise
   * the kernel.
   */
  public static enum Priority  {
    SystemAttribute(50),
    JAASSystem(100),
    Role(200),
    Group(300),
    AttributeType(400),
    SQLTable(500),
    Type(600),
    Attribute(700),
    AccessType(800),
    AccessSet(900),
    EventDefinition(1000);

    /** Stores the priority number. */
    public final int number;

    private Priority(final int _number)  {
      this.number = _number;
    }
  }

  /**
   * Returns the priority number used to define when this cache is reloaded.
   * E.g. the cache for attribute types must be loaded before the cache of
   * types.
   *
   * @return priority number
   */
  public int priority();

  /**
   * The cache is reloaded or initialised.
   *
   * @throws CacheException if the cache could not be reloaed
   */
  public void reloadCache() throws CacheReloadException;
}
