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

package org.efaps.update.access;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.efaps.update.AbstractUpdate;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class AccessTypeUpdate extends AbstractUpdate {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(AccessTypeUpdate.class);

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * 
   */
  public AccessTypeUpdate() {
    super("Admin_Access_AccessType");
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  public static AccessTypeUpdate readXMLFile(final URL _url)  {
    AccessTypeUpdate ret = null;

    try {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("access-type", AccessTypeUpdate.class);

      digester.addCallMethod("access-type/uuid", "setUUID", 1);
      digester.addCallParam("access-type/uuid", 0);

      digester.addObjectCreate("access-type/definition", Definition.class);
      digester.addSetNext("access-type/definition", "addDefinition");

      digester.addCallMethod("access-type/definition/version", "setVersion", 4);
      digester.addCallParam("access-type/definition/version/application", 0);
      digester.addCallParam("access-type/definition/version/global", 1);
      digester.addCallParam("access-type/definition/version/local", 2);
      digester.addCallParam("access-type/definition/version/mode", 3);

      digester.addCallMethod("access-type/definition/name", "setName", 1);
      digester.addCallParam("access-type/definition/name", 0);

      ret = (AccessTypeUpdate) digester.parse(_url);

      if (ret != null) {
        ret.setURL(_url);
      }
    } catch (IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends AbstractDefinition {
  }
}
