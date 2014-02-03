/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.update.schema.ui;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.event.EventType;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.update.event.Event;
import org.efaps.util.EFapsException;

/**
 * This Class is responsible for the Update of "Command" in the Database.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CommandUpdate
    extends AbstractUpdate
{
    /**
     * Set of all links used by commands.
     */
    protected static final Set<Link> ALLLINKS = new HashSet<Link>();

    /** Link from UI object to role. */
    private static final Link LINK2ACCESSROLE = new Link("Admin_UI_Access", "UILink",
                    "Admin_User_RoleAbstract", "UserLink");

    /** Link from UI object to person. */
    private static final Link LINK2ACCESSPERSON = new Link("Admin_UI_Access", "UILink", "Admin_User_Person",
                                                           "UserLink");

    /** Link from UI object to group. */
    private static final Link LINK2ACCESSGROUP = new Link("Admin_UI_Access", "UILink", "Admin_User_Group", "UserLink");

    /** Link from UI object to company. */
    private static final Link LINK2ACCESSCOMPANY = new Link("Admin_UI_Access", "UILink", "Admin_User_Company",
                                                            "UserLink");

    /** Link from command to icon. */
    private static final Link LINK2ICON = new UniqueLink("Admin_UI_LinkIcon", "From", "Admin_UI_Image", "To");

    /** Link from command to command as target. */
    private static final Link LINK2TARGETCMD = new UniqueLink("Admin_UI_LinkTargetCommand", "From", "Admin_UI_Command",
                    "To");

    /** Link from command to table as target. */
    private static final Link LINK2TARGETTABLE = new UniqueLink("Admin_UI_LinkTargetTable", "From", "Admin_UI_Table",
                    "To");

    /** Link from command to form as target. */
    private static final Link LINK2TARGETFORM = new UniqueLink("Admin_UI_LinkTargetForm", "From", "Admin_UI_Form",
                    "To");

    /** Link from command to menu as target. */
    private static final Link LINK2TARGETMENU = new UniqueLink("Admin_UI_LinkTargetMenu", "From", "Admin_UI_Menu",
                    "To");

    /** Link from command to search as target. */
    private static final Link LINK2TARGETSEARCH = new UniqueLink("Admin_UI_LinkTargetSearch", "From", "Admin_UI_Search",
                                                           "To");

    /** Link from command to wiki. */
    private static final Link LINK2TARGETHELP = new UniqueLink("Admin_UI_LinkTargetHelp", "FromMenuLink",
                                                         "Admin_Program_Wiki", "ToWikiLink");

    static {
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2ACCESSROLE);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2ACCESSPERSON);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2ACCESSGROUP);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2ACCESSCOMPANY);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2ICON);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2TARGETCMD);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2TARGETTABLE);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2TARGETFORM);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2TARGETMENU);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2TARGETSEARCH);
        CommandUpdate.ALLLINKS.add(CommandUpdate.LINK2TARGETHELP);
    }

    /**
     * Default contractor used by the XML parser for initialize a command.
     *
     * @param _url URL of the file
     */
    public CommandUpdate(final URL _url)
    {
        super(_url, "Admin_UI_Command", CommandUpdate.ALLLINKS);
    }

    /**
     * @param _url url
     * @param _typeName name of the type
     * @param _allLinks all links
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

    /**
     * Definition for a command.
     *
     */
    protected class CommandDefinition extends AbstractDefinition
    {

        /**
         * Read the xml.
         *
         * @param _tags List of tags
         * @param _attributes map of attributes
         * @param _text text
         * @throws EFapsException on error
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            if ("access".equals(value)) {
                if (_tags.size() > 1) {
                    final String subValue = _tags.get(1);
                    if ("role".equals(subValue)) {
                        // Assigns a role for accessing this command.
                        addLink(CommandUpdate.LINK2ACCESSROLE, new LinkInstance(_text));
                    } else if ("person".equals(subValue)) {
                        // Assigns a person for accessing this command.
                        addLink(CommandUpdate.LINK2ACCESSPERSON, new LinkInstance(_text));
                    } else if ("group".equals(subValue)) {
                        // Assigns a group for accessing this command.
                        addLink(CommandUpdate.LINK2ACCESSGROUP, new LinkInstance(_text));
                    } else if ("company".equals(subValue)) {
                        // Assigns a company for accessing this command.
                        addLink(CommandUpdate.LINK2ACCESSCOMPANY, new LinkInstance(_text));
                    } else {
                        super.readXML(_tags, _attributes, _text);
                    }
                }
            } else if ("icon".equals(value)) {
                // Assigns an image
                addLink(CommandUpdate.LINK2ICON, new LinkInstance(_text));
            } else if ("target".equals(value)) {
                if (_tags.size() == 2) {
                    final String subValue = _tags.get(1);
                    final String name = (_attributes.get("name") == null
                                    ? getValue("Name") + "." + subValue : _attributes.get("name"))
                                                    + (_attributes.get("index") == null
                                                    ? "" : ("." + _attributes.get("index")));
                    if ("evaluate".equals(subValue)) {
                        getEvents().add(new Event(name, EventType.UI_TABLE_EVALUATE,
                                        _attributes.get("program"), _attributes.get("method"),
                                        _attributes.get("index")));
                    } else if ("execute".equals(subValue)) {
                        getEvents().add(new Event(name, EventType.UI_COMMAND_EXECUTE,
                                        _attributes.get("program"), _attributes.get("method"),
                                        _attributes.get("index")));
                    } else if ("instance".equals(subValue)) {
                        getEvents().add(new Event(name, EventType.UI_INSTANCEMANAGER,
                                        _attributes.get("program"), _attributes.get("method"),
                                        _attributes.get("index")));
                    } else if ("command".equals(subValue)) {
                        // assigns a command as target for this command definition.
                        addLink(CommandUpdate.LINK2TARGETCMD, new LinkInstance(_text));
                    } else if ("form".equals(subValue)) {
                        // assigns a form as target for this command definition.
                        addLink(CommandUpdate.LINK2TARGETFORM, new LinkInstance(_text));
                    } else if ("menu".equals(subValue)) {
                        // assigns a menu as target for this command definition.
                        addLink(CommandUpdate.LINK2TARGETMENU, new LinkInstance(_text));
                    } else if ("search".equals(subValue)) {
                        // assigns a search menu as target for this command definition.
                        addLink(CommandUpdate.LINK2TARGETSEARCH, new LinkInstance(_text));
                    } else if ("table".equals(subValue)) {
                        // assigns a table as target for this command definition.
                        addLink(CommandUpdate.LINK2TARGETTABLE, new LinkInstance(_text));
                    } else if ("trigger".equals(subValue)) {
                        getEvents().add(new Event(_attributes.get("name"), EventType.valueOf(_attributes.get("event")),
                                        _attributes.get("program"), _attributes.get("method"),
                                        _attributes.get("index")));
                    } else if ("validate".equals(subValue)) {
                        getEvents().add(new Event(name, EventType.UI_VALIDATE,
                                        _attributes.get("program"), _attributes.get("method"),
                                        _attributes.get("index")));
                    } else if ("help".equals(subValue)) {
                        // assigns a wiki as target for this command definition.
                        addLink(CommandUpdate.LINK2TARGETHELP, new LinkInstance(_text));
                    } else {
                        super.readXML(_tags, _attributes, _text);
                    }
                } else if (_tags.size() == 3) {
                    final String subValue = _tags.get(1);
                    if (("evaluate".equals(subValue) || "execute".equals(subValue) || "trigger".equals(subValue)
                                    || "validate".equals(subValue)) && "property".equals(_tags.get(2))) {
                        getEvents().get(getEvents().size() - 1).addProperty(_attributes.get("name"), _text);
                    } else {
                        super.readXML(_tags, _attributes, _text);
                    }
                } else if (_tags.size() > 2) {
                    // if size == 1, the target tag could be ignored
                    super.readXML(_tags, _attributes, _text);
                }
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}
