/*
 * Copyright 2003 - 2013 The eFaps Team
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


package org.efaps.bpm.transaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.efaps.init.INamingBinds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class TransactionHelper
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TransactionHelper.class);

    /**
     * Sequence used to search for the UserTransaction inside JNDI.
     */
    private static final String[] KNOWN_UT_JNDI_KEYS = new String[] {
        "java:global/" + INamingBinds.RESOURCE_USERTRANSACTION,
        "java:comp/env/" + INamingBinds.RESOURCE_USERTRANSACTION };

    /**
     * Sequence used to search for the Transactionmanager inside JNDI.
     */

    private static final String[] KNOWN_TM_JNDI_KEYS = new String[] {
        "java:global/" + INamingBinds.RESOURCE_TRANSMANAG,
        "java:comp/env/" + INamingBinds.RESOURCE_TRANSMANAG };

    /**
     * Hidden Constructor for Utitlty class.
     */
    private TransactionHelper()
    {
    }

    /**
     * @return the transactionmanager
     */
    public static TransactionManager findTransactionManager()
    {
        TransactionManager ret = null;
        InitialContext context = null;
        try {
            context = new InitialContext();
        } catch (final NamingException ex) {
            TransactionHelper.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
        for (final String utLookup : TransactionHelper.KNOWN_TM_JNDI_KEYS) {
            if (utLookup != null) {
                try {
                    ret = (TransactionManager) context.lookup(utLookup);
                    TransactionHelper.LOG.info("TransactionManager found in JNDI under '{}'", utLookup);
                } catch (final NamingException e) {
                    TransactionHelper.LOG.debug("TransactionManager not found in JNDI under '{}'", utLookup);
                }
            }
        }
        if (ret == null) {
            TransactionHelper.LOG.warn("No TransactionManager found under known names");
        }
        return ret;
    }

    /**
     * @return the usertransaction
     */
    public static UserTransaction findUserTransaction()
    {
        UserTransaction ret = null;
        InitialContext context = null;
        try {
            context = new InitialContext();
        } catch (final NamingException ex) {
            TransactionHelper.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
        for (final String utLookup : TransactionHelper.KNOWN_UT_JNDI_KEYS) {
            if (utLookup != null) {
                try {
                    ret = (UserTransaction) context.lookup(utLookup);
                    TransactionHelper.LOG.info("User Transaction found in JNDI under '{}'", utLookup);
                } catch (final NamingException e) {
                    TransactionHelper.LOG.debug("User Transaction not found in JNDI under '{}'", utLookup);
                }
            }
        }
        if (ret == null) {
            TransactionHelper.LOG.warn("No user transaction found under known names");
        }
        return ret;
    }

}
