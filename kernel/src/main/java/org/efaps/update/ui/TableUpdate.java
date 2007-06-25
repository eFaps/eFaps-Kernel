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

import java.io.File;
import java.io.IOException;

import org.apache.commons.digester.Digester;

import org.xml.sax.SAXException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class TableUpdate extends AbstractCollectionUpdate  {

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

  public static TableUpdate readXMLFile(final String _fileName) throws IOException  {
//    } catch (IOException e)  {
//      LOG.error("could not open file '" + _fileName + "'", e);
    return readXMLFile(new File(_fileName));
  }

  public static TableUpdate readXMLFile(final File _file) throws IOException  {
    TableUpdate ret = null;

    try {
      Digester digester = createDigester("ui-table", TableUpdate.class);
      ret = (TableUpdate) digester.parse(_file);

      if (ret != null) {
        ret.setFile(_file);
      }
    } catch (SAXException e) {
      e.printStackTrace();
      // LOG.error("could not read file '" + _fileName + "'", e);
    }
    return ret;
  }
}
