/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.update.integration;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

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
public class WebDAVUpdate extends AbstractUpdate {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(WebDAVUpdate.class);

  private final static Set<Link> ALLLINKS = new HashSet<Link>();
  {
    /*
     * ALLLINKS.add(LINK2ACCESSTYPE); ALLLINKS.add(LINK2DATAMODELTYPE);
     * ALLLINKS.add(LINK2PERSON); ALLLINKS.add(LINK2ROLE);
     * ALLLINKS.add(LINK2GROUP);
     */
  }

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * 
   */
  public WebDAVUpdate() {
    super("Admin_Integration_WebDAV", ALLLINKS);
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  public static WebDAVUpdate readXMLFile(final URL _url)  {
    WebDAVUpdate ret = null;

    try {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("integration-webdav", WebDAVUpdate.class);

      digester.addCallMethod("integration-webdav/uuid", "setUUID", 1);
      digester.addCallParam("integration-webdav/uuid", 0);

      digester.addObjectCreate("integration-webdav/definition",
          Definition.class);
      digester.addSetNext("integration-webdav/definition", "addDefinition");

      digester.addCallMethod("integration-webdav/definition/version",
          "setVersion", 4);
      digester.addCallParam(
          "integration-webdav/definition/version/application", 0);
      digester.addCallParam("integration-webdav/definition/version/global", 1);
      digester.addCallParam("integration-webdav/definition/version/local", 2);
      digester.addCallParam("integration-webdav/definition/version/mode", 3);

      digester
          .addCallMethod("integration-webdav/definition/name", "setName", 1);
      digester.addCallParam("integration-webdav/definition/name", 0);

      digester
          .addCallMethod("integration-webdav/definition/path", "setPath", 1);
      digester.addCallParam("integration-webdav/definition/path", 0);

      digester.addCallMethod("integration-webdav/definition/property",
          "addProperty", 2);
      digester
          .addCallParam("integration-webdav/definition/property", 0, "name");
      digester.addCallParam("integration-webdav/definition/property", 1);

      ret = (WebDAVUpdate) digester.parse(_url);

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
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends AbstractDefinition {

    /**
     * 
     * @see #values
     */
    public void setPath(final String _path) {
      addValue("Path", _path);
    }
  }
}
