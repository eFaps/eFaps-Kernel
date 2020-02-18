/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.access.user.AccessCache;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.index.Queue;
import org.efaps.ci.CIAttribute;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 *
 */
public class Update
{
    /**
     * Variable to get the Status of this update.
     */
    private static final Status STATUSOK = new Status();

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Update.class);

    /**
     * The instance variable stores the instance for which this update is made.
     */
    private Instance instance = null;

    /**
     * Mapping of SQL-Table to Attribute and Value.
     */
    private final Map<SQLTable, List<Value>> table2values = new Hashtable<>();

    /**
     * Mapping of attribute to values.
     */
    protected final Map<Attribute, Value> attr2values = new HashMap<>();

    /**
     * Mapping of attribute to values.
     */
    private final Map<Attribute, Value> trigRelevantAttr2values = new HashMap<>();

    /**
     * @param _type     Type to be updated
     * @param _id       id to be updated
     * @throws EFapsException on error
     */
    public Update(final Type _type,
                  final String _id)
        throws EFapsException
    {
        this(Instance.get(_type, _id));
    }

    /**
     * @param _type     Type to be updated
     * @param _id       id to be updated
     * @throws EFapsException on error
     */
    public Update(final Type _type,
                  final long _id)
        throws EFapsException
    {
        this(Instance.get(_type, _id));
    }

    /**
     * @param _type     Type to be updated
     * @param _id       id to be updated
     * @throws EFapsException on error
     */
    public Update(final String _type,
                  final String _id)
        throws EFapsException
    {
        this(Type.get(_type), _id);
    }

    /**
     * @param _oid OID of the instance to be updated.
     * @throws EFapsException on error
     */
    public Update(final String _oid)
        throws EFapsException
    {
        this(Instance.get(_oid));
    }

    /**
     * @param _instance  instance to be updated.
     * @throws EFapsException on error
     */
    public Update(final Instance _instance)
        throws EFapsException
    {
        setInstance(_instance);
        addAlwaysUpdateAttributes();
        if (!Update.STATUSOK.getStati().isEmpty()) {
            Update.STATUSOK.getStati().clear();
        }
    }

    /**
     * Add all attributes of the type which must be always updated.
     *  @throws EFapsException on error
     */
    protected void addAlwaysUpdateAttributes()
        throws EFapsException
    {
        final Iterator<?> iter = getInstance().getType().getAttributes().entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
            final Attribute attr = (Attribute) entry.getValue();
            final AttributeType attrType = attr.getAttributeType();
            if (attrType.isAlwaysUpdate()) {
                addInternal(attr, false, (Object[]) null);
            }
        }
    }

    /**
     * The method closes the SQL statement.
     *
     * @see #statement
     * @throws EFapsException on error
     */
    public void close()
        throws EFapsException
    {
    }

    /**
     * The method gets all events for the given EventType and executes them in
     * the given order. If no events are defined, nothing is done. The method
     * return TRUE if a event was found, otherwise FALSE.
     *
     * @param _eventtype trigger events to execute
     * @return true if a trigger was found and executed, otherwise false
     * @throws EFapsException on error
     */
    protected boolean executeEvents(final EventType _eventtype)
        throws EFapsException
    {
        boolean ret = false;
        final List<EventDefinition> triggers = getInstance().getType().getEvents(_eventtype);
        if (triggers != null) {
            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.NEW_VALUES, getNewValuesMap());
            parameter.put(ParameterValues.INSTANCE, getInstance());
            for (final EventDefinition evenDef : triggers) {
                evenDef.execute(parameter);
            }
            ret = true;
        }
        return ret;
    }

    /**
     * @return the map of new values send to the triggers
     */
    protected final Map<Attribute, Object[]> getNewValuesMap()
    {
        // convert the map in a more simple map (following existing API)
        final Map<Attribute, Object[]> ret = new HashMap<>();
        for (final Entry<Attribute, Value> entry : trigRelevantAttr2values.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().getValues());
        }
        return ret;
    }

    /**
     * @param _attr     name of attribute to update
     * @param _values   attribute values
     * @throws EFapsException on error
     * @return Status
     */
    public Status add(final String _attr,
                      final Object... _values)
        throws EFapsException
    {
        final Attribute attr = getInstance().getType().getAttribute(_attr);
        if (attr == null) {
            throw new EFapsException(getClass(), "add.UnknownAttributeName", _attr);
        }
        return add(attr, _values);
    }

    /**
     * @param _attr     attribute to update
     * @param _values    attribute value
     * @throws EFapsException on error
     * @return Status
     */
    public Status add(final Attribute _attr,
                      final Object... _values)
        throws EFapsException
    {
        return addInternal(_attr, true, _values);
    }

    /**
     * @param _attr     attribute to update
     * @param _values    attribute value
     * @throws EFapsException on error
     * @return Status
     */
    public Status add(final CIAttribute _attr,
                      final Object... _values)
        throws EFapsException
    {
        final Attribute attr = getInstance().getType().getAttribute(_attr.name);
        if (attr == null) {
            throw new EFapsException(getClass(), "add.UnknownAttributeName", _attr);
        }
        return add(attr, _values);
    }

    /**
     * @param _attr             Attribute to add
     * @param _value            value to add
     * @param _triggerRelevant  is the attribute triggerrelevant
     * @return Status
     * @throws EFapsException on error
     */
    protected Status addInternal(final Attribute _attr,
                                 final boolean _triggerRelevant,
                                 final Object... _value)
        throws EFapsException
    {
        Status ret = Update.STATUSOK;
        if (_attr.hasEvents(EventType.VALIDATE)) {
            final List<Return> returns = _attr.executeEvents(EventType.VALIDATE, ParameterValues.NEW_VALUES, _value);
            for (final Return retu : returns) {
                if (retu.get(ReturnValues.TRUE) == null) {
                    ret = new Status(retu.get(ReturnValues.VALUES), _attr, _value);
                    break;
                }
            }
        }
        final Value value = getValue(_attr, _value);
        validate(getInstance(), value);
        List<Value> values = table2values.get(_attr.getTable());
        if (values == null) {
            values = new ArrayList<>();
            table2values.put(_attr.getTable(), values);
        }
        values.add(value);

        attr2values.put(_attr, value);

        if (_triggerRelevant) {
            trigRelevantAttr2values.put(_attr, value);
        }
        return ret;
    }

    /**
     * @param _instance instance to be validated
     * @param _value value to be validated
     * @throws EFapsException if invalid
     */
    protected void validate(final Instance _instance,
                            final Value _value)
        throws EFapsException
    {
        _value.getAttribute().getAttributeType().getDbAttrType()
                        .validate4Update(_value.getAttribute(), getInstance(), _value.getValues());
    }

    /**
     * @param _attr     Attribute
     * @param _value    values to be evaluated
     * @return value
     */
    protected Value getValue(final Attribute _attr,
                             final Object[] _value)
    {
        final List<Object> values = new ArrayList<>();
        if (_value != null) {
            for (final Object obj : _value) {
                final Object object;
                if (obj instanceof Instance) {
                    object = ((Instance) obj).getId();
                } else if (obj instanceof org.efaps.admin.datamodel.Status) {
                    object = ((org.efaps.admin.datamodel.Status) obj).getId();
                } else if (obj instanceof UoM) {
                    object = ((UoM) obj).getId();
                } else {
                    object = obj;
                }
                values.add(object);
            }
        }
        return new Value(_attr, _value == null ? null : values.toArray());
    }

    /**
     * Getter method for the instance variable {@link #table2values}.
     *
     * @return value of instance variable {@link #table2values}
     */
    protected Map<SQLTable, List<Value>> getTable2values()
    {
        return table2values;
    }

    /**
     * @throws EFapsException thrown from {@link #executeWithoutAccessCheck}
     * @see #executeWithoutAccessCheck
     */
    public void execute()
        throws EFapsException
    {
        final Set<Attribute> attributes = new HashSet<>();
        for (final Attribute attr : attr2values.keySet()) {
            final AttributeType attrType = attr.getAttributeType();
            if (!attrType.isAlwaysUpdate()) {
                attributes.add(attr);
            }
        }
        AccessCache.registerUpdate(getInstance());
        final boolean hasAccess = getType().hasAccess(getInstance(), AccessTypeEnums.MODIFY.getAccessType(),
                        attributes);

        if (!hasAccess) {
            throw new EFapsException(getClass(), "execute.NoAccess", Context.getThreadContext().getPerson());
        }
        executeWithoutAccessCheck();
    }

    /**
     * Executes the update without checking the access rights (but with
     * triggers).
     * <ol>
     * <li>executes the pre update trigger (if exists)</li>
     * <li>executes the override trigger (if exists)</li>
     * <li>executes if no override trigger exists or the override trigger is not
     * executed the update ({@see #executeWithoutTrigger})</li>
     * <li>executes the post update trigger (if exists)</li>
     * </ol>
     *
     * @throws EFapsException thrown from {@link #executeWithoutTrigger} or when
     *             the Status is invalid
     * @see #executeWithoutTrigger
     */
    public void executeWithoutAccessCheck()
        throws EFapsException
    {
        if (Update.STATUSOK.getStati().isEmpty()) {

            executeEvents(EventType.UPDATE_PRE);

            if (!executeEvents(EventType.UPDATE_OVERRIDE)) {
                executeWithoutTrigger();
            }

            executeEvents(EventType.UPDATE_POST);
        } else {
            throw new EFapsException(getClass(), "executeWithout.StatusInvalid", Update.STATUSOK.getStati());
        }
    }

    /**
     * The update is done without calling triggers and check of access rights.
     *
     * @throws EFapsException if update not possible (unique key, object does
     *             not exists, etc...)
     */
    public void executeWithoutTrigger()
        throws EFapsException
    {
        if (Update.STATUSOK.getStati().isEmpty()) {
            final Context context = Context.getThreadContext();
            ConnectionResource con = null;
            try {
                con = context.getConnectionResource();
                for (final Entry<SQLTable, List<Value>> entry : table2values.entrySet()) {
                    final SQLUpdate update = Context.getDbType().newUpdate(entry.getKey().getSqlTable(),
                                    entry.getKey().getSqlColId(),
                                    getInstance().getId());
                    // iterate in reverse order and only execute the ones that are not added yet, permitting
                    // to overwrite the value for attributes by adding them later
                    final ReverseListIterator<Value> iterator = new ReverseListIterator<>(entry.getValue());

                    final Set<String> added = new HashSet<>();
                    while (iterator.hasNext()) {
                        final Value value = iterator.next();
                        final String colKey = value.getAttribute().getSqlColNames().toString();
                        if (!added.contains(colKey)) {
                            value.getAttribute().prepareDBUpdate(update, value.getValues());
                            added.add(colKey);
                        }
                    }
                    final Set<String> updatedColumns = update.execute(con);
                    final Iterator<Entry<Attribute, Value>> attrIter = trigRelevantAttr2values.entrySet()
                                    .iterator();
                    while (attrIter.hasNext()) {
                        final Entry<Attribute, Value> trigRelEntry = attrIter.next();
                        if (trigRelEntry.getKey().getTable().equals(entry.getKey())) {
                            boolean updated = false;
                            for (final String colName : trigRelEntry.getKey().getSqlColNames()) {
                                if (updatedColumns.contains(colName)) {
                                    updated = true;
                                    break;
                                }
                            }
                            if (!updated) {
                                attrIter.remove();
                            }
                        }
                    }
                }
                AccessCache.registerUpdate(getInstance());
                Queue.registerUpdate(getInstance());
            } catch (final SQLException e) {
                Update.LOG.error("Update of '" + instance + "' not possible", e);
                throw new EFapsException(getClass(), "executeWithoutTrigger.SQLException", e, instance);
            }
        } else {
            throw new EFapsException(getClass(), "executeWithout.StatusInvalid", Update.STATUSOK.getStati());
        }
    }

    /**
     * The instance method returns the Type instance of {@link #instance}.
     *
     * @return type of {@link #instance}
     * @see #instance
     */
    protected Type getType()
    {
        return getInstance().getType();
    }

    /**
     * The instance method returns the id of {@link #instance}.
     *
     * @return id of {@link #instance}
     * @see #instance
     */
    public long getId()
    {
        return getInstance().getId();
    }

    /**
     * This is the getter method for instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     * @see #instance
     * @see #setInstance
     */
    public Instance getInstance()
    {
        return instance;
    }

    /**
     * This is the setter method for instance variable {@link #instance}.
     *
     * @param _instance new value for instance variable {@link #instance}
     * @see #instance
     * @see #getInstance
     */
    protected void setInstance(final Instance _instance)
    {
        instance = _instance;
    }

    /**
     * Class is used to set a satus of the update.
     */
    public static class Status
    {

        /**
         * this instance variable is only used in static Status STATUSOK.<br>
         * It stores all Instances of Status which are not ok.
         *
         * @see #getStati()
         */
        private final List<Update.Status> stati = new ArrayList<>();

        /**
         * This instance variable stores the ReturnValue of the esjp.
         *
         * @see #getReturnValue()
         */
        private final Object returnValue;

        /**
         * this instance variable stores the Value wich was thought for the
         * Attribute.
         *
         * @see #getAttribute()
         */
        private final Object value;

        /**
         * this instance variable stores the Attribute wich led to the creation
         * of this Status.
         */
        private final Attribute attribute;

        /**
         * Constructor setting the instance variables and stores this Status in
         * STATUSOK.
         *
         * @param _returnvalue  value to be returned
         * @param _attribute    attribute
         * @param _value        value
         */
        public Status(final Object _returnvalue,
                      final Attribute _attribute,
                      final Object _value)
        {
            returnValue = _returnvalue;
            value = _value;
            attribute = _attribute;
            Update.STATUSOK.getStati().add(this);
        }

        /**
         * defualt constructor.
         */
        public Status()
        {
            returnValue = null;
            value = null;
            attribute = null;
        }

        /**
         * This method can be called to see if the Status is Ok.
         *
         * @return true if ok, else false
         */
        public boolean isOk()
        {
            boolean ret = false;
            if (equals(Update.STATUSOK)) {
                ret = true;
            }
            return ret;
        }

        /**
         * This is the getter method for the instance variable
         * {@link #returnValue}.
         *
         * @return value of instance variable {@link #returnValue}
         */

        public Object getReturnValue()
        {
            return returnValue;
        }

        /**
         * This is the getter method for the instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */

        public Object getValue()
        {
            return value;
        }

        /**
         * This is the getter method for the instance variable
         * {@link #attribute}.
         *
         * @return value of instance variable {@link #attribute}
         */

        public Attribute getAttribute()
        {
            return attribute;
        }

        /**
         * This is the getter method for the instance variable {@link #stati}.
         *
         * @return value of instance variable {@link #stati}
         */

        public List<Update.Status> getStati()
        {
            return stati;
        }

        /**
         * @see java.lang.Object#toString()
         * @return String representation of this class
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this).append("AttributeName", getAttribute().getName())
                .append(" Value", getValue()).append(" ReturnValue:", getReturnValue()).toString();
        }
    }

    /**
     * Represents one value for an attribte that will be updated.
     */
    public static final class Value
    {
        /**
         * Attribute the value belongs to.
         */
        private final Attribute attribute;

        /**
         * Values for the Attribute.
         */
        private final Object[] values;

        /**
         * @param _attribute    Attribute the value belongs to
         * @param _values       Valuers for the Attribute
         */
        private Value(final Attribute _attribute,
                      final Object... _values)
        {
            attribute = _attribute;
            values = _values;
        }

        /**
         * @return the attribute
         */
        public Attribute getAttribute()
        {
            return attribute;
        }

        /**
         * @return the values
         */
        public Object[] getValues()
        {
            return values;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
