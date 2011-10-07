/*
 * Copyright 2003 - 2011 The eFaps Team
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.ui.AbstractCollection;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.AbstractUserInterfaceObject;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Table;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.RequestHandler;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the Fields of the UserInterface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Field
    extends AbstractUserInterfaceObject
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
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Field.class);

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
     * This field has got a filter. Only if this is set to true
     * {@link #filterMemoryBased}, {@link #filterRequired},
     * {@link #filterPickList} should be evaluated.
     */
    private boolean filter = false;

    /**
     * Is the filter a picklist or a FreeText filter.
     */
    private boolean filterPickList = true;

    /**
     * Is the filter memory or database based.
     */
    private boolean filterMemoryBased = true;

    /**
     * Is this filter required.
     */
    private boolean filterRequired = false;

    /**
     * Set the default value for a filter.
     */
    private String filterDefault;

    /**
     * String containing the attributes to be used for the filter. It may
     * contain up to two attributes separated by a comma. This allows to filter
     * by an attribute and display a phrase.
     */
    private String filterAttributes;

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
     * The numbering for the field is activated/deactivated.
     */
    private boolean showNumbering = false;

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
    private final Map<AbstractUserInterfaceObject.TargetMode, Field.Display> mode2display =
        new HashMap<AbstractUserInterfaceObject.TargetMode, Field.Display>();

    /**
     * Stores the select that returns the value for this field.
     */
    private String select;

    /**
     * Stores the name of the attribute that returns the value for this field.
     */
    private String attribute;

    /**
     * Stores the phrase that returns the value for this field.
     */
    private String phrase;

    /**
     * Stores the select that returns the value for the alternate OID.
     */
    private String selectAlternateOID;

    /**
     * Stores the value of the align attribute for a field.
     */
    private String align = "left";

    /**
     * UUID of the parent collection. It is used to have a lazy low cost
     * access to the collection this FIeld belongs to.
     */
    private UUID collectionUUID;

    /**
     * This is the constructor of the field class.
     *
     * @param _id id of the field instance
     * @param _uuid UUID of the field instance
     * @param _name name of the field instance
     */
    public Field(final long _id,
                 final String _uuid,
                 final String _name)
    {
        super(_id, _uuid, _name);

    }

    /**
     * Test, if the value of instance variable
     * {@link org.efaps.admin.ui.AbstractCommand.target} is
     * equal to {@link org.efaps.admin.ui.AbstractCommand.TARGET_CONTENT}.
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
     * Test, if the value of instance variable
     * {@link org.efaps.admin.ui.AbstractCommand.target} is
     * equal to {@link org.efaps.admin.ui.AbstractCommand.TARGET_POPUP}.
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
     * Test, if the value of instance variable
     * {@link org.efaps.admin.ui.AbstractCommand.target} is
     * equal to {@link org.efaps.admin.ui.AbstractCommand.TARGET_HIDDEN}.
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
     * Getter method for instance variable {@link #filter}.
     *
     * @return value of instance variable {@link #filter}
     */
    public boolean isFilter()
    {
        return this.filter;
    }

    /**
     * Getter method for instance variable {@link #filterPickList}.
     *
     * @return value of instance variable {@link #filterPickList}
     */
    public boolean isFilterPickList()
    {
        return this.filterPickList;
    }

    /**
     * Getter method for instance variable {@link #filterMemoryBased}.
     *
     * @return value of instance variable {@link #filterMemoryBased}
     */
    public boolean isFilterMemoryBased()
    {
        return this.filterMemoryBased;
    }

    /**
     * Getter method for instance variable {@link #filterRequired}.
     *
     * @return value of instance variable {@link #filterRequired}
     */
    public boolean isFilterRequired()
    {
        return this.filterRequired;
    }

    /**
     * Getter method for instance variable {@link #filterDefault}.
     *
     * @return value of instance variable {@link #filterDefault}
     */
    public String getFilterDefault()
    {
        return this.filterDefault;
    }

    /**
     * Getter method for instance variable {@link #filterAttributes}.
     *
     * @return value of instance variable {@link #filterAttributes}
     */
    public String getFilterAttributes()
    {
        return this.filterAttributes;
    }

    /**
     * Getter method for the instance variable {@link #align}.
     *
     * @return value of instance variable {@link #align}
     */
    public String getAlign()
    {
        return this.align;
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
     * This is the getter method for instance variable {@link #rowSpan}.
     *
     * @return the value of the instance variable {@link #rowSpan}.
     */
    public int getRowSpan()
    {
        return this.rowSpan;
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
     * This is the getter method for the instance variable {@link #showNumbering}
     *
     * @return value of instance variable {@link #showNumbering}
     */
    public boolean isShowNumbering()
    {
        return this.showNumbering;
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
    private void setWidth(final String _value)
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
     * Getter method for instance variable {@link #select}.
     *
     * @return value of instance variable {@link #select}
     */
    public String getSelect()
    {
        return this.select;
    }

    /**
     * Getter method for instance variable {@link #attribute}.
     *
     * @return value of instance variable {@link #attribute}
     */
    public String getAttribute()
    {
        return this.attribute;
    }

    /**
     * Getter method for instance variable {@link #phrase}.
     *
     * @return value of instance variable {@link #phrase}
     */
    public String getPhrase()
    {
        return this.phrase;
    }

    /**
     * Getter method for the instance variable {@link #collectionOID}.
     *
     * @return value of instance variable {@link #collectionOID}
     */
    public AbstractCollection getCollection()
    {
        AbstractCollection ret = Form.get(this.collectionUUID);
        if (ret == null) {
            ret = Table.get(this.collectionUUID);
        }
        return ret;
    }

    /**
     * Setter method for instance variable {@link #collectionOID}.
     *
     * @param _collectionOID value for instance variable {@link #collectionOID}
     */

    public void setCollectionUUID(final UUID _collectionOID)
    {
        this.collectionUUID = _collectionOID;
    }


    /**
     * Getter method for instance variable {@link #selectAlternateOID}.
     *
     * @return value of instance variable {@link #selectAlternateOID}
     */
    public String getSelectAlternateOID()
    {
        return this.selectAlternateOID;
    }

    /**
     * Is this field editable in the given target mode. If not explicitly set in
     * the definition following defaults apply:
     * <ul>
     * <li>ModeConnect: false</li>
     * <li>ModeCreate: false</li>
     * <li>ModeView: false</li>
     * <li>ModePrint: false</li>
     * <li>ModeEdit: false</li>
     * <li>ModeSearch: false</li>
     * </ul>
     *
     * @param _mode target mode
     * @return true if editable in the given mode, else false
     */
    public boolean isEditableDisplay(final TargetMode _mode)
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
     * <li>ModePrint: true</li>
     * <li>ModeEdit: true</li>
     * <li>ModeSearch: false</li>
     * </ul>
     *
     * @param _mode target mode
     * @return true if editable in the given mode, else false
     */
    public boolean isReadonlyDisplay(final TargetMode _mode)
    {
        boolean ret = false;
        if (this.mode2display.containsKey(_mode)) {
            ret = this.mode2display.get(_mode).equals(Field.Display.READONLY);
        } else if (_mode.equals(TargetMode.CONNECT) || _mode.equals(TargetMode.VIEW)
                        || _mode.equals(TargetMode.EDIT) || _mode.equals(TargetMode.PRINT)) {
            ret = true;
        }
        return ret;
    }

    /**
     * Is this field hidden in the given target mode. If not explicitly set in
     * the definition following defaults apply:
     * <ul>
     * <li>ModeConnect: false</li>
     * <li>ModeCreate: false</li>
     * <li>ModeView: false</li>
     * <li>ModeEdit: false</li>
     * <li>ModeSearch: false</li>
     * <li>ModePrint: false</li>
     * </ul>
     *
     * @param _mode target mode
     * @return true if editable in the given mode, else false
     */
    public boolean isHiddenDisplay(final TargetMode _mode)
    {
        boolean ret = false;
        if (this.mode2display.containsKey(_mode)) {
            ret = this.mode2display.get(_mode).equals(Field.Display.HIDDEN);
        }
        return ret;
    }

    /**
     * Is this field not displayed in the given target mode. If not explicitly
     * set in the definition following defaults apply:
     * <ul>
     * <li>ModeConnect: false</li>
     * <li>ModeCreate: true</li>
     * <li>ModeView: false</li>
     * <li>ModeEdit: false</li>
     * <li>ModeSearch: true</li>
     * <li>ModePrint: false</li>
     * </ul>
     *
     * @param _mode target mode
     * @return true if editable in the given mode, else false
     */
    public boolean isNoneDisplay(final TargetMode _mode)
    {
        boolean ret = false;
        if (this.mode2display.containsKey(_mode)) {
            ret = this.mode2display.get(_mode).equals(Field.Display.NONE);
        } else if (_mode.equals(TargetMode.CREATE) || _mode.equals(TargetMode.SEARCH)) {
            ret = true;
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
     * @param _mode target mode
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
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.Field);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.Field.ID, _id);
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.Field.Collection);
            multi.executeWithoutAccessCheck();

            if (multi.next()) {
                final Long colId = multi.<Long> getAttribute(CIAdminUserInterface.Field.Collection);
                col = Form.get(colId);
                if (col == null) {
                    col = Table.get(colId);
                }
            }
        } catch (final EFapsException e) {
            Field.LOG.error("get(long)", e);
        }
        return col.getFieldsMap().get(_id);
    }

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
    protected void setLinkProperty(final Type _linkType,
                                   final long _toId,
                                   final Type _toType,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkType.isKindOf(CIAdminUserInterface.LinkIcon.getType())) {
            this.icon = RequestHandler.replaceMacrosInUrl(RequestHandler.URL_IMAGE + _toName);
        } else {
            super.setLinkProperty(_linkType, _toId, _toType, _toName);
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
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        if ("Align".equals(_name)) {
            this.align = _value;
        } else if ("ClassNameUI".equals(_name)) {
            try {
                this.classUI = (UIInterface) Class.forName(_value).newInstance();
            } catch (final ClassNotFoundException e) {
                throw new CacheReloadException("could not found class '" + _value + "' for '" + getName() + "'", e);
            } catch (final InstantiationException e) {
                throw new CacheReloadException("could not instantiate class '" + _value + "' for '" + getName() + "'",
                                e);
            } catch (final IllegalAccessException e) {
                throw new CacheReloadException("could not access class '" + _value + "' for '" + getName() + "'", e);
            }
        } else if ("Columns".equals(_name)) {
            this.cols = Integer.parseInt(_value);
        } else if ("CreateValue".equals(_name)) {
            this.createValue = _value;
        } else if ("SelectAlternateOID".equals(_name)) {
            this.selectAlternateOID = _value;
        } else if ("Select".equals(_name)) {
            this.select = _value;
        } else if ("Attribute".equals(_name)) {
            this.attribute = _value;
        } else if ("Phrase".equals(_name)) {
            this.phrase = _value;
        } else if ("Width".equals(_name)) {
            setWidth(_value);
        } else if ("SortAble".equals(_name)) {
            this.sortAble = !"false".equals(_value);
        } else if ("FilterBase".equals(_name)) {
            this.filterMemoryBased = !"DATABASE".equalsIgnoreCase(_value);
        } else if ("FilterDefault".equals(_name)) {
            this.filterDefault = _value.trim();
        } else if ("FilterType".equals(_name)) {
            this.filter = true;
            this.filterPickList = !"FREETEXT".equalsIgnoreCase(_value);
        } else if ("FilterRequired".equals(_name)) {
            this.filterRequired = "TRUE".equalsIgnoreCase(_value);
        } else if ("FilterAttributes".equals(_name)) {
            this.filterAttributes = _value;
        } else if ("HideLabel".equals(_name)) {
            this.hideLabel = "true".equals(_value);
        } else if ("HRef".equals(_name)) {
            this.reference = RequestHandler.replaceMacrosInUrl(_value);
        } else if ("Icon".equals(_name)) {
            this.icon = RequestHandler.replaceMacrosInUrl(_value);
        } else if ("Label".equals(_name)) {
            this.label = _value;
        } else if ("ModeConnect".equals(_name)) {
            this.mode2display.put(TargetMode.CONNECT, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ModeCreate".equals(_name)) {
            this.mode2display.put(TargetMode.CREATE, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ModeEdit".equals(_name)) {
            this.mode2display.put(TargetMode.EDIT, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ModePrint".equals(_name)) {
            this.mode2display.put(TargetMode.PRINT, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ModeSearch".equals(_name)) {
            this.mode2display.put(TargetMode.SEARCH, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("ModeView".equals(_name)) {
            this.mode2display.put(TargetMode.VIEW, Field.Display.valueOf(_value.toUpperCase()));
        } else if ("Required".equals(_name)) {
            this.required = "true".equalsIgnoreCase(_value);
        } else if ("Rows".equals(_name)) {
            this.rows = Integer.parseInt(_value);
        } else if ("RowSpan".equals(_name)) {
            this.rowSpan = Integer.parseInt(_value);
        } else if ("ShowTypeIcon".equals(_name)) {
            this.showTypeIcon = "true".equalsIgnoreCase(_value);
        } else if ("ShowNumbering".equals(_name)) {
                this.showNumbering = "true".equalsIgnoreCase(_value);
        } else if ("Target".equals(_name)) {
            if ("content".equals(_value)) {
                this.target = Target.CONTENT;
            } else if ("hidden".equals(_value)) {
                this.target = Target.HIDDEN;
            } else if ("popup".equals(_value)) {
                this.target = Target.POPUP;
            }
        } else {
            super.setProperty(_name, _value);
        }
    }
}
