/*
 * Copyright 2006 The eFaps Team
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.Role;
import org.efaps.admin.user.UserObject;
import org.efaps.beans.TableBean;
import org.efaps.db.Cache;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.servlet.RequestHandler;

/**
 *
 */
public abstract class CommandAbstract extends UserInterfaceObject  {

  /**
   * Constructor to set the id and name of the command object.
   * The constructor also sets the label of the command.
   *
   * @param _id   id  of the command to set
   * @param _name name of the command to set
   * @see #label
   */
  protected CommandAbstract(long _id, String _name)  {
    super(_id, _name);
    setLabel(_name + ".Label");
    setTargetTitle(_name + ".Title");
  }

  /**
   * The target of the href is not known. This is maybe, if a javascript
   * function is directly called.
   *
   * @see #target
   */
  static public final int TARGET_UNKNOWN  = 0;

  /**
   * The target of the href is the content frame.
   *
   * @see #target
   */
  static public final int TARGET_CONTENT  = 1;

  /**
   * The target of the href is a new window popped up.
   *
   * @see #target
   */
  static public final int TARGET_POPUP    = 2;


  /**
   * The target of the href is the hidden frame.
   *
   * @see #target
   */
  static public final int TARGET_HIDDEN    = 3;

  static public final int TARGET_MODE_UNKNOWN = 0;
  static public final int TARGET_MODE_VIEW = 1;
  static public final int TARGET_MODE_EDIT = 2;
  static public final int TARGET_MODE_CREATE = 3;
  static public final int TARGET_MODE_CONNECT = 4;

  static public final int TABLE_SORT_DIRECTION_UP = 0;
  static public final int TABLE_SORT_DIRECTION_DOWN = 1;


  static public final int ACTION_UNKNOWN = 0;
  static public final int ACTION_DELETE = 1;


  /**
   * Check, if the user of the context has access to this command.
   * If no access user is assigned to this command, all user have access.
   * Otherwise check if the context person contains one of the assigned role
   * of this command.
   *
   * @param _context  context for this request (including the person)
   * @return  <i>true</i>if context user has access, otherwise <i>false</i> is
   *          returned
   */
  public boolean checkAccess(Context _context)  {
    boolean ret = false;

    if (getAccess().isEmpty())  {
      ret = true;
    } else  {
      Iterator iter = getAccess().iterator();
      while (iter.hasNext())  {
        Role role = (Role)iter.next();
        if (_context.getPerson().getRoles().contains(role))  {
          ret = true;
          break;
        }
      }
    }

    return ret;
  }

  /**
   * Test, if the value of instance variable {@link #target} is equal to
   * {@link #TARGET_CONTENT}.
   *
   * @return <i>true</i> if value is equal, otherwise false
   * @see #target
   * @see #getTarget
   */
  public boolean isTargetContent()  {
    return getTarget() == TARGET_CONTENT;
  }

  /**
   * Test, if the value of instance variable {@link #target} is equal to
   * {@link #TARGET_POPUP}.
   *
   * @return <i>true</i> if value is equal, otherwise false
   * @see #target
   * @see #getTarget
   */
  public boolean isTargetPopup()  {
    return getTarget() == TARGET_POPUP;
  }

  /**
   * Test, if the value of instance variable {@link #target} is equal to
   * {@link #TARGET_HIDDEN}.
   *
   * @return <i>true</i> if value is equal, otherwise false
   * @see #target
   * @see #getTarget
   */
  public boolean isTargetHidden()  {
    return getTarget() == TARGET_HIDDEN;
  }

