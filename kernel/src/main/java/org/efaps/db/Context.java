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

package org.efaps.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.HashSet;
import java.util.Stack;

import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

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

  /**
   * Static variable storing the database type.
   */
  private static AbstractDatabase dbType = null;

  /**
   * Each thread has his own context object. The value is automatically
   * assigned from the filter class.
   */
  private static ThreadLocal<Context>threadContext = new ThreadLocal<Context>();

  public static void setDbType(AbstractDatabase _dbType) throws ClassNotFoundException, IllegalAccessException {
System.out.println("---------------- DBType = "+_dbType);
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


private static DataSource dataSource = null;

public static void setDataSource(final DataSource _dataSource)  {
  dataSource = _dataSource;
}

protected static DataSource getDataSource()  {
  return dataSource;
}


  /**
   * Stores all created connection resources.
   */
  private HashSet<ConnectionResource> connectionStore =
                                      new HashSet<ConnectionResource>();

  /**
   * Stack used to store returned connections for reuse.
   */
  private Stack<ConnectionResource> connectionStack =
                                      new Stack<ConnectionResource>();

  /**
   * Stores all created store resources.
   */
  private HashSet<StoreResource> storeStore = new HashSet<StoreResource>();


  /**
   * Default constructor used to get a SQL connection to the database.
   */
  public Context() throws NamingException, SQLException  {
    this((Person)null, (Locale)null);
  }

  /**
   * Constructor for
   *
   * @see #person
   */
  public Context(final Person _person) throws NamingException, SQLException  {
    this(_person, (Locale)null);
  }

  /**
   * Constructor for
   *
   * @see #person
   * @see #locale
   */
  public Context(final Person _person, final Locale _locale) throws NamingException,SQLException  {
System.out.println("--------------------------------- new context");
//    InitialContext initCtx = new InitialContext();
//    javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
//    DataSource ds = (DataSource) envCtx.lookup("jdbc/eFaps");
//    setConnection(ds.getConnection());
    setConnection(getDataSource().getConnection());
getConnection().setAutoCommit(true);
setPerson(_person);
setLocale(_locale);

System.out.println("++++++++++++++++++++++++++ connection.closed=" + getConnection().isClosed()+":_locale="+_locale);

  }

private Transaction transaction = null;
  /**
   * Constructor for
   *
   * @see #person
   * @see #locale
   */
  public Context(final Transaction _transaction, final Person _person, final Locale _locale) throws NamingException,SQLException  {
System.out.println("--------------------------------- new context");
this.transaction = _transaction;
    setConnection(getDataSource().getConnection());
getConnection().setAutoCommit(true);
setPerson(_person);
setLocale(_locale);
System.out.println("++++++++++++++++++++++++++ connection.closed=" + getConnection().isClosed()+":_locale="+_locale);
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
}
      this.connectionStore.add(con);
    } else  {
      con =this.connectionStack.pop();
    }

    con.open();
System.out.println("getConnectionResource.con="+con);

    return con;
  }

  /**
   *
   */
  public void returnConnectionResource(ConnectionResource _con)  {
System.out.println("returnConnectionResource.con="+_con);
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

String provider  = _type.getProperty("VFSProvider");
if (provider.equals("org.efaps.db.vfs.provider.sqldatabase.SQLDataBaseFileProvider"))  {
  storeRsrc = new JDBCStoreResource(this, _type, _fileId);
} else  {
  storeRsrc = new VFSStoreResource(this, _type, _fileId);
}
System.out.println("storeRsrc.getContext()="+storeRsrc.getContext());
    storeRsrc.open();
    return storeRsrc;
  }

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
  public static void setThreadContext(final Context _context)
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

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the instance variable for the SQL Connection to the database.
   *
   * @see #getConnection
   * @see #setConnection
   */
  private Connection connection = null;

  /**
   * This instance variable represents the user name of the context.
   */
  private Person person = null;

  /**
   * The instance variable stores the locale object defined by the user
   * interface (locale object of the current logged in eFaps user).
   * The information is needed to create localised information within eFaps.
   *
   * @see #getLocale
   * @see #setLocale
   */
  private Locale locale = null;

  /////////////////////////////////////////////////////////////////////////////

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
   * @see #setPerson
   */
  public final Person getPerson()  {
    return this.person;
  }

  /**
   * This is the setter method for instance variable {@link #person}.
   *
   * @param _person new value for instance variable {@link #person}
   * @see #person
   * @see #getPerson
   */
  private void setPerson(final Person _person)  {
    this.person = _person;
  }

  /**
   * This is the getter method for instance variable {@link #locale}.
   *
   * @return value of instance variable {@link #locale}
   * @see #locale
   * @see #setLocale
   */
  public final Locale getLocale()  {
    return this.locale;
  }

  /**
   * This is the setter method for instance variable {@link #locale}.
   *
   * @param _locale new value for instance variable {@link #locale}
   * @see #locale
   * @see #getLocale
   */
  private void setLocale(final Locale _locale)  {
    this.locale = _locale;
  }
}