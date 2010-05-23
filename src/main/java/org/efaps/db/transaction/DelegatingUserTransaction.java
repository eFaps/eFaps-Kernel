/*
 * Copyright 2003 - 2010 The eFaps Team
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


package org.efaps.db.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;


/**
 * Delegate a UserTransaction to the TransactionManager used by the Context.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DelegatingUserTransaction
    implements UserTransaction
{

    private final TransactionManager transmanager;

    public DelegatingUserTransaction(final TransactionManager _manager)
    {
        this.transmanager = _manager;
    }

    /* (non-Javadoc)
     * @see javax.transaction.UserTransaction#begin()
     */
    @Override
    public void begin()
        throws NotSupportedException, SystemException
    {
        try {
            if (Context.isActive()) {
                if (this.transmanager.getStatus() == Status.STATUS_NO_TRANSACTION) {
                    this.transmanager.begin();
                }
            } else {
                Context.begin();
            }
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see javax.transaction.UserTransaction#commit()
     */
    @Override
    public void commit()
        throws HeuristicMixedException,
               HeuristicRollbackException,
               RollbackException,
               SystemException
    {
        try {
            if (!Context.isTMNoTransaction() && Context.isActive()) {
                if (Context.isTMActive()) {
                    Context.commit();
                } else {
                    Context.rollback();
                }
            }
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see javax.transaction.UserTransaction#getStatus()
     */
    @Override
    public int getStatus()
        throws SystemException
    {
        return this.transmanager.getStatus();
    }

    /* (non-Javadoc)
     * @see javax.transaction.UserTransaction#rollback()
     */
    @Override
    public void rollback()
        throws SystemException
    {
        try {
            Context.rollback();
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see javax.transaction.UserTransaction#setRollbackOnly()
     */
    @Override
    public void setRollbackOnly()
        throws IllegalStateException, SystemException
    {
        this.transmanager.setRollbackOnly();
    }

    /* (non-Javadoc)
     * @see javax.transaction.UserTransaction#setTransactionTimeout(int)
     */
    @Override
    public void setTransactionTimeout(final int _timeOut)
        throws SystemException
    {
        this.transmanager.setTransactionTimeout(_timeOut);
    }
}
