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

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.event.EventType;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.update.event.Event;

/**
 * This Class is responsible for the Update of "Command" in the Database.<br/>It
 * reads with <code>org.apache.commons.digester</code> a XML-File to create
 * the <code>CommandDefinition</code>. It is than inserted into the Database
 * by the SuperClass <code>AbstractUpdate</code>.
 *
 * @author tmo
 * @version $Id$
 */
public class CommandUpdate extends AbstractUpdate
{

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /** Link from UI object to role */
  private final static Link LINK2ACCESSROLE   = new Link("Admin_UI_Access",
                                                         "UILink",
                                                         "Admin_User_Role", "UserLink");

  /** Link from command to icon */
  private final static Link LINK2ICON         = new Link("Admin_UI_LinkIcon",
                                                         "From",
                                                         "Admin_UI_Image", "To");

  /** Link from command to table as target */
  private final static Link LINK2TARGETTABLE  = new Link("Admin_UI_LinkTargetTable",
                                                         "From",
                                                         "Admin_UI_Table", "To");

  /** Link from command to form as target */
  private final static Link LINK2TARGETFORM   = new Link("Admin_UI_LinkTargetForm",
                                                         "From",
                                                         "Admin_UI_Form", "To");

  /** Link from command to menu as target */
  private final static Link LINK2TARGETMENU   = new Link("Admin_UI_LinkTargetMenu",
                                                         "From",
                                                         "Admin_UI_Menu", "To");

  /** Link from command to search as target */
  private final static Link LINK2TARGETSEARCH = new Link("Admin_UI_LinkTargetSearch",
                                                         "From",
                                                         "Admin_UI_Search", "To");

  /**
   * Set of all links used by commands.s
   */
  protected final static Set<Link> ALLLINKS = new HashSet<Link>();
  static  {
    ALLLINKS.add(LINK2ACCESSROLE);
    ALLLINKS.add(LINK2ICON);
    ALLLINKS.add(LINK2TARGETTABLE);
    ALLLINKS.add(LINK2TARGETFORM);
    ALLLINKS.add(LINK2TARGETMENU);
    ALLLINKS.add(LINK2TARGETSEARCH);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Default contractor used by the XML parser for initialize a command.
   *
   * @param _url        URL of the file
   */
  public CommandUpdate(final URL _url)
  {
    super(_url, "Admin_UI_Command", ALLLINKS);
  }

  /**
   *
   */
  protected CommandUpdate(final URL _url,
                          final String _typeName,
                          final Set<Link> _allLinks)
  {
    super(_url, _typeName, _allLinks);
  }

  /**
   * Creates new instance of class {@link CommandDefinition}.
   *
   * @return new definition instance
   * @see CommandDefinition
   */
  @Override
  protected AbstractDefinition newDefinition()
  {
    return new CommandDefinition();
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  protected class CommandDefinition extends AbstractDefinition {

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String,String> _attributes,
                           final String _text)
    {
      final String value = _tags.get(0);
      if ("access".equals(value))  {
        if (_tags.size() > 1)  {
          final String subValue = _tags.get(1);
          if ("role".equals(subValue))  {
            // Assigns a role for accessing this command.
            addLink(LINK2ACCESSROLE,new LinkInstance(_text));
          } else  {
            super.readXML(_tags, _attributes, _text);
          }
        }
      } else if ("icon".equals(value))  {
        // Assigns an image
        addLink(LINK2ICON,new LinkInstance(_text));
      } else if ("target".equals(value))  {
        if (_tags.size() == 2)  {
          final String subValue = _tags.get(1);
          if ("evaluate".equals(subValue))  {
            this.events.add(new Event(_attributes.get("name"),
                                      EventType.UI_TABLE_EVALUATE,
                                      _attributes.get("program"),
                                      _attributes.get("method"),
                                      _attributes.get("index")));
          } else if ("execute".equals(subValue))  {
            this.events.add(new Event(_attributes.get("name"),
                                      EventType.UI_COMMAND_EXECUTE,
                                      _attributes.get("program"),
                                      _attributes.get("method"),
                                      _attributes.get("index")));
          } else if ("form".equals(subValue))  {
            // assigns a form as target for this command definition.
            addLink(LINK2TARGETFORM, new LinkInstance(_text));
          } else if ("menu".equals(subValue))  {
            // assigns a menu as target for this command definition.
            addLink(LINK2TARGETMENU, new LinkInstance(_text));
          } else if ("search".equals(subValue))  {
            // assigns a search menu as target for this command definition.
            addLink(LINK2TARGETSEARCH, new LinkInstance(_text));
          } else if ("table".equals(subValue))  {
            // assigns a table as target for this command definition.
            addLink(LINK2TARGETTABLE,new LinkInstance(_text) );
          } else if ("trigger".equals(subValue))  {
            this.events.add(new Event(_attributes.get("name"),
                                      EventType.valueOf(_attributes.get("event")),
                                      _attributes.get("program"),
                                      _attributes.get("method"),
                                      _attributes.get("index")));
          } else if ("validate".equals(subValue))  {
            this.events.add(new Event(_attributes.get("name"),
                                      EventType.UI_VALIDATE,
                                      _attributes.get("program"),
                                      _attributes.get("method"),
                                      _attributes.get("index")));
          } else  {
            super.readXML(_tags, _attributes, _text);
          }
        } else if (_tags.size() == 3)  {
          final String subValue = _tags.get(1);
          if (("evaluate".equals(subValue)
                || "execute".equals(subValue)
                || "trigger".equals(subValue)
                || "validate".equals(subValue)) && "property".equals(_tags.get(2))) {
            this.events.get(this.events.size() - 1).addProperty(_attributes.get("name"),
                                                                _text);
          } else  {
            super.readXML(_tags, _attributes, _text);
          }
        } else if (_tags.size() > 2)  {
          // if size == 1, the target tag could be ignored
          super.readXML(_tags, _attributes, _text);
        }
      } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }
  }

}
