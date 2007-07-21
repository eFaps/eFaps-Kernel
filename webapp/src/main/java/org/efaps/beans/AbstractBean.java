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
 * Revision:        $Rev:1031 $
 * Last Changed:    $Date:2007-07-14 12:36:33 +0200 (Sa, 14 Jul 2007) $
 * Last Changed By: $Author:tmo $
 */

package org.efaps.beans;

import java.util.Locale;

import org.apache.commons.fileupload.FileItem;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public abstract class AbstractBean  {

  public AbstractBean() throws EFapsException {
    String oid = getParameter("oid");
    if (oid != null) {
      this.instance = new Instance(oid);
    }
  }

  public void finalize() {
  }

  /**
   * The instance method sets the object id for this bean. To set the object id
   * means to set the instance for this bean.
   * 
   * @param _oid
   *          object id
   * @see #instance
   */
  public void setOid(String _oid) throws EFapsException {
    if (_oid != null && _oid.length() > 0) {
      setInstance(new Instance(_oid));
    }
  }

  /**
   * @return value for given parameter
   * @todo description
   */
  public String getParameter(String _name) throws EFapsException {
    String ret = null;

    String[] values = Context.getThreadContext().getParameters().get(_name);
    if (values != null) {
      ret = values[0];
    }
    return ret;
  }

  /**
   * @todo description
   */
  public FileItem getFileParameter(String _name) throws EFapsException {
    FileItem fileItem = null;
    Context context = Context.getThreadContext();
    if (context.getFileParameters() != null) {
      fileItem = context.getFileParameters().get(_name);
    }
    return fileItem;
  }


  /**
   * For given name of command / menu, the related command and menu Java
   * instance is searched and, if found, returned.
   *
   * @param _name name of searched command object
   * @return found command / menu instance, or <code>null</null> if not found
   */
  public CommandAbstract getCommand(final String _name) throws EFapsException  {
    CommandAbstract cmd = Command.get(_name);
    if (cmd == null)  {
      cmd = Menu.get(_name);
    }
    return cmd;
  }


  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the instance object for which this bean is
   * created.
   * 
   * @see #getInstance
   * @see #setInstance
   */
  private Instance            instance    = null;

  /**
   * The instance variable is the flag if this class instance is already
   * initialised.
   * 
   * @see #isInitialised
   * @see #setInitialised
   */
  private boolean             initialised = false;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the instance variable {@link #instance}.
   * 
   * @return value of instance variable {@link #instance}
   * @see #instance
   * @see #setInstance
   */
  public Instance getInstance() {
    return this.instance;
  }

  /**
   * This is the setter method for the instance variable {@link #instance}.
   * 
   * @param _instance
   *          new value for instance variable {@link #instance}
   * @see #instance
   * @see #getInstance
   */
  protected void setInstance(Instance _instance) {
    this.instance = _instance;
  }

  /**
   * This is the getter method for the initialised variable {@link #initialised}.
   * 
   * @return value of initialised variable {@link #initialised}
   * @see #initialised
   * @see #setInitialised
   */
  public boolean isInitialised() {
    return this.initialised;
  }

  /**
   * This is the setter method for the initialised variable {@link #initialised}.
   * 
   * @param _initialised
   *          new value for initialised variable {@link #initialised}
   * @see #initialised
   * @see #isInitialised
   */
  public void setInitialised(boolean _initialised) {
    this.initialised = _initialised;
  }
}
