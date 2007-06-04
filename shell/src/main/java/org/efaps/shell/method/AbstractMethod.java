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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.js.Shell;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public abstract class AbstractMethod  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(AbstractMethod.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the login name of the current user.
   */
  private String userName = null;

  /**
   * All options which could be defined in this method implementation.
   */
  private final Options options = new Options();

  /**
   * The name of the option which is used for this implementation of this
   * method.
   */
  private final String optionName;
  
  /**
   * The description of the option which is used for this implementation for 
   * this method. 
   */
  private final String optionDescription;

  /**
   * All arguments given to the shell after defining the method are stored
   * in this instance variable. The value is automatically set by the shell.
   */
  private CommandLine commandLine = null;

  /////////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  /**
   * @param _optionName name of the option used to call this method
   * @param _optionDesc description of the option to call this method
   * @param _options    all allowed options implemented by this method
   */
  protected AbstractMethod(final String _optionName,
                           final String _optionDesc,
                           final Option... _options)  {
    this.optionName = _optionName;
    this.optionDescription = _optionDesc;
    this.options.addOption(this.optionName, false, this.optionDescription);
    this.options.addOption(
      OptionBuilder.withArgName("file")
                   .hasArg()
                   .withDescription("defines the bootstrap file")
                   .isRequired()
                   .create("bootstrap")
    );
    this.options.addOption(new Option("help", "print this message"));
    for (Option option : _options)  {
      this.options.addOption(option);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The method parses the command line parameters into {@link #commandLine}
   * defined in the arguments. With this options, the bootstrap file is read
   * and the database connection is set by method {@link #initDatabase}.
   *
   * @param _args arguments to set options
   * @see #initDatabase
   * @see #commandLine
   */
  public boolean init(final String... _args)  {
    boolean initialised = false;
    
    try  {
      // parse command line
      CommandLineParser parser = new GnuParser();
      this.commandLine = parser.parse(this.options, _args);
      initialised = true;
    } catch (ParseException e)  {
      LOG.error("could not parse commandline parameters", e);
    }
    if (initialised)  {
      initialised = initDatabase();
    }
    return initialised;
  }

  /**
   * Initiliase the database information read from the bootstrap file:
   * <ul>
   * <li>read boostrap properties from bootstrap file</li>
   * <li>configure the database type</li>
   * <li>initiliase the sql datasource (JDBC connection to the database)</li>
   * </ul>
   */
  protected boolean initDatabase()  {
    boolean initialised = false;
    String bootstrap = this.commandLine.getOptionValue("bootstrap");

    Properties props = new Properties();
    try  {
      // read bootstrap properties
      FileInputStream fstr = new FileInputStream(bootstrap);
      props.loadFromXML(fstr);
      fstr.close();
    } catch (FileNotFoundException e)  {
      LOG.error("could not open file '" + bootstrap + "'", e);
    } catch (IOException e)  {
      LOG.error("could not read file '" + bootstrap + "'", e);
    }

    // configure database type
    String dbClass   = null;
    try  {
      Object dbTypeObj = props.get("dbType");
      if ((dbTypeObj == null) || (dbTypeObj.toString().length() == 0))  {
        LOG.error("could not initaliase database type");
      } else  {
        dbClass = dbTypeObj.toString();
        AbstractDatabase dbType = ((Class<AbstractDatabase>)Class.forName(dbClass)).newInstance();
        if (dbType == null)  {
          LOG.error("could not initaliase database type");
        }
        Context.setDbType(dbType);
        initialised = true;
      }
    } catch (ClassNotFoundException e)  {
      LOG.error("could not found database description class "
                + "'" + dbClass + "'", e);
    } catch (InstantiationException e)  {
      LOG.error("could not initialise database description class "
                + "'" + dbClass + "'", e);
    } catch (IllegalAccessException e)  {
      LOG.error("could not access database description class "
                + "'" + dbClass + "'", e);
    }

    // buildup reference and initialise datasource object
    String factory = props.get("factory").toString();
    Reference ref = new Reference(DataSource.class.getName(), factory, null);
    for (Object key : props.keySet())  {
      Object value = props.get(key);
      ref.add(new StringRefAddr(key.toString(), 
                                (value == null) ? null : value.toString()));
    }
    ObjectFactory of = null;
    try  {
      Class factClass = Class.forName(ref.getFactoryClassName());
      of = (ObjectFactory) factClass.newInstance();
    } catch (ClassNotFoundException e)  {
      LOG.error("could not found data source class "
                + "'" + ref.getFactoryClassName() + "'", e);
    } catch (InstantiationException e)  {
      LOG.error("could not initialise data source class "
                + "'" + ref.getFactoryClassName() + "'", e);
    } catch (IllegalAccessException e)  {
      LOG.error("could not access data source class "
                + "'" + ref.getFactoryClassName() + "'", e);
    }
    if (of != null)  {
      DataSource ds = null;
      try  {
        ds = (DataSource) of.getObjectInstance(ref, null, null, null);
      } catch (Exception e)  {
        LOG.error("coud not get object instance of factory "
                  + "'" + ref.getFactoryClassName() + "'", e);
      }
      if (ds != null)  {
        Context.setDataSource(ds);
        initialised = initialised && true;
      }
    }
    
    return initialised;
  }
  
  /**
   * @todo remove Exception
   * @todo description
   */
  public void execute() throws EFapsException,Exception  {
    if (getCommandLine().hasOption("help"))  {
      printHelp();
    } else  {
      doMethod();
    }
  }
  
  /**
   *
   */
  public void printHelp()  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(this.optionName, this.options);
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
    RunLevel.init("shell");
    RunLevel.execute();
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
   * This is the getter method for instance variable {@link #optionName}.
   *
   * @return value of instance variable {@link #optionName}
   * @see #optionName
   */
  public final String getOptionName()  {
    return this.optionName;
  }

  /**
   * This is the getter method for instance variable {@link #optionDescription}.
   *
   * @return value of instance variable {@link #optionDescription}
   * @see #optionDescription
   */
  public final String getOptionDescription()  {
    return this.optionDescription;
  }

  /**
   * This is the getter method for instance variable {@link #commandLine}.
   *
   * @return value of instance variable {@link #commandLine}
   * @see #commandLine
   */
  protected final CommandLine getCommandLine()  {
    return this.commandLine;
  }
}
