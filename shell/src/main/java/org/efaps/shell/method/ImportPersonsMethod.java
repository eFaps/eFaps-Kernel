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

import org.efaps.jaas.ImportHandler;
import org.efaps.util.EFapsException;

/**
 * The class is used to start the import of persons directly as parameter from
 * the eFaps shell.<br/>
 * To start the import, call the shell with
 * <code>shell.sh -importPersons</code><br/>
 * Following Java system properties must / could be set to configure the
 * import:
 * <dl>
 *   <dt>java.security.auth.login.config</dt>
 *   <dd>Defines the JAAS configuration file.</dd>
 *   <dt>org.efaps.shell.method.ImportPersonsMethod.Application</dt>
 *   <dd>Defines the name of the JAAS application defined in the JAAS 
 *       configuration used to import.</dd>
 * </dl>
 *
 * @author tmo
 * @version $Id$
 */
public final class ImportPersonsMethod extends AbstractMethod  {
  
  /////////////////////////////////////////////////////////////////////////////
  // static variables
  
  /**
   * The static variable defines the Java system property name to define
   * the JAAS application name.
   */
  private final static String PROP_NAME_APPLICATION
                    = "org.efaps.shell.method.ImportPersonsMethod.Application";

  /**
   * The static variable defines the default JAAS application name. It value
   * is used if no Java system property is defined for 
   * {@link #PROP_NAME_APPLICATION}.
   */
  private final static String DEFAULT_APPLICATION = "eFaps";

/*  private final static Option PROPERTY_APPLICATION  = OptionBuilder
        .withArgName( PROP_NAME_APPLICATION + "=value" )
        .hasArg()
        .withValueSeparator()
        .withDescription("Defines the name of the JAAS application defined in "
                         + "the JAAS configuration used to import. "
                         + "The default value is "
                         + "'" + DEFAULT_APPLICATION + "'.")
        .create( "D" + PROP_NAME_APPLICATION );

  private final static Option PROPERTY_JAAS  = OptionBuilder
        .withArgName( "=value" )
        .withArgName( PROP_NAME_APPLICATION + "=value" )
        .hasArg()
        .withValueSeparator()
        .withDescription("Defines the JAAS configuration file.")
        .create( "Djava.security.auth.login.config" );
*/

  /////////////////////////////////////////////////////////////////////////////
  // constructors / desctructors
  
  /**
   *
   */
  public ImportPersonsMethod()  {
    super("importPersons", 
          "imports all persons via JAAS");
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The import of persons is started. First the cache is reloaded and then
   * the import itself is done.
   *
   * @todo remove Exception
   */
  public void doMethod() throws EFapsException,Exception {

    String appl = System.getProperty(PROP_NAME_APPLICATION, 
                                     DEFAULT_APPLICATION);
    reloadCache();
    startTransaction();
    (new ImportHandler(appl)).importPersons();
    commitTransaction();
  }
}
