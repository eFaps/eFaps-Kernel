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

package org.efaps.shell.method;

import javax.transaction.TransactionManager;

import org.efaps.db.Cache;
import org.efaps.db.Context;
import org.efaps.js.Shell;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public abstract class AbstractMethod  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the login name of the current user.
   */
  private String userName = null;

  /**
   * All arguments given to the shell after defining the method are stored
   * in this instance variable. The value is automatically set by the shell.
   */
  private String[] arguments = null;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * @todo remove Exception
   * @todo description
   */
  public void execute() throws EFapsException,Exception  {
    doMethod();
  }

  /**
   * @todo remove Exception
   * @todo description
   */
  protected abstract void doMethod() throws EFapsException,Exception;

  /**
   * @todo remove Exception
   * @todo description
   */
  protected void reloadCache() throws EFapsException,Exception  {
    startTransaction();
    Cache.reloadCache(Context.getThreadContext());
    abortTransaction();
  }
  
  /**
   * @todo remove Exception
   * @todo description
   */
  protected void startTransaction() throws EFapsException,Exception  {
    getTransactionManager().begin();
    Context.newThreadContext(getTransactionManager().getTransaction(),
                             this.userName);
  }
  
  /**
   * @todo remove Exception
   * @todo description
   */
  protected void abortTransaction() throws EFapsException,Exception  {
    getTransactionManager().rollback();
    Context.getThreadContext().close();
  }

  /**
   * @todo remove Exception
   * @todo description
   */
  protected void commitTransaction() throws EFapsException,Exception  {
    getTransactionManager().commit();
    Context.getThreadContext().close();
  }

  /**
   * @todo description
   */
  protected TransactionManager getTransactionManager()  {
    return Shell.transactionManager;
  }

  /**
   * The user with given user name and password makes a login.
   *
   * @param _userName   name of user who wants to make a login
   * @param _password   password of the user used to check
   * @throws EFapsException if the user could not login
   * @see #userName
   * @todo real login with check of password
   */
  protected void login(final String _userName, 
                       final String _password) throws EFapsException {
    this.userName = _userName;
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter instance methods

  /**
   * This is the getter method for instance variable {@link #arguments}.
   *
   * @return value of instance variable {@link #arguments}
   * @see #setArguments
   * @see #arguments
   */
  public String[] getArguments()  {
    return this.arguments;
  }

  /**
   * This is the setter method for instance variable {@link #arguments}.
   *
   * @param _args new value for instance variable {@link #arguments}
   * @see #getArguments
   * @see #arguments
   */
  public void setArguments(final String[] _arguments)  {
    this.arguments = _arguments;
  }
}
