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

    /**
     * TransactionManager.
     */
    private final TransactionManager transmanager;

    /**
     * Constructor.
     * @param _manager TransactionManager
     */
    public DelegatingUserTransaction(final TransactionManager _manager)
    {
        this.transmanager = _manager;
    }

    /**
     * @see javax.transaction.UserTransaction#begin()
     * @throws NotSupportedException on error
     * @throws SystemException on error
     */
    @Override
    public void begin()
        throws NotSupportedException, SystemException
    {
        try {
            // Quartz brings on the first run his own Context.
            if (Context.isActive()) {
                if (this.transmanager.getStatus() == Status.STATUS_NO_TRANSACTION) {
                    Context.getThreadContext().close();
                    Context.begin("Administrator");
                }
            } else {
                Context.begin("Administrator");
            }
        } catch (final EFapsException e) {
            throw new SystemException(e.getMessage());
        }
    }

    /**
     * Commit.
     * @see javax.transaction.UserTransaction#commit()
     * @throws HeuristicMixedException on error
     * @throws HeuristicRollbackException on error
     * @throws RollbackException on error
     * @throws SystemException on error
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
            throw new SystemException(e.getMessage());
        }
    }

    /**
     * @see javax.transaction.UserTransaction#getStatus()
     * @return Status of the TransactionManager.
     * @throws SystemException on error
     */
    @Override
    public int getStatus()
        throws SystemException
    {
        return this.transmanager.getStatus();
    }

    /**
     * Rollback.
     * @see javax.transaction.UserTransaction#rollback()
     * @throws SystemException on error
     */
    @Override
    public void rollback()
        throws SystemException
    {
        try {
            Context.rollback();
        } catch (final EFapsException e) {
            throw new SystemException(e.getMessage());
        }
    }

    /**
     * Set rollback.
     * @see javax.transaction.UserTransaction#setRollbackOnly()
     * @throws SystemException on error
     */
    @Override
    public void setRollbackOnly()
        throws SystemException
    {
        this.transmanager.setRollbackOnly();
    }

    /**
     * @see javax.transaction.UserTransaction#setTransactionTimeout(int)
     * @param _timeOut TimeOut
     * @throws SystemException on error
     */
    @Override
    public void setTransactionTimeout(final int _timeOut)
        throws SystemException
    {
        this.transmanager.setTransactionTimeout(_timeOut);
    }
}
