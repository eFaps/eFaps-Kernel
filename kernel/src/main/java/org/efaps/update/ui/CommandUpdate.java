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
import org.efaps.update.AbstractUpdate;
import org.efaps.update.event.EventFactory;
import org.xml.sax.SAXException;

/**
 * This Class is responsible for the Update of "Command" in the Database.<br/>It
 * reads with <code>org.apache.commons.digester</code> a XML-File to create
 * the <code>CommandDefinition</code>. It is than inserted into the Database
 * by the SuperClass <code>AbstractUpdate</code>.
 * 
 * @author tmo
 * @version $Id$
 * 
 */
public class CommandUpdate extends AbstractUpdate {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

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

  protected final static Set<Link> ALLLINKS          = new HashSet<Link>();
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
   * Method that converts the given FileName into a <code>File</code> and then
   * parses the File to <code>CommandUpdate readXMLFile(final File _file)</code>.
   * {@link CommandUpdate}
   * 
   * @param _fileName
   *          Name of the XML-File to be read by the digester
   * @return CommandUpdate Definition read by digester
   * @throws IOException
   *           if file is not readable
   */
  public static CommandUpdate readXMLFile(final String _fileName)
                                                                 throws IOException {
    return readXMLFile(new File(_fileName));
  }

  /**
   * Method that reads the given XML-File and than uses the
   * <code>org.apache.commons.digester</code> to create the diffrent Class and
   * invokes the Methods to Update a Command
   * 
   * @param _file
   *          XML-File to be read by the digester
   * @return Command Definition read by digester
   * @throws IOException
   *           if file is not readable
   */
  public static CommandUpdate readXMLFile(final File _file) throws IOException {
    CommandUpdate ret = null;

    try {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("ui-command", CommandUpdate.class);

      digester.addCallMethod("ui-command/uuid", "setUUID", 1);
      digester.addCallParam("ui-command/uuid", 0);

      digester
          .addObjectCreate("ui-command/definition", CommandDefinition.class);
      digester.addSetNext("ui-command/definition", "addDefinition");

      digester.addCallMethod("ui-command/definition/version", "setVersion", 4);
      digester.addCallParam("ui-command/definition/version/application", 0);
      digester.addCallParam("ui-command/definition/version/global", 1);
      digester.addCallParam("ui-command/definition/version/local", 2);
      digester.addCallParam("ui-command/definition/version/mode", 3);

      digester.addCallMethod("ui-command/definition/name", "setName", 1);
      digester.addCallParam("ui-command/definition/name", 0);

      digester.addCallMethod("ui-command/definition/icon", "assignIcon", 1);
      digester.addCallParam("ui-command/definition/icon", 0);

      digester.addCallMethod("ui-command/definition/access/role", "assignAccessRole", 1);
      digester.addCallParam("ui-command/definition/access/role", 0);

      // target definitions
      digester.addCallMethod("ui-command/definition/target/form", "assignTargetForm", 1);
      digester.addCallParam("ui-command/definition/target/form", 0);

      digester.addCallMethod("ui-command/definition/target/menu", "assignTargetMenu", 1);
      digester.addCallParam("ui-command/definition/target/menu", 0);

      digester.addCallMethod("ui-command/definition/target/search", "assignTargetSearch", 1);
      digester.addCallParam("ui-command/definition/target/search", 0);

      digester.addCallMethod("ui-command/definition/target/table", "assignTargetTable", 1);
      digester.addCallParam("ui-command/definition/target/table", 0);

      digester.addFactoryCreate("ui-command/definition/target/evalute", new EventFactory("Admin_UI_TableEvaluateEvent"), false);
      digester.addCallMethod("ui-command/definition/target/evalute/property", "addProperty", 2);
      digester.addCallParam("ui-command/definition/target/evalute/property", 0, "name");
      digester.addCallParam("ui-command/definition/target/evalute/property", 1);
      digester.addSetNext("ui-command/definition/target/evalute", "addEvent", "org.efaps.update.event.Event");

      // properties
      digester
          .addCallMethod("ui-command/definition/property", "addProperty", 2);
      digester.addCallParam("ui-command/definition/property", 0, "name");
      digester.addCallParam("ui-command/definition/property", 1);

      // Trigger
      digester.addFactoryCreate("ui-command/definition/trigger", new EventFactory(), false);
      digester.addCallMethod("ui-command/definition/trigger/property", "addProperty", 2);
      digester.addCallParam("ui-command/definition/trigger/property", 0, "name");
      digester.addCallParam("ui-command/definition/trigger/property", 1);
      digester.addSetNext("ui-command/definition/trigger", "addEvent", "org.efaps.update.event.Event");

      ret = (CommandUpdate) digester.parse(_file);

      if (ret != null) {
        ret.setFile(_file);
      }
    } catch (SAXException e) {
      e.printStackTrace();
      // LOG.error("could not read file '" + _fileName + "'", e);
    }
    return ret;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class CommandDefinition extends DefinitionAbstract {

    // /////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * Assigns a role for accessing this command.
     * 
     * @param _role
     *          name of the role
     */
    public void assignAccessRole(final String _role) {
      addLink(LINK2ACCESSROLE, _role);
    }

    /**
     * Assigns a table as target for this command definition.
     * 
     * @param _targetTable
     *          name of the target table
     */
    public void assignIcon(final String _icon) {
      addLink(LINK2ICON, _icon);
    }

    /**
     * Assigns a table as target for this command definition.
     * 
     * @param _targetTable
     *          name of the target table
     */
    public void assignTargetTable(final String _targetTable) {
      addLink(LINK2TARGETTABLE, _targetTable);
    }

    /**
     * Assigns a form as target for this command definition.
     * 
     * @param _targetForm
     *          name of the target form
     */
    public void assignTargetForm(final String _targetForm) {
      addLink(LINK2TARGETFORM, _targetForm);
    }

    /**
     * Assigns a menu as target for this command definition.
     * 
     * @param _targetMenu
     *          name of the target menu
     */
    public void assignTargetMenu(final String _targetMenu) {
      addLink(LINK2TARGETMENU, _targetMenu);
    }

    /**
     * Assigns a search menu as target for this command definition.
     * 
     * @param _targetSearch
     *          name of the target search
     */
    public void assignTargetSearch(final String _targetSearch) {
      addLink(LINK2TARGETSEARCH, _targetSearch);
    }

  }

}
