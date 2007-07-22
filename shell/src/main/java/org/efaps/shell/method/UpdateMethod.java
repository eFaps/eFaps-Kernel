/*
 * Copyright 2003-2007 The eFaps Team
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

import java.io.File;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import org.efaps.update.Install;
import org.efaps.util.EFapsException;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public final class UpdateMethod extends AbstractMethod  {
  
  /////////////////////////////////////////////////////////////////////////////
  // static variables

  private final static Option PROPERTY_VERSION  = OptionBuilder
        .withArgName("number")
        .hasArg()
        .withDescription("Defines global import version")
        .create("version");

  /////////////////////////////////////////////////////////////////////////////
  // constructors / desctructors
  
  /**
   *
   */
  public UpdateMethod()  {
    super("update", "updates eFaps instance",
          PROPERTY_VERSION);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   *
   * @todo remove Exception
   */
  public void doMethod() throws EFapsException,Exception {
    reloadCache();
    startTransaction();

    Install install = new Install();
    for (String fileName : getCommandLine().getArgs())  {
      install.addURL(new File(fileName).toURL());
    }

    String versionStr = getCommandLine().getOptionValue("version");
    Long version = null;
    if (versionStr != null)  {
      version = Long.parseLong(versionStr);
    }

    install.install(version);

    commitTransaction();
  }
}
