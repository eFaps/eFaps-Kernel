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

package org.efaps.beans;

import java.util.Hashtable;
import java.util.Map;

import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Context;

public class CacheSessionBean  {

  public CacheSessionBean()  {
  }


  public void put(String _key, Object _object)  {
    getCache().put(_key, _object);
  }

  public Object get(String _key)  {
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
  public TableBean getTableBean(String _key, String _commandName) throws Exception  {
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
  public FormBean getFormBean(String _commandName) throws Exception  {
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
  private CommandAbstract getCommand(String _commandName) throws Exception  {
    CommandAbstract ret = null;
    Context context = new Context();
    try  {
      ret = Command.get(context, _commandName);
      if (ret==null)  {
        ret = Menu.get(context, _commandName);
      }
    } catch (Exception e)  {
      throw e;
    } finally  {
      try  {
        context.close();
      } catch (Exception e)  {
      }
    }
    return ret;
  }


  public void remove(String _key)  {
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
