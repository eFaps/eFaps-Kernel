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


package org.efaps.jms;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.RandomStringUtils;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.UserAttributesSet;
import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class JmsSession
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JmsSession.class);

    /**
     * The map holds all cached Sessions.
     */
    private static final Map<String, JmsSession> CACHE = new HashMap<String, JmsSession>();

    /**
     * Timer used to look for expired Session.
     */
    private static final Timer TIMER = new Timer();
    static {
        JmsSession.TIMER.schedule(new ExpireTask(), 0, 1000);
    }

    /**
     * Session timeout in seconds. Default 5 min = 300s.
     */
    private static int SESSIONTIMEOUT = 300;

    /**
     * This instance map stores the Attributes which are valid for the whole
     * session. It is passed on to the Context while opening it.
     *
     * @see #openContext()
     */
    private final Map<String, Object> sessionAttributes = new HashMap<String, Object>();

    /**
     * Key to this Session.
     */
    private final String sessionKey;

    /**
     * TimeStamp of the Session creation / last Context opening.
     */
    private Date timeStamp;

    /**
     * Name of the User this Session belongs to.
     */
    private final String userName;

    /**
     * Private Constructor.
     * @param _userName name of the user this Session belong to.
     * @throws EFapsException on error
     */
    private JmsSession(final String _userName)
        throws EFapsException
    {
        this.sessionKey = RandomStringUtils.randomAlphanumeric(15);
        this.timeStamp = new Date();
        this.userName = _userName;
    }

    /**
     * Getter method for the instance variable {@link #sessionKey}.
     *
     * @return value of instance variable {@link #sessionKey}
     */
    private String getSessionKey()
    {
        return this.sessionKey;
    }

    /**
     * Method to check if a user is checked in.
     *
     * @return true if a user is checked in, else false
     * @see #userName
     */
    public boolean isLogedIn()
    {
        boolean ret = false;
        if (this.userName != null) {
            ret = true;
        }
        return ret;
    }

    /**
     * Method that opens a new Context in eFaps, setting the User, the Locale,
     * the Attributes of this Session {@link #sessionAttributes}.
     * @throws EFapsException on error
     *
     * @see #attach()
     */
    public void openContext()
        throws EFapsException
    {
        if (isLogedIn()) {
            if (!Context.isTMActive()) {
                if (!this.sessionAttributes.containsKey(UserAttributesSet.CONTEXTMAPKEY)) {
                    Context.begin(null, false);
                    this.sessionAttributes.put(UserAttributesSet.CONTEXTMAPKEY, new UserAttributesSet(this.userName));
                    Context.rollback();
                }
                Context.begin(this.userName, null, this.sessionAttributes, null, null, false);
                this.timeStamp = new Date();
            }
        }
    }

    /**
     * Method that closes the opened Context {@link #openContext()}, by
     * committing or rollback it.
     * @throws EFapsException on error
     *
     * @see #detach()
     */
    public void closeContext()
        throws EFapsException
    {
        if (isLogedIn()) {
            try {
                if (!Context.isTMNoTransaction()) {
                    if (Context.isTMActive()) {
                        Context.commit();
                    } else {
                        Context.rollback();
                    }
                }
            } catch (final SecurityException e) {
                throw new EFapsException("SecurityException", e);
            } catch (final IllegalStateException e) {
                throw new EFapsException("IllegalStateException", e);
            }
        }
    }

    /**
     * Method to remove the current Session from the Cache.
     */
    public void logout()
    {
        JmsSession.CACHE.remove(getSessionKey());
    }
    /**
     * Getter method for the instance variable {@link #timeStamp}.
     *
     * @return value of instance variable {@link #timeStamp}
     */
    protected Date getTimeStamp()
    {
        return this.timeStamp;
    }

    /**
     * @param _sessionKey   get the Session for a given Key
     * @return if found Session for the given Sessionkey, else null
     */
    public static JmsSession getSession(final String _sessionKey)
    {
        return JmsSession.CACHE.get(_sessionKey);
    }

    /**
     * Login and create a Session.
     *
     * @param _userName         Name of the user to login
     * @param _passwd           Password of the user to login
     * @param _applicationKey   key of the application that requests
     * @return SessionKey if login successful, else null
     * @throws EFapsException on error
     */
    public static String login(final String _userName,
                               final String _passwd,
                               final String _applicationKey)
        throws EFapsException
    {
        String ret = null;
        if (JmsSession.checkLogin(_userName, _passwd, _applicationKey)) {
            final JmsSession session = new JmsSession(_userName);
            JmsSession.CACHE.put(session.getSessionKey(), session);
            ret = session.getSessionKey();
        }
        return ret;
    }

    /**
     * @param _userName         name of the User to login
     * @param _passwd           Password of the User
     * @param _applicationKey   key of the application that requests
     * @return true if login successfull, else false
     */
    private static boolean checkLogin(final String _userName,
                                      final String _passwd,
                                      final String _applicationKey)
    {
        boolean loginOk = false;
        Context context = null;
        try {
            if (Context.isTMActive()) {
                context = Context.getThreadContext();
            } else {
                context = Context.begin(null, false);
            }
            boolean ok = false;

            try {
                // on a new login the cache for Person is reseted
                Person.initialize();
                final LoginHandler loginHandler = new LoginHandler(_applicationKey);
                final Person person = loginHandler.checkLogin(_userName, _passwd);
                if (person != null && !person.getRoles().isEmpty()) {
                    loginOk = true;
                }
                ok = true;
            } finally {
                if (ok && context.allConnectionClosed() && Context.isTMActive()) {
                    Context.commit();
                } else {
                    if (Context.isTMMarkedRollback()) {
                        JmsSession.LOG.error("transaction is marked to roll back");
                    } else if (!context.allConnectionClosed()) {
                        JmsSession.LOG.error("not all connection to database are closed");
                    } else {
                        JmsSession.LOG.error("transaction manager in undefined status");
                    }
                    Context.rollback();
                }
            }
        } catch (final EFapsException e) {
            JmsSession.LOG.error("could not check name and password", e);
        } finally {
            context.close();
        }
        return loginOk;
    }

    /**
     * Setter method for variable {@link #SESSIONTIMEOUT}.
     *
     * @param _sessionTimeout value for variable {@link #SESSIONTIMEOUT}
     */

    protected static void setSessionTimeout(final int _sessionTimeout)
    {
        JmsSession.SESSIONTIMEOUT = _sessionTimeout;
    }

    /**
     * Expire the Session.
     */
    public static class ExpireTask
        extends TimerTask
    {
        @Override
        public void run()
        {
            final Date now = new Date();
            for (final Entry<String, JmsSession> entry : JmsSession.CACHE.entrySet()) {
                if (now.getTime() - entry.getValue().getTimeStamp().getTime() > 1000 * JmsSession.SESSIONTIMEOUT) {
                    entry.getValue().logout();
                }
            }
        }
    }
}
