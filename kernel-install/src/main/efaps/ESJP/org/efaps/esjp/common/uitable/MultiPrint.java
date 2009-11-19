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

package org.efaps.esjp.common.uitable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.DateTimeType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("cbab3ae6-fe28-4604-838c-4c6d260156fb")
@EFapsRevision("$Rev$")
public class MultiPrint
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MultiPrint.class);

    /**
     * @param _parameter Parameter
     * @return Return with List of instances
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = _parameter.getInstance();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final Map<?, ?> filter = (Map<?, ?>) _parameter.get(ParameterValues.OTHERS);

        final String types = (String) properties.get("Types");

        final boolean expandChildTypes = "true".equals(properties.get("ExpandChildTypes"));

        final String expand = (String) properties.get("Expand");
        final String alternateAttribute = (String) properties.get("AlternateAttribute");

        if (MultiPrint.LOG.isDebugEnabled()) {
            MultiPrint.LOG.debug("types=" + types);
        }

        final SearchQuery query = new SearchQuery();
        Type type = null;
        if (types != null) {
            query.setQueryTypes(types);
            query.setExpandChildTypes(expandChildTypes);
            type = Type.get(types);
        } else if (expand != null) {
            query.setExpand(instance, expand);
            type = Type.get(expand.substring(0, expand.indexOf("\\")));
        }

        final List<Instance> instances = getInstances(_parameter, filter, query, type);

        if (alternateAttribute != null) {
            final SearchQuery query2 = new SearchQuery();
            query2.setExpand(instance, expand.substring(0, expand.indexOf("\\") + 1) + alternateAttribute);
            instances.addAll(getInstances(_parameter, filter, query2, type));
        }

        ret.put(ReturnValues.VALUES, instances);

        return ret;
    }

    /**
     * Method to get the List of instances.
     * @param _parameter    parameter from the eFaps API
     * @param _filter       map of filters
     * @param _query        query to be executed
     * @param _type         type the query is based on
     * @return  List of instance
     * @throws EFapsException on error
     */
    protected List<Instance> getInstances(final Parameter _parameter,
                                          final Map<?, ?> _filter,
                                          final SearchQuery _query,
                                          final Type _type)
        throws EFapsException
    {
        boolean exec = true;
        if (_filter != null && _filter.size() > 0) {
            final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
            // filtering
            for (final Entry<?, ?> entry : _filter.entrySet()) {
                final String fieldName = (String) entry.getKey();
                final Field field = command.getTargetTable().getField(fieldName);
                if (!field.isFilterPickList()) {
                    String attrName = field.getExpression() == null ? field.getAttribute() : field.getExpression();
                    String[] attrNames = null;
                    if (field.getFilterAttributes() != null) {
                        if (field.getFilterAttributes().contains(",")) {
                            attrNames = field.getFilterAttributes().split(",");
                        } else {
                            attrName = field.getFilterAttributes();
                        }
                    }
                    if (attrNames != null) {
                        exec = addInsideRangeFilter(entry, _query, _type, attrNames, field);
                    } else {
                        exec = addFilter(entry, _query, _type, attrName, field);
                    }
                    if (!exec) {
                        break;
                    }
                }
            }
        }
        final List<Instance> instances = new ArrayList<Instance>();
        if (exec) {
            _query.addSelect("OID");
            _query.executeWithoutAccessCheck();
            while (_query.next()) {
                final String oid = (String) _query.get("OID");
                if (oid != null) {
                    instances.add(Instance.get(oid));
                }
            }
        }
        return instances;
    }

    /**
     * Method to add a Filter for a inside Range of two attributes.
     * @param _entry        entry to be evaluated
     * @param _query        query the filter will be applied to
     * @param _type         type for the query
     * @param _attrNames    names of the attributes
     * @param _field        field the filter belongs to
     * @return true if the query must be executed, else false
     * @throws EFapsException on error
     */
    protected boolean addInsideRangeFilter(final Entry<?, ?> _entry,
                                           final SearchQuery _query,
                                           final Type _type,
                                           final String[] _attrNames,
                                           final Field _field)
        throws EFapsException
    {
        boolean ret = true;
        final Attribute attr1 = _type.getAttribute(_attrNames[0]);
        final UUID attrTypeUUId = attr1.getAttributeType().getUUID();
        final Map<?, ?> inner = (Map<?, ?>) _entry.getValue();
        final String from = (String) inner.get("from");
        final String to = (String) inner.get("to");
        if ((from == null || to == null) && _field.getFilterDefault() == null) {
            ret = false;
        } else {
            // Date or DateTime
            if (UUID.fromString("68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e").equals(attrTypeUUId)
                            || UUID.fromString("e764db0f-70f2-4cd4-b2fe-d23d3da72f78").equals(attrTypeUUId)) {
                final DateTimeType dateType = new DateTimeType();
                DateTime dateFrom = null;
                DateTime dateTo = null;
                if (from == null || to == null) {
                    if ("today".equalsIgnoreCase(_field.getFilterDefault())) {
                        dateType.set(new DateTime[] { new DateTime() });
                        dateFrom = dateType.getValue().toDateMidnight().toDateTime().plusSeconds(1);
                        dateTo = dateFrom.plusDays(1).minusSeconds(1);
                    } else if ("week".equalsIgnoreCase(_field.getFilterDefault())) {
                        dateType.set(new DateTime[] { new DateTime() });
                        DateMidnight tmp = dateType.getValue().toDateMidnight();
                        tmp = tmp.minusDays(tmp.getDayOfWeek() - 1);
                        dateFrom = tmp.toDateTime().plusSeconds(1);
                        dateTo = tmp.toDateTime().plusWeeks(1).minusSeconds(1);
                    } else if ("month".equalsIgnoreCase(_field.getFilterDefault())) {
                        dateType.set(new DateTime[] { new DateTime() });
                        DateMidnight tmp = dateType.getValue().toDateMidnight();
                        tmp = tmp.minusDays(tmp.getDayOfMonth() - 1);
                        dateFrom = tmp.toDateTime().plusSeconds(1);
                        dateTo = tmp.toDateTime().plusMonths(1).minusSeconds(1);
                    }
                } else {
                    dateType.set(new String[] { from });
                    dateFrom = dateType.getValue().plusSeconds(1);
                    dateType.set(new String[] { to });
                    dateTo = dateType.getValue().plusDays(1).minusSeconds(1);
                }
                _query.addWhereExprLessValue(_attrNames[0], dateFrom);
                _query.addWhereExprGreaterValue(_attrNames[1], dateTo);
            }
        }
        return ret;
    }

    /**
     * Method to add a Filter for one attribute.
     * @param _entry    entry to be evaluated
     * @param _query    query the filter will be applied to
     * @param _type     type for the query
     * @param _attrName name of the attribute
     * @param _field    field the filter belongs to
     * @return true if the query must be executed, else false
     * @throws EFapsException on error
     */
    protected boolean addFilter(final Entry<?, ?> _entry,
                                final SearchQuery _query,
                                final Type _type,
                                final String _attrName,
                                final Field _field)
        throws EFapsException
    {
        boolean ret = true;
        final Attribute attr = _type.getAttribute(_attrName);
        final UUID attrTypeUUId = attr.getAttributeType().getUUID();
        final Map<?, ?> inner = (Map<?, ?>) _entry.getValue();
        final String from = (String) inner.get("from");
        final String to = (String) inner.get("to");
        if ((from == null || to == null) && _field.getFilterDefault() == null) {
            ret = false;
        } else {
            // Date or DateTime
            if (UUID.fromString("68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e").equals(attrTypeUUId)
                            || UUID.fromString("e764db0f-70f2-4cd4-b2fe-d23d3da72f78").equals(attrTypeUUId)) {
                final DateTimeType dateType = new DateTimeType();
                DateTime dateFrom = null;
                DateTime dateTo = null;
                if (from == null || to == null) {
                    if ("today".equalsIgnoreCase(_field.getFilterDefault())) {
                        dateType.set(new DateTime[] { new DateTime() });
                        dateFrom = dateType.getValue().toDateMidnight().toDateTime().minusSeconds(1);
                        dateTo = dateFrom.plusDays(1).plusSeconds(1);
                    } else if ("week".equalsIgnoreCase(_field.getFilterDefault())) {
                        dateType.set(new DateTime[] { new DateTime() });
                        DateMidnight tmp = dateType.getValue().toDateMidnight();
                        tmp = tmp.minusDays(tmp.getDayOfWeek() - 1);
                        dateFrom = tmp.toDateTime().minusSeconds(1);
                        dateTo = tmp.toDateTime().plusWeeks(1);
                    } else if ("month".equalsIgnoreCase(_field.getFilterDefault())) {
                        dateType.set(new DateTime[] { new DateTime() });
                        DateMidnight tmp = dateType.getValue().toDateMidnight();
                        tmp = tmp.minusDays(tmp.getDayOfMonth() - 1);
                        dateFrom = tmp.toDateTime().minusSeconds(1);
                        dateTo = tmp.toDateTime().plusMonths(1);
                    }
                } else {
                    dateType.set(new String[] { from });
                    dateFrom = dateType.getValue().minusSeconds(1);
                    dateType.set(new String[] { to });
                    dateTo = dateType.getValue().plusDays(1);
                }
                _query.addWhereExprGreaterValue(_attrName, dateFrom);
                _query.addWhereExprLessValue(_attrName, dateTo);
            }
        }
        return ret;
    }
}
