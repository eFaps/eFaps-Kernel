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

package org.efaps.admin.ui.field;

import static org.efaps.admin.EFapsClassNames.FIELD;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.ui.AbstractCollection;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.RequestHandler;
import org.efaps.util.cache.CacheReloadException;

/**
 * This class represents the Fields of the UserInterface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Field extends AbstractUserInterfaceObject
{
    /**
     * Used to define the different display modes for the Userinterface.
     */
    public static enum Display {
        /** the displayed Field is editabel. */
        EDITABLE,
        /** the displayed Field is read only.. */
        READONLY,
        /** the displayed Field is rendered but hidden. */
        HIDDEN,
        /** the field will not be displayed. */
        NONE;
    }
    /**
     * The static variable defines the class name in eFaps.
     */
    public static final EFapsClassNames EFAPS_CLASSNAME = FIELD;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Field.class);

    /**
     * This is the expression to get a field from the database.
     *
     * @see #setExpression
     * @see #getExpression
     */
    private String expression;

    /**
     * This is the expression to get the alternate id of a field from the
     * database.
     *
     * @see #setAlternateOID
     * @see #getAlternateOID
     */
    private String alternateOID = null;

    /**
     * This is the value in the create process. Default value is <i>null</i>.
     *
     * @see #setCreateValue
     * @see #getCreateValue
     */
    private String createValue = null;

    /**
     * Instance variable to hold the label.
     *
     * @see #setLabel
     * @see #getLabel
     */
    private String label = null;

    /**
     * Is a field multi line? If yes, the value must be higher than the default
     * value <i>1</i>. The value is only used for a create or a modify form.
     *
     * @see #setRows
     * @see #getRows
     */
    private int rows = 1;

    /**
     * Number of columns for a field. It is used only for a create or a modify
     * form. Default value is <i>20</i>. The column size is not the size of a
     * field in the database!
     *
     * @see #setCols
     * @see #getCols
     */
    private int cols = 20;

    /**
     * Is a field required? Default value is <i>false</i>.
     *
     * @see #setRequired
     * @see #isRequired
     */
    private boolean required = false;

    /**
     * Instance variable to hold the reference to call.
     *
     * @see #setReference
     * @see #getReference
     */
    private String reference = null;

    /**
     * Instance variable to hold the selected index.
     *
     * @see #setSelIndex
     * @see #getSelIndex
     */
    private int selIndex;

    /**
     * The field is represented by a radio button, if the field is editable.
     *
     * @see #setRadioButton
     * @see #isRadioButton
     */
    private boolean radioButton = false;

    /**
     * The field is represented by an program value.
     *
     * @see #setProgramValue
     * @see #getProgramValue
     */
    private FieldProgramValueInterface programValue = null;

    /**
     * The field has an icon..
     *
     * @see #setIcon
     * @see #getIcon
     */
    private String icon = null;

    /**
     * The target of the field href is the content frame.
     *
     * @set #getTarget
     * @see #setTarget
     */
    private Target target = Target.UNKNOWN;

    /**
     * The type icon for this field is shown if value is set to true.
     *
     * @see #setShowTypeIcon
     * @see #isShowTypeIcon
     */
    private boolean showTypeIcon = false;

    /**
     * The class is used to generate for a field user specific field values.
     *
     * @see #setClassUI
     * @see #getClassUI
     */
    private UIInterface classUI = null;

    /**
     * This field can be sorted in a Webtable.
     *
     * @see #isSortAble()
     */
    private boolean sortAble = true;

    /**
     * This field can be filtered in a Webtable.
     *
     * @see #isFilterable()
     * @see #setFilterable(boolean)
     */
    private boolean filterable = false;

    /**
     * The width of the field as weighted int.
     */
    private int width;

    /**
     * Is the width of this field fixed or or weighted.
     */
    private boolean fixedWidth = false;

    /**
     * Should the Label been hidden.
     */
    private boolean hideLabel = false;

    /**
     * Should rows been spanned.
     */
    private int rowSpan = 0;

    /**
     * Map stores the target mode to display relations for this field.
     */
    private final Map<AbstractUserInterfaceObject.TargetMode, Field.Display> mode2display
                                                 = new HashMap<AbstractUserInterfaceObject.TargetMode, Field.Display>();

    /**
     * Standart-Constructor.
     */
    public Field()
    {
        super(0, null, null);
    }

    /**
     * This is the constructor of the field class.
     *
     * @param _id id of the field instance
     * @param _uuid UUID of the field instance
     * @param _name name of the field instance
     */
    public Field(final long _id, final String _uuid, final String _name)
    {
        super(_id, _uuid, _name);

    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Field}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Field}
     */
    public static Field get(final long _id)
    {
        AbstractCollection col = null;

        try {
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(Type.get(FIELD).getName());
            query.setExpandChildTypes(true);
            query.addSelect("Collection");
            query.addWhereExprEqValue("ID", _id);
            query.executeWithoutAccessCheck();

            if (query.next()) {
                col = Form.get(((Number) query.get("Collection")).longValue());
                if (col == null) {
                    col = Table.get(((Number) query.get("Collection")).longValue());
                }
                query.close();
            }
        } catch (final EFapsException e) {
            Field.LOG.error("get(long)", e);
        }
        return col.getFieldsMap().get(_id);
    }

    /**
     * Test, if the value of instance variable {@link AbstractCommand.target} is
     * equal to {@link AbstractCommand.TARGET_CONTENT}.
     *
     * @return <i>true</i> if value is equal, otherwise false
     * @see #target
     * @see #getTarget
     */
    public boolean isTargetContent()
    {
        return getTarget() == Target.CONTENT;
    }

    /**
     * Test, if the value of instance variable {@link AbstractCommand.target} is
     * equal to {@link AbstractCommand.TARGET_POPUP}.
     *
     * @return <i>true</i> if value is equal, otherwise false
     * @see #target
     * @see #getTarget
     */
    public boolean isTargetPopup()
    {
        return getTarget() == Target.POPUP;
    }

    /**
     * Test, if the value of instance variable {@link AbstractCommand.target} is
     * equal to {@link AbstractCommand.TARGET_HIDDEN}.
     *
     * @return <i>true</i> if value is equal, otherwise false
     * @see #target
     * @see #getTarget
     */
    public boolean isTargetHidden()
    {
        return getTarget() == Target.HIDDEN;
    }

    /**
     * The method overrides the original method 'toString' and returns the
     * information of the field user interface object.
     *
     * @return name of the user interface object
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("expression", getExpression()).append(
                        "alternateOID", getAlternateOID()).toString();
    }

    // ///////////////////////////////////////////////////////////////////////////

    /**
     *
     * Set a link property for the field.
     *
     * @param _linkType type of the link property
     * @param _toId to id
     * @param _toType to type
     * @param _toName to name
     * @throws EFapsException on error
     */
    @Override
    protected void setLinkProperty(final EFapsClassNames _linkType, final long _toId, final EFapsClassNames _toType,
                    final String _toName) throws EFapsException
    {
        switch (_linkType) {
            case LINK_ICON:
                setIcon(RequestHandler.replaceMacrosInUrl(RequestHandler.URL_IMAGE + _toName));
                break;
            default:
                super.setLinkProperty(_linkType, _toId, _toType, _toName);
                break;
        }
    }

    /**
     * The instance method sets a new property value.
     *
     * @param _name name of the property
     * @param _value value of the property
     * @throws CacheReloadException on problems with the cache
     */
    @Override
    protected void setProperty(final String _name, final String _value) throws CacheReloadException
    {
        if ("AlternateOID".equals(_name)) {
            setAlternateOID(_value);
        } else if ("ClassNameUI".equals(_name)) {
            try {
                setClassUI((UIInterface) Class.forName(_value).newInstance());
            } catch (final ClassNotFoundException e) {
                throw new CacheReloadException("could not found class '" + _value + "' for '" + getName() + "'", e);
            } catch (final InstantiationException e) {
                throw new CacheReloadException("could not instantiate class '" + _value + "' for '" + getName() + "'",
                                e);
            } catch (final IllegalAccessException e) {
                throw new CacheReloadException("could not access class '" + _value + "' for '" + getName() + "'", e);
            }
        } else if ("Columns".equals(_name)) {
            setCols(Integer.parseInt(_value));
        } else if ("CreateValue".equals(_name)) {
            setCreateValue(_value);
        } else if ("Expression".equals(_name)) {
            setExpression(_value);
        } else if ("Width".equals(_name)) {
            setWidth(_value);
        } else if ("SortAble".equals(_name)) {
            setSortAble(!"false".equals(_value));
        } else if ("Filterable".equals(_name)) {
            setFilterable("true".equals(_value));
        } else if ("HideLabel".equals(_name)) {
            setHideLabel("true".equals(_value));
        } else if ("HRef".equals(_name)) {
            setReference(RequestHandler.replaceMacrosInUrl(_value));
        } else if ("Icon".equals(_name)) {
            setIcon(RequestHandler.replaceMacrosInUrl(_value));
        } else if ("Label".equals(_name)) {
            setLabel(_value);
        } else if ("ModeConnect".equals(_name)) {
            this.mode2display.put(TargetMode.CONNECT, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ModeCreate".equals(_name)) {
            this.mode2display.put(TargetMode.CREATE, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ModeEdit".equals(_name)) {
            this.mode2display.put(TargetMode.EDIT, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ModeSearch".equals(_name)) {
            this.mode2display.put(TargetMode.SEARCH, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ModeView".equals(_name)) {
            this.mode2display.put(TargetMode.VIEW, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ProgramValue".equals(_name)) {
            try {
                final Class<?> programValueClass = Class.forName(_value);
                setProgramValue((FieldProgramValueInterface) programValueClass.newInstance());
            } catch (final ClassNotFoundException e) {
                throw new CacheReloadException("could not found class '" + _value + "' for '" + getName() + "'", e);
            } catch (final InstantiationException e) {
                throw new CacheReloadException("could not instantiate class '" + _value + "' for '" + getName() + "'",
                                e);
            } catch (final IllegalAccessException e) {
                throw new CacheReloadException("could not access class '" + _value + "' for '" + getName() + "'", e);
            }

        } else if ("Required".equals(_name)) {
            if ("true".equals(_value)) {
                setRequired(true);
            }
        } else if ("Rows".equals(_name)) {
            setRows(Integer.parseInt(_value));
        } else if ("RowSpan".equals(_name)) {
            setRowSpan(Integer.parseInt(_value));
        } else if ("ShowTypeIcon".equals(_name)) {
            setShowTypeIcon("true".equals(_value));
        } else if ("Target".equals(_name)) {
            if ("content".equals(_value)) {
                setTarget(Target.CONTENT);
            } else if ("hidden".equals(_value)) {
                setTarget(Target.HIDDEN);
            } else if ("popup".equals(_value)) {
                setTarget(Target.POPUP);
            }
        } else {
            super.setProperty(_name, _value);
        }
    }

    /**
     * This is the setter method for instance variable {@link #expression}.
     *
     * @param _expression new value for instance variable {@link #expression}
     * @see #expression
     * @see #getExpression
     */
    public void setExpression(final String _expression)
    {
        this.expression = _expression;
    }

    /**
     * This is the getter method for instance variable {@link #expression}.
     *
     * @return the value of the instance variable {@link #expression}.
     * @see #expression
     * @see #setExpression
     */
    public String getExpression()
    {
        return this.expression;
    }

    /**
     * This is the setter method for instance variable {@link #alternateOID}.
     *
     * @param _alternateOID new value for instance variable
     *            {@link #alternateOID}
     * @see #alternateOID
     * @see #getAlternateOID
     */
    public void setAlternateOID(final String _alternateOID)
    {
        this.alternateOID = _alternateOID;
    }

    /**
     * This is the getter method for instance variable {@link #alternateOID}.
     *
     * @return the value of the instance variable {@link #alternateOID}.
     * @see #alternateOID
     * @see #setAlternateOID
     */
    public String getAlternateOID()
    {
        return this.alternateOID;
    }

    /**
     * This is the setter method for instance variable {@link #createValue}.
     *
     * @param _createValue new value for instance variable {@link #createValue}
     * @see #createValue
     * @see #getCreateValue
     */
    public void setCreateValue(final String _createValue)
    {
        this.createValue = _createValue;
    }

    /**
     * This is the getter method for instance variable {@link #createValue}.
     *
     * @return the value of the instance variable {@link #createValue}.
     * @see #createValue
     * @see #setCreateValue
     */
    public String getCreateValue()
    {
        return this.createValue;
    }

    /**
     * This is the setter method for instance variable {@link #label}.
     *
     * @param _label new value for instance variable {@link #label}
     * @see #label
     * @see #getLabel
     */
    public void setLabel(final String _label)
    {
        this.label = _label;
    }

    /**
     * This is the getter method for instance variable {@link #label}.
     *
     * @return the value of the instance variable {@link #label}.
     * @see #label
     * @see #setLabel
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * This is the setter method for instance variable {@link #rows}.
     *
     * @param _rows new value for instance variable {@link #rows}
     * @see #rows
     * @see #getRows
     */
    public void setRows(final int _rows)
    {
        this.rows = _rows;
    }

    /**
     * This is the getter method for instance variable {@link #rows}.
     *
     * @return the value of the instance variable {@link #rows}.
     * @see #rows
     * @see #setRows
     */
    public int getRows()
    {
        return this.rows;
    }

    /**
     * This is the setter method for instance variable {@link #cols}.
     *
     * @param _cols new value for instance variable {@link #cols}
     * @see #cols
     * @see #getCols
     */
    public void setCols(final int _cols)
    {
        this.cols = _cols;
    }

    /**
     * This is the getter method for instance variable {@link #cols}.
     *
     * @return the value of the instance variable {@link #cols}.
     * @see #cols
     * @see #setCols
     */
    public int getCols()
    {
        return this.cols;
    }


    /**
     * This is the getter method for instance variable {@link #filterable}.
     *
     * @return the value of the instance variable {@link #filterable}
     * @see #filterable
     * @see #setFilterable
     */
    public boolean isFilterable()
    {
        return this.filterable;
    }

    /**
     * This is the setter method for instance variable {@link #filterable}.
     *
     * @param _filterable set instance variable to this
     * @see #filterable
     * @see #setFilterable
     */
    private void setFilterable(final boolean _filterable)
    {
        this.filterable = _filterable;
    }


    /**
     * This is the setter method for instance variable {@link #required}.
     *
     * @param _required new value for instance variable {@link #required}
     * @see #required
     * @see #isRequired
     */
    public void setRequired(final boolean _required)
    {
        this.required = _required;
    }

    /**
     * This is the getter method for instance variable {@link #required}.
     *
     * @return the value of the instance variable {@link #required}.
     * @see #required
     * @see #setRequired
     */
    public boolean isRequired()
    {
        return this.required;
    }

    /**
     * This is the getter method for instance variable {@link #hideLabel}.
     *
     * @return the value of the instance variable {@link #hideLabel}.
     */
    public boolean isHideLabel()
    {
        return this.hideLabel;
    }

    /**
     * This is the setter method for instance variable {@link #hideLabel}.
     *
     * @param _hideLabel new value for instance variable {@link #hideLabel}
     *
     */
    public void setHideLabel(final boolean _hideLabel)
    {
        this.hideLabel = _hideLabel;
    }

    /**
     * This is the getter method for instance variable {@link #rowSpan}.
     *
     * @return the value of the instance variable {@link #rowSpan}.
     */
    public int getRowSpan()
    {
        return this.rowSpan;
    }

    /**
     * This is the setter method for instance variable {@link #rowSpan}.
     *
     * @param _rowSpan new value for instance variable {@link #rowSpan}
     */
    public void setRowSpan(final int _rowSpan)
    {
        this.rowSpan = _rowSpan;
    }

    /**
     * This is the setter method for instance variable {@link #reference}.
     *
     * @param _reference new value for instance variable {@link #reference}
     * @see #reference
     * @see #getReference
     */
    public void setReference(final String _reference)
    {
        this.reference = _reference;
    }

    /**
     * This is the getter method for instance variable {@link #reference}.
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
     * This is the setter method for instance variable {@link #selIndex}.
     *
     * @param _selIndex new value for instance variable {@link #selIndex}
     * @see #selIndex
     * @see #getSelIndex
     */
    protected void setSelIndex(final int _selIndex)
    {
        this.selIndex = _selIndex;
    }

    /**
     * This is the getter method for instance variable {@link #selIndex}.
     *
     * @return the value of the instance variable {@link #selIndex}.
     * @see #selIndex
     * @see #setSelIndex
     */
    public int getSelIndex()
    {
        return this.selIndex;
    }

    /**
     * This is the setter method for instance variable {@link #radioButton}.
     *
     * @param _radioButton new value for instance variable {@link #radioButton}
     * @see #radioButton
     * @see #isRadioButton
     */
    public void setRadioButton(final boolean _radioButton)
    {
        this.radioButton = _radioButton;
    }

    /**
     * This is the getter method for instance variable {@link #radioButton}.
     *
     * @return the value of the instance variable {@link #radioButton}.
     * @see #radioButton
     * @see #setRadioButton
     */
    public boolean isRadioButton()
    {
        return this.radioButton;
    }

    /**
     * This is the setter method for instance variable {@link #sortAble}.
     *
     * @return the value of the instance variable {@link #sortAble}.
     * @see #sortAble
     * @see #setSortAble
     */
    public boolean isSortAble()
    {
        return this.sortAble;
    }

    /**
     * This is the getter method for instance variable {@link #sortAble}.
     *
     * @param _sortable the value of the instance variable {@link #sortAble}.
     * @see #sortAble
     * @see #isSortAble
     */
    public void setSortAble(final boolean _sortable)
    {
        this.sortAble = _sortable;
    }

    /**
     * This is the setter method for instance variable {@link #programValue}.
     *
     * @param _programValue new value for instance variable
     *            {@link #programValue}
     * @see #programValue
     * @see #getProgramValue
     */
    public void setProgramValue(final FieldProgramValueInterface _programValue)
    {
        this.programValue = _programValue;
    }

    /**
     * This is the getter method for instance variable {@link #programValue}.
     *
     * @return the value of the instance variable {@link #programValue}.
     * @see #programValue
     * @see #setProgramValue
     */
    public FieldProgramValueInterface getProgramValue()
    {
        return this.programValue;
    }

    /**
     * This is the setter method for instance variable {@link #icon}.
     *
     * @param _icon new value for instance variable {@link #icon}
     * @see #icon
     * @see #getIcon
     */
    public void setIcon(final String _icon)
    {
        this.icon = _icon;
    }

    /**
     * This is the getter method for instance variable {@link #icon}.
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
     * This is the setter method for the instance variable {@link #target}.
     *
     * @param _target new value for instance variable {@link #target}
     * @see #target
     * @see #getTarget
     */
    public void setTarget(final Target _target)
    {
        this.target = _target;
    }

    /**
     * This is the setter method for the instance variable {@link #showTypeIcon}
     * .
     *
     * @return value of instance variable {@link #showTypeIcon}
     * @see #showTypeIcon
     * @see #setShowTypeIcon
     */
    public boolean isShowTypeIcon()
    {
        return this.showTypeIcon;
    }

    /**
     * This is the setter method for the instance variable {@link #showTypeIcon}
     * .
     *
     * @param _showTypeIcon new value for instance variable
     *            {@link #showTypeIcon}
     * @see #showTypeIcon
     * @see #getShowTypeIcon
     */
    public void setShowTypeIcon(final boolean _showTypeIcon)
    {
        this.showTypeIcon = _showTypeIcon;
    }

    /**
     * This is the setter method for the instance variable {@link #classUI}.
     *
     * @return value of instance variable {@link #classUI}
     * @see #classUI
     * @see #setClassUI
     */
    public UIInterface getClassUI()
    {
        return this.classUI;
    }

    /**
     * This is the setter method for the instance variable {@link #classUI}.
     *
     * @param _classUI new value for instance variable {@link #classUI}
     * @see #classUI
     * @see #getClassUI
     */
    private void setClassUI(final UIInterface _classUI)
    {
        this.classUI = _classUI;
    }

    /**
     * This is the getter method for the instance variable {@link #width}.
     *
     * @return value of instance variable {@link #width}
     */
    public int getWidth()
    {
        if (this.width == 0) {
            this.width = 1;
        }
        return this.width;
    }

    /**
     * This is the setter method for the instance variable {@link #width}.
     *
     * @param _value the width to set
     */
    public void setWidth(final String _value)
    {
        String strwidth = _value;
        if (strwidth.endsWith("px")) {
            this.fixedWidth = true;
            strwidth = _value.substring(0, strwidth.length() - 2);
        }
        this.width = Integer.parseInt(strwidth);
    }

    /**
     * This is the getter method for the instance variable {@link #fixedWidth}.
     *
     * @return value of instance variable {@link #fixedWidth}
     */
    public boolean isFixedWidth()
    {
        return this.fixedWidth;
    }

    /**
     * Is this field editable in the given target mode. If not explicitly set
     * in the definition following defaults apply:
     * <ul>
     * <li>ModeConnect: false</li>
     * <li>ModeCreate: false</li>
     * <li>ModeView: false</li>
     * <li>ModeEdit: false</li>
     * <li>ModeSearch: false</li>
     * </ul>
     *
     * @param _mode  target mode
     * @return true if editable in the given mode, else false
     */
    public boolean isEditable(final TargetMode _mode)
    {
        boolean ret = false;
        if (this.mode2display.containsKey(_mode)) {
            ret = this.mode2display.get(_mode).equals(Field.Display.EDITABLE);
        }
        return ret;
    }

    /**
     * Is this field read only in the given target mode. If not explicitly set
     * in the definition following defaults apply:
     * <ul>
     * <li>ModeConnect: true</li>
     * <li>ModeCreate: false</li>
     * <li>ModeView: true</li>
     * <li>ModeEdit: true</li>
     * <li>ModeSearch: false</li>
     * </ul>
     *
     * @param _mode  target mode
     * @return true if editable in the given mode, else false
     */
    public boolean isReadonly(final TargetMode _mode)
    {
        boolean ret = false;
        if (this.mode2display.containsKey(_mode)) {
            ret = this.mode2display.get(_mode).equals(Field.Display.READONLY);
        } else if (_mode.equals(TargetMode.CONNECT) || _mode.equals(TargetMode.VIEW)
                        || _mode.equals(TargetMode.EDIT)) {
            ret = true;
        }
        return ret;
    }

    /**
     * Is this field hidden in the given target mode. If not explicitly set
     * in the definition following defaults apply:
     * <ul>
     * <li>ModeConnect: false</li>
     * <li>ModeCreate: false</li>
     * <li>ModeView: false</li>
     * <li>ModeEdit: false</li>
     * <li>ModeSearch: false</li>
     * </ul>
     *
     * @param _mode  target mode
     * @return true if editable in the given mode, else false
     */
    public boolean isHidden(final TargetMode _mode)
    {
        boolean ret = false;
        if (this.mode2display.containsKey(_mode)) {
            ret = this.mode2display.get(_mode).equals(Field.Display.HIDDEN);
        }
        return ret;
    }

    /**
     * Is this field not displayed in the given target mode. If not explicitly set
     * in the definition following defaults apply:
     * <ul>
     * <li>ModeConnect: false</li>
     * <li>ModeCreate: false</li>
     * <li>ModeView: false</li>
     * <li>ModeEdit: false</li>
     * <li>ModeSearch: false</li>
     * </ul>
     *
     * @param _mode  target mode
     * @return true if editable in the given mode, else false
     */
    public boolean isNoDisplay(final TargetMode _mode)
    {
        boolean ret = false;
        if (this.mode2display.containsKey(_mode)) {
            ret = this.mode2display.get(_mode).equals(Field.Display.NONE);
        }
        return ret;
    }

    /**
     * Method to get the display for the given target mode. The following
     * defaults apply:
     * <ul>
     * <li>ModeConnect: READONLY</li>
     * <li>ModeCreate: NONE</li>
     * <li>ModeView: READONLY</li>
     * <li>ModeEdit: READONLY</li>
     * <li>ModeSearch: NONE</li>
     * </ul>
     *
     * @param _mode  target mode
     * @return display for the given target mode
     */
    public Display getDisplay(final TargetMode _mode)
    {
        Display ret = Field.Display.NONE;
        if (this.mode2display.containsKey(_mode)) {
            ret = this.mode2display.get(_mode);
        } else if (_mode.equals(TargetMode.CONNECT) || _mode.equals(TargetMode.VIEW)
                        || _mode.equals(TargetMode.EDIT)) {
            ret = Field.Display.READONLY;
        }
        return ret;
    }

}
