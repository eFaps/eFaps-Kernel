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

import java.util.HashSet;
import java.util.Set;
import javax.transaction.TransactionManager;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.slide.transaction.SlideTransactionManager;

import org.efaps.db.Context;
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

  static Context context=null;

  final private static Set < AbstractMethod > methods 
                                 = new HashSet  < AbstractMethod > ();
  {
    methods.add(new org.efaps.shell.method.CreateMethod());
    methods.add(new org.efaps.shell.method.GenerateUUIDMethod());
    methods.add(new org.efaps.shell.method.ImportPersonsMethod());
    methods.add(new org.efaps.shell.method.InstallMethod());
    methods.add(new org.efaps.shell.method.ShellMethod());
    methods.add(new org.efaps.shell.method.UpdateMethod());
  }


  /**
   * Main entry point.
   */
  public static void main(String _args[]) throws Exception  {
    (new Shell()).run(_args);
  }



  private void run(final String... _args)  {
    AbstractMethod method = null;
    for (String arg : _args)  {
      for (AbstractMethod search : methods)  {
        if (search.getOptionName().equals(arg.substring(1)))  {
          method = search;
          break;
        }
      }
      if (method != null)  {
        break;
      }
    }
    
    if (method != null)  {
      try {
        if (method.init(_args))  {
          method.execute();
        }
      } catch (Throwable e)  {
        e.printStackTrace();
      }
    } else  {
      OptionGroup optionGroup = new OptionGroup();
      for (AbstractMethod methodHelp : methods)  {
        optionGroup.addOption(new Option(methodHelp.getOptionName(), methodHelp.getOptionDescription()));
      }
      Options options = new Options();
      options.addOption("help", 
                        false,
                        "print this text; "
                                + "use it together with an option to get help");
      options.addOptionGroup(optionGroup);
      (new HelpFormatter()).printHelp("eFaps", options);
    }
  }
  
  
static public void setContext(Context _context)  {
context=_context;
}

static public Context getContext()  {
return context;
}

}
