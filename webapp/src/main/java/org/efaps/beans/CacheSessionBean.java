/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.beans;

import java.util.Hashtable;
import java.util.Map;

import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Context;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class CacheSessionBean  {

  public CacheSessionBean()  {
  }


  public void put(final String _key, final Object _object)  {
    getCache().put(_key, _object);
  }

  public Object get(final String _key)  {
    return getCache().get(_key);
  }

  /**
   * The instance method returns for the given key and for the given command
   * name the table bean.
   *
   * @param _key          key for which the table bean must be returned
   * @param _commandName  name of the command for which the bean must be
   *                      created
   * @see #getCommand
   */
  public TableBean getTableBean(final String _key, 
                                final String _commandName) throws Exception  {
    TableBean tableBean = (TableBean)get(_key);
    if (tableBean==null)  {

      CommandAbstract command = getCommand(_commandName);
      if (command!=null && command.getTargetTableBean()!=null)  {
        tableBean = (TableBean)command.getTargetTableBean().newInstance();
      } else  {
        tableBean = new TableBean();
      }
      getCache().put(_key, tableBean);
    }
    return tableBean;
  }

  /**
   * The instance method returns for the given key and for the given command
   * name the form bean.
   *
   * @param _key          key for which the form bean must be returned
   * @param _commandName  name of the command for which the bean must be
   *                      created
   * @see #getCommand
   */
  public FormBean getFormBean(final String _commandName) throws Exception  {
    FormBean formBean = null;
    CommandAbstract command = getCommand(_commandName);
    if (command!=null && command.getTargetFormBean()!=null)  {
      formBean = (FormBean)command.getTargetFormBean().newInstance();
    } else  {
      formBean = new FormBean();
    }
    return formBean;
  }

  /**
   * The method returns for the given command name the command or menu java
   * instance.
   *
   * @param _commandName  name of the command
   * @see #getTableBean
   */
  private CommandAbstract getCommand(final String _commandName)  {
    CommandAbstract ret = null;

    ret = Command.get(_commandName);
    if (ret == null)  {
      ret = Menu.get(_commandName);
    }
    return ret;
  }


  public void remove(final String _key)  {
    getCache().remove(_key);
  }


  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable is the store of the cache for the web form / table
   * beans.
   *
   * @see #getCache
   */
  private Map<String,Object> cache = new Hashtable<String,Object>();

  /////////////////////////////////////////////////////////////////////////////

  /**
  /**
   * This is the getter method for the instance variable {@link #cache}.
   *
   * @return value of instance variable {@link #cache}
   * @see #cache
   */
  private Map<String,Object> getCache()  {
    return this.cache;
  }
}
