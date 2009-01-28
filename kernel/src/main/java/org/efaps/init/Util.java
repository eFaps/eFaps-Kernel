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

package org.efaps.init;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for the init package to startup database connections and
 * stores.
 *
 * @author tmo
 * @version $Id$
 */
public class Util
{

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(Util.class);

  /**
   * Bing given object at the given name path within given name context.
   *
   * @param _ctx      naming context
   * @param _nameStr  string with complete name path
   * @param _object   object to bind
   * @throws NamingException
   */
  public static void bind(final Context _ctx,
                          final String _nameStr,
                          final Object _object)
     throws NamingException
  {
    final Name names = _ctx.getNameParser("").parse(_nameStr);
    if (names.size() > 0)  {
      Context subCtx = _ctx;
      for (int idx = 0; idx < names.size() - 1; idx++)  {
        final String name = names.get(idx);
        try {
          subCtx = (Context) subCtx.lookup(name);
          if (LOG.isDebugEnabled())  {
            LOG.debug("Subcontext " + name + " already exists");
          }
        } catch (NameNotFoundException e) {
          subCtx = subCtx.createSubcontext(name);
          if (LOG.isDebugEnabled())  {
            LOG.debug("Subcontext " + name + " created");
          }
        }
      }
      subCtx.rebind(names.get(names.size() - 1), _object);
      if (LOG.isDebugEnabled())  {
        LOG.debug("Bound object to " + names.get(names.size() - 1));
      }
    }
  }
}