  /**
   * The instance method returns the label of a command (or also menu). The
   * instance method looks in the properties, if a property entry with prefix
   * <i>Command.</i> and name is found. This value is returned. If no entry
   * is found, the name of the command is returned.
   *
   * @param _context  context for this request
   * @return label of the command (or menu)
   */
  public String getViewableName(Context _context)  {
    String name = getName();
    ResourceBundle msgs = ResourceBundle.getBundle("org.efaps.properties.AttributeRessource", _context.getLocale());
    try  {
      name = msgs.getString("Command."+name);
    } catch (MissingResourceException e)  {
    }
    return name;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * @param _context  eFaps context for this request
   * @param _linkType type of the link property
   * @param _toId     to id
   * @param _toType   to type
   * @param _toName   to name
   */
  protected void setLinkProperty(Context _context, EFapsClassName _linkType, long _toId, EFapsClassName _toType, String _toName) throws Exception  {
    switch (_linkType)  {
      case LINK_ICON:           setIcon(RequestHandler.replaceMacrosInUrl("${ROOTURL}/servlet/image/" + _toName));break;
      case LINK_TARGET_FORM:    setTargetForm(Form.get(_context, _toId));break;
      case LINK_TARGET_MENU:    setTargetMenu(Menu.get(_toId));break;
      case LINK_TARGET_SEARCH:  setTargetSearch(Search.get(_context, _toName));break;
      case LINK_TARGET_TABLE:   setTargetTable(Table.get(_context, _toId));break;
      default:                  super.setLinkProperty(_context, _linkType, _toId, _toType, _toName);
    }
  }

  /**
   * The instance method sets a new property value.
   *
   * @param _context  eFaps context for this request
   * @param _name     name of the property
   * @param _value    value of the property
   */
  protected void setProperty(Context _context, String _name, String _value) throws Exception  {
    if (_name.equals("Action"))  {
      if (_value.equals("delete"))  {
        setAction(ACTION_DELETE);
      }
    } else if (_name.equals("AskUser"))  {
      if ("true".equalsIgnoreCase(_value))  {
        setAskUser(true);
      } else  {
        setAskUser(false);
      }
    } else if (_name.equals("DefaultSelected"))  {
      if ("true".equalsIgnoreCase(_value))  {
        setDefaultSelected(true);
      } else  {
        setDefaultSelected(false);
      }
    } else if (_name.equals("DeleteIndex"))  {
      setDeleteIndex(Integer.parseInt(_value));
    } else if (_name.equals("HRef"))  {
      setReference(RequestHandler.replaceMacrosInUrl(_value));
    } else if (_name.equals("Icon"))  {
      setIcon(RequestHandler.replaceMacrosInUrl(_value));
    } else if (_name.equals("Label"))  {
      setLabel(_value);
    } else if (_name.equals("Submit"))  {
      if (_value.equals("true"))  {
        setSubmit(true);
      }
    } else if (_name.equals("Target"))  {
      if (_value.equals("content"))  {
        setTarget(TARGET_CONTENT);
      } else if (_value.equals("hidden"))  {
        setTarget(TARGET_HIDDEN);
      } else if (_value.equals("popup"))  {
        setTarget(TARGET_POPUP);
      }
    } else if (_name.equals("TargetBottomHeight"))  {
      setTargetBottomHeight(Integer.parseInt(_value));
    } else if (_name.equals("TargetConnectAttribute"))  {
      setTargetConnectAttribute(Attribute.get(_value));
// "TargetConnectChildAttribute"
// "TargetConnectParentAttribute"
// "TargetConnectType"
    } else if (_name.equals("TargetCreateType"))  {
      setTargetCreateType(Type.get(_value));
    } else if (_name.equals("TargetFormBean"))  {
      setTargetFormBean(Class.forName(_value));
    } else if (_name.equals("TargetMode"))  {
      if (_value.equals("create"))  {
        setTargetMode(TARGET_MODE_CREATE);
      } else if (_value.equals("edit"))  {
        setTargetMode(TARGET_MODE_EDIT);
      } else if (_value.equals("connect"))  {
        setTargetMode(TARGET_MODE_CONNECT);
      } else if (_value.equals("view"))  {
        setTargetMode(TARGET_MODE_VIEW);
      }
    } else if (_name.equals("TargetShowCheckBoxes"))  {
      if ("true".equalsIgnoreCase(_value))  {
        setTargetShowCheckBoxes(true);
      } else  {
        setTargetShowCheckBoxes(false);
      }
    } else if (_name.equals("TargetTableBean"))  {
      setTargetTableBean(Class.forName(_value));
    } else if (_name.startsWith("TargetTableFilter"))  {
      int index = Integer.parseInt(_name.substring(17));
      if (getTargetTableFilters()==null)  {
        setTargetTableFilters(new Vector<TargetTableFilter>());
      }
      getTargetTableFilters().add(new TargetTableFilter(_value));
    } else if (_name.equals("TargetTableSortKey"))  {
      setTargetTableSortKey(_value);
    } else if (_name.equals("TargetTableSortDirection"))  {
      if (_value.equals("up"))  {
        setTargetTableSortDirection(TABLE_SORT_DIRECTION_UP);
      } else if (_value.equals("down"))  {
        setTargetTableSortDirection(TABLE_SORT_DIRECTION_DOWN);
      }
    } else if (_name.equals("TargetTitle"))  {
      setTargetTitle(_value);
    } else if (_name.equals("WindowHeight"))  {
      setWindowHeight(Integer.parseInt(_value));
    } else if (_name.equals("WindowWidth"))  {
      setWindowWidth(Integer.parseInt(_value));
    } else {
      super.setProperty(_context, _name, _value);
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Instance variable to hold the reference to call.
   *
   * @see #setReference
   * @see #getReference
   */
  private String reference=null;

  /**
   * Instance variable to hold the reference to the icon file.
   *
   * @see #setIcon
   * @see #getIcon
   */
  private String icon=null;

  /**
   * Access HashSet to store all users who have access to this menu.
   *
   * @see #getAccess
   * @see #add(Role)
   */
  private HashSet<UserObject> access = new HashSet<UserObject>();

  /**
   * If the value is set to <i>true</i>. the commands submits the current
   * form to the given href url and the given target. The default value is
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
  private int target = TARGET_UNKNOWN;

  /**
   * The instance variable stores the target user interface form object which
   * is shown by the this abstract commmand.
   *
   * @see #getTargetForm
   * @see #setTargetForm
   */
  private Form targetForm = null;

  /**
   * The instance variable stores the table bean for commands calling a form.
   * The form bean overwrites the original form bean class.
   *
   * @see #getTargetFormBean
   * @see #setTargetFormBean
   */
  private Class targetFormBean = null;

  /**
   * The instance method stores the complete menu. Default value is a null and
   * no menu is shown.
   *
   * @see #setTargetMenu
   * @see #getTargetMenu
   */
  private Menu targetMenu = null;

  /**
   * The instance variable stores the mode of the target user interface
   * object.
   *
   * @see #getTargetMode
   * @see #setTargetMode
   */
  private int targetMode = TARGET_MODE_UNKNOWN;

  /**
   * The instance variable stores the search of target user interface object.
   *
   * @see #getTargetSearch
   * @see #setTargetSearch
   */
  private Search targetSearch = null;

  /**
   * The instance variable stores the target user interface table object which
   * is shown by the this abstract commmand.
   *
   * @see #getTargetTable
   * @see #setTargetTable
   */
  private Table targetTable = null;

  /**
   * The instance variable stores the table bean for commands calling a table.
   * The table bean overwrites the original table bean class.
   *
   * @see #getTargetTableBean
   * @see #setTargetTableBean
   */
  private Class targetTableBean = null;

  /**
   * The instance variable stores for target user interface table object the
   * default sort key.
   *
   * @see #getTargetTableSortKey
   * @see #setTargetTableSortKey
   */
  private String targetTableSortKey = null;

  /**
   * The instance variable stores for target user interface table object the
   * default sort direction. The default value is
   * {@link #TABLE_SORT_DIRECTION_UP}.
   *
   * @see #getTargetTableSortDirection
   * @see #setTargetTableSortDirection
   */
  private int targetTableSortDirection = TABLE_SORT_DIRECTION_UP;

  /**
   * The instance variable store the filters for the targer table.
   *
   * @see #getTargetTableFilters
   * @see #setTargetTableFilters
   */
  private List<TargetTableFilter> targetTableFilters = null;

  /**
   * The instance variable stores the target connect attribute used for the
   * connect in a form.
   *
   * @see #getTargetConnectAttribute
   * @see #setTargetConnectAttribute
   */
  private Attribute targetConnectAttribute = null;

  /**
   * The instance variable stores the create type for the target user
   * interface object.
   *
   * @see #getTargetCreateType
   * @see #setTargetCreateType
   */
  private Type targetCreateType = null;

  /**
   * The instance variable stores the height for the target bottom. Only a
   * is set, the value is used from the JSP pages.
   *
   * @see #getTargetBottomHeight
   * @see #setTargetBottomHeight
   */
  private int targetBottomHeight = 0;

  /**
   * Standard checkboxes for a table must be shown. The checkboxes are used
   * e.g. to delete selected.
   *
   * @see #isTargetShowCheckBoxes
   * @see #setTargetShowCheckBoxes
   */
  private boolean targetShowCheckBoxes = false;

  /**
   * Sets the title of the target window.
   *
   * @see #getTargetTitle
   * @see #setTargetTitle
   */
  private String targetTitle = null;

  /**
   * The instance variable stores if the execution of the command needs a
   * confirmation of the user. The default value is <i>false</i>.
   *
   * @see #isAskUser
   * @see #setAskUser
   */
  private boolean askUser = false;

  /**
   * The instance variable stores the window height of the popup window
   * ({@link #target} is set to {@link #TARGET_POPUP}). The default value is
   * <i>400</i>.
   *
   * @see #getWindowHeight
   * @see #setWindowHeight
   */
  private int windowHeight = 400;

  /**
   * The instance variable stores the window width of the popup window
   * ({@link #target} is set to {@link #TARGET_POPUP}). The default value is
   * <i>600</i>.
   *
   * @see #getWindowWidth
   * @see #setWindowWidth
   */
  private int windowWidth = 600;

  /**
   * The instance variable stores the predefined actions which can be executed
   * e.g. from the footer menu of a web table.
   *
   * @see #getAction
   * @see #setAction
   */
  private int action = ACTION_UNKNOWN;

  /**
   * If the instance variable is set to <i>tree</i>, the command is selected
   * as default command in the navigation tree.
   *
   * @see #isDefaultSelected
   * @see #setDefaultSelected
   */
  private boolean defaultSelected = false;

  /**
   * The instance variable stores the index of the delete object id in the
   * string of all oids of the checkbox.
   *
   * @see #getDeleteIndex
   * @see #setDeleteIndex
   */
  private int deleteIndex = 0;

  /**
   * The instance variable stores the label of this command instance. The
   * default value is set from the constructor to the name plus extension
   * '.Label'.
   *
   * @see #getLabel
   * @see #setLabel
   */
  private String label = null;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Set the new reference value.
   *
   * @param _reference new reference to set
   * @see #reference
   * @see #getReference
   */
  public void setReference(String _reference)  {
    this.reference  =_reference;
  }

  /**
   * Get the current reference value.
   *
   * @return the value of the instance variable {@link #reference}.
   * @see #reference
   * @see #setReference
   */
  public String getReference()  {
    return this.reference;
  }

  /**
   * Set the new icon reference value.
   *
   * @param _icon new icon reference to set
   * @see #icon
   * @see #getIcon
   */
  public void setIcon(String _icon)  {
    this.icon = _icon;
  }

  /**
   * Get the current icon reference value.
   *
   * @return the value of the instance variable {@link #icon}.
   * @see #icon
   * @see #setIcon
   */
  public String getIcon()  {
    return this.icon;
  }

  /**
   * Getter method for the HashSet instance variable {@link #access}.
   *
   * @return value of the HashSet instance variable {@link #access}
   * @see #access
   * @see #add(Role)
   */
  public Set<UserObject> getAccess()  {
    return this.access;
  }

  /**
   * Add a new role for access to this command.
   *
   * @param _role
   * @see #access
   * @see #getAccess
   */
  protected void add(Role _role)  {
    getAccess().add(_role);
  }

  /**
   * This is the setter method for the instance variable {@link #submit}.
   *
   * @return value of instance variable {@link #submit}
   * @see #submit
   * @see #setSubmit
   */
  public boolean isSubmit()  {
    return this.submit;
  }

  /**
   * This is the setter method for the instance variable {@link #submit}.
   *
   * @param _submit  new value for instance variable {@link #submit}
   * @see #submit
   * @see #isSubmit
   */
  public void setSubmit(boolean _submit)  {
    this.submit = _submit;
  }

  /**
   * This is the setter method for the instance variable {@link #target}.
   *
   * @return value of instance variable {@link #target}
   * @see #target
   * @see #setTarget
   */
  public int getTarget()  {
    return this.target;
  }

  /**
   * This is the setter method for the instance variable {@link #target}.
   *
   * @param _target  new value for instance variable {@link #target}
   * @see #target
   * @see #getTarget
   */
  public void setTarget(int _target)  {
    this.target = _target;
  }

  /**
   * This is the setter method for the instance variable {@link #targetForm}.
   *
   * @return value of instance variable {@link #targetForm}
   * @see #targetForm
   * @see #setTargetForm
   */
  public Form getTargetForm()  {
    return this.targetForm;
  }

  /**
   * This is the setter method for the instance variable {@link #targetForm}.
   *
   * @param _targetForm  new value for instance variable {@link #targetForm}
   * @see #targetForm
   * @see #getTargetForm
   */
  public void setTargetForm(Form _targetForm)  {
    this.targetForm = _targetForm;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetFormBean}.
   *
   * @return value of instance variable {@link #targetFormBean}
   * @see #targetFormBean
   * @see #setTargetFormBean
   */
  public Class getTargetFormBean()  {
    return this.targetFormBean;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetFormBean}.
   *
   * @param _targetFormBean  new value for instance variable
   *                          {@link #targetFormBean}
   * @see #targetFormBean
   * @see #getTargetFormBean
   */
  public void setTargetFormBean(Class _targetFormBean)  {
    this.targetFormBean = _targetFormBean;
  }

  /**
   * This is the setter method for the instance variable {@link #targetMenu}.
   *
   * @return value of instance variable {@link #targetMenu}
   * @see #targetMenu
   * @see #setTargetMenu
   */
  public Menu getTargetMenu()  {
    return this.targetMenu;
  }

  /**
   * This is the setter method for the instance variable {@link #targetMenu}.
   *
   * @param _targetMenu  new value for instance variable {@link #targetMenu}
   * @see #targetMenu
   * @see #getTargetMenu
   */
  public void setTargetMenu(Menu _targetMenu)  {
    this.targetMenu = _targetMenu;
  }

  /**
   * This is the setter method for the instance variable {@link #targetMode}.
   *
   * @return value of instance variable {@link #targetMode}
   * @see #targetMode
   * @see #setTargetMode
   */
  public int getTargetMode()  {
    return this.targetMode;
  }

  /**
   * This is the setter method for the instance variable {@link #targetMode}.
   *
   * @param _targetMode  new value for instance variable {@link #targetMode}
   * @see #targetMode
   * @see #getTargetMode
   */
  public void setTargetMode(int _targetMode)  {
    this.targetMode = _targetMode;
  }

  /**
   * This is the setter method for the instance variable {@link #targetSearch}.
   *
   * @return value of instance variable {@link #targetSearch}
   * @see #targetSearch
   * @see #setTargetSearch
   */
  public Search getTargetSearch()  {
    return this.targetSearch;
  }

  /**
   * This is the setter method for the instance variable {@link #targetSearch}.
   *
   * @param _targetSearch  new value for instance variable {@link #targetSearch}
   * @see #targetSearch
   * @see #getTargetSearch
   */
  public void setTargetSearch(Search _targetSearch)  {
    this.targetSearch = _targetSearch;
  }

  /**
   * This is the setter method for the instance variable {@link #targetTable}.
   *
   * @return value of instance variable {@link #targetTable}
   * @see #targetTable
   * @see #setTargetTable
   */
  public Table getTargetTable()  {
    return this.targetTable;
  }

  /**
   * This is the setter method for the instance variable {@link #targetTable}.
   *
   * @param _targetTable  new value for instance variable {@link #targetTable}
   * @see #targetTable
   * @see #getTargetTable
   */
  public void setTargetTable(Table _targetTable)  {
    this.targetTable = _targetTable;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableBean}.
   *
   * @return value of instance variable {@link #targetTableBean}
   * @see #targetTableBean
   * @see #setTargetTableBean
   */
  public Class getTargetTableBean()  {
    return this.targetTableBean;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableBean}.
   *
   * @param _targetTableBean  new value for instance variable
   *                          {@link #targetTableBean}
   * @see #targetTableBean
   * @see #getTargetTableBean
   */
  public void setTargetTableBean(Class _targetTableBean)  {
    this.targetTableBean = _targetTableBean;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableSortKey}.
   *
   * @return value of instance variable {@link #targetTableSortKey}
   * @see #targetTableSortKey
   * @see #setTargetTableSortKey
   */
  public String getTargetTableSortKey()  {
    return this.targetTableSortKey;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableSortKey}.
   *
   * @param _targetTableSortKey   new value for instance variable
   *                              {@link #targetTableSortKey}
   * @see #targetTableSortKey
   * @see #getTargetTableSortKey
   */
  public void setTargetTableSortKey(String _targetTableSortKey)  {
    this.targetTableSortKey = _targetTableSortKey;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableSortDirection}.
   *
   * @return value of instance variable {@link #targetTableSortDirection}
   * @see #targetTableSortDirection
   * @see #setTargetTableSortDirection
   */
  public int getTargetTableSortDirection()  {
    return this.targetTableSortDirection;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableSortDirection}.
   *
   * @param _targetTableSortDirection new value for instance variable
   *                                  {@link #targetTableSortDirection}
   * @see #targetTableSortDirection
   * @see #getTargetTableSortDirection
   */
  public void setTargetTableSortDirection(int _targetTableSortDirection)  {
    this.targetTableSortDirection = _targetTableSortDirection;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableFilters}.
   *
   * @return value of instance variable {@link #targetTableFilters}
   * @see #targetTableFilters
   * @see #setTargetTableFilters
   */
  public List<TargetTableFilter> getTargetTableFilters()  {
    return this.targetTableFilters;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetTableFilters}.
   *
   * @param _targetTableFilters new value for instance variable
   *                                  {@link #targetTableFilters}
   * @see #targetTableFilters
   * @see #getTargetTableFilters
   */
  private void setTargetTableFilters(List<TargetTableFilter> _targetTableFilters)  {
    this.targetTableFilters = _targetTableFilters;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetConnectAttribute}.
   *
   * @return value of instance variable {@link #targetConnectAttribute}
   * @see #targetConnectAttribute
   * @see #setTargetConnectAttribute
   */
  public Attribute getTargetConnectAttribute()  {
    return this.targetConnectAttribute;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetConnectAttribute}.
   *
   * @param _targetConnectAttribute  new value for instance variable
   *                                {@link #targetConnectAttribute}
   * @see #targetConnectAttribute
   * @see #getTargetConnectAttribute
   */
  public void setTargetConnectAttribute(Attribute _targetConnectAttribute)  {
    this.targetConnectAttribute = _targetConnectAttribute;
  }

  /**
   * This is the setter method for the instance variable {@link #targetCreateType}.
   *
   * @return value of instance variable {@link #targetCreateType}
   * @see #targetCreateType
   * @see #setTargetCreateType
   */
  public Type getTargetCreateType()  {
    return this.targetCreateType;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetCreateType}.
   *
   * @param _targetCreateType   new value for instance variable
   *                            {@link #targetCreateType}
   * @see #targetCreateType
   * @see #getTargetCreateType
   */
  public void setTargetCreateType(Type _targetCreateType)  {
    this.targetCreateType = _targetCreateType;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetBottomHeight}.
   *
   * @return value of instance variable {@link #targetBottomHeight}
   * @see #targetBottomHeight
   * @see #setTargetBottomHeight
   */
  public int getTargetBottomHeight()  {
    return this.targetBottomHeight;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetBottomHeight}.
   *
   * @param _targetBottomHeight   new value for instance variable
   *                                {@link #targetBottomHeight}
   * @see #targetBottomHeight
   * @see #getTargetBottomHeight
   */
  public void setTargetBottomHeight(int _targetBottomHeight)  {
    this.targetBottomHeight = _targetBottomHeight;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetShowCheckBoxes}.
   *
   * @return value of instance variable {@link #targetShowCheckBoxes}
   * @see #targetShowCheckBoxes
   * @see #setTargetShowCheckBoxes
   */
  public boolean isTargetShowCheckBoxes()  {
    return this.targetShowCheckBoxes;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #targetShowCheckBoxes}.
   *
   * @param _targetShowCheckBoxes   new value for instance variable
   *                                {@link #targetShowCheckBoxes}
   * @see #targetShowCheckBoxes
   * @see #isTargetShowCheckBoxes
   */
  public void setTargetShowCheckBoxes(boolean _targetShowCheckBoxes)  {
    this.targetShowCheckBoxes = _targetShowCheckBoxes;
  }

  /**
   * This is the setter method for the instance variable {@link #targetTitle}.
   *
   * @return value of instance variable {@link #targetTitle}
   * @see #targetTitle
   * @see #setTargetTitle
   */
  public String getTargetTitle()  {
    return this.targetTitle;
  }

  /**
   * This is the setter method for the instance variable {@link #targetTitle}.
   *
   * @param _targetTitle  new value for instance variable {@link #targetTitle}
   * @see #targetTitle
   * @see #isTargetTitle
   */
  public void setTargetTitle(String _targetTitle)  {
    this.targetTitle = _targetTitle;
  }

  /**
   * This is the getter method for the instance variable {@link #askUser}.
   *
   * @return value of instance variable {@link #askUser}
   * @see #askUser
   * @see #setAskUser
   */
  public boolean isAskUser()  {
    return this.askUser;
  }

  /**
   * This is the setter method for the instance variable {@link #askUser}.
   *
   * @param _askUser  new value for instance variable {@link #askUser}
   * @see #askUser
   * @see #getAskUser
   */
  private void setAskUser(boolean _askUser)  {
    this.askUser = _askUser;
  }

  /**
   * This is the getter method for the instance variable {@link #windowHeight}.
   *
   * @return value of instance variable {@link #windowHeight}
   * @see #windowHeight
   * @see #setWindowHeight
   */
  public int getWindowHeight()  {
    return this.windowHeight;
  }

  /**
   * This is the setter method for the instance variable {@link #windowHeight}.
   *
   * @param _windowHeight new value for instance variable {@link #windowHeight}
   * @see #windowHeight
   * @see #getWindowHeight
   */
  private void setWindowHeight(int _windowHeight)  {
    this.windowHeight = _windowHeight;
  }

  /**
   * This is the getter method for the instance variable {@link #windowWidth}.
   *
   * @return value of instance variable {@link #windowWidth}
   * @see #windowWidth
   * @see #setWindowWidth
   */
  public int getWindowWidth()  {
    return this.windowWidth;
  }

  /**
   * This is the setter method for the instance variable {@link #windowWidth}.
   *
   * @param _windowWidth  new value for instance variable {@link #windowWidth}
   * @see #windowWidth
   * @see #getWindowWidth
   */
  private void setWindowWidth(int _windowWidth)  {
    this.windowWidth = _windowWidth;
  }

  /**
   * This is the setter method for the instance variable {@link #action}.
   *
   * @return value of instance variable {@link #action}
   * @see #action
   * @see #setAction
   */
  public int getAction()  {
    return this.action;
  }

  /**
   * This is the setter method for the instance variable {@link #action}.
   *
   * @param _action  new value for instance variable {@link #action}
   * @see #action
   * @see #getAction
   */
  public void setAction(int _action)  {
    this.action = _action;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #defaultSelected}.
   *
   * @return value of instance variable {@link #defaultSelected}
   * @see #defaultSelected
   * @see #setDefaultSelected
   */
  public boolean isDefaultSelected()  {
    return this.defaultSelected;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #defaultSelected}.
   *
   * @param _defaultSelected  new value for instance variable
   *                          {@link #defaultSelected}
   * @see #defaultSelected
   * @see #isDefaultSelected
   */
  public void setDefaultSelected(boolean _defaultSelected)  {
    this.defaultSelected = _defaultSelected;
  }

  /**
   * This is the setter method for the instance variable {@link #deleteIndex}.
   *
   * @return value of instance variable {@link #deleteIndex}
   * @see #deleteIndex
   * @see #setDeleteIndex
   */
  public int getDeleteIndex()  {
    return this.deleteIndex;
  }

  /**
   * This is the setter method for the instance variable {@link #deleteIndex}.
   *
   * @param _deleteIndex  new value for instance variable
   *                          {@link #deleteIndex}
   * @see #deleteIndex
   * @see #getDeleteIndex
   */
  public void setDeleteIndex(int _deleteIndex)  {
    this.deleteIndex = _deleteIndex;
  }

  /**
   * This is the setter method for the instance variable {@link #label}.
   *
   * @return value of instance variable {@link #label}
   * @see #label
   * @see #setLabel
   */
  public String getLabel()  {
    return this.label;
  }

  /**
   * This is the setter method for the instance variable {@link #label}.
   *
   * @param _label  new value for instance variable
   *                          {@link #label}
   * @see #label
   * @see #getLabel
   */
  public void setLabel(String _label)  {
    this.label = _label;
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////

  /**
   * The class stores the filter of the target table.
   */
  public class TargetTableFilter  {

    /**
     * Constructor to create a new target table filter instance.
     *
     * @param _clause  sql where clause for this filter
     */
    private TargetTableFilter(String _clause)  {
      setClause(_clause);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance variable stores the sql clause.
     *
     * @see #getClause
     * @see #setClause
     */
    private String clause = null;

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the setter method for the instance variable {@link #clause}.
     *
     * @return value of instance variable {@link #clause}
     * @see #clause
     * @see #setClause
     */
    public String getClause()  {
      return this.clause;
    }

    /**
     * This is the setter method for the instance variable {@link #clause}.
     *
     * @param _clause  new value for instance variable {@link #clause}
     * @see #clause
     * @see #getClause
     */
    public void setClause(String _clause)  {
      this.clause = _clause;
    }
  }
}
