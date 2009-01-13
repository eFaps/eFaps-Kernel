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

package org.efaps.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.efaps.eclipse.EfapsPlugin;

/**
 * Eclipse Handler called from the connect command used to connect to the eFaps
 * data base and to reload the eFaps cache..
 *
 * @author tmo
 * @version $Id$
 */
public class ConnectHandler extends AbstractHandler {

  /**
   * Calls the eFaps connect method.
   *
   * @param _event  execution event
   * @see EfapsPlugin#connect()     called method to connect to eFaps data base
   * @see EfapsPlugin#reloadCache() called method to reload the eFaps cache
   * @throws ExecutionException on error
   * @return null
   */
  public Object execute(final ExecutionEvent _event) throws ExecutionException {
    final boolean connected = EfapsPlugin.getDefault().connect()
                              && EfapsPlugin.getDefault().reloadCache();
    if (connected)  {
      EfapsPlugin.getDefault().showInfo(_event,
                                        getClass(),
                                        "execute.connected");
    } else  {
      EfapsPlugin.getDefault().showError(_event,
                                         getClass(),
                                         "execute.failed");
    }
    return null;
  }
}
