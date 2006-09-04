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

  /**
   * @todo remove Exception
   * @todo description
   */
  public abstract void doMethod() throws EFapsException,Exception;

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
    Context.newThreadContext(getTransactionManager().getTransaction());
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
}
