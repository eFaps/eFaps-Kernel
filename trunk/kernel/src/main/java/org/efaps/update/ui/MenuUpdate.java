/*
 * Copyright 2003 - 2009 The eFaps Team
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.efaps.update.LinkInstance;

/**
 * @author tmo
 * @author jmox
 * @version $Id$
 * @todo description
 */
public class MenuUpdate extends CommandUpdate {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(MenuUpdate.class);

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /** Link from menu to child command / menu */
  private final static Link LINK2CHILD
      = new OrderedLink("Admin_UI_Menu2Command",
                        "FromMenu",
                        "Admin_UI_Command", "ToCommand");

  /** Link from menu to type as type tree menu */
  private final static Link LINK2TYPE
      = new Link("Admin_UI_LinkIsTypeTreeFor",
                 "From",
                 "Admin_DataModel_Type", "To");

  protected final static Set<Link> ALLLINKS = new HashSet<Link>();
  static  {
    ALLLINKS.add(LINK2CHILD);
    ALLLINKS.add(LINK2TYPE);
    ALLLINKS.addAll(CommandUpdate.ALLLINKS);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public MenuUpdate(final URL _url)
  {
    super(_url, "Admin_UI_Menu", ALLLINKS);
  }

  /**
   *
   * @param _url        URL of the file
   */
  protected MenuUpdate(final URL _url,
                       final String _typeName,
                       final Set<Link> _allLinks)
  {
    super(_url, _typeName, _allLinks);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   *
   * @param _root     root URL
   * @param _url      URL of the file depending of the root URL
   * @return menu update definition read by digester
   */
  public static MenuUpdate readFile(final URL _url)
  {
    MenuUpdate ret = null;

    try {
      final Digester digester = new Digester();
      digester.setValidating(false);

      digester.addCallMethod("ui-menu/definition/update", "setUpdate", 1,
          new Class[] { Boolean.class });
      digester.addCallParam("ui-menu/definition/update", 0);



      ret = (MenuUpdate) digester.parse(_url);

    } catch (final IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (final SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return ret;
  }

  /**
   * Creates new instance of class {@link MenuDefinition}.
   *
   * @return new definition instance
   * @see MenuDefinition
   */
  @Override
  protected AbstractDefinition newDefinition()
  {
    return new MenuDefinition();
  }

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  protected class MenuDefinition extends CommandDefinition {

    ///////////////////////////////////////////////////////////////////////////
    // instance methods

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String,String> _attributes,
                           final String _text)
    {
      final String value = _tags.get(0);
      if ("childs".equals(value))  {
        if (_tags.size() > 1)  {
          final String subValue = _tags.get(1);
          if ("child".equals(subValue))  {
            // assigns / removes child commands / menus to this menu
            if ("remove".equals(_attributes.get("modus")))  {
            } else  {
              final LinkInstance child = new LinkInstance(_text);
              final String order = _attributes.get("order");
              if (order != null)  {
                child.setOrder(Integer.parseInt(order));
              }
              addLink(LINK2CHILD, child);
            }
          } else  {
            super.readXML(_tags, _attributes, _text);
          }
        }
      } else if ("type".equals(value))  {
        // assigns a type the menu for which this menu instance is the type
        // tree menu
        addLink(LINK2TYPE, new LinkInstance(_text));
     } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }
  }
}
