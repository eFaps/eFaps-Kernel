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

package org.efaps.update.ui;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.update.event.EventFactory;

/**
 * This Class is responsible for the Update of "Command" in the Database.<br/>It
 * reads with <code>org.apache.commons.digester</code> a XML-File to create
 * the <code>CommandDefinition</code>. It is than inserted into the Database
 * by the SuperClass <code>AbstractUpdate</code>.
 *
 * @author tmo
 * @version $Id$
 */
public class CommandUpdate extends AbstractUpdate {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(CommandUpdate.class);

  /** Link from UI object to role */
  private final static Link LINK2ACCESSROLE   = new Link("Admin_UI_Access",
                                                         "UILink",
                                                         "Admin_User_Role",
                                                         "UserLink");

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
                                                         "Admin_UI_Search",
                                                         "To");

  protected final static Set<Link> ALLLINKS = new HashSet<Link>();
  {
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
   * Default contractor used by the XML parser for initialise a command.
   */
  public CommandUpdate() {
    super("Admin_UI_Command", ALLLINKS);
  }

  /**
   *
   */
  protected CommandUpdate(final String _typeName, final Set<Link> _allLinks) {
    super(_typeName, _allLinks);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * Method that reads the given XML-File and than uses the
   * <code>org.apache.commons.digester</code> to create the diffrent Class and
   * invokes the Methods to Update a Command
   *
   * @param _file
   *                XML-File to be read by the digester
   * @return Command Definition read by digester
   * @throws IOException
   *                 if file is not readable
   */
  public static CommandUpdate readXMLFile(final URL _url) {
    CommandUpdate ret = null;

    try {
      final Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("ui-command", CommandUpdate.class);

      digester.addCallMethod("ui-command/uuid", "setUUID", 1);
      digester.addCallParam("ui-command/uuid", 0);

      final String def = "ui-command/definition";

      digester.addObjectCreate(def, CommandDefinition.class);
      digester.addSetNext(def, "addDefinition");

      digester.addCallMethod(def + "/version", "setVersion", 4);
      digester.addCallParam(def + "/version/application", 0);
      digester.addCallParam(def + "/version/global", 1);
      digester.addCallParam(def + "/version/local", 2);
      digester.addCallParam(def + "/version/mode", 3);

      digester.addCallMethod(def + "/name", "setName", 1);
      digester.addCallParam(def + "/name", 0);

      digester.addCallMethod(def + "/icon", "assignIcon", 1);
      digester.addCallParam(def + "/icon", 0);

      digester.addCallMethod(def + "/access/role", "assignAccessRole", 1);
      digester.addCallParam(def + "/access/role", 0);

      // target definitions
      digester.addCallMethod(def + "/target/form", "assignTargetForm", 1);
      digester.addCallParam(def + "/target/form", 0);

      digester.addCallMethod(def + "/target/menu", "assignTargetMenu", 1);
      digester.addCallParam(def + "/target/menu", 0);

      digester.addCallMethod(def + "/target/search", "assignTargetSearch", 1);
      digester.addCallParam(def + "/target/search", 0);

      digester.addCallMethod(def + "/target/table", "assignTargetTable", 1);
      digester.addCallParam(def + "/target/table", 0);

      digester.addFactoryCreate(def + "/target/evaluate", new EventFactory(
          "Admin_UI_TableEvaluateEvent"), false);
      digester.addCallMethod(def + "/target/evaluate/property", "addProperty",2);
      digester.addCallParam(def + "/target/evaluate/property", 0, "name");
      digester.addCallParam(def + "/target/evaluate/property", 1);
      digester.addSetNext(def + "/target/evaluate", "addEvent",
          "org.efaps.update.event.Event");

      digester.addFactoryCreate(def + "/target/execute", new EventFactory(
          "Admin_UI_CommandExecuteEvent"), false);
      digester.addCallMethod(def + "/target/execute/property", "addProperty", 2);
      digester.addCallParam(def + "/target/execute/property", 0, "name");
      digester.addCallParam(def + "/target/execute/property", 1);
      digester.addSetNext(def + "/target/execute", "addEvent",
          "org.efaps.update.event.Event");

      digester.addFactoryCreate(def + "/target/validate", new EventFactory(
          "Admin_UI_ValidateEvent"), false);
      digester.addCallMethod(def + "/target/validate/property", "addProperty", 2);
      digester.addCallParam(def + "/target/validate/property", 0, "name");
      digester.addCallParam(def + "/target/validate/property", 1);
      digester.addSetNext(def + "/target/validate", "addEvent",
          "org.efaps.update.event.Event");

      // properties
      digester.addCallMethod(def + "/property", "addProperty", 2);
      digester.addCallParam(def + "/property", 0, "name");
      digester.addCallParam(def + "/property", 1);

      // Trigger
      digester.addFactoryCreate(def + "/trigger", new EventFactory(), false);
      digester.addCallMethod(def + "/trigger/property", "addProperty", 2);
      digester.addCallParam(def + "/trigger/property", 0, "name");
      digester.addCallParam(def + "/trigger/property", 1);
      digester.addSetNext(def + "/trigger", "addEvent",
          "org.efaps.update.event.Event");

      ret = (CommandUpdate) digester.parse(_url);

      if (ret != null) {
        ret.setURL(_url);
      }
    } catch (final IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (final SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return ret;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class CommandDefinition extends AbstractDefinition {

    // /////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * Assigns a role for accessing this command.
     *
     * @param _role
     *                name of the role
     */
    public void assignAccessRole(final String _role) {
      addLink(LINK2ACCESSROLE,new LinkInstance(_role) );
    }

    /**
     * Assigns a table as target for this command definition.
     *
     * @param _targetTable
     *                name of the target table
     */
    public void assignIcon(final String _icon) {
      addLink(LINK2ICON,new LinkInstance(_icon) );
    }

    /**
     * Assigns a table as target for this command definition.
     *
     * @param _targetTable
     *                name of the target table
     */
    public void assignTargetTable(final String _targetTable) {
      addLink(LINK2TARGETTABLE,new LinkInstance(_targetTable) );
    }

    /**
     * Assigns a form as target for this command definition.
     *
     * @param _targetForm
     *                name of the target form
     */
    public void assignTargetForm(final String _targetForm) {
      addLink(LINK2TARGETFORM, new LinkInstance(_targetForm));
    }

    /**
     * Assigns a menu as target for this command definition.
     *
     * @param _targetMenu
     *                name of the target menu
     */
    public void assignTargetMenu(final String _targetMenu) {
      addLink(LINK2TARGETMENU, new LinkInstance(_targetMenu));
    }

    /**
     * Assigns a search menu as target for this command definition.
     *
     * @param _targetSearch
     *                name of the target search
     */
    public void assignTargetSearch(final String _targetSearch) {
      addLink(LINK2TARGETSEARCH, new LinkInstance(_targetSearch));
    }

  }

}
