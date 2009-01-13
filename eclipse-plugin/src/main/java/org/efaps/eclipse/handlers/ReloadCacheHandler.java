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
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.efaps.eclipse.EfapsPlugin;

/**
 * Class is used to call the eFaps reload cache method.
 *
 * @author tmo
 * @version $Id$
 */
public class ReloadCacheHandler  extends AbstractHandler {

  /**
   * The action has been activated. The argument of the method represents the
   * 'real' action sitting in the workbench UI.
   *
   * @see IWorkbenchWindowActionDelegate#run
   * @param _event  execution event
   * @throws ExecutionException on error
   * @return null
   */
  public Object execute(final ExecutionEvent _event)
      throws ExecutionException {
    final EfapsPlugin plugin = EfapsPlugin.getDefault();
    if (plugin.isInitialized()) {
      if (!plugin.reloadCache())  {
        EfapsPlugin.getDefault().showError(_event,
                                           getClass(),
                                           "execute.failed");
      }
    } else {
      EfapsPlugin.getDefault().showError(_event,
                                         getClass(),
                                         "execute.notInitialized");
    }
    return null;
  }
}
