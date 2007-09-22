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

package org.efaps.update.ui;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class TableUpdate extends AbstractCollectionUpdate  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(TableUpdate.class);

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public TableUpdate() {
    super("Admin_UI_Table");
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  public static TableUpdate readXMLFile(final URL _url)  {
    TableUpdate ret = null;

    try {
      Digester digester = createDigester("ui-table", TableUpdate.class);
      ret = (TableUpdate) digester.parse(_url);

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
}
