/*
 * Copyright 2003 - 2016 The eFaps Team
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


package org.efaps.bpm.transaction;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;

/**
 * Hibernate implementation for using JTA.
 *
 * @author The eFaps Team
 *
 */
public class EFapsJTAPlatform
    extends AbstractJtaPlatform
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Get the TransactionManager.
     * @see org.hibernate.service.jta.platform.internal.AbstractJtaPlatform#locateTransactionManager()
     * @return a UserTransaction from eFaps
     */
    @Override
    protected TransactionManager locateTransactionManager()
    {
        return TransactionHelper.findTransactionManager();
    }

    /**
     * Get the UserTransaction.
     * @see org.hibernate.service.jta.platform.internal.AbstractJtaPlatform#locateUserTransaction()
     * @return a UserTransaction from eFaps
     */
    @Override
    protected UserTransaction locateUserTransaction()
    {
        return TransactionHelper.findUserTransaction();
    }
}
