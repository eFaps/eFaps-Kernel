/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.admin.ui.field;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.common.MsgPhrase;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.ui.AbstractCollection;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.AbstractUserInterfaceObject;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Table;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the Fields of the UserInterface.
 *
 * @author The eFaps Team
 *
 */
public class Field
    extends AbstractUserInterfaceObject
{
    /**
     * Used to define the different display modes for the Userinterface.
     */
    public enum Display {
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
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Name of the Cache by ID.
     */
    private static String IDCACHE = Field.class.getName();

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
     * The filter for this field.
     */
    private final Filter filter = new Filter();

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
     */
    private IUIProvider uiProvider = null;

    /**
     * This field can be sorted in a Webtable.
     *
     * @see #isSortAble()
     */
    private boolean sortAble = true;

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
        new HashMap<>();

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
     * Stores the classification for this field.
     */
    private String classificationName;

    /**
     * MessagePhrase String.
     */
    private String msgPhrase;

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
     * This is the getter method for the instance variable {@link #showNumbering}.
     *
     * @return value of instance variable {@link #showNumbering}
     */
    public boolean isShowNumbering()
    {
        return this.showNumbering;
    }

    /**
     * This is the getter method for instance variable {@link #classUI}.
     *
     * @return value of instance variable {@link #classUI}
     */
    public IUIProvider getUIProvider()
    {
        return this.uiProvider;
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
     * @return the messagephrase for this field
     * @throws EFapsException on error
     */
    public MsgPhrase getMsgPhrase()
        throws EFapsException
    {
        final MsgPhrase ret;
        if (this.msgPhrase == null) {
            ret = null;
        } else if (this.msgPhrase
                        .matches("[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}")) {
            ret = MsgPhrase.get(UUID.fromString(this.msgPhrase));
        } else {
            ret = MsgPhrase.get(this.msgPhrase);
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #filter}.
     *
     * @return value of instance variable {@link #filter}
     */
    public Filter getFilter()
    {
        return this.filter;
    }

    /**
     * Getter method for the instance variable {@link #collectionOID}.
     *
     * @return value of instance variable {@link #collectionOID}
     * @throws CacheReloadException on error
     */
    public AbstractCollection getCollection()
        throws CacheReloadException
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
     * Getter method for instance variable {@link #classificationName}.
     *
     * @return value of instance variable {@link #classificationName}
     */
    public String getClassificationName()
    {
        return this.classificationName;
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
        Field ret = null;
        final Cache<Long, Field> cache = InfinispanCache.get().<Long, Field>getCache(Field.IDCACHE);
        if (cache.containsKey(_id)) {
            ret = cache.get(_id);
        } else {
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
            if (col != null) {
                ret = col.getFieldsMap().get(_id);
                if (ret != null) {
                    cache.put(_id, ret);
                }
            }
        }
        return ret;
    }

    /**
     * Reset the cache.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(Field.IDCACHE)) {
            InfinispanCache.get().<Long, Field>getCache(Field.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Field>getCache(Field.IDCACHE);
            InfinispanCache.get().<Long, Field>getCache(Field.IDCACHE).addListener(new CacheLogListener(Field.LOG));
        }
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
            this.icon = _toName;
        }
        super.setLinkProperty(_linkTypeUUID, _toId, _toTypeUUID, _toName);
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
        } else if ("UIProvider".equals(_name) || "ClassNameUI".equals(_name)) {
            try {
                this.uiProvider  = (IUIProvider) Class.forName(_value).newInstance();
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
        } else if ("Classification".equals(_name)) {
            this.classificationName = _value;
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
        } else if ("MsgPhrase".equals(_name)) {
            this.msgPhrase = _value;
        } else if ("SortAble".equals(_name)) {
            this.sortAble = !"false".equals(_value);
        } else if ("FilterBase".equals(_name)) {
            this.filter.evalBase(_value);
        } else if ("FilterDefault".equals(_name)) {
            this.filter.setDefaultValue(_value.trim());
        } else if ("FilterType".equals(_name)) {
            this.filter.evalType(_value);
        } else if ("FilterRequired".equals(_name)) {
            this.filter.setRequired("TRUE".equalsIgnoreCase(_value));
        } else if ("FilterAttributes".equals(_name)) {
            this.filter.setAttributes(_value);
        } else if ("HideLabel".equals(_name)) {
            this.hideLabel = "true".equals(_value);
        } else if ("HRef".equals(_name)) {
            this.reference = _value;
        } else if ("Icon".equals(_name)) {
            this.icon = _value;
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
        }
        super.setProperty(_name, _value);
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof Field) {
            ret = ((Field) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return Long.valueOf(getId()).intValue();
    }
}
