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

package org.efaps.admin.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.user.Role;
import org.efaps.util.RequestHandler;
import org.efaps.util.cache.CacheReloadException;

/**
 * This class represents the Commands which enable the interaction with a User.
 * <br>
 * Buttons in the UserInterface a represented by this Class.
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractCommand extends AbstractUserInterfaceObject {

  // ///////////////////////////////////////////////////////////////////////////
  // static Variables

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
    public final String value;

    /**
     * Private constructor setting the value for the enum.
     *
     * @param _value value
     */
    private SortDirection(final String _value) {
      this.value = _value;
      MAPPER.put(this.value, this);
    }

    /**
     * Method to get a SortDirection by its value.
     *
     * @param _value  Value for sort direction
     * @return SortDirection
     */
    public static SortDirection getEnum(final String _value) {
      return MAPPER.get(_value);
    }
  }

  /**
   * This map is used as a store by the enum SortDirection for the method
   * getEnum.
   */
  private static final Map<String, SortDirection> MAPPER =
                                          new HashMap<String, SortDirection>();

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

  // ///////////////////////////////////////////////////////////////////////////
  // instance Variables

  /**
   * The instance variable stores if the execution of the command needs a
   * confirmation of the user. The default value is <i>false</i>.
   *
   * @see #isAskUser
   * @see #setAskUser
   */
  private boolean askUser = false;

  /**
   * If the instance variable is set to <i>tree</i>, the command is selected as
   * default command in the navigation tree.
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
   * The target of the command is the content frame.
   *
   * @see #isTargetContent
   * @see #isTargetPopup
   * @set #getTarget
   * @see #setTarget
   */
  private Target target = Target.UNKNOWN;

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
   *
   * @see #getTargetConnectAttribute
   * @see #setTargetConnectAttribute
   */
  private Attribute targetConnectAttribute = null;

  /**
   * The instance variable stores the create type for the target user interface
   * object.
   *
   * @see #getTargetCreateType
   * @see #setTargetCreateType
   */
  private Type targetCreateType = null;

  /**
   * Is the target Menu/Command the default.
   */
  private boolean targetDefaultMenu = true;

  /**
   * The instance variable stores the target user interface form object which is
   * shown by the this abstract commmand.
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
   * The instance variable stores the mode of the target user interface object.
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
   * Standard checkboxes for a table must be shown. The checkboxes are used e.g.
   * to delete selected.
   *
   * @see #isTargetShowCheckBoxes
   * @see #setTargetShowCheckBoxes
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
   * The instance variable store the filters for the targer table.
   *
   * @see #getTargetTableFilters
   * @see #setTargetTableFilters
   */
  private List<TargetTableFilter> targetTableFilters = null;

  /**
   * The instance variable stores for target user interface table object the
   * default sort direction. The default value is NONE. .
   *
   * @see #getTargetTableSortDirection
   * @see #setTargetTableSortDirection
   */
  private SortDirection targetTableSortDirection = SortDirection.NONE;

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
   * The instance variable stores the window height of the popup window
   * ({@link #target}
   * is set to {@link #TARGET_POPUP}). The default value is <i>400</i>.
   *
   * @see #getWindowHeight
   * @see #setWindowHeight
   */
  private int windowHeight = 400;

  /**
   * The instance variable stores the window width of the popup window
   * ({@link #target}
   * is set to {@link #TARGET_POPUP}). The default value is <i>600</i>.
   *
   * @see #getWindowWidth
   * @see #setWindowWidth
   */
  private int windowWidth = 600;

  // ///////////////////////////////////////////////////////////////////////////
  // Constructors
  /**
   * Constructor to set the id and name of the command object. The constructor
   * also sets the label of the command and the titel of the target.
   *
   * @param _id     id of the command to set
   * @param _name   name of the command to set
   * @param _uuid   uuid of the command to set
   * @see #label
   */
  protected AbstractCommand(final long _id, final String _uuid,
                            final String _name) {
    super(_id, _uuid, _name);
    setLabel(_name + ".Label");
    setTargetTitle(_name + ".Title");
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance Methods

  /**
   * Add a new role for access to this command.
   *
   * @param _role Role to add
   * @see #access
   * @see #getAccess
   */
  protected void add(final Role _role) {
    getAccess().add(_role);
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Get the current icon reference value.
   *
   * @return the value of the instance variable {@link #icon}.
   * @see #icon
   * @see #setIcon
   */
  public String getIcon() {
    return this.icon;
  }

  /**
   * Set the new icon reference value.
   *
   * @param _icon
   *                new icon reference to set
   * @see #icon
   * @see #getIcon
   */
  public void setIcon(final String _icon) {
    this.icon = _icon;
  }

  /**
   * This method returns the Property of the Label and not the name.
   *
   * @return String
   */
  public String getLabelProperty() {
    return DBProperties.getProperty(this.label);
  }

  /**
   * This is the setter method for the instance variable {@link #label}.
   *
   * @return value of instance variable {@link #label}
   * @see #label
   * @see #setLabel
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * This is the setter method for the instance variable {@link #label}.
   *
   * @param _label
   *                new value for instance variable {@link #label}
   * @see #label
   * @see #getLabel
   */
  public void setLabel(final String _label) {
    this.label = _label;
  }

  /**
   * Get the current reference value.
   *
   * @return the value of the instance variable {@link #reference}.
   * @see #reference
   * @see #setReference
   */
  public String getReference() {
    return this.reference;
  }

  /**
   * Set the new reference value.
   *
   * @param _reference
   *                new reference to set
   * @see #reference
   * @see #getReference
   */
  public void setReference(final String _reference) {
    this.reference = _reference;
  }

  /**
   * This is the setter method for the instance variable {@link #target}.
   *
   * @return value of instance variable {@link #target}
   * @see #target
   * @see #setTarget
   */
  public Target getTarget() {
    return this.target;
  }

  /**
   * This is the setter method for the instance variable {@link #target}.
   *
   * @param _target
   *                new value for instance variable {@link #target}
   * @see #target
   * @see #getTarget
   */
  public void setTarget(final Target _target) {
    this.target = _target;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetBottomHeight}.
   *
   * @return value of instance variable {@link #targetBottomHeight}
   * @see #targetBottomHeight
   * @see #setTargetBottomHeight
   */
  public int getTargetBottomHeight() {
    return this.targetBottomHeight;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetBottomHeight}.
   *
   * @param _targetBottomHeight
   *                new value for instance variable {@link #targetBottomHeight}
   * @see #targetBottomHeight
   * @see #getTargetBottomHeight
   */
  public void setTargetBottomHeight(final int _targetBottomHeight) {
    this.targetBottomHeight = _targetBottomHeight;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetConnectAttribute}.
   *
   * @return value of instance variable {@link #targetConnectAttribute}
   * @see #targetConnectAttribute
   * @see #setTargetConnectAttribute
   */
  public Attribute getTargetConnectAttribute() {
    return this.targetConnectAttribute;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetConnectAttribute}.
   *
   * @param _targetConnectAttr  new value for instance variable
   *                            {@link #targetConnectAttribute}
   * @see #targetConnectAttribute
   * @see #getTargetConnectAttribute
   */
  public void setTargetConnectAttribute(final Attribute _targetConnectAttr) {
    this.targetConnectAttribute = _targetConnectAttr;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetCreateType}.
   *
   * @return value of instance variable {@link #targetCreateType}
   * @see #targetCreateType
   * @see #setTargetCreateType
   */
  public Type getTargetCreateType() {
    return this.targetCreateType;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetCreateType}.
   *
   * @param _targetCreateType
   *                new value for instance variable {@link #targetCreateType}
   * @see #targetCreateType
   * @see #getTargetCreateType
   */
  public void setTargetCreateType(final Type _targetCreateType) {
    this.targetCreateType = _targetCreateType;
  }

  /**
   * This is the getter method for the instance variable
   * {@link #targetDefaultMenu}.
   *
   * @return value of instance variable {@link #targetDefaultMenu}
   */
  public boolean hasTargetDefaultMenu() {
    return this.targetDefaultMenu;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetDefaultMenu}.
   *
   * @param _targetDefaultMenu the targetDefaultMenu to set
   */
  public void setTargetDefaultMenu(final boolean _targetDefaultMenu) {
    this.targetDefaultMenu = _targetDefaultMenu;
  }

  /**
   * This is the setter method for the instance variable {@link #targetForm}.
   *
   * @return value of instance variable {@link #targetForm}
   * @see #targetForm
   * @see #setTargetForm
   */
  public Form getTargetForm() {
    return this.targetForm;
  }

  /**
   * This is the setter method for the instance variable {@link #targetForm}.
   *
   * @param _targetForm
   *                new value for instance variable {@link #targetForm}
   * @see #targetForm
   * @see #getTargetForm
   */
  public void setTargetForm(final Form _targetForm) {
    this.targetForm = _targetForm;
  }

  /**
   * This is the setter method for the instance variable {@link #targetMenu}.
   *
   * @return value of instance variable {@link #targetMenu}
   * @see #targetMenu
   * @see #setTargetMenu
   */
  public Menu getTargetMenu() {
    Menu ret = null;
    if (this.targetDefaultMenu) {
      // reads the Value from "Common_Main_DefaultMenu"
      final SystemConfiguration kernelConfig = SystemConfiguration.get(
                    UUID.fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079"));
      final String menuname = kernelConfig.getAttributeValue("DefaultMenu");

      if (!"none".equals(menuname)) {
        if (this.targetMenu == null) {
          ret = Menu.get(menuname);
        } else {
          this.targetMenu.addAll(Menu.get(menuname));
          ret = this.targetMenu;
        }
      } else {
        ret = this.targetMenu;
      }
    }
    return ret;
  }

  /**
   * This is the setter method for the instance variable {@link #targetMenu}.
   *
   * @param _targetMenu
   *                new value for instance variable {@link #targetMenu}
   * @see #targetMenu
   * @see #getTargetMenu
   */
  public void setTargetMenu(final Menu _targetMenu) {
    this.targetMenu = _targetMenu;

  }

  /**
   * This is the setter method for the instance variable {@link #targetMode}.
   *
   * @return value of instance variable {@link #targetMode}
   * @see #targetMode
   * @see #setTargetMode
   */
  public TargetMode getTargetMode() {
    return this.targetMode;
  }

  /**
   * This is the setter method for the instance variable {@link #targetMode}.
   *
   * @param _targetMode
   *                new value for instance variable {@link #targetMode}
   * @see #targetMode
   * @see #getTargetMode
   */
  public void setTargetMode(final TargetMode _targetMode) {
    this.targetMode = _targetMode;
  }

  /**
   * This is the setter method for the instance variable {@link #targetSearch}.
   *
   * @return value of instance variable {@link #targetSearch}
   * @see #targetSearch
   * @see #setTargetSearch
   */
  public Search getTargetSearch() {
    return this.targetSearch;
  }

  /**
   * This is the setter method for the instance variable {@link #targetSearch}.
   *
   * @param _targetSearch
   *                new value for instance variable {@link #targetSearch}
   * @see #targetSearch
   * @see #getTargetSearch
   */
  public void setTargetSearch(final Search _targetSearch) {
    this.targetSearch = _targetSearch;
  }

  /**
   * This is the setter method for the instance variable {@link #targetTable}.
   *
   * @return value of instance variable {@link #targetTable}
   * @see #targetTable
   * @see #setTargetTable
   */
  public Table getTargetTable() {
    return this.targetTable;
  }

  /**
   * This is the setter method for the instance variable {@link #targetTable}.
   *
   * @param _targetTable
   *                new value for instance variable {@link #targetTable}
   * @see #targetTable
   * @see #getTargetTable
   */
  public void setTargetTable(final Table _targetTable) {
    this.targetTable = _targetTable;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableFilters}.
   *
   * @return value of instance variable {@link #targetTableFilters}
   * @see #targetTableFilters
   * @see #setTargetTableFilters
   */
  public List<TargetTableFilter> getTargetTableFilters() {
    return this.targetTableFilters;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableFilters}.
   *
   * @param _targetTableFilters
   *                new value for instance variable {@link #targetTableFilters}
   * @see #targetTableFilters
   * @see #getTargetTableFilters
   */
  private void setTargetTableFilters(
      final List<TargetTableFilter> _targetTableFilters) {
    this.targetTableFilters = _targetTableFilters;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableSortDirection}.
   *
   * @return value of instance variable {@link #targetTableSortDirection}
   * @see #targetTableSortDirection
   * @see #setTargetTableSortDirection
   */
  public SortDirection getTargetTableSortDirection() {
    return this.targetTableSortDirection;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableSortDirection}.
   *
   * @param _targetTableSortDirection
   *                new value for instance variable
   *                {@link #targetTableSortDirection}
   * @see #targetTableSortDirection
   * @see #getTargetTableSortDirection
   */
  public void setTargetTableSortDirection(
      final SortDirection _targetTableSortDirection) {
    this.targetTableSortDirection = _targetTableSortDirection;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableSortKey}.
   *
   * @return value of instance variable {@link #targetTableSortKey}
   * @see #targetTableSortKey
   * @see #setTargetTableSortKey
   */
  public String getTargetTableSortKey() {
    return this.targetTableSortKey;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableSortKey}.
   *
   * @param _targetTableSortKey
   *                new value for instance variable {@link #targetTableSortKey}
   * @see #targetTableSortKey
   * @see #getTargetTableSortKey
   */
  public void setTargetTableSortKey(final String _targetTableSortKey) {
    this.targetTableSortKey = _targetTableSortKey;
  }

  /**
   * This is the setter method for the instance variable {@link #targetTitle}.
   *
   * @return value of instance variable {@link #targetTitle}
   * @see #targetTitle
   * @see #setTargetTitle
   */
  public String getTargetTitle() {
    return this.targetTitle;
  }

  /**
   * This is the setter method for the instance variable {@link #targetTitle}.
   *
   * @param _targetTitle
   *                new value for instance variable {@link #targetTitle}
   * @see #targetTitle
   * @see #isTargetTitle
   */
  public void setTargetTitle(final String _targetTitle) {
    this.targetTitle = _targetTitle;
  }

  /**
   * The instance method returns the label of a command (or also menu). The
   * instance method looks in the DBProperties, if a property entry with prefix
   * <i>Command.</i> and name is found. This value is returned. If no entry is
   * found, the name of the command is returned.
   *
   * @return label of the command (or menu)
   */
  public String getViewableName() {
    String name = getName();
    if (DBProperties.hasProperty("Command." + getName())) {
      name = DBProperties.getProperty("Command." + getName());
    }
    return name;
  }

  /**
   * This is the getter method for the instance variable {@link #windowHeight}.
   *
   * @return value of instance variable {@link #windowHeight}
   * @see #windowHeight
   * @see #setWindowHeight
   */
  public int getWindowHeight() {
    return this.windowHeight;
  }

  /**
   * This is the setter method for the instance variable {@link #windowHeight}.
   *
   * @param _windowHeight
   *                new value for instance variable {@link #windowHeight}
   * @see #windowHeight
   * @see #getWindowHeight
   */
  private void setWindowHeight(final int _windowHeight) {
    this.windowHeight = _windowHeight;
  }

  /**
   * This is the getter method for the instance variable {@link #windowWidth}.
   *
   * @return value of instance variable {@link #windowWidth}
   * @see #windowWidth
   * @see #setWindowWidth
   */
  public int getWindowWidth() {
    return this.windowWidth;
  }

  /**
   * This is the setter method for the instance variable {@link #windowWidth}.
   *
   * @param _windowWidth
   *                new value for instance variable {@link #windowWidth}
   * @see #windowWidth
   * @see #getWindowWidth
   */
  private void setWindowWidth(final int _windowWidth) {
    this.windowWidth = _windowWidth;
  }

  /**
   * This is the getter method for the instance variable {@link #askUser}.
   *
   * @return value of instance variable {@link #askUser}
   * @see #askUser
   * @see #setAskUser
   */
  public boolean isAskUser() {
    return this.askUser;
  }

  /**
   * This is the setter method for the instance variable {@link #askUser}.
   *
   * @param _askUser
   *                new value for instance variable {@link #askUser}
   * @see #askUser
   * @see #getAskUser
   */
  private void setAskUser(final boolean _askUser) {
    this.askUser = _askUser;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #defaultSelected}.
   *
   * @return value of instance variable {@link #defaultSelected}
   * @see #defaultSelected
   * @see #setDefaultSelected
   */
  public boolean isDefaultSelected() {
    return this.defaultSelected;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #defaultSelected}.
   *
   * @param _defaultSelected
   *                new value for instance variable {@link #defaultSelected}
   * @see #defaultSelected
   * @see #isDefaultSelected
   */
  public void setDefaultSelected(final boolean _defaultSelected) {
    this.defaultSelected = _defaultSelected;
  }

  /**
   * This is the setter method for the instance variable {@link #submit}.
   *
   * @return value of instance variable {@link #submit}
   * @see #submit
   * @see #setSubmit
   */
  public boolean isSubmit() {
    return this.submit;
  }

  /**
   * This is the setter method for the instance variable {@link #submit}.
   *
   * @param _submit
   *                new value for instance variable {@link #submit}
   * @see #submit
   * @see #isSubmit
   */
  public void setSubmit(final boolean _submit) {
    this.submit = _submit;
  }

  /**
   * Test, if the value of instance variable {@link #target} is equal to
   * {@link #TARGET_CONTENT}.
   *
   * @return <i>true</i> if value is equal, otherwise false
   * @see #target
   * @see #getTarget
   */
  public boolean isTargetContent() {
    return getTarget() == Target.CONTENT;
  }

  /**
   * Test, if the value of instance variable {@link #target} is equal to
   * {@link #TARGET_HIDDEN}.
   *
   * @return <i>true</i> if value is equal, otherwise false
   * @see #target
   * @see #getTarget
   */
  public boolean isTargetHidden() {
    return getTarget() == Target.HIDDEN;
  }

  /**
   * Test, if the value of instance variable {@link #target} is equal to
   * {@link #TARGET_POPUP}.
   *
   * @return <i>true</i> if value is equal, otherwise false
   * @see #target
   * @see #getTarget
   */
  public boolean isTargetPopup() {
    return getTarget() == Target.POPUP;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetShowCheckBoxes}.
   *
   * @return value of instance variable {@link #targetShowCheckBoxes}
   * @see #targetShowCheckBoxes
   * @see #setTargetShowCheckBoxes
   */
  public boolean isTargetShowCheckBoxes() {
    return this.targetShowCheckBoxes;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetShowCheckBoxes}.
   *
   * @param _targetShowCheckBoxes
   *                new value for instance variable
   *                {@link #targetShowCheckBoxes}
   * @see #targetShowCheckBoxes
   * @see #isTargetShowCheckBoxes
   */
  public void setTargetShowCheckBoxes(final boolean _targetShowCheckBoxes) {
    this.targetShowCheckBoxes = _targetShowCheckBoxes;
  }

  /**
   * @param _linkType   type of the link property
   * @param _toId       to id
   * @param _toType     to type
   * @param _toName     to name
   * @throws Exception on error
   */
  @Override
  protected void setLinkProperty(final EFapsClassNames _linkType,
                                 final long _toId,
                                 final EFapsClassNames _toType,
                                 final String _toName)
      throws Exception {
    switch (_linkType) {
      case LINK_ICON:
        setIcon(RequestHandler.replaceMacrosInUrl(RequestHandler.URL_IMAGE
            + _toName));
        break;
      case LINK_TARGET_FORM:
        setTargetForm(Form.get(_toId));
        break;
      case LINK_TARGET_MENU:
        setTargetMenu(Menu.get(_toId));
        break;
      case LINK_TARGET_SEARCH:
        setTargetSearch(Search.get(_toName));
        break;
      case LINK_TARGET_TABLE:
        setTargetTable(Table.get(_toId));
        break;
      default:
        super.setLinkProperty(_linkType, _toId, _toType, _toName);
    }
  }

  /**
   * The instance method sets a new property value.
   *
   * @param _name     name of the property
   * @param _value    value of the property
   * @throws CacheReloadException on error during reload
   */
  @Override
  protected void setProperty(final String _name,
                             final String _value)
      throws CacheReloadException {
    if ("AskUser".equals(_name)) {
      if ("true".equalsIgnoreCase(_value)) {
        setAskUser(true);
      } else {
        setAskUser(false);
      }
    } else if ("DefaultSelected".equals(_name)) {
      if ("true".equalsIgnoreCase(_value)) {
        setDefaultSelected(true);
      } else {
        setDefaultSelected(false);
      }
    } else if ("HRef".equals(_name)) {
      setReference(RequestHandler.replaceMacrosInUrl(_value));
    } else if ("Icon".equals(_name)) {
      setIcon(RequestHandler.replaceMacrosInUrl(_value));
    } else if ("Label".equals(_name)) {
      setLabel(_value);
    } else if ("Submit".equals(_name)) {
      if ("true".equals(_value)) {
        setSubmit(true);
      }
    } else if ("Target".equals(_name)) {
      if ("content".equals(_value)) {
        setTarget(Target.CONTENT);
      } else if ("hidden".equals(_value)) {
        setTarget(Target.HIDDEN);
      } else if ("popup".equals(_value)) {
        setTarget(Target.POPUP);
      } else if ("modal".equals(_value)) {
        setTarget(Target.MODAL);
      }
    } else if ("TargetBottomHeight".equals(_name)) {
      setTargetBottomHeight(Integer.parseInt(_value));
    } else if ("TargetConnectAttribute".equals(_name)) {
      setTargetConnectAttribute(Attribute.get(_value));
    } else if ("TargetCreateType".equals(_name)) {
      setTargetCreateType(Type.get(_value));
    } else if ("TargetDefaultMenu".equals(_name)) {
      if ("none".equals(_value)) {
        setTargetDefaultMenu(false);
      }
    } else if ("TargetMode".equals(_name)) {
      if ("create".equals(_value)) {
        setTargetMode(TargetMode.CREATE);
      } else if ("edit".equals(_value)) {
        setTargetMode(TargetMode.EDIT);
      } else if ("connect".equals(_value)) {
        setTargetMode(TargetMode.CONNECT);
      } else if ("search".equals(_value)) {
        setTargetMode(TargetMode.SEARCH);
      } else if ("view".equals(_value)) {
        setTargetMode(TargetMode.VIEW);
      }
    } else if ("TargetShowCheckBoxes".equals(_name)) {
      if ("true".equalsIgnoreCase(_value)) {
        setTargetShowCheckBoxes(true);
      } else {
        setTargetShowCheckBoxes(false);
      }
    } else if (_name.startsWith("TargetTableFilter")) {
      if (getTargetTableFilters() == null) {
        setTargetTableFilters(new Vector<TargetTableFilter>());
      }
      getTargetTableFilters().add(new TargetTableFilter(_value));
    } else if ("TargetTableSortKey".equals(_name)) {
      setTargetTableSortKey(_value);
      setTargetTableSortDirection(SortDirection.ASCENDING);
    } else if ("TargetTableSortDirection".equals(_name)) {
      if (SortDirection.DESCENDING.value.equals(_value)) {
        setTargetTableSortDirection(SortDirection.DESCENDING);
      } else {
        setTargetTableSortDirection(SortDirection.ASCENDING);
      }
    } else if ("TargetTitle".equals(_name)) {
      setTargetTitle(_value);
    } else if ("WindowHeight".equals(_name)) {
      setWindowHeight(Integer.parseInt(_value));
    } else if ("WindowWidth".equals(_name)) {
      setWindowWidth(Integer.parseInt(_value));
    } else {
      super.setProperty(_name, _value);
    }
  }

  /**
   * The class stores the filter of the target table.
   */
  public final class TargetTableFilter {

    /**
     * The instance variable stores the sql clause.
     *
     * @see #getClause
     * @see #setClause
     */
    private String clause = null;

    // /////////////////////////////////////////////////////////////////////////

    /**
     * Constructor to create a new target table filter instance.
     *
     * @param _clause
     *                sql where clause for this filter
     */
    private TargetTableFilter(final String _clause) {
      setClause(_clause);
    }

    // /////////////////////////////////////////////////////////////////////////

    /**
     * This is the setter method for the instance variable {@link #clause}.
     *
     * @return value of instance variable {@link #clause}
     * @see #clause
     * @see #setClause
     */
    public String getClause() {
      return this.clause;
    }

    /**
     * This is the setter method for the instance variable {@link #clause}.
     *
     * @param _clause
     *                new value for instance variable {@link #clause}
     * @see #clause
     * @see #getClause
     */
    public void setClause(final String _clause) {
      this.clause = _clause;
    }
  }

}
