/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.admin.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.attributetype.AssociationLinkType;
import org.efaps.admin.datamodel.attributetype.BitEnumType;
import org.efaps.admin.datamodel.attributetype.CompanyLinkType;
import org.efaps.admin.datamodel.attributetype.ConsortiumLinkType;
import org.efaps.admin.datamodel.attributetype.EnumType;
import org.efaps.admin.datamodel.attributetype.GroupLinkType;
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
import org.efaps.ci.CIType;
import org.efaps.db.AttributeQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.QueryCache;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the class for the type description. The type description holds
 * information about creation of a new instance of a type with default values.
 *
 * @author The eFaps Team
 */
public class Type
    extends AbstractDataModelObject
{

    /**
     * Enum for the different purpose of a type.
     */
    public enum Purpose
        implements IBitEnum
    {
        /** Abstract purpose. */
        ABSTRACT,
        /** classification purpose. */
        CLASSIFICATION,
        /** GeneralInstane. */
        GENERALINSTANCE,
        /** No GeneralInstane. */
        NOGENERALINSTANCE,
        /** Has history and therfor cannot be deleted.*/
        HISTORY;

        /**
         * {@inheritDoc}
         */
        @Override
        public int getInt()
        {
            return BitEnumType.getInt4Index(ordinal());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getBitIndex()
        {
            return ordinal();
        }
    }

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

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
                    .column("PARENTCLASSDMTYPE")
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
                    .column("PARENTCLASSDMTYPE")
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
                    .column("PARENTCLASSDMTYPE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select the ids of child types from the database.
     */
    private static final String SQL_CHILD = new SQLSelect()
                    .column("ID")
                    .column("PURPOSE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "PARENTDMTYPE").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select the ids of child types from the database.
     */
    private static final String SQL_CLASSCHILD = new SQLSelect()
                    .column("ID")
                    .column("PURPOSE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "PARENTCLASSDMTYPE")
                        .addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static String UUIDCACHE = Type.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static String IDCACHE = Type.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static String NAMECACHE = Type.class.getName() + ".Name";

    /**
     * Instance variable for the parent type from which this type is derived.
     *
     * @see #getParentType
     * @see #setParentType
     */
    private Long parentTypeId = null;

    /**
     * Instance variable for all child types ids derived from this type.
     *
     * @see #getChildTypes
     */
    private final Set<Long> childTypes = new TreeSet<>();

    /**
     * Classification ids which are classifying this type.
     */
    private final Set<Long> classifiedByTypes = new HashSet<>();

    /**
     * The instance variables stores all attributes for this type object.
     *
     * @see #getAttributes()
     * @see #add(Attribute)
     * @see #getAttribute
     * @see #getAttributes(Class)
     */
    private final Map<String, Attribute> attributes = new HashMap<>();

    /**
     * Instance of a HashSet to store all needed tables for this type. The
     * tables are automatically added via the method {@link #add(Attribute)}.
     *
     * @see #add(Attribute)
     * @see #getTables
     */
    private final Set<SQLTable> tables = new HashSet<>();

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
     * All access sets  ids which are assigned to this type are store in this
     * instance variable. If <code>null</code> the variable was not evaluated yet;
     *
     * @see #addAccessSet
     * @see #getAccessSets
     */
    private final Set<Long> accessSets = new HashSet<>();

    /**
     * Have the accessSet been evaluated.
     */
    private boolean checked4AccessSet = false;

    /**
     * Have the children been evaluated.
     */
    private boolean checked4Children = false;

    /**
     * Internal boolean to store if for this type was already checked if it is
     * classified by an classification.
     */
    private boolean checked4classifiedBy = false;

    /**
     * Stores all type of events which are allowed to fire on this type.
     *
     * @see #setLinkProperty
     */
    private final Set<Long> allowedEventTypes = new HashSet<>();

    /**
     * Id of the store for this type.
     */
    private long storeId;

    /**
     * Is the type abstract.
     */
    private boolean abstractBool;

    /**
     * Is the type abstract.
     */
    private boolean history;

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
     * Stores the name of attribute that contains the association of this type. (if
     * exist)
     */
    private String associationAttributeName;

    /**
     * Stores the name of attribute that contains the company of this type. (if
     * exist)
     */
    private String groupAttributeName;

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
        return abstractBool;
    }

    /**
     * Setter method for instance variable {@link #abstractBool}.
     *
     * @param _abstract value for instance variable {@link #abstractBool}
     */
    private void setAbstract(final boolean _abstract)
    {
        abstractBool = _abstract;
    }

    /**
     * Getter method for the instance variable {@link #generalInstance}.
     *
     * @return value of instance variable {@link #generalInstance}
     */
    public boolean isGeneralInstance()
    {
        boolean ret = true;
        if (generalInstance != null) {
            ret = generalInstance;
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
        generalInstance = _generalInstance;
    }

    /**
     * Getter method for the instance variable {@link #history}.
     *
     * @return value of instance variable {@link #history}
     */
    public boolean isHistory()
    {
        return history;
    }

    /**
     * Setter method for instance variable {@link #history}.
     *
     * @param _history value for instance variable {@link #history}
     */
    private void setHistory(final boolean _history)
    {
        history = _history;
    }

    /**
     * Add attributes to this type and all child types of this type.
     * Recursive method.
     * @param _inherited is the attribute inherited or form this type
     * @param _attributes attributes to add
     * @throws CacheReloadException on error
     */
    protected void addAttributes(final boolean _inherited,
                                 final Attribute... _attributes)
        throws CacheReloadException
    {
        for (final Attribute attribute : _attributes) {
            if (!attributes.containsKey(attribute.getName())) {
                Type.LOG.trace("adding Attribute:'{}' to type: '{}'", attribute.getName(), getName());
                // evaluate for type attribute
                if (attribute.getAttributeType().getClassRepr().equals(TypeType.class)) {
                    typeAttributeName = attribute.getName();
                } else if (attribute.getAttributeType().getClassRepr().equals(StatusType.class) && !_inherited) {
                    // evaluate for status, an inherited attribute will not
                    // overwrite the original attribute
                    statusAttributeName = attribute.getName();
                } else if (attribute.getAttributeType().getClassRepr().equals(CompanyLinkType.class)
                                || attribute.getAttributeType().getClassRepr().equals(ConsortiumLinkType.class)) {
                    // evaluate for company
                    companyAttributeName = attribute.getName();
                } else if (attribute.getAttributeType().getClassRepr().equals(GroupLinkType.class)) {
                    // evaluate for group
                    groupAttributeName = attribute.getName();
                } else if (attribute.getAttributeType().getClassRepr().equals(AssociationLinkType.class)) {
                    // evaluate for association
                    associationAttributeName = attribute.getName();
                }
                attributes.put(attribute.getName(), attribute);
                if (attribute.getTable() != null) {
                    tables.add(attribute.getTable());
                    attribute.getTable().addType(getId());
                    if (getMainTable() == null) {
                        setMainTable(attribute.getTable());
                    }
                }
                setDirty();
            }
        }
    }

    /**
     * Inherit Attributes are child types.
     * @throws CacheReloadException on error
     */
    protected void inheritAttributes()
        throws CacheReloadException
    {
        Type parent = getParentType();
        final List<Attribute> attributesTmp = new ArrayList<>();
        while (parent != null) {
            for (final Attribute attribute : getParentType().getAttributes().values()) {
                attributesTmp.add(attribute.copy(getId()));
            }
            parent = parent.getParentType();
        }
        addAttributes(true, attributesTmp.toArray(new Attribute[attributesTmp.size()]));
    }

    /**
     * Getter method for instance variable {@link #statusAttribute}.
     *
     * @return value of instance variable {@link #statusAttribute}
     */
    public Attribute getStatusAttribute()
    {
        return attributes.get(statusAttributeName);
    }

    /**
     * Method to evaluate if the status must be checked on an accesscheck.
     *
     * @return true if {@link #statusAttribute} !=null , else false
     */
    public boolean isCheckStatus()
    {
        return statusAttributeName != null;
    }

    /**
     * Method to evaluate if this type depends on companies.
     *
     * @return true if {@link #companyAttribute} !=null , else false
     */
    public boolean isCompanyDependent()
    {
        return companyAttributeName != null;
    }

    public boolean hasAssociation()
    {
        return associationAttributeName != null;
    }

    /**
     * Method to evaluate if this type depends on companies.
     *
     * @return true if {@link #groupAttributeName} !=null , else false
     */
    public boolean isGroupDependent()
    {
        return groupAttributeName != null;
    }

    /**
     * Get the attribute containing the company information.
     *
     * @return attribute containing the company information
     */
    public Attribute getCompanyAttribute()
    {
        return attributes.get(companyAttributeName);
    }

    /**
     * Get the attribute containing the company information.
     *
     * @return attribute containing the company information
     */
    public Attribute getAssociationAttribute()
    {
        return attributes.get(associationAttributeName);
    }

    /**
     * Get the attribute containing the group information.
     *
     * @return attribute containing the group information
     */
    public Attribute getGroupAttribute()
    {
        return attributes.get(groupAttributeName);
    }

    /**
     * Get the attribute containing the type information.
     *
     * @return attribute containing the type information
     */
    public Attribute getTypeAttribute()
    {
        final Attribute ret;
        if (typeAttributeName == null && getParentType() != null) {
            ret = getParentType().getTypeAttribute();
        } else {
            ret = attributes.get(typeAttributeName);
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
        final Set<Attribute> ret = new HashSet<>();
        for (final Attribute attr : getAttributes().values()) {
            if (attr.getAttributeType().getClassRepr() == _class) {
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
        if (_type != null) {
            Type type = this;
            while (type != null && type.getId() != _type.getId()) {
                type = type.getParentType();
            }
            if (type != null && type.getId() == _type.getId()) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Tests, if this type is kind of the type in the parameter (question is, is
     * this type a child of the parameter type).
     *
     * @param _ciType CIType to test for parent
     * @return true if this type is a child, otherwise false
     */
    public boolean isKindOf(final CIType _ciType)
    {
        return isKindOf(_ciType.getType());
    }

    /**
     * Tests, if this type is the given CIType.
     *
     * @param _ciType CIType to test for parent
     * @return true if this type is a child, otherwise false
     */
    public boolean isCIType(final CIType _ciType)
    {
        return equals(_ciType.getType());
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
        if (value == null && getParentType() != null) {
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
            parameter.put(ParameterValues.CLASS, this);

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
    public Map<Instance, Boolean> checkAccess(final Collection<Instance> _instances,
                                              final AccessType _accessType)
        throws EFapsException
    {
        Map<Instance, Boolean> ret = new HashMap<>();
        if (_instances != null && !_instances.isEmpty() && _instances.size() == 1) {
            final Instance instance = _instances.iterator().next();
            ret.put(instance, hasAccess(instance, _accessType));
        } else {
            final List<EventDefinition> events = super.getEvents(EventType.ACCESSCHECK);
            if (events != null) {
                final Parameter parameter = new Parameter();
                parameter.put(ParameterValues.OTHERS, _instances);
                parameter.put(ParameterValues.ACCESSTYPE, _accessType);
                parameter.put(ParameterValues.CLASS, this);
                for (final EventDefinition event : events) {
                    final Return retrn = event.execute(parameter);
                    ret = (Map<Instance, Boolean>) retrn.get(ReturnValues.VALUES);
                }
            } else {
                for (final Instance instance : _instances) {
                    ret.put(instance, true);
                }
            }
        }
        return ret;
    }

    /**
     * @param _accessSet AccessSet to add to this Type
     */
    public void addAccessSet(final AccessSet _accessSet)
    {
        accessSets.add(_accessSet.getId());
        setDirty();
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
        if (!checked4AccessSet) {
            checked4AccessSet = true;
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.AccessSet2DataModelType);
            queryBldr.addWhereAttrEqValue(CIAdminAccess.AccessSet2DataModelType.DataModelTypeLink, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminAccess.AccessSet2DataModelType.AccessSetLink);
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                final Long accessSet = multi.<Long>getAttribute(CIAdminAccess.AccessSet2DataModelType.AccessSetLink);
                AccessSet.get(accessSet);
                accessSets.add(accessSet);
            }
            setDirty();
        }
        final Set<AccessSet> ret = new HashSet<>();
        for (final Long id : accessSets) {
            ret.add(AccessSet.get(id));
        }
        return Collections.unmodifiableSet(ret);
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
        if (_linkTypeUUID.equals(CIAdminDataModel.Type2Store.uuid)) {
            storeId = _toId;
        } else if (_linkTypeUUID.equals(CIAdminDataModel.TypeEventIsAllowedFor.uuid)) {
            allowedEventTypes.add(_toId);
        }
        super.setLinkProperty(_linkTypeUUID, _toId, _toTypeUUID, _toName);
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
        Type ret = null;
        if (parentTypeId != null && parentTypeId != 0) {
            try {
                ret = Type.get(parentTypeId);
            } catch (final CacheReloadException e) {
                Type.LOG.error("Could not read parentType for id: {}", parentTypeId);
            }
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #parentTypeId}.
     *
     * @return value of instance variable {@link #parentTypeId}
     */
    protected Long getParentTypeId()
    {
        return parentTypeId;
    }

    /**
     * Setter method for instance variable {@link #parentType}.
     *
     * @param _parentTypeId parentid to set
     */
    protected void setParentTypeID(final long _parentTypeId)
    {
        parentTypeId = _parentTypeId;
    }

    /**
     * Add a root Classification to this type.
     *
     * @param _classification classifixation that classifies this type
     */
    protected void addClassifiedByType(final Classification _classification)
    {
        checked4classifiedBy = true;
        classifiedByTypes.add(_classification.getId());
        setDirty();
    }

    /**
     * Getter method for instance variable {@link #classifiedByTypes}.
     * The method retrieves lazy the Classification Types.
     * @return value of instance variable {@link #classifiedByTypes}
     * @throws EFapsException on error
     */
    public Set<Classification> getClassifiedByTypes()
        throws EFapsException
    {
        if (!checked4classifiedBy) {
            final QueryBuilder attrQueryBldr = new QueryBuilder(CIAdminDataModel.TypeClassifies);
            attrQueryBldr.addWhereAttrEqValue(CIAdminDataModel.TypeClassifies.To, getId());
            final AttributeQuery attrQuery = attrQueryBldr.getAttributeQuery(CIAdminDataModel.TypeClassifies.From);
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminDataModel.Type);
            queryBldr.addWhereAttrInQuery(CIAdminDataModel.Type.ID, attrQuery);
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();
            while (query.next()) {
                Type.get(query.getCurrentValue().getId());
            }
            checked4classifiedBy = true;
            setDirty();
        }
        final Set<Classification> ret = new HashSet<>();
        if (getParentType() != null) {
            ret.addAll(getParentType().getClassifiedByTypes());
        }
        for (final Long id : classifiedByTypes) {
            ret.add((Classification) Type.get(id));
        }
        return Collections.unmodifiableSet(ret);
    }

    /**
     * This is the getter method for instance variable {@link #childTypes}.
     *
     * @return value of instance variable {@link #childTypes}
     * @see #childTypes
     * @throws CacheReloadException on error
     */
    public Set<Type> getChildTypes()
        throws CacheReloadException
    {
        final Set<Type> ret = new LinkedHashSet<>();
        for (final Long id : childTypes) {
            final Type child = Type.get(id);
            ret.add(child);
            ret.addAll(child.getChildTypes());
        }
        return Collections.unmodifiableSet(ret);
    }

    public boolean hasChildren()
    {
        return !childTypes.isEmpty();
    }

    /**
     * This is the getter method for instance variable {@link #attributes}.
     *
     * @return value of instance variable {@link #attributes}
     * @see #attributes
     */
    public Map<String, Attribute> getAttributes()
    {
        return attributes;
    }

    /**
     * This is the getter method for instance variable {@link #tables}.
     *
     * @return value of instance variable {@link #tables}
     * @see #tables
     */
    public Set<SQLTable> getTables()
    {
        return tables;
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
        SQLTable ret = mainTable;
        if (mainTable == null && getParentType() != null) {
            ret = getParentType().getMainTable();
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
        mainTable = table;
    }

    /**
     * This is the getter method for instance variable
     * {@link #allowedEventTypes}.
     *
     * @return value of instance variable {@link #allowedEventTypes}
     * @see #allowedEventTypes
     * @throws CacheReloadException on error
     */
    public Set<Type> getAllowedEventTypes()
        throws CacheReloadException
    {
        final Set<Type> ret = new HashSet<>();
        for (final Long id : allowedEventTypes) {
            ret.add(Type.get(id));
        }
        return Collections.unmodifiableSet(ret);
    }

    /**
     * Getter method for instance variable {@link #storeId}.
     *
     * @return value of instance variable {@link #storeId}
     */
    public long getStoreId()
    {
        final long ret;
        if (storeId == 0 && getParentType() != null) {
            ret = getParentType().getStoreId();
        } else {
            ret = storeId;
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
        if (typeMenu == null) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.LinkIsTypeTreeFor);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.LinkIsTypeTreeFor.To, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.LinkIsTypeTreeFor.From);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Long menuId = multi.<Long>getAttribute(CIAdminUserInterface.LinkIsTypeTreeFor.From);
                ret = Menu.get(menuId);
                if (ret != null) {
                    typeMenu = ret.getId();
                    ret.setTypeMenu(true);
                } else {
                    typeMenu = (long) 0;
                }
            } else {
                typeMenu = (long) 0;
            }
            setDirty();
        }
        if (typeMenu == 0 && getParentType() != null) {
            ret = getParentType().getTypeMenu();
        } else {
            ret = Menu.get(typeMenu);
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
        if (typeIcon == null) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.LinkIsTypeIconFor);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.LinkIsTypeIconFor.To, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.LinkIsTypeIconFor.From);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Long menuId = multi.<Long>getAttribute(CIAdminUserInterface.LinkIsTypeIconFor.From);
                ret = Image.get(menuId);
                if (ret != null) {
                    typeIcon = ret.getId();
                } else {
                    typeIcon = (long) 0;
                }
            } else {
                typeIcon = (long) 0;
            }
            setDirty();
        }
        if (typeIcon == 0 && getParentType() != null) {
            ret = getParentType().getTypeIcon();
        } else {
            ret = Image.get(typeIcon);
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
        if (typeForm == null) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.LinkIsTypeFormFor);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.LinkIsTypeFormFor.To, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.LinkIsTypeFormFor.From);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Long formId = multi.<Long>getAttribute(CIAdminUserInterface.LinkIsTypeFormFor.From);
                ret = Form.get(formId);
                if (ret != null) {
                    typeForm = ret.getId();
                } else {
                    typeForm = (long) 0;
                }
            } else {
                typeForm = (long) 0;
            }
            setDirty();
        }
        if (typeForm == 0 && getParentType() != null) {
             ret = getParentType().getTypeForm();
        } else {
            ret = Form.get(typeForm);
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
                        .append("parentTypeId", parentTypeId)
                        .append("attributes", attributes.size())
                        .append("children", childTypes.size())
                        .append("abstract", abstractBool)
                        .append("accessSets", accessSets.size())
                        .append("companyDependend", isCompanyDependent())
                        .append("hasAssociation", hasAssociation())
                        .append("groupDependend", isGroupDependent())
                        .append("statusDependend", isCheckStatus())
                        .append("checked4AccessSet", checked4AccessSet)
                        .append("checked4Children", checked4Children)
                        .append("checked4classifiedBy", checked4classifiedBy)
                        .append("dirty", isDirty())
                        .toString();
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
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
        if (InfinispanCache.get().exists(EnumType.CACHE)) {
            InfinispanCache.get().getCache(EnumType.CACHE).clear();
        }
        QueryCache.initialize();
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
        Type ret = cache.get(_id);
        if (ret != null && ret.isDirty()) {
            Type.LOG.debug("Recaching dirty Type for id: {}", ret);
            Type.cacheType(ret);
            ret = cache.get(_id);
        }
        return ret;
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
        Type ret = cache.get(_name);
        if (ret != null && ret.isDirty()) {
            Type.LOG.debug("Recaching dirty Type for name: {}", ret);
            Type.cacheType(ret);
            ret = cache.get(_name);
        }
        return ret;
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
        Type ret = cache.get(_uuid);
        if (ret != null && ret.isDirty()) {
            Type.LOG.debug("Recaching dirty Type for uuid: {}", ret);
            Type.cacheType(ret);
            ret = cache.get(_uuid);
        }
        return ret;
    }

    /**
     * @param _type type to be cached
     */
    protected static void cacheType(final Type _type)
    {
        _type.setUndirty();
        final Cache<UUID, Type> cache4UUID = InfinispanCache.get().<UUID, Type>getIgnReCache(Type.UUIDCACHE);
        cache4UUID.put(_type.getUUID(), _type);

        final Cache<String, Type> nameCache = InfinispanCache.get().<String, Type>getIgnReCache(Type.NAMECACHE);
        nameCache.put(_type.getName(), _type);

        final Cache<Long, Type> idCache = InfinispanCache.get().<Long, Type>getIgnReCache(Type.IDCACHE);
        idCache.put(_type.getId(), _type);
    }

    /**
     * In case of a cluster the types must be cached after the final loading
     * again to be sure that the last instance including all the changes like
     * attribute links etc are up to date.
     *
     * @param _type Type the Hierachy must be cached
     * @throws CacheReloadException on error
     */
    protected static void cacheTypesByHierachy(final Type _type)
        throws CacheReloadException
    {
        final Cache<UUID, Type> cache4UUID = InfinispanCache.get().<UUID, Type>getIgnReCache(Type.UUIDCACHE);
        if (cache4UUID.getCacheConfiguration().clustering() != null
                        && !cache4UUID.getCacheConfiguration().clustering().cacheMode().equals(CacheMode.LOCAL)) {
            Type type = _type;
            while (type.getParentTypeId() != null) {
                final Cache<Long, Type> cache = InfinispanCache.get().<Long, Type>getIgnReCache(Type.IDCACHE);
                if (cache.containsKey(type.getParentTypeId())) {
                    type = cache.get(type.getParentTypeId());
                } else {
                    type = type.getParentType();
                }
            }
            type.recacheChildren();
        }
    }

    /**
     * Recache the children in dropdown. Used for Caching in cluster.
     * @throws CacheReloadException on error
     */
    private void recacheChildren()
        throws CacheReloadException
    {
        if (isDirty()) {
            Type.cacheType(this);
        }
        for (final Type child : getChildTypes()) {
            child.recacheChildren();
        }
    }

    /**
     * @param _parentID id to be searched for
     * @param _statement statement to be executed
     * @return a list of object containing the id and the purpose
     * @throws CacheReloadException on error
     */
    private static List<Object[]> getChildTypeIDs(final long _parentID,
                                                  final String _statement)
        throws CacheReloadException
    {
        final List<Object[]> ret = new ArrayList<>();
        Connection con = null;
        try {
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(_statement);
                stmt.setObject(1, _parentID);
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ret.add(new Object[] { rs.getLong(1), rs.getInt(2) });
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            con.close();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read child type ids", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read child type ids", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
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
        Connection con = null;
        try {
            con = Context.getConnection();
            final PreparedStatement stmt = con.prepareStatement(_sql);
            stmt.setObject(1, _criteria);
            final ResultSet rs = stmt.executeQuery();
            long parentTypeId = 0;
            long parentClassTypeId = 0;
            long id = 0;

            if (rs.next()) {
                id = rs.getLong(1);
                final String uuid = rs.getString(2).trim();
                final String name = rs.getString(3).trim();
                final int purpose = rs.getInt(4);
                parentTypeId = rs.getLong(5);
                parentClassTypeId = rs.getLong(6);

                Type.LOG.debug("read type '{}' (id = {}) (purpose = {}) (parentTypeId = {}) (parentClassTypeId = {})",
                                name, id, purpose, parentTypeId, parentClassTypeId);

                if (BitEnumType.isSelected(purpose, Type.Purpose.CLASSIFICATION)) {
                    ret = new Classification(id, uuid, name);
                    if (parentClassTypeId != 0) {
                        ((Classification) ret).setParentClassification(parentClassTypeId);
                    }
                } else {
                    ret = new Type(id, uuid, name);
                }
                if (parentTypeId != 0) {
                    ret.setParentTypeID(parentTypeId);
                }
                ret.setAbstract(BitEnumType.isSelected(purpose, Type.Purpose.ABSTRACT));
                ret.setHistory(BitEnumType.isSelected(purpose, Type.Purpose.HISTORY));
                if (BitEnumType.isSelected(purpose, Type.Purpose.GENERALINSTANCE)) {
                    ret.setGeneralInstance(true);
                }
                if (BitEnumType.isSelected(purpose, Type.Purpose.NOGENERALINSTANCE)) {
                    ret.setGeneralInstance(false);
                }
            }
            rs.close();
            stmt.close();
            con.commit();
            con.close();
            if (ret != null) {
                if (parentTypeId != 0) {
                    Type.LOG.trace("get parent for id = {}",  parentTypeId);
                    final Type parent = Type.get(parentTypeId);
                    // TODO: test if loop
                    if (ret.getId() == parent.getId()) {
                        throw new CacheReloadException("child and parent type is equal!child is " + ret);
                    }
                }
                if (!ret.checked4Children) {
                    ret.checked4Children = true;
                    for (final Object[] childIDs : Type.getChildTypeIDs(ret.getId(), Type.SQL_CHILD)) {
                        Type.LOG.trace("reading Child Type with id: {} for type :{}", childIDs[0], ret.getName());
                        ret.childTypes.add((Long) childIDs[0]);
                    }
                    if (ret instanceof Classification) {
                        for (final Object[] childIDs : Type.getChildTypeIDs(ret.getId(), Type.SQL_CLASSCHILD)) {
                            Type.LOG.trace("reading Child class Type with id: {} for type :{}",
                                            childIDs[0], ret.getName());
                            ((Classification) ret).getChildren().add((Long) childIDs[0]);
                        }
                    }
                    ret.setDirty();
                }
                Attribute.add4Type(ret);
                ret.readFromDB4Links();
                ret.readFromDB4Properties();
                ret.inheritAttributes();
                // needed due to cluster serialization that does not update automatically
                Type.cacheType(ret);
                Type.LOG.trace("ended reading type '{}'", ret.getName());
            }
        } catch (final EFapsException e) {
            Type.LOG.error("initialiseCache()", e);
        } catch (final SQLException e) {
            Type.LOG.error("initialiseCache()", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
        return ret;
    }

    /**
     * Compares a given id and UUID of a type to evaluate if they are from the
     * same Type. In case that they are not cached (during initialize) the
     * database is requested.
     *
     * @param _typeId Id of the type to be checked
     * @param _typeUUID uuid of the type to be checked
     * @return true if the id and the UUID belong to the same type
     * @throws CacheReloadException on error
     */
    protected static boolean check4Type(final long _typeId,
                                        final UUID _typeUUID)
        throws CacheReloadException
    {
        boolean ret = false;
        final Cache<Long, Type> cache = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        if (cache.containsKey(_typeId)) {
            ret = cache.get(_typeId).getUUID().equals(_typeUUID);
        } else {
            Connection con = null;
            String uuidTmp = "";
            try {
                con = Context.getConnection();
                PreparedStatement stmt = null;
                try {
                    stmt = con.prepareStatement(Type.SQL_ID);
                    stmt.setObject(1, _typeId);
                    final ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        uuidTmp = rs.getString(2).trim();
                    }
                    rs.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
                con.close();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read child type ids", e);
            } finally {
                try {
                    if (con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    throw new CacheReloadException("could not read child type ids", e);
                }
            }
            ret = StringUtils.isNotEmpty(uuidTmp) && UUID.fromString(uuidTmp).equals(_typeUUID);
        }
        return ret;
    }

    /**
     * During the initial caching of types, the mapping does not exists but is necessary.
     * @param _typeUUID UUID of the type the id is wanted for
     * @return id of the type
     * @throws CacheReloadException on error
     */
    protected static long getId4UUID(final UUID _typeUUID)
        throws CacheReloadException
    {
        long ret = 0;
        final Cache<UUID, Type> cache = InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE);
        if (cache.containsKey(_typeUUID)) {
            ret = cache.get(_typeUUID).getId();
        } else {
            Connection con = null;
            try {
                con = Context.getConnection();
                PreparedStatement stmt = null;
                try {
                    stmt = con.prepareStatement(Type.SQL_UUID);
                    stmt.setObject(1, _typeUUID.toString());
                    final ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ret = rs.getLong(1);
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
                try {
                    if (con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    throw new CacheReloadException("could not read child type ids", e);
                }
            }
        }
        return ret;
    }

    /**
     * During the initial caching of types, the mapping does not exists but is necessary.
     * @param _typeId id of the type the UUID is wanted for
     * @return id of the type
     * @throws CacheReloadException on error
     */
    public static UUID getUUID4Id(final long _typeId)
        throws CacheReloadException
    {
        UUID ret = null;
        final Cache<Long, Type> cache = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        if (cache.containsKey(_typeId)) {
            ret = cache.get(_typeId).getUUID();
        } else {
            Connection con = null;
            try {
                con = Context.getConnection();
                PreparedStatement stmt = null;
                try {
                    stmt = con.prepareStatement(Type.SQL_ID);
                    stmt.setObject(1, _typeId);
                    final ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ret = UUID.fromString(rs.getString(2).trim());
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
                try {
                    if (con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    throw new CacheReloadException("Cannot read a type for an attribute.", e);
                }
            }
        }
        return ret;
    }

    /**
     * Checks if is initialized.
     *
     * @return true, if is initialized
     */
    public static boolean isInitialized()
    {
        final Cache<Long, Type> cache1 = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        final Cache<String, Type> cache2 = InfinispanCache.get().<String, Type>getCache(Type.NAMECACHE);
        final Cache<UUID, Type> cache3 = InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE);
        return !cache1.isEmpty() || !cache2.isEmpty() || !cache3.isEmpty();
    }
}
