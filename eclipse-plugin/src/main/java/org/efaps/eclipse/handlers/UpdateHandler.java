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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.handlers.HandlerUtil;

import org.efaps.eclipse.EfapsPlugin;

/**
 * Class is uded to call the efaps update method. Used for xml, css, js, and
 * java.
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public class UpdateHandler extends AbstractHandler {

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
    final IEditorPart activeEditor = HandlerUtil.getActiveEditor(_event);
    final Shell shell = HandlerUtil.getActiveShell(_event);

    if (activeEditor != null)  {
      final IEditorInput input = activeEditor.getEditorInput();
      if (input instanceof IFileEditorInput)  {
        final IFile file = ((IFileEditorInput) input).getFile();
        final IPath filePath = file.getLocation();
        final EfapsPlugin plugin = EfapsPlugin.getDefault();
        if (plugin.isInitialized()) {
          if (!EfapsPlugin.getDefault().update(filePath.toString(), shell))  {
            EfapsPlugin.getDefault().showError(_event,
                                               getClass(),
                                               "execute.failed");
          }
        } else {
          EfapsPlugin.getDefault().showError(_event,
                                            getClass(),
                                            "execute.notInitialized");
        }
      }
    }
    return null;
  }
}
