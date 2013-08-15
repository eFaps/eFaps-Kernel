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

package org.efaps.admin.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.EFapsException;
import org.efaps.util.RequestHandler;
import org.efaps.util.cache.CacheReloadException;

/**
 * This class represents the Commands which enable the interaction with a User. <br>
 * Buttons in the UserInterface a represented by this Class.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractCommand
    extends AbstractUserInterfaceObject
{

    /**
     * This enum is used to define the Sortdirection of a Field.
     */
    public static enum SortDirection {
        /**
         * Sortdirection descending.
         */
        DESCENDING("desc"),
        /**
         * Sortdirection ascending.
         */
        ASCENDING("asc"),
        /**
         * Sortdirection none.
         */
        NONE("");

        /**
         * Variable storing the value.
         */
        private final String value;

        /**
         * Private constructor setting the value for the enum.
         *
         * @param _value value
         */
        private SortDirection(final String _value)
        {
            this.value = _value;
            AbstractCommand.MAPPER.put(this.value, this);
        }

        /**
         * Getter method for instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */
        public String getValue()
        {
            return this.value;
        }

        /**
         * Method to get a SortDirection by its value.
         *
         * @param _value Value for sort direction
         * @return SortDirection
         */
        public static SortDirection getEnum(final String _value)
        {
            return AbstractCommand.MAPPER.get(_value);
        }
    }

    /**
     * This map is used as a store by the enum SortDirection for the method
     * getEnum.
     */
    private static final Map<String, AbstractCommand.SortDirection> MAPPER
        = new HashMap<String, AbstractCommand.SortDirection>();

    /**
     * This enum id used to define the different Targets a Command can have.
     */
    public static enum Target {
        /** The target of the href is the content frame. */
        CONTENT,
        /** The target of the href is the hidden frame. */
        HIDDEN,
        /** The target of the href is a Modal Window. */
        MODAL,
        /** The target of the href is a new Popup Window. */
        POPUP,
        /**
         * The target of the href is not known. This is maybe, if a javascript
         * function is directly called.
         */
        UNKNOWN;
    }

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The instance variable stores if the execution of the command needs a
     * confirmation of the user. The default value is <i>false</i>.
     *
     * @see #isAskUser
     * @see #setAskUser
     */
    private boolean askUser = false;

    /**
     * If the instance variable is set to <i>tree</i>, the command is selected
     * as default command in the navigation tree.
     *
     * @see #isDefaultSelected
     * @see #setDefaultSelected
     */
    private boolean defaultSelected = false;

    /**
     * Instance variable to hold the reference to the icon file.
     *
     * @see #setIcon
     * @see #getIcon
     */
    private String icon = null;

    /**
     * The instance variable stores the label of this command instance. The
     * default value is set from the constructor to the name plus extension
     * '.Label'.
     *
     * @see #getLabel
     * @see #setLabel
     */
    private String label = null;

    /**
     * Instance variable to hold the reference to call.
     *
     * @see #setReference
     * @see #getReference
     */
    private String reference = null;

    /**
     * If the value is set to <i>true</i>. the commands submits the current form
     * to the given href url and the given target. The default value is
     * <i>false</i>.
     *
     * @see #isSubmit
     * @see #setSubmit
     */
    private boolean submit = false;

    /**
     * Number of rows that must be committed. Special meanings:
     * <ul>
     * <li>
     * 0: the mechanism expects at least one. Default</li>
     * <li>
     * -1: the mechanism is deactivated.</li>
     * <li>
     * 1, 2, 3 ...: the exact number of selected rows will be checked.</li>
     * </ul>
     *
     */
    private int submitSelectedRows = 0;

    /**
     * The target of the command is the content frame.
     *
     * @see #isTargetContent
     * @see #isTargetPopup
     * @set #getTarget
     * @see #setTarget
     */
    private Target target = AbstractCommand.Target.UNKNOWN;

    /**
     * The instance variable stores the height for the target bottom. Only a is
     * set, the value is used from the JSP pages.
     *
     * @see #getTargetBottomHeight
     * @see #setTargetBottomHeight
     */
    private int targetBottomHeight = 0;

    /**
     * The instance variable stores the target connect attribute used for the
     * connect in a form.
     */
    private long targetConnectAttributeId = 0;

    /**
     * The instance variable stores the create type for the target user
     * interface object.
     */
    private long targetCreateTypeId = 0;

    /**
     * Classifications that will be added to the object on create.
     */
    private final Set<Long> targetCreateClassificationIds = new HashSet<Long>();

    /**
     * Is the target Menu/Command the default.
     */
    private boolean targetDefaultMenu = true;

    /**
     * The instance variable stores the target user interface form object which
     * is shown by the this abstract commmand.
     *
     * @see #getTargetForm
     * @see #setTargetForm
     */
    private Form targetForm = null;

    /**
     * The instance method stores the complete menu. Default value is a null and
     * no menu is shown.
     *
     * @see #setTargetMenu
     * @see #getTargetMenu
     */
    private Menu targetMenu = null;

    /**
     * The instance method stores the command that will be executed after this
     * command.
     */
    private AbstractCommand targetCommand = null;

    /**
     * Should the revise/previous button be rendered.
     */
    private boolean targetCmdRevise = true;

    /**
     * The instance variable stores the mode of the target user interface
     * object.
     *
     * @see #getTargetMode
     * @see #setTargetMode
     */
    private TargetMode targetMode = TargetMode.UNKNOWN;

    /**
     * The instance variable stores the search of target user interface object.
     *
     * @see #getTargetSearch
     * @see #setTargetSearch
     */
    private Search targetSearch = null;

    /**
     * Standard checkboxes for a table must be shown. The checkboxes are used
     * e.g. to delete selected.
     *
     * @see #isTargetShowCheckBoxes
     */
    private boolean targetShowCheckBoxes = false;

    /**
     * The instance variable stores the target user interface table object which
     * is shown by the this abstract commmand.
     *
     * @see #getTargetTable
     * @see #setTargetTable
     */
    private Table targetTable = null;

    /**
     * The instance variable stores for target user interface table object the
     * default sort direction. The default value is NONE. .
     *
     * @see #getTargetTableSortDirection
     * @see #setTargetTableSortDirection
     */
    private SortDirection targetTableSortDirection = AbstractCommand.SortDirection.NONE;

    /**
     * The instance variable stores for target user interface table object the
     * default sort key.
     *
     * @see #getTargetTableSortKey
     * @see #setTargetTableSortKey
     */
    private String targetTableSortKey = null;

    /**
     * Sets the title of the target window.
     *
     * @see #getTargetTitle
     * @see #setTargetTitle
     */
    private String targetTitle = null;

    /**
     * The instance variable stores the window height of the popup window (
     * {@link #target} is set to {@link #TARGET_POPUP}). The default value is
     * <i>400</i>.
     *
     * @see #getWindowHeight
     * @see #setWindowHeight
     */
    private int windowHeight = 400;

    /**
     * The instance variable stores the window width of the popup window (
     * {@link #target} is set to {@link #TARGET_POPUP}). The default value is
     * <i>600</i>.
     *
     * @see #getWindowWidth
     * @see #setWindowWidth
     */
    private int windowWidth = 600;

    /**
     * Does the executed esjp return a file that must be shown.
     */
    private boolean targetShowFile = false;

    /**
     * Must the update after command be deactivated.
     */
    private boolean noUpdateAfterCmd;

    /**
     * Name of the field the Structurbrowser sits in the target.
     */
    private String targetStructurBrowserField;

    /**
     * Name of the target wiki.
     */
    private String targeHelp;

    /**
     * Constructor to set the id and name of the command object. The constructor
     * also sets the label of the command and the titel of the target.
     *
     * @param _id id of the command to set
     * @param _name name of the command to set
     * @param _uuid uuid of the command to set
     * @see #label
     */
    protected AbstractCommand(final long _id,
                              final String _uuid,
                              final String _name)
    {
        super(_id, _uuid, _name);
        this.label = _name + ".Label";
        this.targetTitle = _name + ".Title";
    }

    /**
     * Add a new role for access to this command.
     *
     * @param _role Role to add
     * @see #access
     * @see #getAccess
     */
    protected void add(final Role _role)
    {
        getAccess().add(_role.getId());
    }

    /**
     * Get the current icon reference value.
     *
     * @return the value of the instance variable {@link #icon}.
     * @see #icon
     * @see #setIcon
     */
    public String getIcon()
    {
        return this.icon;
    }

    /**
     * This method returns the Property of the Label and not the name.
     *
     * @return String
     */
    public String getLabelProperty()
    {
        return DBProperties.getProperty(this.label);
    }

    /**
     * This is the setter method for the instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     * @see #label
     * @see #setLabel
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Get the current reference value.
     *
     * @return the value of the instance variable {@link #reference}.
     * @see #reference
     * @see #setReference
     */
    public String getReference()
    {
        return this.reference;
    }

    /**
     * This is the setter method for the instance variable {@link #target}.
     *
     * @return value of instance variable {@link #target}
     * @see #target
     * @see #setTarget
     */
    public Target getTarget()
    {
        return this.target;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #targetBottomHeight}.
     *
     * @return value of instance variable {@link #targetBottomHeight}
     * @see #targetBottomHeight
     * @see #setTargetBottomHeight
     */
    public int getTargetBottomHeight()
    {
        return this.targetBottomHeight;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #targetConnectAttribute}.
     *
     * @return value of instance variable {@link #targetConnectAttribute}
     * @throws CacheReloadException on error
     */
    public Attribute getTargetConnectAttribute()
        throws CacheReloadException
    {
        return Attribute.get(this.targetConnectAttributeId);
    }

    /**
     * This is the setter method for the instance variable
     * {@link #targetCreateType}.
     *
     * @return value of instance variable {@link #targetCreateType}
     * @throws CacheReloadException on error
     */
    public Type getTargetCreateType()
        throws CacheReloadException
    {
        return Type.get(this.targetCreateTypeId);
    }

    /**
     * This is the getter method for the instance variable
     * {@link #targetDefaultMenu}.
     *
     * @return value of instance variable {@link #targetDefaultMenu}
     */
    public boolean hasTargetDefaultMenu()
    {
        return this.targetDefaultMenu;
    }

    /**
     * This is the setter method for the instance variable {@link #targetForm}.
     *
     * @return value of instance variable {@link #targetForm}
     * @see #targetForm
     * @see #setTargetForm
     */
    public Form getTargetForm()
    {
        return this.targetForm;
    }

    /**
     * Getter method for the instance variable {@link #targetMenu}.
     * Adds the default menus if allowed ({@link #targetDefaultMenu}
     * defined in an SystemConfiguration.
     *
     * @return value of instance variable {@link #targetMenu}
     * @throws EFapsException on error
     * @see #targetMenu
     * @see #setTargetMenu
     */
    public Menu getTargetMenu()
        throws EFapsException
    {
        Menu ret = this.targetMenu;
        if (this.targetDefaultMenu
                        && EFapsSystemConfiguration.get().getAttributeValue(KernelSettings.DEFAULTMENU) != null) {
            // reads the Value from "Common_Main_DefaultMenu"
            if (EFapsSystemConfiguration.get().getAttributeValue(KernelSettings.DEFAULTMENU).equals("none")) {
                ret = this.targetMenu;
            } else {
                final Properties prop = EFapsSystemConfiguration.get()
                                .getAttributeValueAsProperties(KernelSettings.DEFAULTMENU);
                for (int i = 0; i < 99; i++) {
                    if (prop.getProperty("Menu" + i) != null) {
                        final String menuname = prop.getProperty("Menu" + i);
                        if (getTargetTable() != null) {
                            // if no Enable4TableN property is set or if the
                            // Enable4TableN is not false the default
                            // menu will be added
                            if (prop.getProperty("Enable4Table" + i) == null
                                            || (prop.getProperty("Enable4Table" + i) != null
                                            && !"false".equalsIgnoreCase(prop.getProperty("Enable4Table" + i)))) {
                                if (this.targetMenu == null && ret == null) {
                                    ret = Menu.get(menuname);
                                    break;
                                } else {
                                    this.targetMenu.addAll(Menu.get(menuname));
                                    ret = this.targetMenu;
                                }
                            }
                        } else if (getTargetForm() != null) {
                            // only if a Enabled4FormN property is set to true the menu will be added
                            if (prop.getProperty("Enable4Form" + i) != null
                                            && "true".equalsIgnoreCase(prop.getProperty("Enable4Form" + i))) {
                                if (this.targetMenu == null && ret == null) {
                                    ret = Menu.get(menuname);
                                    break;
                                } else {
                                    this.targetMenu.addAll(Menu.get(menuname));
                                    ret = this.targetMenu;
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #targetCommand}.
     *
     * @return value of instance variable {@link #targetCommand}
     */
    public AbstractCommand getTargetCommand()
    {
        return this.targetCommand;
    }

    /**
     * Getter method for instance variable {@link #targeHelp}.
     *
     * @return value of instance variable {@link #targeHelp}
     */
    public String getTargetHelp()
    {
        return this.targeHelp;
    }

    /**
     * Getter method for instance variable {@link #targetCmdRevise}.
     *
     * @return value of instance variable {@link #targetCmdRevise}
     */
    public boolean isTargetCmdRevise()
    {
        return this.targetCmdRevise;
    }

    /**
     * @param _value comma separated list of classifications
     * @throws CacheReloadException on error
     */
    private void setTargetCreateClassifications(final String _value)
        throws CacheReloadException
    {
        final String[] values = _value.split(",");
        for (final String value : values) {
            final Type classification = Type.get(value.trim());
            if (classification != null) {
                this.targetCreateClassificationIds.add(classification.getId());
            }
        }
    }

    /**
     * Getter method for instance variable {@link #targetCreateClassification}.
     *
     * @return value of instance variable {@link #targetCreateClassification}
     * @throws CacheReloadException on error
     */
    public Set<Classification> getTargetCreateClassification()
        throws CacheReloadException
    {
        final Set<Classification> ret = new HashSet<Classification>();
        for (final Long id : this.targetCreateClassificationIds) {
            ret.add(Classification.get(id));
        }
        return Collections.unmodifiableSet(ret);
    }

    /**
     * This is the setter method for the instance variable {@link #targetMode}.
     *
     * @return value of instance variable {@link #targetMode}
     * @see #targetMode
     * @see #setTargetMode
     */
    public TargetMode getTargetMode()
    {
        return this.targetMode;
    }

    /**
     * This is the setter method for the instance variable {@link #targetSearch}
     * .
     *
     * @return value of instance variable {@link #targetSearch}
     * @see #targetSearch
     * @see #setTargetSearch
     */
    public Search getTargetSearch()
    {
        return this.targetSearch;
    }

    /**
     * This is the setter method for the instance variable {@link #targetTable}.
     *
     * @return value of instance variable {@link #targetTable}
     * @see #targetTable
     * @see #setTargetTable
     */
    public Table getTargetTable()
    {
        return this.targetTable;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #targetTableSortDirection}.
     *
     * @return value of instance variable {@link #targetTableSortDirection}
     * @see #targetTableSortDirection
     * @see #setTargetTableSortDirection
     */
    public SortDirection getTargetTableSortDirection()
    {
        return this.targetTableSortDirection;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #targetTableSortKey}.
     *
     * @return value of instance variable {@link #targetTableSortKey}
     * @see #targetTableSortKey
     * @see #setTargetTableSortKey
     */
    public String getTargetTableSortKey()
    {
        return this.targetTableSortKey;
    }

    /**
     * This is the setter method for the instance variable {@link #targetTitle}.
     *
     * @return value of instance variable {@link #targetTitle}
     * @see #targetTitle
     * @see #setTargetTitle
     */
    public String getTargetTitle()
    {
        return this.targetTitle;
    }

    /**
     * The instance method returns the label of a command (or also menu). The
     * instance method looks in the DBProperties, if a property entry with
     * prefix <i>Command.</i> and name is found. This value is returned. If no
     * entry is found, the name of the command is returned.
     *
     * @return label of the command (or menu)
     */
    public String getViewableName()
    {
        String name = getName();
        if (DBProperties.hasProperty("Command." + getName())) {
            name = DBProperties.getProperty("Command." + getName());
        }
        return name;
    }

    /**
     * This is the getter method for the instance variable {@link #windowHeight}
     * .
     *
     * @return value of instance variable {@link #windowHeight}
     * @see #windowHeight
     * @see #setWindowHeight
     */
    public int getWindowHeight()
    {
        return this.windowHeight;
    }

    /**
     * This is the getter method for the instance variable {@link #windowWidth}.
     *
     * @return value of instance variable {@link #windowWidth}
     * @see #windowWidth
     * @see #setWindowWidth
     */
    public int getWindowWidth()
    {
        return this.windowWidth;
    }

    /**
     * This is the getter method for the instance variable {@link #askUser}.
     *
     * @return value of instance variable {@link #askUser}
     * @see #askUser
     * @see #setAskUser
     */
    public boolean isAskUser()
    {
        return this.askUser;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #defaultSelected}.
     *
     * @return value of instance variable {@link #defaultSelected}
     * @see #defaultSelected
     * @see #setDefaultSelected
     */
    public boolean isDefaultSelected()
    {
        return this.defaultSelected;
    }

    /**
     * This is the setter method for the instance variable {@link #submit}.
     *
     * @return value of instance variable {@link #submit}
     * @see #submit
     * @see #setSubmit
     */
    public boolean isSubmit()
    {
        return this.submit;
    }

    /**
     * Getter method for the instance variable {@link #submitSelectedRows}.
     *
     * @return value of instance variable {@link #submitSelectedRows}
     */
    public int getSubmitSelectedRows()
    {
        return this.submitSelectedRows;
    }

    /**
     * Test, if the value of instance variable {@link #target} is equal to
     * {@link #TARGET_CONTENT}.
     *
     * @return <i>true</i> if value is equal, otherwise false
     * @see #target
     * @see #getTarget
     */
    public boolean isTargetContent()
    {
        return getTarget() == AbstractCommand.Target.CONTENT;
    }

    /**
     * Test, if the value of instance variable {@link #target} is equal to
     * {@link #TARGET_HIDDEN}.
     *
     * @return <i>true</i> if value is equal, otherwise false
     * @see #target
     * @see #getTarget
     */
    public boolean isTargetHidden()
    {
        return getTarget() == AbstractCommand.Target.HIDDEN;
    }

    /**
     * Test, if the value of instance variable {@link #target} is equal to
     * {@link #TARGET_POPUP}.
     *
     * @return <i>true</i> if value is equal, otherwise false
     * @see #target
     * @see #getTarget
     */
    public boolean isTargetPopup()
    {
        return getTarget() == AbstractCommand.Target.POPUP;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #targetShowCheckBoxes}.
     *
     * @return value of instance variable {@link #targetShowCheckBoxes}
     */
    public boolean isTargetShowCheckBoxes()
    {
        return this.targetShowCheckBoxes;
    }

    /**
     * Getter method for instance variable {@link #targetShowFile}.
     *
     * @return value of instance variable {@link #targetShowFile}
     */
    public boolean isTargetShowFile()
    {
        return this.targetShowFile;
    }

    /**
     * Getter method for instance variable {@link #noUpdateAfterCmd}.
     *
     * @return value of instance variable {@link #noUpdateAfterCmd}
     */
    public boolean isNoUpdateAfterCmd()
    {
        return this.noUpdateAfterCmd;
    }

    /**
     * Getter method for instance variable {@link #targetStructurBrowserField}.
     *
     * @return value of instance variable {@link #targetStructurBrowserField}
     */
    public String getTargetStructurBrowserField()
    {
        return this.targetStructurBrowserField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLinkProperty(final UUID _linkTypeUUID,
                                   final long _toId,
                                   final UUID _toTypeUUID,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkTypeUUID.equals(CIAdminUserInterface.LinkIcon.uuid)) {
            this.icon = RequestHandler.replaceMacrosInUrl(RequestHandler.URL_IMAGE + _toName);
        } else if (_linkTypeUUID.equals(CIAdminUserInterface.LinkTargetForm.uuid)) {
            this.targetForm = Form.get(_toId);
        } else if (_linkTypeUUID.equals(CIAdminUserInterface.LinkTargetMenu.uuid)) {
            this.targetMenu = Menu.get(_toId);
        } else if (_linkTypeUUID.equals(CIAdminUserInterface.LinkTargetSearch.uuid)) {
            this.targetSearch = Search.get(_toId);
        } else if (_linkTypeUUID.equals(CIAdminUserInterface.LinkTargetTable.uuid)) {
            this.targetTable = Table.get(_toId);
        } else if (_linkTypeUUID.equals(CIAdminUserInterface.LinkTargetCommand.uuid)) {
            this.targetCommand = Command.get(_toId);
        } else if (_linkTypeUUID.equals(CIAdminUserInterface.LinkTargetHelp.uuid)) {
            this.targeHelp = _toName;
        } else {
            super.setLinkProperty(_linkTypeUUID, _toId, _toTypeUUID, _toName);
        }
    }

    /**
     * The instance method sets a new property value.
     *
     * @param _name name of the property
     * @param _value value of the property
     * @throws CacheReloadException on error during reload
     */
    @Override
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        if ("AskUser".equals(_name)) {
            this.askUser = "true".equalsIgnoreCase(_value);
        } else if ("DefaultSelected".equals(_name)) {
            this.defaultSelected = "true".equalsIgnoreCase(_value);
        } else if ("HRef".equals(_name)) {
            this.reference = RequestHandler.replaceMacrosInUrl(_value);
        } else if ("Label".equals(_name)) {
            this.label = _value;
        } else if ("Submit".equals(_name)) {
            this.submit = "true".equalsIgnoreCase(_value);
        } else if ("SubmitSelectedRows".equals(_name)) {
            this.submitSelectedRows = Integer.parseInt(_value);
        } else if ("Target".equals(_name)) {
            if ("content".equals(_value)) {
                this.target = AbstractCommand.Target.CONTENT;
            } else if ("hidden".equals(_value)) {
                this.target = AbstractCommand.Target.HIDDEN;
            } else if ("popup".equals(_value)) {
                this.target = AbstractCommand.Target.POPUP;
            } else if ("modal".equals(_value)) {
                this.target = AbstractCommand.Target.MODAL;
            }
        } else if ("TargetBottomHeight".equals(_name)) {
            this.targetBottomHeight = Integer.parseInt(_value);
        } else if ("TargetCmdRevise".equals(_name)) {
            this.targetCmdRevise = "TRUE".equalsIgnoreCase(_value);
        } else if ("TargetConnectAttribute".equals(_name)) {
            final Attribute attr = Attribute.get(_value);
            this.targetConnectAttributeId = attr == null ? 0 : attr.getId();
        } else if ("TargetCreateType".equals(_name)) {
            final Type type = Type.get(_value);
            this.targetCreateTypeId = type == null ? 0 : type.getId();
        } else if ("TargetCreateClassifications".equals(_name)) {
            setTargetCreateClassifications(_value);
        } else if ("TargetDefaultMenu".equals(_name)) {
            this.targetDefaultMenu = "none".equalsIgnoreCase(_value);
        } else if ("TargetMode".equals(_name)) {
            if ("create".equals(_value)) {
                this.targetMode = TargetMode.CREATE;
            } else if ("edit".equals(_value)) {
                this.targetMode = TargetMode.EDIT;
            } else if ("connect".equals(_value)) {
                this.targetMode = TargetMode.CONNECT;
            } else if ("search".equals(_value)) {
                this.targetMode = TargetMode.SEARCH;
            } else if ("view".equals(_value)) {
                this.targetMode = TargetMode.VIEW;
            }
        } else if ("TargetShowCheckBoxes".equals(_name)) {
            this.targetShowCheckBoxes = "true".equalsIgnoreCase(_value);
        } else if ("TargetTableSortKey".equals(_name)) {
            this.targetTableSortKey = _value;
            this.targetTableSortDirection = AbstractCommand.SortDirection.ASCENDING;
        } else if ("TargetTableSortDirection".equals(_name)) {
            if (AbstractCommand.SortDirection.DESCENDING.value.equals(_value)) {
                this.targetTableSortDirection = AbstractCommand.SortDirection.DESCENDING;
            } else {
                this.targetTableSortDirection = AbstractCommand.SortDirection.ASCENDING;
            }
        } else if ("TargetTitle".equals(_name)) {
            this.targetTitle = _value;
        } else if ("TargetShowFile".equals(_name)) {
            this.targetShowFile = "true".equalsIgnoreCase(_value);
        } else if ("NoUpdateAfterCOMMAND".equals(_name)) {
            this.noUpdateAfterCmd = "true".equalsIgnoreCase(_value);
        } else if ("TargetStructurBrowserField".equals(_name)) {
            this.targetStructurBrowserField = _value.trim();
        } else if ("WindowHeight".equals(_name)) {
            this.windowHeight = Integer.parseInt(_value);
        } else if ("WindowWidth".equals(_name)) {
            this.windowWidth = Integer.parseInt(_value);
        } else {
            super.setProperty(_name, _value);
        }
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof AbstractCommand) {
            ret = ((AbstractCommand) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return  Long.valueOf(getId()).intValue();
    }
}
