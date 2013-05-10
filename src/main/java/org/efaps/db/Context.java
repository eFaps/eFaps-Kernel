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

package org.efaps.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.efaps.admin.user.Company;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.UserAttributesSet;
import org.efaps.admin.user.UserAttributesSet.UserAttributesDefinition;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.databases.DataBaseFactory;
import org.efaps.db.store.Resource;
import org.efaps.db.store.Store;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.init.INamingBinds;
import org.efaps.init.IeFapsProperties;
import org.efaps.init.StartupException;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.joda.time.Chronology;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public final class Context
    implements INamingBinds
{

    /**
     * Key used to access the current company from the userattributes.
     */
    public static final String CURRENTCOMPANY = "CurrentCompany";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Context.class);

    /**
     * Static variable storing the database type.
     */
    private static AbstractDatabase<?> DBTYPE;

    /**
     * SQL data source to the database.
     */
    private static DataSource DATASOURCE;

    /**
     * Stores the transaction manager.
     *
     * @see #setTransactionManager
     */
    private static TransactionManager TRANSMANAG;

    /**
     * STore the timeout for the transaction manager.
     */
    private static int TRANSMANAGTIMEOUT = 0;

    static {
        try {
            final InitialContext initCtx = new InitialContext();
            javax.naming.Context envCtx = null;
            try {
                envCtx = (javax.naming.Context) initCtx.lookup("java:/comp/env");
            } catch (final NamingException e) {
                Context.LOG.info("Expected NamingException during evaluation for Context, No action required");
            }
            // for a build the context might be different, try this before surrender
            if (envCtx == null) {
                envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            }

            Context.DATASOURCE = (DataSource) envCtx.lookup(INamingBinds.RESOURCE_DATASOURCE);
            Context.DBTYPE = DataBaseFactory.getDatabase(Context.DATASOURCE.getConnection());
            Context.TRANSMANAG = (TransactionManager) envCtx.lookup(INamingBinds.RESOURCE_TRANSMANAG);

            try {
                Context.TRANSMANAGTIMEOUT = 0;
                final Map<?, ?> props = (Map<?, ?>) envCtx.lookup(INamingBinds.RESOURCE_CONFIGPROPERTIES);
                if (props != null) {
                    final String transactionTimeoutString = (String) props.get(IeFapsProperties.TRANSACTIONTIMEOUT);
                    if (transactionTimeoutString != null) {
                        Context.TRANSMANAGTIMEOUT = Integer.parseInt(transactionTimeoutString);
                    }
                }
            } catch (final NamingException e) {
                // this is actual no error, so nothing is presented
                Context.TRANSMANAGTIMEOUT = 0;
            }
        } catch (final NamingException e) {
            Context.LOG.error("NamingException", e);
            throw new Error(e);
        } catch (final SQLException e) {
            Context.LOG.error("SQLException", e);
            throw new Error(e);
        }
    }

    /**
     * Each thread has his own context object. The value is automatically
     * assigned from the filter class. This allows to have a different Context
     * for every Users which is connect to the WebApp Server. For the case that
     * a thread creates a child threat the context is inherited to this new
     * thread. This is needed e.g. in JasperReport for SubReports.
     * @see #inherit
     */
    private static ThreadLocal<Context> INHERITTHREADCONTEXT = new InheritableThreadLocal<Context>();

    /**
     * Each thread has his own context object. The value is automatically
     * assigned from the filter class. This allows to have a different Context
     * for every Users which is connect to the WebApp Server. For the case that
     * a thread creates a child threat a different context is created this is
     * needed e.g. for background process form quartz.
     */
    private static ThreadLocal<Context> THREADCONTEXT = new ThreadLocal<Context>();

    /**
     * The instance variable stores all open instances of {@link Resource}.
     *
     * @see #getStoreResource(Instance)
     * @see #getStoreResource(Type,long)
     */
    private final Set<Resource> storeStore = new HashSet<Resource>();

    /**
     * Stores all created connection resources.
     */
    private final Set<ConnectionResource> connectionStore = new HashSet<ConnectionResource>();

    /**
     * Stack used to store returned connections for reuse.
     */
    private final Stack<ConnectionResource> connectionStack = new Stack<ConnectionResource>();

    /**
     * Transaction for the context.
     */
    private final Transaction transaction;

    /**
     * This is the instance variable for the SQL Connection to the database.
     *
     * @see #getConnection
     * @see #setConnection
     */
    private Connection connection = null;

    /**
     * This instance variable represents the user of the context.
     *
     * @see #getPerson
     */
    private Person person = null;

    /**
     * The current active company.
     */
    private Long companyId = null;

    /**
     * The parameters used to open a new thread context are stored in this
     * instance variable (e.g. the request parameters from a http servlet are
     * stored in this variable).
     *
     * @see #getParameters
     */
    private final Map<String, String[]> parameters;

    /**
     * The file parameters used to open a new thread context are stored in this
     * instance variable (e.g. the request parameters from a http servlet or in
     * the shell the parameters from the command shell). The file item
     * represents one file which includes an input stream, the name and the
     * length of the file.
     *
     * @see #getFileParameters
     */
    private final Map<String, FileParameter> fileParameters;

    /**
     * A map to be able to set attributes with a lifetime of a request (e.g.
     * servlet request).
     *
     * @see #containsRequestAttribute
     * @see #getRequestAttribute
     * @see #setRequestAttribute
     */
    private final Map<String, Object> requestAttributes = new HashMap<String, Object>();

    /**
     * A map to be able to set attributes with a lifetime of a session (e.g. as
     * long as the user is logged in).
     *
     * @see #containsSessionAttribute
     * @see #getSessionAttribute
     * @see #setSessionAttribute
     */
    private Map<String, Object> sessionAttributes = new HashMap<String, Object>();

    /**
     * Holds the timezone belonging to the user of this context.
     */
    private DateTimeZone timezone;

    /**
     * Holds the locale belonging to the user of this context.
     */
    private Locale locale;

    /**
     * Holds the chronology belonging to the user of this context.
     */
    private Chronology chronology;

    /**
     * Holds the iso code of the language belonging to the user of this context.
     */
    private String language;

    /**
     * If used in a webapp the context path of the webapp can be stored here, so
     * that it is accessible for e.g. esjps.
     */
    private String path;

    /**
     * Must the ThreadContext be inherit or not.
     */
    private final boolean inherit;

    /**
     * Private Constructor.
     *
     * @see #begin(String, Locale, Map, Map, Map)
     *
     * @param _transaction Transaction to be used in this context
     * @param _locale Locale to be used in this context
     * @param _sessionAttributes attributes belonging to this session
     * @param _parameters parameters beloonging to this session
     * @param _fileParameters paramters for file up/download
     * @param _inherit              must the context be inherited to child threads
     * @throws EFapsException on error
     */
    private Context(final Transaction _transaction,
                    final Locale _locale,
                    final Map<String, Object> _sessionAttributes,
                    final Map<String, String[]> _parameters,
                    final Map<String, FileParameter> _fileParameters,
                    final boolean _inherit)
        throws EFapsException
    {
        this.inherit = _inherit;
        this.transaction = _transaction;

        this.parameters = (_parameters == null) ? new HashMap<String, String[]>() : _parameters;
        this.fileParameters = (_fileParameters == null) ? new HashMap<String, FileParameter>() : _fileParameters;
        this.sessionAttributes = (_sessionAttributes == null) ? new HashMap<String, Object>() : _sessionAttributes;
        try {
            setConnection(Context.DATASOURCE.getConnection());
        } catch (final SQLException e) {
            Context.LOG.error("could not get a sql connection", e);
        }
    }

    /**
     * @return ThreadLocal related to this context
     */
    private ThreadLocal<Context> getThreadLocal()
    {
        ThreadLocal<Context> ret;
        if (this.inherit) {
            ret = Context.INHERITTHREADCONTEXT;
        } else {
            ret = Context.THREADCONTEXT;
        }
        return ret;
    }

    /**
     * Destructor of class <code>Context</code>.
     */
    @Override
    public void finalize()
    {
        if (Context.LOG.isDebugEnabled()) {
            Context.LOG.debug("finalize context for " + this.person);
            Context.LOG.debug("connection is " + getConnection());
        }
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (final SQLException e) {
                Context.LOG.error("could not close a sql connection", e);
            }
        }
    }

    /**
     * The method tests if all resources (JDBC connection and store resources)
     * are closed, that means that the resources are freeed and returned for
     * reuse.
     *
     * @return <i>true</i> if all resources are closed, otherwise <i>false</i>
     *         is returned
     * @see #connectionStore
     * @see #storeStore
     */
    public boolean allConnectionClosed()
    {
        boolean closed = true;

        for (final ConnectionResource con : this.connectionStore) {
            if (con.isOpened()) {
                closed = false;
                break;
            }
        }
        if (closed) {
            for (final Resource store : this.storeStore) {
                if (store.isOpened()) {
                    closed = false;
                    break;
                }
            }
        }
        return closed;
    }

    /**
     * Close this contexts, meaning this context object is removed as thread
     * context.<br/>
     * If not all connection are closed, all connection are closed.
     *
     * TODO: better description
     */
    public void close()
    {
        if (Context.LOG.isDebugEnabled()) {
            Context.LOG.debug("close context for " + this.person);
            Context.LOG.debug("connection is " + getConnection());
        }
        if (this.connection != null) {
            try {
                //this.connection.commit();
                this.connection.close();
            } catch (final SQLException e) {
                Context.LOG.error("could not close a sql connection", e);
            }
        }

        setConnection(null);
        if ((getThreadLocal().get() != null) && (getThreadLocal().get() == this)) {
            getThreadLocal().set(null);
        }
        // check if all JDBC connection are close...
        for (final ConnectionResource con : this.connectionStore) {
            try {
                if ((con.getConnection() != null) && !con.getConnection().isClosed()) {
                    con.getConnection().close();
                    Context.LOG.error("connection was not closed!");
                }
            } catch (final SQLException e) {
                Context.LOG.error("QLException is thrown while trying to get close status of "
                                + "connection or while trying to close", e);
            }
        }
    }

    /**
     * Method to abort the transaction.
     *
     * @throws EFapsException if setting of rollback was not successfully
     */
    public void abort()
        throws EFapsException
    {
        try {
            this.transaction.setRollbackOnly();
        } catch (final SystemException e) {
            throw new EFapsException(getClass(), "abort.SystemException", e);
        }
    }

    /**
     * Returns a opened connection resource. If a previous close connection
     * resource already exists, this already existing connection resource is
     * returned.
     *
     * @return opened connection resource
     * @throws EFapsException if connection resource cannot be created
     */
    public ConnectionResource getConnectionResource()
        throws EFapsException
    {
        ConnectionResource con = null;
        if (this.connectionStack.isEmpty()) {
            try {
                con = new ConnectionResource(Context.DATASOURCE.getConnection());
            } catch (final SQLException e) {
                throw new EFapsException(getClass(), "getConnectionResource.SQLException", e);
            }
            this.connectionStore.add(con);
        } else {
            con = this.connectionStack.pop();
        }
        if (!con.isOpened()) {
            con.open();
        }
        return con;
    }

    /**
     * @param _con ConnectionResource
     */
    public void returnConnectionResource(final ConnectionResource _con)
    {
        this.connectionStack.push(_con);
    }

    /**
     * Method to get the sore resource.
     *
     * @param _instance Instance to get the StoreResource for
     * @param _event    StorEvent the store is wanted for
     * @throws EFapsException on error
     * @return StoreResource
     * @see #getStoreResource(Type,long)
     */
    public Resource getStoreResource(final Instance _instance,
                                     final Resource.StoreEvent _event)
        throws EFapsException
    {
        Resource storeRsrc = null;
        final Store store = Store.get(_instance.getType().getStoreId());
        storeRsrc = store.getResource(_instance);
        storeRsrc.open(_event);
        this.storeStore.add(storeRsrc);
        return storeRsrc;
    }

    /**
     * If a person is assigned to this context, the id of this person is
     * returned. Otherwise the default person id value is returned. The method
     * guarantees to return value which is valid!<br/>
     * The value could be used e.g. if a a value is inserted into the database
     * and the person id is needed for the creator and / or modifier.
     *
     * @return person id of current person or default person id value
     */
    public long getPersonId()
    {
        long ret = 1;

        if (this.person != null) {
            ret = this.person.getId();
        }
        return ret;
    }

    /**
     * Method to get a parameter from the context.
     *
     * @param _key Key for the parameter
     * @return String value of the parameter
     */
    public String getParameter(final String _key)
    {
        String value = null;
        if (this.parameters != null) {
            final String[] values = this.parameters.get(_key);
            if ((values != null) && (values.length > 0)) {
                value = values[0];
            }
        }
        return value;
    }

    /**
     * Getter method for instance variable {@link #path}.
     *
     * @return value of instance variable {@link #path}
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     * Setter method for instance variable {@link #path}.
     *
     * @param _path value for instance variable {@link #path}
     */
    public void setPath(final String _path)
    {
        this.path = _path;
    }

    /**
     * Returns true if request attributes maps one or more keys to the specified
     * object. More formally, returns <i>true</i> if and only if the request
     * attributes contains at least one mapping to a object o such that (o==null
     * ? o==null : o.equals(o)).
     *
     * @param _key key whose presence in the request attributes is to be tested
     * @return <i>true</i> if the request attributes contains a mapping for
     *         given key, otherwise <i>false</i>
     * @see #requestAttributes
     * @see #getRequestAttribute
     * @see #setRequestAttribute
     */
    public boolean containsRequestAttribute(final String _key)
    {
        return this.requestAttributes.containsKey(_key);
    }

    /**
     * Returns the object to which this request attributes maps the specified
     * key. Returns <code>null</code> if the request attributes contains no
     * mapping for this key. A return value of <code>null</code> does not
     * necessarily indicate that the request attributes contains no mapping for
     * the key; it's also possible that the request attributes explicitly maps
     * the key to null. The {@link #containsRequestAttribute} operation may be
     * used to distinguish these two cases.<br/>
     * More formally, if the request attributes contains a mapping from a key k
     * to a object o such that (key==null ? k==null : key.equals(k)), then this
     * method returns o; otherwise it returns <code>null</code> (there can be at
     * most one such mapping).
     *
     * @param _key key name of the mapped attribute to be returned
     * @return object to which the request attribute contains a mapping for
     *         specified key, or <code>null</code> if not specified in the
     *         request attributes
     * @see #requestAttributes
     * @see #containsRequestAttribute
     * @see #setRequestAttribute
     */
    public Object getRequestAttribute(final String _key)
    {
        return this.requestAttributes.get(_key);
    }

    /**
     * Associates the specified value with the specified key in the request
     * attributes. If the request attributes previously contained a mapping for
     * this key, the old value is replaced by the specified value.
     *
     * @param _key key name of the attribute to set
     * @param _value _value of the attribute to set
     * @return Object
     * @see #requestAttributes
     * @see #containsRequestAttribute
     * @see #getRequestAttribute
     */
    public Object setRequestAttribute(final String _key,
                                      final Object _value)
    {
        return this.requestAttributes.put(_key, _value);
    }

    /**
     * Returns true if session attributes maps one or more keys to the specified
     * object. More formally, returns <i>true</i> if and only if the session
     * attributes contains at least one mapping to a object o such that (o==null
     * ? o==null : o.equals(o)).
     *
     * @param _key key whose presence in the session attributes is to be tested
     * @return <i>true</i> if the session attributes contains a mapping for
     *         given key, otherwise <i>false</i>
     * @see #sessionAttributes
     * @see #getSessionAttribute
     * @see #setSessionAttribute
     */
    public boolean containsSessionAttribute(final String _key)
    {
        return this.sessionAttributes.containsKey(_key);
    }

    /**
     * Returns the object to which this session attributes maps the specified
     * key. Returns <code>null</code> if the session attributes contains no
     * mapping for this key. A return value of <code>null</code> does not
     * necessarily indicate that the session attributes contains no mapping for
     * the key; it's also possible that the session attributes explicitly maps
     * the key to null. The {@link #containsSessionAttribute} operation may be
     * used to distinguish these two cases.<br/>
     * More formally, if the session attributes contains a mapping from a key k
     * to a object o such that (key==null ? k==null : key.equals(k)), then this
     * method returns o; otherwise it returns <code>null</code> (there can be at
     * most one such mapping).
     *
     * @param _key key name of the mapped attribute to be returned
     * @return object to which the session attribute contains a mapping for
     *         specified key, or <code>null</code> if not specified in the
     *         session attributes
     * @see #sessionAttributes
     * @see #containsSessionAttribute
     * @see #setSessionAttribute
     */
    public Object getSessionAttribute(final String _key)
    {
        return this.sessionAttributes.get(_key);
    }

    /**
     * Associates the specified value with the specified key in the session
     * attributes. If the session attributes previously contained a mapping for
     * this key, the old value is replaced by the specified value.
     *
     * @param _key key name of the attribute to set
     * @param _value value of the attribute to set
     * @return Object
     * @see #sessionAttributes
     * @see #containsSessionAttribute
     * @see #getSessionAttribute
     */
    public Object setSessionAttribute(final String _key,
                                      final Object _value)
    {
        return this.sessionAttributes.put(_key, _value);
    }

    /**
     * Remove a attribute form the Session.
     * @param _key key of the session attribute to be removed.
     */
    public void removeSessionAttribute(final String _key)
    {
        this.sessionAttributes.remove(_key);
    }

    /**
     * This method retrieves a UserAttribute of the Person this Context belongs
     * to. The UserAttributes are stored in the {@link #sessionAttributes} Map,
     * therefore are thought to be valid for one session.
     *
     * @param _key key to Search for
     * @return String with the value
     * @throws EFapsException on error
     */
    public String getUserAttribute(final String _key)
        throws EFapsException
    {
        if (containsSessionAttribute(UserAttributesSet.CONTEXTMAPKEY)) {
            return ((UserAttributesSet) getSessionAttribute(UserAttributesSet.CONTEXTMAPKEY)).getString(_key);
        } else {
            throw new EFapsException(Context.class, "getUserAttribute.NoSessionAttribute");
        }
    }

    /**
     * This method determines if UserAttribute of the Person this Context
     * belongs to exists.The UserAttributes are stored in the
     * {@link #sessionAttributes} Map, therefore are thought to be valid for one
     * session.
     *
     * @param _key key to Search for
     * @return true if found, else false
     *
     * @throws EFapsException on error
     */
    public boolean containsUserAttribute(final String _key)
        throws EFapsException
    {
        boolean ret = false;
        if (containsSessionAttribute(UserAttributesSet.CONTEXTMAPKEY)) {
            ret = ((UserAttributesSet) getSessionAttribute(UserAttributesSet.CONTEXTMAPKEY)).containsKey(_key);
        }
        return ret;
    }

    /**
     * Set a new UserAttribute for the UserAttribute of the Person this
     * Context.The UserAttributes are stored in the {@link #sessionAttributes}
     * Map, therefore are thought to be valid for one session.
     *
     * @param _key Key of the UserAttribute
     * @param _value Value of the UserAttribute
     * @throws EFapsException on error
     */
    public void setUserAttribute(final String _key,
                                 final String _value)
        throws EFapsException
    {
        if (containsSessionAttribute(UserAttributesSet.CONTEXTMAPKEY)) {
            ((UserAttributesSet) getSessionAttribute(UserAttributesSet.CONTEXTMAPKEY)).set(_key, _value);
        } else {
            final UserAttributesSet userAttribute = new UserAttributesSet(getPersonId());
            userAttribute.set(_key, _value);
            setSessionAttribute(UserAttributesSet.CONTEXTMAPKEY, userAttribute);
        }
    }

    /**
     * Set a new UserAttribute for the UserAttribute of the Person this
     * Context.The UserAttributes are stored in the {@link #sessionAttributes}
     * Map, therefore are thought to be valid for one session.
     *
     * @param _key Key of the UserAttribute
     * @param _value Value of the UserAttribute
     * @param _definition Definition
     * @throws EFapsException on error
     */
    public void setUserAttribute(final String _key,
                                 final String _value,
                                 final UserAttributesDefinition _definition)
        throws EFapsException
    {
        if (containsSessionAttribute(UserAttributesSet.CONTEXTMAPKEY)) {
            ((UserAttributesSet) getSessionAttribute(UserAttributesSet.CONTEXTMAPKEY)).set(_key, _value, _definition);
        } else {
            throw new EFapsException(Context.class, "getUserAttributes.NoSessionAttribute");
        }
    }

    /**
     * Method to get the UserAttributesSet of the user of this context.
     *
     * @return UserAttributesSet
     * @throws EFapsException on error
     */
    public UserAttributesSet getUserAttributes()
        throws EFapsException
    {
        if (containsSessionAttribute(UserAttributesSet.CONTEXTMAPKEY)) {
            return (UserAttributesSet) getSessionAttribute(UserAttributesSet.CONTEXTMAPKEY);
        } else {
            throw new EFapsException(Context.class, "getUserAttributes.NoSessionAttribute");
        }
    }

    /**
     * This is the getter method for instance variable {@link #connection}.
     *
     * @return value of instance variable {@link #connection}
     * @see #connection
     * @see #setConnection
     */
    public Connection getConnection()
    {
        return this.connection;
    }

    /**
     * This is the setter method for instance variable {@link #connection}.
     *
     * @param _connection new value for instance variable {@link #connection}
     * @see #connection
     * @see #getConnection
     */
    private void setConnection(final Connection _connection)
    {
        this.connection = _connection;
    }

    /**
     * This is the getter method for instance variable {@link #transaction}.
     *
     * @return value of instance variable {@link #transaction}
     * @see #transaction
     */
    public Transaction getTransaction()
    {
        return this.transaction;
    }

    /**
     * This is the getter method for instance variable {@link #person}.
     *
     * @return value of instance variable {@link #person}
     * @see #person
     */
    public Person getPerson()
    {
        return this.person;
    }

    /**
     * Get the Company currently valid for this context.
     *
     * @return value of instance variable {@link #company}
     */
    public Company getCompany()
        throws CacheReloadException
    {
        return this.companyId == null ?  null : Company.get(this.companyId);
    }

    /**
     * This is the getter method for instance variable {@link #locale}.
     *
     * @return value of instance variable {@link #locale}
     * @see #locale
     */
    public Locale getLocale()
    {
        return this.locale;
    }

    /**
     * This is the getter method for instance variable {@link #timezone}.
     *
     * @return value of instance variable {@link #timezone}
     * @see #timezone
     */
    public DateTimeZone getTimezone()
    {
        return this.timezone;
    }

    /**
     * This is the getter method for instance variable {@link #chronology}.
     *
     * @return value of instance variable {@link #chronology}
     * @see #locale
     */
    public Chronology getChronology()
    {
        return this.chronology;
    }

    /**
     * Getter method for instance variable {@link #language}.
     *
     * @return value of instance variable {@link #language}
     */
    public String getLanguage()
    {
        return this.language;
    }

    /**
     * This is the getter method for instance variable {@link #parameters}.
     *
     * @return value of instance variable {@link #parameters}
     * @see #parameters
     */
    public Map<String, String[]> getParameters()
    {
        return this.parameters;
    }

    /**
     * This is the getter method for instance variable {@link #fileParameters}.
     *
     * @return value of instance variable {@link #fileParameters}
     * @see #fileParameters
     */
    public Map<String, FileParameter> getFileParameters()
    {
        return this.fileParameters;
    }

    /**
     * Is a Thread active.
     *
     * @return true if either the ThreadContext or the
     *              Inherited ThreadContext is no null
     */
    public static boolean isThreadActive()
    {
        return Context.INHERITTHREADCONTEXT.get() != null || Context.THREADCONTEXT.get() != null;
    }

    /**
     * The method checks if for the current thread a context object is defined.
     * This found context object is returned.
     *
     * @return defined context object of current thread
     * @throws EFapsException if no context object for current thread is defined
     * @see #INHERITTHREADCONTEXT
     */
    public static Context getThreadContext()
        throws EFapsException
    {
        Context context = Context.THREADCONTEXT.get();
        if (context == null) {
            context = Context.INHERITTHREADCONTEXT.get();
        }
        if (context == null) {
            throw new EFapsException(Context.class, "getThreadContext.NoContext4ThreadDefined");
        }
        return context;
    }

    /**
     * Method to get a new Context.
     *
     * @see #begin(String, Locale, Map, Map, Map)
     * @throws EFapsException on error
     * @return new Context
     */
    public static Context begin()
        throws EFapsException
    {
        return Context.begin(null, null, null, null, null, true);
    }

    /**
     * Method to get a new Context.
     *
     * @see #begin(String, Locale, Map, Map, Map)
     * @param _userName Naem of the user the Context must be created for
     * @throws EFapsException on error
     * @return new Context
     *
     */
    public static Context begin(final String _userName)
        throws EFapsException
    {
        return Context.begin(_userName, null, null, null, null, true);
    }

    /**
     * Method to get a new Context.
     *
     * @see #begin(String, Locale, Map, Map, Map)
     * @param _userName Naem of the user the Context must be created for
     * @param _inherit              must the context be inherited to child threads
     * @throws EFapsException on error
     * @return new Context
     *
     */
    public static Context begin(final String _userName,
                                final boolean _inherit)
        throws EFapsException
    {
        return Context.begin(_userName, null, null, null, null, _inherit);
    }

    /**
     * For current thread a new context object must be created.
     *
     * @param _userName             name of current user to set
     * @param _locale               locale instance (which language settings has the user)
     * @param _sessionAttributes    attributes for this session
     * @param _parameters           map with parameters for this thread context
     * @param _fileParameters       map with file parameters
     * @param _inherit              must the context be inherited to child threads
     * @return new context of thread
     * @throws EFapsException if a new transaction could not be started or if
     *             current thread context is already set
     * @see #INHERITTHREADCONTEXT
     */
    public static Context begin(final String _userName,
                                final Locale _locale,
                                final Map<String, Object> _sessionAttributes,
                                final Map<String, String[]> _parameters,
                                final Map<String, FileParameter> _fileParameters,
                                final boolean _inherit)
        throws EFapsException
    {
        if ((_inherit && Context.INHERITTHREADCONTEXT.get() != null)
                        || (!_inherit  && Context.THREADCONTEXT.get() != null)) {
            throw new EFapsException(Context.class, "begin.Context4ThreadAlreadSet");
        }

        try {
            // the timeout set is reseted on creation of a new Current object in
            // the transaction manager,
            // so if the default must be overwritten it must be set explicitly
            // again
            if (Context.TRANSMANAGTIMEOUT > 0) {
                Context.TRANSMANAG.setTransactionTimeout(Context.TRANSMANAGTIMEOUT);
            }
            Context.TRANSMANAG.begin();
        } catch (final SystemException e) {
            throw new EFapsException(Context.class, "begin.beginSystemException", e);
        } catch (final NotSupportedException e) {
            throw new EFapsException(Context.class, "begin.beginNotSupportedException", e);
        }
        Transaction transaction;
        try {
            transaction = Context.TRANSMANAG.getTransaction();
        } catch (final SystemException e) {
            throw new EFapsException(Context.class, "begin.getTransactionSystemException", e);
        }
        final Context context = new Context(transaction, (_locale == null) ? Locale.ENGLISH : _locale,
                        _sessionAttributes, _parameters, _fileParameters, _inherit);
        if (_inherit) {
            Context.INHERITTHREADCONTEXT.set(context);
        } else {
            Context.THREADCONTEXT.set(context);
        }

        if (_userName != null) {
            context.person = Person.get(_userName);
            context.locale = context.person.getLocale();
            context.timezone = context.person.getTimeZone();
            context.chronology = context.person.getChronology();
            context.language = context.person.getLanguage();
            if (_sessionAttributes != null) {
                if (context.containsUserAttribute(Context.CURRENTCOMPANY)) {
                    final Company comp = Company.get(Long.parseLong(context.getUserAttribute(Context.CURRENTCOMPANY)));
                    if (comp != null && !context.person.getCompanies().isEmpty() && context.person.isAssigned(comp)) {
                        context.companyId = comp.getId();
                    } else {
                        context.setUserAttribute(Context.CURRENTCOMPANY, "0");
                    }
                }
                if (context.companyId == null && context.person.getCompanies().size() > 0) {
                    for (final Long compID : context.person.getCompanies()) {
                        context.setUserAttribute(Context.CURRENTCOMPANY, compID.toString());
                        context.companyId = compID;
                        break;
                    }
                }
            }
        }
        return context;
    }

    public static void commit()
        throws EFapsException
    {
        Context.commit(true);
    }

    /**
     * @throws EFapsException if commit of the transaction manager failed TODO:
     *             description
     */
    public static void commit(final boolean _close)
        throws EFapsException
    {
        try {
            Context.TRANSMANAG.commit();
        } catch (final IllegalStateException e) {
            throw new EFapsException(Context.class, "commit.IllegalStateException", e);
        } catch (final SecurityException e) {
            throw new EFapsException(Context.class, "commit.SecurityException", e);
        } catch (final HeuristicMixedException e) {
            throw new EFapsException(Context.class, "commit.HeuristicMixedException", e);
        } catch (final HeuristicRollbackException e) {
            throw new EFapsException(Context.class, "commit.HeuristicRollbackException", e);
        } catch (final RollbackException e) {
            throw new EFapsException(Context.class, "commit.RollbackException", e);
        } catch (final SystemException e) {
            throw new EFapsException(Context.class, "commit.SystemException", e);
        } finally {
            if (_close) {
                Context.getThreadContext().close();
            }
        }
    }

    /**
     * @throws EFapsException if roll back of the transaction manager failed
     */
    public static void rollback()
        throws EFapsException
    {
        try {
            Context.TRANSMANAG.rollback();
        } catch (final IllegalStateException e) {
            throw new EFapsException(Context.class, "rollback.IllegalStateException", e);
        } catch (final SecurityException e) {
            throw new EFapsException(Context.class, "rollback.SecurityException", e);
        } catch (final SystemException e) {
            throw new EFapsException(Context.class, "rollback.SystemException", e);
        } finally {
            Context.getThreadContext().close();
        }
    }

    /**
     * Is the status of transaction manager active?
     *
     * @return <i>true</i> if transaction manager is active, otherwise
     *         <i>false</i>
     * @throws EFapsException if the status of the transaction manager could not
     *             be evaluated
     * @see #TRANSMANAG
     */
    public static boolean isTMActive()
        throws EFapsException
    {
        try {
            return Context.TRANSMANAG.getStatus() == Status.STATUS_ACTIVE;
        } catch (final SystemException e) {
            throw new EFapsException(Context.class, "isTMActive.SystemException", e);
        }
    }

    /**
     * Is a transaction associated with a target object for transaction manager?
     *
     * @return <i>true</i> if a transaction associated, otherwise <i>false</i>
     * @throws EFapsException if the status of the transaction manager could not
     *             be evaluated
     * @see #TRANSMANAG
     */
    public static boolean isTMNoTransaction()
        throws EFapsException
    {
        try {
            return Context.TRANSMANAG.getStatus() == Status.STATUS_NO_TRANSACTION;
        } catch (final SystemException e) {
            throw new EFapsException(Context.class, "isTMNoTransaction.SystemException", e);
        }
    }

    /**
     * Is the status of transaction manager marked roll back?
     *
     * @return <i>true</i> if transaction manager is marked roll back, otherwise
     *         <i>false</i>
     * @throws EFapsException if the status of the transaction manager could not
     *             be evaluated
     * @see #TRANSMANAG
     */
    public static boolean isTMMarkedRollback()
        throws EFapsException
    {
        try {
            return Context.TRANSMANAG.getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (final SystemException e) {
            throw new EFapsException(Context.class, "isTMMarkedRollback.SystemException", e);
        }
    }

    /**
     * Returns the database type of the default connection (database where the
     * data model definition is stored).
     *
     * @see #DBTYPE
     * @return AbstractDatabase
     */
    public static AbstractDatabase<?> getDbType()
    {
        return Context.DBTYPE;
    }

    /**
     * Resets the context to current defined values in the Javax naming
     * environment.
     *
     * @throws StartupException if context could not be reseted to new values
     * @see #DBTYPE
     * @see #DATASOURCE
     * @see #TRANSMANAG
     * @see #TRANSMANAGTIMEOUT
     */
    public static void reset()
        throws StartupException
    {
        try {
            final InitialContext initCtx = new InitialContext();
            final javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            Context.DBTYPE = (AbstractDatabase<?>) envCtx.lookup(INamingBinds.RESOURCE_DBTYPE);
            Context.DATASOURCE = (DataSource) envCtx.lookup(INamingBinds.RESOURCE_DATASOURCE);
            Context.TRANSMANAG = (TransactionManager) envCtx.lookup(INamingBinds.RESOURCE_TRANSMANAG);
            try {
                Context.TRANSMANAGTIMEOUT = 0;
                final Map<?, ?> props = (Map<?, ?>) envCtx.lookup(INamingBinds.RESOURCE_CONFIGPROPERTIES);
                if (props != null) {
                    final String transactionTimeoutString = (String) props.get(IeFapsProperties.TRANSACTIONTIMEOUT);
                    if (transactionTimeoutString != null) {
                        Context.TRANSMANAGTIMEOUT = Integer.parseInt(transactionTimeoutString);
                    }
                }
            } catch (final NamingException e) {
                // this is actual no error, so nothing is presented
                Context.TRANSMANAGTIMEOUT = 0;
            }
        } catch (final NamingException e) {
            throw new StartupException("eFaps context could not be initialized", e);
        }
    }

    /**
     * Interfaces defining file parameters used to access file parameters (e.g.
     * uploads from the user within the web application).
     */
    public interface FileParameter
    {

        /**
         * Closes the file for this this file parameter is defined (e.g. deletes
         * the file in the temporary directory, if needed).
         *
         * @throws IOException if the close failed
         */
        void close()
            throws IOException;

        /**
         * Returns the input stream of the file for which this file parameter is
         * defined.
         *
         * @return input stream of the file
         * @throws IOException if the input stream could not be returned
         */
        InputStream getInputStream()
            throws IOException;

        /**
         * Returns the size of the file for which this file parameter is
         * defined.
         *
         * @return size of file
         */
        long getSize();

        /**
         * Returns the content type of the file for which this file parameter is
         * defined.
         *
         * @return content type of the file
         */
        String getContentType();

        /**
         * Returns the name of the file for which this file parameter is
         * defined.
         *
         * @return name of file
         */
        String getName();

        /**
         * Returns the name of the parameter for which this file parameter is
         * defined.
         *
         * @return parameter name
         */
        String getParameterName();
    }
}
