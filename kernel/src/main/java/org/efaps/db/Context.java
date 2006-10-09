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

package org.efaps.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.Person;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.transaction.JDBCStoreResource;
import org.efaps.db.transaction.StoreResource;
import org.efaps.db.transaction.VFSStoreResource;
import org.efaps.util.EFapsException;

/**
 *
 */
public class Context {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(Context.class);

  /**
   * Static variable storing the database type.
   */
  private static AbstractDatabase dbType = null;

  /**
   * Each thread has his own context object. The value is automatically
   * assigned from the filter class.
   */
  private static ThreadLocal < Context > threadContext 
                                      = new ThreadLocal < Context > ();

  private static DataSource dataSource = null;

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores all open instances of {@link StoreResource}.
   *
   * @see #getStoreResource(Instance)
   * @see #getStoreResource(Type,long)
   */
  private Set < StoreResource > storeStore = new HashSet < StoreResource > ();

  /**
   * Stores all created connection resources.
   */
  private Set<ConnectionResource> connectionStore =
                                      new HashSet<ConnectionResource>();

  /**
   * Stack used to store returned connections for reuse.
   */
  private Stack<ConnectionResource> connectionStack =
                                      new Stack<ConnectionResource>();

  private final Transaction transaction;

  /**
   * This is the instance variable for the SQL Connection to the database.
   *
   * @see #getConnection
   * @see #setConnection
   */
  private Connection connection = null;

  /**
   * This instance variable represents the user name of the context.
   *
   * @see #getPerson
   */
  private Person person = null;

  /**
   * The instance variable stores the locale object defined by the user
   * interface (locale object of the current logged in eFaps user).
   * The information is needed to create localised information within eFaps.
   *
   * @see #getLocale
   */
  private final Locale locale;

  /**
   * The parameters used to open a new thread context are stored in this 
   * instance variable (e.g. the request parameters from a http servlet are
   * stored in this variable).
   *
   * @see #getParameters
   */
  private final Map < String, String[] > parameters;

  /**
   * The file parameters used to open a new thread context are stored in this
   * instance variable (e.g. the request parameters from a http servlet or
   * in the shell the parameters from the command shell). The file item 
   * represents one file which includes an input stream, the name and the 
   * length of the file.
   *
   * @see #getFileParameters
   * @todo replace FileItem against own implementation
   */
  private final Map < String, FileItem > fileParameters;

  /////////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  /**
   * Constructor for
   *
   * @see #person
   * @see #locale
   */
  private Context(final Transaction _transaction, 
                  final Person _person, 
                  final Locale _locale,
                  final Map < String, String[] > _parameters,
                  final Map < String, FileItem > _fileParameters)
                                                      throws EFapsException  {
System.out.println("--------------------------------- new context");
    this.transaction = _transaction;
    this.person = _person;
    this.locale = _locale;
    this.parameters = _parameters;
    this.fileParameters = _fileParameters;
try  {
    setConnection(getDataSource().getConnection());
  getConnection().setAutoCommit(true);
} catch (SQLException e)  {
  LOG.error("could not get a sql connection", e);
// TODO: LOG + Exception
e.printStackTrace();
}
  }


