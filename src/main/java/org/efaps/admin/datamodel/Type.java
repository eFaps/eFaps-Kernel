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

package org.efaps.admin.datamodel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.attributetype.CompanyLinkType;
import org.efaps.admin.datamodel.attributetype.ConsortiumLinkType;
import org.efaps.admin.datamodel.attributetype.StatusType;
import org.efaps.admin.datamodel.attributetype.TypeType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.Menu;
import org.efaps.ci.CIAdminAccess;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the class for the type description. The type description holds
 * information about creation of a new instance of a type with default values.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Type
    extends AbstractDataModelObject
{

    /**
     * Enum for the different purpose of a type.
     */
    public enum Purpose {
        /** Abstract purpose. */
        ABSTRACT(1, 0),
        /** classification purpose. */
        CLASSIFICATION(2, 1),
        /** GeneralInstane. */
        GENERALINSTANCE(4, 2),
        /** No GeneralInstane. */
        NOGENERALINSTANCE(8, 3);
        /** id of this purpose. */
        private final int id;
        /** digit of this purpose. */
        private final int digit;

        /**
         * Constructor setting the id.
         *
         * @param _id id of this purpose
         * @param _digit digit of this purpose
         */
        private Purpose(final int _id,
                        final int _digit)
        {
            this.id = _id;
            this.digit = _digit;
        }

        /**
         * Getter method for the instance variable {@link #digit}.
         *
         * @return value of instance variable {@link #digit}
         */
        public int getDigit()
        {
            return this.digit;
        }

        /**
         * Getter method for instance variable {@link #id}.
         *
         * @return id of this purpose
         */
        public Integer getId()
        {
            return this.id;
        }
    }

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Type.class);

    /**
     * SQL select statement to select a type from the database by its UUID.
     */
    private static final String SQL_UUID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("PURPOSE")
                    .column("PARENTDMTYPE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select a type from the database by its ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("PURPOSE")
                    .column("PARENTDMTYPE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * SQL select statement to select a type from the database by its Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("PURPOSE")
                    .column("PARENTDMTYPE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select the ids of child types from the database.
     */
    private static final String SQL_CHILD = new SQLSelect()
                    .column("ID")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "PARENTDMTYPE").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static String UUIDCACHE = "Type4UUID";

    /**
     * Name of the Cache by ID.
     */
    private static String IDCACHE = "Type4ID";

    /**
     * Name of the Cache by Name.
     */
    private static String NAMECACHE = "Type4Name";


    /**
     * Instance variable for the parent type from which this type is derived.
     *
     * @see #getParentType
     * @see #setParentType
     */
    private Type parentType = null;

    /**
     * Instance variable for all child types derived from this type.
     *
     * @see #getChildTypes
     */
    private final Set<Type> childTypes = new HashSet<Type>();

    /**
     * Classifications which are classifying this type.
     */
    private final Set<Classification> classifiedByTypes = new HashSet<Classification>();

    /**
     * The instance variables stores all attributes for this type object.
     *
     * @see #getAttributes()
     * @see #add(Attribute)
     * @see #getAttribute
     * @see #getAttributes(Class)
     */
    private final Map<String, Attribute> attributes = new HashMap<String, Attribute>();

    /**
     * Instance of a HashSet to store all needed tables for this type. The
     * tables are automatically added via the method {@link #add(Attribute)}.
     *
     * @see #add(Attribute)
     * @see #getTables
     */
    private final Set<SQLTable> tables = new HashSet<SQLTable>();

    /**
     * The instance variable stores the main table, which must be inserted
     * first. In the main table stands also the select statement to get a new
     * id. The value is automatically set with method {@link #add(Attribute)}.
     *
     * @see Table.mainTable
     * @see #add(Attribute)
     * @see #getMainTable
     * @see #setMainTable
     */
    private SQLTable mainTable = null;

    /**
     * All attributes which are used as links are stored in this map.
     *
     * @see #getLinks
     */
    private final Map<String, Attribute> links = new HashMap<String, Attribute>();

    /**
     * All access sets which are assigned to this type are store in this
     * instance variable. If <code>null</code> the variable was not evaluated yet;
     *
     * @see #addAccessSet
     * @see #getAccessSets
     */
    private final Set<AccessSet> accessSets = new HashSet<AccessSet>();

    /**
     * Have the accessSet been evaluated.
     */
    private boolean checked4AccessSet = false;

    /**
     * Have the children been evaluated.
     */
    private boolean checked4Children = false;


    /**
     * Stores all type of events which are allowed to fire on this type.
     *
     * @see #setLinkProperty
     */
    private final Set<Type> allowedEventTypes = new HashSet<Type>();

    /**
     * Id of the store for this type.
     */
    private long storeId;

    /**
     * Is the type abstract.
     */
    private boolean abstractBool;

    /**
     * Are the instance of this type general also. Used as a TRISTATE
     * <ol>
     * <li>null = Inherit the value from the parent.</li>
     * <li>true = The instance of this type are general too</li>
     * <li>false = The instance are not general</li>
     * </ol>
     */
    private Boolean generalInstance;

    /**
     * Stores the name of attribute that contains the status of this type. (if
     * exist)
     */
    private String statusAttributeName;

    /**
     * Stores the name of attribute that contains the company of this type. (if
     * exist)
     */
    private String companyAttributeName;

    /**
     * Stores the name of attribute that contains the type of this type. (if
     * exist)
     */
    private String typeAttributeName;

    /**
     * Id of the Menu defined as TypeMenu for this Type.<br/>
     * TRISTATE:<br/>
     * <ul>
     * <li>NULL: TypeMenu not evaluated yet</li>
     * <li>0: has got no TypeMenu</li>
     * <li>n: ID of the TypeMenu</li>
     * </ul>
     */
    private Long typeMenu;

    /**
     * Id of the Icon defined as TypeIcon for this Type.<br/>
     * TRISTATE:<br/>
     * <ul>
     * <li>NULL: TypeIcon not evaluated yet</li>
     * <li>0: has got no TypeMenu</li>
     * <li>n: ID of the TypeMenu</li>
     * </ul>
     */
    private Long typeIcon;


    /**
     * Id of the From defined as TypeFrom for this Type.<br/>
     * TRISTATE:<br/>
     * <ul>
     * <li>NULL: TypeFrom not evaluated yet</li>
     * <li>0: has got no TypeMenu</li>
     * <li>n: ID of the TypeMenu</li>
     * </ul>
     */
    private Long typeForm;

    /**
     * This is the constructor for class Type. Every instance of class Type must
     * have a name (parameter <i>_name</i>).
     *
     * @param _id id of th type
     * @param _uuid universal unique identifier
     * @param _name name of the type name of the instance
     * @throws CacheReloadException on error
     */
    protected Type(final long _id,
                   final String _uuid,
                   final String _name)
        throws CacheReloadException
    {
        super(_id, _uuid, _name);
    }

    /**
     * Getter method for instance variable {@link #abstractBool}.
     *
     * @return value of instance variable {@link #abstractBool}
     */
    public boolean isAbstract()
    {
        return this.abstractBool;
    }

    /**
     * Setter method for instance variable {@link #abstractBool}.
     *
     * @param _abstract value for instance variable {@link #abstractBool}
     */
    private void setAbstract(final boolean _abstract)
    {
        this.abstractBool = _abstract;
    }

    /**
     * Getter method for the instance variable {@link #generalInstance}.
     *
     * @return value of instance variable {@link #generalInstance}
     */
    public boolean isGeneralInstance()
    {
        boolean ret = true;
        if (this.generalInstance != null) {
            ret = this.generalInstance;
        } else if (getParentType() != null) {
            ret = getParentType().isGeneralInstance();
        }
        return ret;
    }

    /**
     * Setter method for instance variable {@link #generalInstance}.
     *
     * @param _generalInstance value for instance variable {@link #generalInstance}
     */

    private void setGeneralInstance(final boolean _generalInstance)
    {
        this.generalInstance = _generalInstance;
    }

    /**
     * Add an attribute to this type and all child types of this type.
     *
     * @param _attribute attribute to add
     * @param _inherited is the attribute inherited or form this type
     */
    protected void addAttribute(final Attribute _attribute,
                                final boolean _inherited)
    {
        if (!getAttributes().containsKey(_attribute.getName())) {
            Type.LOG.trace("adding Attribute:'{}' to type: '{}'", _attribute.getName(), getName());
            _attribute.setParent(this);
            // evaluate for status, an inherited attribute will not overwrite the
            // original attribute
            if (_attribute.getAttributeType().getClassRepr().equals(StatusType.class) && !_inherited) {
                this.statusAttributeName = _attribute.getName();
            }
            // evaluate for company
            if (_attribute.getAttributeType().getClassRepr().equals(CompanyLinkType.class)
                            || _attribute.getAttributeType().getClassRepr().equals(ConsortiumLinkType.class)) {
                this.companyAttributeName = _attribute.getName();
            }
            // evaluate for type attribute
            if (_attribute.getAttributeType().getClassRepr().equals(TypeType.class)) {
                this.typeAttributeName = _attribute.getName();
            }

            getAttributes().put(_attribute.getName(), _attribute);
            if (_attribute.getTable() != null) {
                getTables().add(_attribute.getTable());
                _attribute.getTable().add(this);
                if (getMainTable() == null) {
                    setMainTable(_attribute.getTable());
                }
            }
        }
        for (final Type child : getChildTypes()) {
            child.addAttribute(_attribute.copy(), true);
        }
    }

    /**
     * Adds link from an attribute to this type. The link is also registered
     * under the name of all child types of the attribute.
     *
     * @param _attr attribute with the link to this type
     */
    protected void addLink(final Attribute _attr)
    {
        getLinks().put(_attr.getParent().getName() + "\\" + _attr.getName(), _attr);
        for (final Type type : _attr.getParent().getChildTypes()) {
            getLinks().put(type.getName() + "\\" + _attr.getName(), _attr);
        }
        for (final Type child : getChildTypes()) {
            if (child.getParentType().getId() == getId()) {
                child.addLink(_attr);
            }
        }
    }

    /**
     * Getter method for instance variable {@link #statusAttribute}.
     *
     * @return value of instance variable {@link #statusAttribute}
     */
    public Attribute getStatusAttribute()
    {
        return this.attributes.get(this.statusAttributeName);
    }

    /**
     * Method to evaluate if the status must be checked on an accesscheck.
     *
     * @return true if {@link #statusAttribute} !=null , else false
     */
    public boolean isCheckStatus()
    {
        return this.statusAttributeName != null;
    }

    /**
     * Method to evaluate if this type depends on companies.
     *
     * @return true if {@link #companyAttribute} !=null , else false
     */
    public boolean isCompanyDepended()
    {
        return this.companyAttributeName != null;
    }

    /**
     * Get the attribute containing the company information.
     *
     * @return attribute containing the company information
     */
    public Attribute getCompanyAttribute()
    {
        return this.attributes.get(this.companyAttributeName);
    }

    /**
     * Get the attribute containing the type information.
     *
     * @return attribute containing the type information
     */
    public Attribute getTypeAttribute()
    {
        Attribute ret;
        if (this.typeAttributeName == null && this.parentType != null) {
            ret = this.parentType.getTypeAttribute();
        } else {
            ret = this.attributes.get(this.typeAttributeName);
        }
        return ret;
    }

    /**
     * Returns for the given parameter <b>_name</b> the attribute.
     *
     * @param _name name of the attribute for this type to return
     * @return instance of class {@link Attribute}
     */
    public final Attribute getAttribute(final String _name)
    {
        return getAttributes().get(_name);
    }

    /**
     * The instance method returns all attributes which are from the same
     * attribute type as the described with the parameter <i>_class</i>.
     *
     * @param _class searched attribute type
     * @return all attributes assigned from parameter <i>_class</i>
     */
    public final Set<Attribute> getAttributes(final Class<?> _class)
    {
        final Set<Attribute> ret = new HashSet<Attribute>();
        for (final Attribute attr : getAttributes().values()) {
            if (attr.getAttributeType().getClassRepr().isAssignableFrom(_class)) {
                ret.add(attr);
            }
        }
        return ret;
    }

    /**
     * Tests, if this type is kind of the type in the parameter (question is, is
     * this type a child of the parameter type).
     *
     * @param _type type to test for parent
     * @return true if this type is a child, otherwise false
     */
    public boolean isKindOf(final Type _type)
    {
        boolean ret = false;
        Type type = this;
        while ((type != null) && (type.getId() != _type.getId())) {
            type = type.getParentType();
        }
        if ((type != null) && (type.getId() == _type.getId())) {
            ret = true;
        }
        return ret;
    }

    /**
     * Checks if the current type holds the property with the given name. If
     * not, the value of the property of the parent type (see
     * {@link #getParentType}) is returned (if a parent type exists).
     *
     * @param _name name of the property (key)
     * @return value of the property with the given name / key.
     * @see org.efaps.admin.AbstractAdminObject#getProperty
     */
    @Override
    public String getProperty(final String _name)
    {
        String value = super.getProperty(_name);
        if ((value == null) && (getParentType() != null)) {
            value = getParentType().getProperty(_name);
        }
        return value;
    }


    /**
     * Checks, if the current context user has all access defined in the list of
     * access types for the given instance.
     *
     * @param _instance instance for which the access must be checked
     * @param _accessType list of access types which must be checked
     * @throws EFapsException on error
     * @return true if user has access, else false
     */
    public boolean hasAccess(final Instance _instance,
                             final AccessType _accessType)
        throws EFapsException
    {
        return hasAccess(_instance, _accessType, null);
    }


    /**
     * Checks, if the current context user has all access defined in the list of
     * access types for the given instance.
     *
     * @param _instance instance for which the access must be checked
     * @param _accessType list of access types which must be checked
     * @param _newValues objects that will be passed to esjp as <code>NEW_VALUES</code>
     * @throws EFapsException on error
     * @return true if user has access, else false
     */
    public boolean hasAccess(final Instance _instance,
                             final AccessType _accessType,
                             final Object _newValues)
        throws EFapsException
    {
        boolean hasAccess = true;
        final List<EventDefinition> events = super.getEvents(EventType.ACCESSCHECK);
        if (events != null) {
            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.INSTANCE, _instance);
            parameter.put(ParameterValues.ACCESSTYPE, _accessType);
            parameter.put(ParameterValues.NEW_VALUES, _newValues);

            for (final EventDefinition event : events) {
                final Return ret = event.execute(parameter);
                hasAccess = ret.get(ReturnValues.TRUE) != null;
            }
        }
        return hasAccess;
    }

    /**
     * Method to check the access right for a list of instances.
     *
     * @param _instances list of instances
     * @param _accessType access type
     * @throws EFapsException on error
     * @return Map of instances to boolean
     */
    @SuppressWarnings("unchecked")
    public Map<Instance, Boolean> checkAccess(final List<Instance> _instances,
                                              final AccessType _accessType)
        throws EFapsException
    {
        final List<EventDefinition> events = super.getEvents(EventType.ACCESSCHECK);
        Map<Instance, Boolean> ret = new HashMap<Instance, Boolean>();
        if (events != null) {
            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.OTHERS, _instances);
            parameter.put(ParameterValues.ACCESSTYPE, _accessType);

            for (final EventDefinition event : events) {
                final Return retrn = event.execute(parameter);
                ret = (Map<Instance, Boolean>) retrn.get(ReturnValues.VALUES);
            }
        } else {
            for (final Instance instance : _instances) {
                ret.put(instance, true);
            }
        }
        return ret;
    }

    /**
     * @param _accessSet AccessSet to add to this Type
     */
    public void addAccessSet(final AccessSet _accessSet)
    {
        this.accessSets.add(_accessSet);
    }

    /**
     * This is the getter method for instance variable {@link #accessSets}.
     *
     * @return value of instance variable {@link #accessSets}
     * @see #accessSets
     * @throws EFapsException on error
     */
    public Set<AccessSet> getAccessSets()
        throws EFapsException
    {
        if (!this.checked4AccessSet) {
            this.checked4AccessSet = true;
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.AccessSet2DataModelType);
            queryBldr.addWhereAttrEqValue(CIAdminAccess.AccessSet2DataModelType.DataModelTypeLink, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminAccess.AccessSet2DataModelType.AccessSetLink);
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                final Long accessSet = multi.<Long>getAttribute(CIAdminAccess.AccessSet2DataModelType.AccessSetLink);
                AccessSet.get(accessSet);
            }
        }
        return this.accessSets;
    }

    /**
     *
     * Sets the link properties for this object.
     *
     * @param _linkType type of the link property
     * @param _toId to id
     * @param _toType to type
     * @param _toName to name
     * @throws EFapsException o error
     *
     */
    @Override
    protected void setLinkProperty(final Type _linkType,
                                   final long _toId,
                                   final Type _toType,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkType.isKindOf(CIAdminDataModel.Type2Store.getType())) {
            this.storeId = _toId;
        } else if (_linkType.isKindOf(CIAdminDataModel.TypeEventIsAllowedFor.getType())) {
            final Type eventType = Type.get(_toId);
            this.allowedEventTypes.add(eventType);
        } else {
            super.setLinkProperty(_linkType, _toId, _toType, _toName);
        }
    }

    /**
     * Add a new child type for this type. All sub child types of the child type
     * are also defined as child type of this type.<br/>
     * Also for all parent types (of this type), the child type (with sub child
     * types) are added.
     *
     * @param _childType child type to add
     * @see #childTypes
     */
    protected void addChildType(final Type _childType)
    {
        for (final Attribute attribute : this.attributes.values()) {
            _childType.addAttribute(attribute.copy(), true);
        }
        Type parent = this;
        while (parent != null) {
            parent.getChildTypes().add(_childType);
            parent.getChildTypes().addAll(_childType.getChildTypes());
            parent = parent.getParentType();
        }
    }

    /**
     * For the given type it is tested if a store is defined for the type.
     *
     * @return <i>true</i> if a store resource is defined for the type,
     *         otherwise <i>false</i> is returned
     */
    public boolean hasStore()
    {
        return getStoreId() > 0 ? true : false;
    }

    /**
     * This is the getter method for instance variable {@link #parentType}.
     *
     * @return value of instance variable {@link #parentType}
     * @see #parentType
     * @see #setParentType
     */
    public Type getParentType()
    {
        return this.parentType;
    }

    /**
     * Setter method for instance variable {@link #parentType}.
     *
     * @param _parentType parent to set
     */
    protected void setParentType(final Type _parentType)
    {
        this.parentType = _parentType;
    }

    /**
     * Add a root Classification to this type.
     *
     * @param _classification classifixation that classifies this type
     */
    private void addClassifiedByType(final Classification _classification)
    {
        this.classifiedByTypes.add(_classification);
    }

    /**
     * Getter method for instance variable {@link #classifiedByTypes}.
     *
     * @return value of instance variable {@link #classifiedByTypes}
     */
    public Set<Classification> getClassifiedByTypes()
    {
        return this.classifiedByTypes;
    }

    /**
     * This is the getter method for instance variable {@link #childTypes}.
     *
     * @return value of instance variable {@link #childTypes}
     * @see #childTypes
     */
    public Set<Type> getChildTypes()
    {
        return this.childTypes;
    }

    /**
     * This is the getter method for instance variable {@link #attributes}.
     *
     * @return value of instance variable {@link #attributes}
     * @see #attributes
     */
    public Map<String, Attribute> getAttributes()
    {
        return this.attributes;
    }

    /**
     * This is the getter method for instance variable {@link #tables}.
     *
     * @return value of instance variable {@link #tables}
     * @see #tables
     */
    public Set<SQLTable> getTables()
    {
        return this.tables;
    }

    /**
     * This is the getter method for instance variable {@link #mainTable}.
     *
     * @return value of instance variable {@link #mainTable}
     * @see #setMainTable
     * @see #mainTable
     */
    public SQLTable getMainTable()
    {
        SQLTable ret = this.mainTable;
        if (this.mainTable == null && this.parentType != null) {
            ret = this.parentType.getMainTable();
        }
        return ret;
    }

    /**
     * This is the setter method for instance variable {@link #mainTable}.
     *
     * @param _mainTable new value for instance variable {@link #mainTable}
     * @see #getMainTable
     * @see #mainTable
     */
    private void setMainTable(final SQLTable _mainTable)
    {
        SQLTable table = _mainTable;
        while (table.getMainTable() != null) {
            table = table.getMainTable();
        }
        this.mainTable = table;
    }

    /**
     * This is the getter method for instance variable {@link #links}.
     *
     * @return value of instance variable {@link #links}
     * @see #links
     */
    public Map<String, Attribute> getLinks()
    {
        return this.links;
    }

    /**
     * This is the getter method for instance variable
     * {@link #allowedEventTypes}.
     *
     * @return value of instance variable {@link #allowedEventTypes}
     * @see #allowedEventTypes
     */
    public Set<Type> getAllowedEventTypes()
    {
        return this.allowedEventTypes;
    }

    /**
     * Getter method for instance variable {@link #storeId}.
     *
     * @return value of instance variable {@link #storeId}
     */
    public long getStoreId()
    {
        final long ret;
        if (this.storeId == 0 && this.parentType != null) {
            ret = this.parentType.getStoreId();
        } else {
            ret = this.storeId;
        }
        return ret;
    }

    /**
     * Method to get the key to the label.
     *
     * @return key to the label
     */
    public String getLabelKey()
    {
        final StringBuilder keyStr = new StringBuilder();
        return keyStr.append(getName()).append(".Label").toString();
    }

    /**
     * Method to get the translated label for this Status.
     *
     * @return translated Label
     */
    public String getLabel()
    {
        return DBProperties.getProperty(getLabelKey());
    }

    /**
     * @return the TypeMenu for this type
     * @throws EFapsException on errot
     */
    public Menu getTypeMenu()
        throws EFapsException
    {
        Menu ret = null;
        if (this.typeMenu == null) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.LinkIsTypeTreeFor);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.LinkIsTypeTreeFor.To, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.LinkIsTypeTreeFor.From);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Long menuId = multi.<Long>getAttribute(CIAdminUserInterface.LinkIsTypeTreeFor.From);
                ret = Menu.get(menuId);
                if (ret != null) {
                    this.typeMenu = ret.getId();
                } else {
                    this.typeMenu = Long.valueOf(0);
                }
            }
        } else if (this.typeMenu == 0) {
            ret = getParentType().getTypeMenu();
        } else {
            ret = Menu.get(this.typeMenu);
        }
        return ret;
    }

    /**
     * @return the TypeIcon for this type
     * @throws EFapsException on errot
     */

    public Image getTypeIcon()
        throws EFapsException
    {
        Image ret = null;
        if (this.typeIcon == null) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.LinkIsTypeIconFor);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.LinkIsTypeIconFor.To, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.LinkIsTypeIconFor.From);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Long menuId = multi.<Long>getAttribute(CIAdminUserInterface.LinkIsTypeTreeFor.From);
                ret = Image.get(menuId);
                if (ret != null) {
                    this.typeIcon = ret.getId();
                } else {
                    this.typeIcon = Long.valueOf(0);
                }
            }
        } else if (this.typeIcon == 0) {
            ret = getParentType().getTypeIcon();
        } else {
            ret = Image.get(this.typeIcon);
        }
        return ret;
    }

    /**
     * @return the TypeFrom for this type
     * @throws EFapsException on errot
     */
    public Form getTypeForm()
        throws EFapsException
    {
        Form ret = null;
        if (this.typeForm == null) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.LinkIsTypeFormFor);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.LinkIsTypeFormFor.To, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.LinkIsTypeFormFor.From);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Long menuId = multi.<Long>getAttribute(CIAdminUserInterface.LinkIsTypeFormFor.From);
                ret = Form.get(menuId);
                if (ret != null) {
                    this.typeForm = ret.getId();
                } else {
                    this.typeForm = Long.valueOf(0);
                }
            }
        } else if (this.typeForm == 0) {
            ret = getParentType().getTypeForm();
        } else {
            ret = Form.get(this.typeForm);
        }
        return ret;
    }

    /**
     * The method overrides the original method 'toString' and returns
     * information about this type instance.
     *
     * @return name of the user interface object
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).appendSuper(super.toString())
                        .append("parentType", getParentType() != null ? getParentType().getName() : "")
                        .append("has attributes:", !this.attributes.isEmpty())
                        .toString();
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof Type) {
            ret = ((Type) _obj).getId() == getId();
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

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @param _class class that called the method
     * @throws CacheReloadException on error
     */
    public static void initialize(final Class<?> _class)
        throws CacheReloadException
    {
        if (InfinispanCache.get().exists(Type.UUIDCACHE)) {
            InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE).addListener(new CacheLogListener(Type.LOG));
        }
        if (InfinispanCache.get().exists(Type.IDCACHE)) {
            InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE).addListener(new CacheLogListener(Type.LOG));
        }
        if (InfinispanCache.get().exists(Type.NAMECACHE)) {
            InfinispanCache.get().<String, Type>getCache(Type.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Type>getCache(Type.NAMECACHE).addListener(new CacheLogListener(Type.LOG));
        }
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        Type.initialize(Type.class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class {@link Type}
     * .
     *
     * @param _id id of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static Type get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Type> cache = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        if (!cache.containsKey(_id)) {
            Type.getTypeFromDB(Type.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Type}.
     *
     * @param _name name of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static Type get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, Type> cache = InfinispanCache.get().<String, Type>getCache(Type.NAMECACHE);
        if (!cache.containsKey(_name)) {
            Type.getTypeFromDB(Type.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Type}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static Type get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, Type> cache = InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            Type.getTypeFromDB(Type.SQL_UUID, _uuid.toString());
        }
        return cache.get(_uuid);
    }

    /**
     * @param _type type to be cached
     */
    protected static void cacheType(final Type _type)
    {
        final Cache<UUID, Type> cache4UUID = InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE);
        cache4UUID.putIfAbsent(_type.getUUID(), _type);

        final Cache<String, Type> nameCache = InfinispanCache.get().<String, Type>getCache(Type.NAMECACHE);
        nameCache.putIfAbsent(_type.getName(), _type);

        final Cache<Long, Type> idCache = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        idCache.putIfAbsent(_type.getId(), _type);
    }

    /**
     * @param _parentID id to be searched for
     * @return a list of ids
     * @throws CacheReloadException on error
     */
    private static List<Long> getChildTypeIDs(final long _parentID)
        throws CacheReloadException
    {
        final List<Long> ret = new ArrayList<Long>();
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            PreparedStatement stmt = null;
            try {
                stmt = con.getConnection().prepareStatement(Type.SQL_CHILD);
                stmt.setObject(1, _parentID);
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ret.add(rs.getLong(1));
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read child type ids", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read child type ids", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                try {
                    con.abort();
                } catch (final EFapsException e) {
                    throw new CacheReloadException("could not read child type ids", e);
                }
            }
        }
        return ret;
    }

    /**
     * @param _sql SQLStatement to be executed
     * @param _criteria the filter criteria
     * @return Type instance
     * @throws CacheReloadException on error
     */
    private static Type getTypeFromDB(final String _sql,
                                      final Object _criteria)
        throws CacheReloadException
    {
        Type ret = null;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            final PreparedStatement stmt = con.getConnection().prepareStatement(_sql);
            stmt.setObject(1, _criteria);
            final ResultSet rs = stmt.executeQuery();
            long parentTypeId = 0;
            long id = 0;
            if (rs.next()) {
                id = rs.getLong(1);
                final String uuid = rs.getString(2).trim();
                final String name = rs.getString(3).trim();
                final int purpose = rs.getInt(4);
                parentTypeId = rs.getLong(5);

                Type.LOG.debug("read type '{}' (id = {}) (purpose = {})", name, id, purpose);

                final char[] purpose2 = ("00000000" + Integer.toBinaryString(purpose)).toCharArray();
                ArrayUtils.reverse(purpose2);
                final char trueCriteria = "1".toCharArray()[0];
                if (trueCriteria == purpose2[Type.Purpose.CLASSIFICATION.getDigit()]) {
                    ret = new Classification(id, uuid, name);
                } else {
                    ret = new Type(id, uuid, name);
                }
                ret.setAbstract(trueCriteria == purpose2[Type.Purpose.ABSTRACT.getDigit()]);

                if (trueCriteria == purpose2[Type.Purpose.GENERALINSTANCE.getDigit()]) {
                    ret.setGeneralInstance(true);
                }
                if (trueCriteria == purpose2[Type.Purpose.NOGENERALINSTANCE.getDigit()]) {
                    ret.setGeneralInstance(false);
                }
            }
            rs.close();
            stmt.close();
            con.commit();
            if (ret != null) {
                Type.cacheType(ret);
                 if (parentTypeId != 0) {
                    Type.LOG.trace("get parent for id = {}",  parentTypeId);
                    final Type parent = Type.get(parentTypeId);
                    // TODO: test if loop
                    if (ret.getId() == parent.getId()) {
                        throw new CacheReloadException("child and parent type is equal!child is " + ret);
                    }
                    if (ret instanceof Classification) {
                        ((Classification) ret).setParentClassification((Classification) parent);
                        ((Classification) parent).getChildClassifications().add((Classification) ret);
                        if (((Classification) ret).isRoot()) {
                            ((Classification) ret).getClassifiesType().addClassifiedByType((Classification) ret);
                        }
                    } else {
                        ret.setParentType(parent);
                        parent.addChildType(ret);
                    }
                }
                if (!ret.checked4Children) {
                    ret.checked4Children = true;
                    for (final Long childID : Type.getChildTypeIDs(ret.getId())) {
                        Type.LOG.trace("reading Child Type with id: {} for type :{}", childID, ret.getName());
                        Type.get(childID);
                    }
                }
                Attribute.add4Type(ret);
                ret.readFromDB4Links();
                ret.readFromDB4Properties();
                Type.LOG.trace("ended reading type '{}'", ret.getName());
            }
        } catch (final EFapsException e) {
            Type.LOG.error("initialiseCache()", e);
        } catch (final SQLException e) {
            Type.LOG.error("initialiseCache()", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                try {
                    con.abort();
                } catch (final EFapsException e) {
                    throw new CacheReloadException("could not read child tyep ids", e);
                }
            }
        }
        return ret;
    }
}
