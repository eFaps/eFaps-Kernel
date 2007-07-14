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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class SearchUpdate extends MenuUpdate  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

 

  /** Link from search to default search command */
  private final static Link LINK2DEFAULTCMD = new Link("Admin_UI_LinkDefaultSearchCommand",
                                                       "From",
                                                       "Admin_UI_Command",
                                                       "To");

  protected final static Set <Link> ALLLINKS = new HashSet < Link > ();  {
    ALLLINKS.add(LINK2DEFAULTCMD);
    ALLLINKS.addAll(MenuUpdate.ALLLINKS);
  }

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public SearchUpdate() {
    super("Admin_UI_Search", ALLLINKS);
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  public static SearchUpdate readXMLFile(final String _fileName) throws IOException  {
//    } catch (IOException e)  {
//      LOG.error("could not open file '" + _fileName + "'", e);
    return readXMLFile(new File(_fileName));
  }

  public static SearchUpdate readXMLFile(final File _file) throws IOException  {
    SearchUpdate ret = null;

    try  {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("ui-search", SearchUpdate.class);

      digester.addCallMethod("ui-search/uuid", "setUUID", 1);
      digester.addCallParam("ui-search/uuid", 0);

      digester.addObjectCreate("ui-search/definition", SearchDefinition.class);
      digester.addSetNext("ui-search/definition", "addDefinition");

      digester.addCallMethod("ui-search/definition/version", "setVersion", 4);
      digester.addCallParam("ui-search/definition/version/application", 0);
      digester.addCallParam("ui-search/definition/version/global", 1);
      digester.addCallParam("ui-search/definition/version/local", 2);
      digester.addCallParam("ui-search/definition/version/mode", 3);
      
      digester.addCallMethod("ui-search/definition/name", "setName", 1);
      digester.addCallParam("ui-search/definition/name", 0);

      digester.addCallMethod("ui-search/definition/icon", "assignIcon", 1);
      digester.addCallParam("ui-search/definition/icon", 0);

      digester.addCallMethod("ui-search/definition/childs/child", "assignChild", 1);
      digester.addCallParam("ui-search/definition/childs/child", 0);

      digester.addCallMethod("ui-search/definition/property", "addProperty", 2);
      digester.addCallParam("ui-search/definition/property", 0, "name");
      digester.addCallParam("ui-search/definition/property", 1);

      digester.addCallMethod("ui-search/definition/default/command", "assignDefaultCMD", 1);
      digester.addCallParam("ui-search/definition/default/command", 0);

      ret = (SearchUpdate) digester.parse(_file);

      if (ret != null)  {
        ret.setFile(_file);
      }
    } catch (SAXException e)  {
e.printStackTrace();
      //      LOG.error("could not read file '" + _fileName + "'", e);
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class SearchDefinition extends MenuDefinition  {
    
    ///////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * Assigns a command as default for the serch menu
     * 
     * @param _defaultCmd name of the default command used for the search
     */
    public void assignDefaultCMD(final String _defaultCmd) {
      addLink(LINK2DEFAULTCMD, _defaultCmd);
    }
  }
}
