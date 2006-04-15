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

/**
 * The shell program.
 *
 * Can execute scripts interactively or in batch mode at the command line.
 * An example of controlling the JavaScript engine.
 *
 * @author Norris Boyd
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
   */
  public static void main(String _args[]) throws Exception  {

// read input arguments
boolean create = false;
boolean shell = false;
String bootstrap = null;
for (int i=0; i<_args.length; i++)  {
  if (_args[i].equals("-bootstrap"))  {
    bootstrap = _args[++i];
  } else if (_args[i].equals("-create"))  {
    create = true;
  } else if (_args[i].equals("-shell"))  {
    shell = true;
  } else  {
    throw new Exception("unknown parameter "+_args[i]);
  }
}

if (bootstrap==null)  {
  throw new Exception("Unknown Bootstrap.");
}

// read properties
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


//System.setProperty("java.util.logging.config.file", "logging.properties");

org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();

Global global = Main.getGlobal();
ScriptableObject.defineClass(global, EFapsInstance.class);

// run init javascript file
ClassLoader classLoader = Shell.class.getClassLoader();
Reader in = new InputStreamReader(classLoader.getResourceAsStream("org/efaps/js/Init.js"));
Main.evaluateScript(cx, global, in, null, "Init", 1, null);


context = new Context();


if (create)  {
  StringReader reader = new StringReader("eFapsCreateAll();");
try {
  Main.evaluateScript(cx, Main.getGlobal(), reader, null, "<stdin>", 0, null);
} catch (Throwable e)  {
  e.printStackTrace();
}

} else if (shell)  {
  context = new Context(Person.get("Administrator"));
  int result = Main.exec(new String[0]);
  if (result != 0)  {
    System.exit(result);
  }
} else  {
  context = new Context(Person.get("Administrator"));
  StringReader reader = new StringReader("shell()");
  Main.evaluateScript(cx, Main.getGlobal(), reader, null, "<stdin>", 0, null);
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
