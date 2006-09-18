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

package org.efaps.js;

import java.io.StringReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Properties;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.slide.transaction.SlideTransactionManager;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.Main;

import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.shell.method.AbstractMethod;

/**
 * The shell program.
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Shell {

  /**
   * Theoretically all efaps contexts object instances must include a
   * transaction manager.
   */
  final public static TransactionManager transactionManager = new SlideTransactionManager();

  /**
   * Main entry point.
   *
   * Process arguments as would a normal Java program. Also
   * create a new Context and associate it with the current thread.
   * Then set up the execution environment and begin to
   * execute scripts.
   *
   * @todo using org.efaps.shell.method.* classes as parameter definitions
   *       (first letter of class name in lower case and withou Method at the 
   *       end)
   */
  public static void main(String _args[]) throws Exception  {

// read input arguments
String bootstrap = null;
Class < AbstractMethod > methodClass = null;
String[] args = null;
for (int i = 0; i < _args.length; i++)  {
  if (_args[i].equals("-bootstrap"))  {
    bootstrap = _args[++i];
  } else  {
    String className = "org.efaps.shell.method." 
            + _args[i].substring(1,2).toUpperCase()
            + _args[i].substring(2)
            + "Method";

    try  {
      methodClass = (Class < AbstractMethod >)Class.forName(className);
      args = new String[_args.length - i - 1];
      for (int j = i + 1, k = 0; j < _args.length; j++, k++)  {
        args[k] = _args[j];
      }
      break;
    } catch (ClassNotFoundException e)  {
      throw new Exception("unknown parameter "+_args[i]);
    }
  }
}

if (bootstrap==null)  {
  throw new Exception("Unknown Bootstrap.");
}

// read bootstrap properties
Properties props = new Properties();
FileInputStream fstr = new FileInputStream(bootstrap);
props.loadFromXML(fstr);
fstr.close();

// buildup reference
String factory = props.get("factory").toString();
Reference ref = new Reference(DataSource.class.getName(), factory, null);
for (Object key : props.keySet())  {
  Object value = props.get(key);
   ref.add(new StringRefAddr(key.toString(), (value==null ? null : value.toString())));
}

// configure database type
Object dbTypeObj = props.get("dbType");
if ((dbTypeObj == null) || (dbTypeObj.toString().length() == 0))  {
  throw new Exception("could not initaliase database type");
}
AbstractDatabase dbType = ((Class<AbstractDatabase>)Class.forName(dbTypeObj.toString())).newInstance();
if (dbType == null)  {
  throw new Exception("could not initaliase database type");
}
Context.setDbType(dbType);

// get datasource object
ObjectFactory of = (ObjectFactory)(Class.forName(ref.getFactoryClassName())).newInstance();
DataSource ds = (DataSource)of.getObjectInstance(ref, null, null, null);
Context.setDataSource(ds);

//context = new Context();


if (methodClass != null)  {
  try {
    AbstractMethod method = methodClass.newInstance();
    method.setArguments(args);
    method.execute();
  } catch (Throwable e)  {
    e.printStackTrace();
  }
/*} else  {
org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();

Global global = Main.getGlobal();
ScriptableObject.defineClass(global, EFapsInstance.class);

// run init javascript file
ClassLoader classLoader = Shell.class.getClassLoader();
Reader in = new InputStreamReader(classLoader.getResourceAsStream("org/efaps/js/Init.js"));
Main.evaluateScript(cx, global, in, null, "Init", 1, null);

context = new Context(Person.get("Administrator"));
  StringReader reader = new StringReader("shell()");
  Main.evaluateScript(cx, Main.getGlobal(), reader, null, "<stdin>", 0, null);
*/
}



  }

static Context context=null;


static public void setContext(Context _context)  {
context=_context;
}

static public Context getContext()  {
return context;
}

}