  /**
   * Destructor of class <code>Context</code>.
   */
  public final void finalize()  {
System.out.println("--------------------------------- context.finalize connection=" + getConnection());
    try  {
      getConnection().close();
    } catch (Exception e)  {
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

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
  public boolean allConnectionClosed()  {
    boolean closed = true;

    for (ConnectionResource con : this.connectionStore)  {
      if (con.isOpened())  {
        closed = false;
        break;
      }
    }

    if (closed)  {
      for (StoreResource store : this.storeStore)  {
        if (store.isOpened())  {
          closed = false;
          break;
        }
      }
    }

    return closed;
  }

  public void close()  {
System.out.println("--------------------------------- context.close");
    try  {
      getConnection().close();
    } catch (Exception e)  {
    }
    setConnection(null);
    if ((threadContext.get() != null) && (threadContext.get() == this))  {
      threadContext.set(null);
    }
// check if all JDBC connection are close...
for (ConnectionResource con : this.connectionStore)  {
  try  {
// TODO: write in log-file...
    if ((con.getConnection() != null) && !con.getConnection().isClosed())  {
      con.getConnection().close();
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("");
      System.out.println("");
      System.out.println("connection not closed");
      System.out.println("");
      System.out.println("");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
    }
  } catch (SQLException e)  {
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("");
      System.out.println("");
      System.out.println("SQLException is thrown while trying to get close status of connection or while trying to close");
      System.out.println("");
      System.out.println("");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
      System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
  }
}

  }


  /**
   *
   */
  public void abort() throws EFapsException  {
    try  {
      this.transaction.setRollbackOnly();
    } catch (SystemException e)  {
      throw new EFapsException(getClass(), "abort.SystemException", e);
    }
  }

  /**
   * Returns a opened connection resource. If a previous close connection
   * resource already exists, this already existing connection resource is
   * returned.
   *
   * @return opened connection resource
   */
  public ConnectionResource getConnectionResource() throws EFapsException  {
    ConnectionResource con = null;

    if (this.connectionStack.isEmpty())  {
try  {
      con = new ConnectionResource(this, getDataSource().getConnection());
} catch (SQLException e)  {
e.printStackTrace();
  throw new EFapsException(getClass(), "getConnectionResource.SQLException", e);
}
      this.connectionStore.add(con);
    } else  {
      con = this.connectionStack.pop();
    }

    con.open();
//System.out.println("getConnectionResource.con="+con);

    return con;
  }

  /**
   *
   */
  public void returnConnectionResource(ConnectionResource _con)  {
//System.out.println("returnConnectionResource.con="+_con);
    if (_con == null)  {
// throw new EFapsException();
    }
    this.connectionStack.push(_con);
  }

  /**
   * @see #getStoreResource(Type,long)
   */
  public StoreResource getStoreResource(final Instance _instance) throws EFapsException  {
    return getStoreResource(_instance.getType(), _instance.getId());
  }

  /**
   *
   */
  public StoreResource getStoreResource(final Type _type, final long _fileId) throws EFapsException  {
    StoreResource storeRsrc = null;

// TODO: dynamic class loading instead of hard coded store resource name
String provider  = _type.getProperty("StoreResource");
if (provider.equals("org.efaps.db.transaction.JDBCStoreResource"))  {
  storeRsrc = new JDBCStoreResource(this, _type, _fileId);
} else  {
  storeRsrc = new VFSStoreResource(this, _type, _fileId);
}
System.out.println("storeRsrc.getContext()="+storeRsrc.getContext());
    storeRsrc.open();
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
  public long getPersonId()  {
    long ret = 1;
    
    if (this.person != null)  {
      ret = this.person.getId();
    }
    return ret;
  }

  /**
   *
   */
  public String getParameter(final String _key)  {
    String value = null;
    if (this.parameters != null)  {
      String[] values = this.parameters.get(_key);
      if ((values != null) && (values.length > 0))  {
        value = values[0];
      }
    }
    return value;
  }


  /////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the getter method for instance variable {@link #connection}.
   *
   * @return value of instance variable {@link #connection}
   * @see #connection
   * @see #setConnection
   */
  public final Connection getConnection()  {
    return this.connection;
  }

  /**
   * This is the setter method for instance variable {@link #connection}.
   *
   * @param _connection new value for instance variable {@link #connection}
   * @see #connection
   * @see #getConnection
   */
  private void setConnection(final Connection _connection)  {
    this.connection = _connection;
  }

  /**
   * This is the getter method for instance variable {@link #transaction}.
   *
   * @return value of instance variable {@link #transaction}
   * @see #transaction
   */
  public final Transaction getTransaction()  {
    return this.transaction;
  }

  /**
   * This is the getter method for instance variable {@link #person}.
   *
   * @return value of instance variable {@link #person}
   * @see #person
   */
  public final Person getPerson()  {
    return this.person;
  }

  /**
   * This is the getter method for instance variable {@link #locale}.
   *
   * @return value of instance variable {@link #locale}
   * @see #locale
   */
  public final Locale getLocale()  {
    return this.locale;
  }

  /**
   * This is the getter method for instance variable {@link #parameters}.
   *
   * @return value of instance variable {@link #parameters}
   * @see #parameters
   */
  public final Map < String, String[] > getParameters()  {
    return this.parameters;
  }

  /**
   * This is the getter method for instance variable {@link #fileParameters}.
   *
   * @return value of instance variable {@link #fileParameters}
   * @see #fileParameters
   */
  public final Map < String, FileItem > getFileParameters()  {
    return this.fileParameters;
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * The method checks if for the current thread a context object is defined.
   * This found context object is returned.
   *
   * @return defined context object of current thread
   * @throws EFapsException if no context object for current thread is defined
   * @see #threadContext
   */
  public static Context getThreadContext() throws EFapsException  {
    Context context = threadContext.get();
    if (context == null)  {
      throw new EFapsException(Context.class,
          "getThreadContext.NoContext4ThreadDefined");
    }
    return context;
  }

  /**
   * For current thread the context object must be set with this static method.
   *
   * @param _context  new eFaps context object to set
   * @throws EFapsException if current thread context is alread set or the new
   *         context is null
   * @see #threadContext
   */
  private static void setThreadContext(final Context _context)
      throws EFapsException  {

    if (threadContext.get() != null)  {
      throw new EFapsException(Context.class,
          "setThreadContext.Context4ThreadAlreadSet");
    }
    if (_context == null)  {
      throw new EFapsException(Context.class,
          "setThreadContext.NewContextIsNull");
    }
    threadContext.set(_context);
  }

  /**
   * For current thread a new context object must be created
   *
   * @param _transaction  transaction of the new thread
   * @return new context of thread
   * @throws EFapsException if current thread context is alread set
   * @see #threadContext
   */
  public static Context newThreadContext(final Transaction _transaction)
      throws EFapsException  {

    return newThreadContext(_transaction, null, null, null, null);
  }

  /**
   * For current thread a new context object must be created
   *
   * @param _transaction  transaction of the new thread
   * @param _userName     name of current user to set
   * @return new context of thread
   * @throws EFapsException if current thread context is alread set
   * @see #threadContext
   */
  public static Context newThreadContext(final Transaction _transaction, 
                        final String _userName)
                throws EFapsException  {
    return newThreadContext(_transaction, _userName, null, null, null);
  }

  /**
   * For current thread a new context object must be created
   *
   * @param _transaction  transaction of the new thread
   * @param _userName     name of current user to set
   * @return new context of thread
   * @throws EFapsException if current thread context is alread set
   * @see #threadContext
   */
  public static Context newThreadContext(final Transaction _transaction, 
                        final String _userName, final Locale _locale)
                throws EFapsException  {

    return newThreadContext(_transaction, _userName, _locale, null, null);
  }

  /**
   * For current thread a new context object must be created
   *
   * @param _transaction    transaction of the new thread
   * @param _userName       name of current user to set
   * @param _locale         locale instance (which langage settings has the 
   *                        user)
   * @param _parameters     map with parameters for this thread context
   * @param _fileParameters map with file parameters
   * @return new context of thread
   * @throws EFapsException if current thread context is alread set
   * @see #threadContext
   */
  public static Context newThreadContext(final Transaction _transaction, 
                        final String _userName, final Locale _locale,
                        final Map < String, String[] > _parameters,
                        final Map < String, FileItem > _fileParameters)
                throws EFapsException  {

    Context context = new Context(_transaction, null, _locale, 
                                  _parameters, _fileParameters);
    setThreadContext(context);
    if (_userName != null)  {
      context.person = Person.get(_userName);
    }
    return context;
  }

  /////////////////////////////////////////////////////////////////////////////
  // static getter and setter methods

  public static void setDbType(final AbstractDatabase _dbType)  {
    dbType = _dbType;
  }

  /**
   * Returns the database type of the default connection (database where the
   * data model definition is stored).
   *
   * @see #dbType
   */
  public static AbstractDatabase getDbType()  {
    return dbType;
  }

  public static void setDataSource(final DataSource _dataSource)  {
    dataSource = _dataSource;
  }
  
  protected static DataSource getDataSource()  {
    return dataSource;
  }
}