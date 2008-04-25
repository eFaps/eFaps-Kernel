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

package org.efaps.update.common;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.efaps.db.Insert;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author jmox
 * @version $Id$
 */
public class SystemAttributeUpdate extends AbstractUpdate {

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(SystemAttributeUpdate.class);

  public SystemAttributeUpdate() {
    super("Admin_Common_SystemAttribute");
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * Method that reads the given XML-File and than uses the
   * <code>org.apache.commons.digester</code> to create the different Class
   * and invokes the Methods to Update a SystemAttribute
   *
   * @param _url
   *                XML-File to be read by the digester
   * @return SystemAttributeUpdate Definition read by digester
   */
  public static SystemAttributeUpdate readXMLFile(final URL _root, final URL _url)
  {
    SystemAttributeUpdate ret = null;
    try {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("common-systemattribute",
          SystemAttributeUpdate.class);

      // set the UUID for the SystemAtribute
      digester.addCallMethod("common-systemattribute/uuid", "setUUID", 1);
      digester.addCallParam("common-systemattribute/uuid", 0);

      // add a new Definition for the SystemAtribute
      digester.addObjectCreate("common-systemattribute/definition",
          Definition.class);
      digester.addSetNext("common-systemattribute/definition", "addDefinition");

      // set the Version of the SystemAtribute-Definition
      digester.addCallMethod("common-systemattribute/definition/version",
          "setVersion", 4);
      digester.addCallParam(
          "common-systemattribute/definition/version/application", 0);
      digester.addCallParam("common-systemattribute/definition/version/global",
          1);
      digester.addCallParam("common-systemattribute/definition/version/local",
          2);
      digester
          .addCallParam("common-systemattribute/definition/version/mode", 3);

      // set the Name of the SystemAtribute-Definition
      digester.addCallMethod("common-systemattribute/definition/name",
          "setName", 1);
      digester.addCallParam("common-systemattribute/definition/name", 0);

      // set the Value for the SystemAtribute-Definition
      digester.addCallMethod("common-systemattribute/definition/value",
          "addValue", 1);
      digester.addCallParam("common-systemattribute/definition/value", 0);

      ret = (SystemAttributeUpdate) digester.parse(_url);

    } catch (IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return ret;
  }

  public static class Definition extends AbstractDefinition {

    /**
     * Because the attribute 'Value' of the system attribute is a required
     * attribute, the attribute value is also set for the create.
     *
     * @param _insert  insert instance
     */
    @Override
    protected void createInDB(final Insert _insert) throws EFapsException
    {
      _insert.add("Value", getValue("Value"));
      super.createInDB(_insert);
    }

    public void addValue(final String _value) {
      super.addValue("Value", _value);
    }
  }
}
