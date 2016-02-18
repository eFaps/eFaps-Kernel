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

package org.efaps.eql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.IBitEnum;
import org.efaps.eql.stmt.IPrintStmt;
import org.efaps.eql.stmt.parts.select.AbstractSelect;
import org.efaps.json.data.AbstractValue;
import org.efaps.json.data.BooleanValue;
import org.efaps.json.data.DataList;
import org.efaps.json.data.DateTimeValue;
import org.efaps.json.data.DecimalValue;
import org.efaps.json.data.IntegerValue;
import org.efaps.json.data.LongValue;
import org.efaps.json.data.NullValue;
import org.efaps.json.data.ObjectData;
import org.efaps.json.data.StringListValue;
import org.efaps.json.data.StringValue;
import org.efaps.json.data.UUIDValue;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public final class JSONData
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JSONData.class);

    /**
     * Singelton Constructor.
     */
    private JSONData()
    {
    }

    /**
     * @param _printStmt Statement the datalist will be created for
     * @return a DataList
     * @throws EFapsException on error
     */
    @SuppressWarnings("checkstyle:illegalcatch")
    public static DataList getDataList(final IPrintStmt _printStmt)
        throws EFapsException
    {
        final DataList ret = new DataList();
        if (_printStmt instanceof PrintStmt) {
            try {
                final PrintStmt printStmt = (PrintStmt) _printStmt;
                final Map<String, AbstractSelect> mapping = printStmt.getAlias2Selects();
                for (final Map<String, Object> map : printStmt.getData()) {
                    final ObjectData data = new ObjectData();
                    for (final Entry<String, AbstractSelect> entry : mapping.entrySet()) {
                        final Object obj = map.get(entry.getKey());
                        data.getValues().add(getValue(entry.getKey(), obj));
                    }
                    ret.add(data);
                }

                final Map<String, Boolean> sortMap = printStmt.getSortKey2desc();
                if (!sortMap.isEmpty()) {
                    final ComparatorChain<ObjectData> comparator = new ComparatorChain<>();
                    for (final Entry<String, Boolean> entry : sortMap.entrySet()) {
                        AbstractValue<?> sortVal = null;
                        if (StringUtils.isNumeric(entry.getKey())) {
                            final int idx = Integer.parseInt(entry.getKey());
                            sortVal = ret.get(0).getValues().get(idx - 1);
                        } else {
                            for (final AbstractValue<?> val : ret.get(0).getValues()) {
                                if (val.getKey().equals(entry.getKey())) {
                                    sortVal = val;
                                    break;
                                }
                            }
                        }
                        comparator.addComparator(new ObjectDataComparator(sortVal, !entry.getValue()));
                    }
                    Collections.sort(ret, comparator);
                }
                LOG.debug("Generated DataList: {}", ret);
            } catch (final Exception e) {
                if (e instanceof EFapsException) {
                    throw (EFapsException) e;
                } else {
                    throw new EFapsException("Could not create JSONData", e);
                }
            }
        }
        return ret;
    }

    /**
     * @param _key key the value is wanted for
     * @param _object oebjct to be converted
     * @return AbstractValue for the key
     */
    @SuppressWarnings("unchecked")
    public static AbstractValue<?> getValue(final String _key,
                                            final Object _object)
    {
        AbstractValue<? extends Object> ret = null;
        if (_object instanceof String) {
            ret = new StringValue().setValue((String) _object);
        } else if (_object instanceof BigDecimal) {
            ret = new DecimalValue().setValue((BigDecimal) _object);
        } else if (_object instanceof Long) {
            ret = new LongValue().setValue((Long) _object);
        } else if (_object instanceof Integer) {
            ret = new IntegerValue().setValue((Integer) _object);
        } else if (_object instanceof DateTime) {
            ret = new DateTimeValue().setValue((DateTime) _object);
        } else if (_object instanceof UUID) {
            ret = new UUIDValue().setValue((UUID) _object);
        } else if (_object instanceof Boolean) {
            ret = new BooleanValue().setValue((Boolean) _object);
        } else if (_object instanceof List) {
            final List<?> list = (List<?>) _object;
            if (!list.isEmpty()) {
                final Object inner = list.get(0);
                if (inner instanceof String) {
                    ret = new StringListValue().setValue((List<String>) list);
                } else if (inner instanceof IBitEnum) {
                    final List<String> tmpList = new ArrayList<>();
                    for (final Object obj : list) {
                        tmpList.add(obj.toString());
                    }
                    ret = new StringListValue().setValue(tmpList);
                }
            }
        } else if (_object != null) {
            ret = new StringValue().setValue(_object.toString());
        } else {
            ret = new NullValue();
        }
        if (ret != null) {
            ret.setKey(_key);
        }
        return ret;
    }

    /**
     * The Class ObjectDataComparator.
     *
     */
    public static class ObjectDataComparator
        implements Comparator<ObjectData>
    {

        /** The sort val. */
        private final AbstractValue<?> sortVal;

        /** The asc. */
        private final boolean asc;

        /**
         * Instantiates a new object data comparator.
         *
         * @param _sortVal the _sort val
         * @param _asc the _asc
         */
        public ObjectDataComparator(final AbstractValue<?> _sortVal,
                                    final boolean _asc)
        {
            this.sortVal = _sortVal;
            this.asc = _asc;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public int compare(final ObjectData _o1,
                           final ObjectData _o2)
        {
            int ret = 0;
            final Object data1 = isAsc() ? getValue(_o1) : getValue(_o2);
            final Object data2 = isAsc() ? getValue(_o2) : getValue(_o1);
            if (data1 == null && data2 != null && data2 instanceof Comparable
                            || data2 == null && data1 != null && data1 instanceof Comparable
                            || data1 != null && data1 instanceof Comparable && data2 != null
                            && data2 instanceof Comparable) {
                ret = ObjectUtils.compare((Comparable) data1, (Comparable) data2);
            }
            return ret;
        }

        /**
         * Gets the value.
         *
         * @param _data the _data
         * @return the value
         */
        protected Object getValue(final ObjectData _data)
        {
            Object ret = null;
            for (final AbstractValue<?> val : _data.getValues()) {
                if (val.getKey().equals(getSortVal().getKey())) {
                    ret = val.getValue();
                    break;
                }
            }
            return ret;
        }

        /**
         * Getter method for the instance variable {@link #sortVal}.
         *
         * @return value of instance variable {@link #sortVal}
         */
        public AbstractValue<?> getSortVal()
        {
            return this.sortVal;
        }

        /**
         * Getter method for the instance variable {@link #asc}.
         *
         * @return value of instance variable {@link #asc}
         */
        public Boolean isAsc()
        {
            return this.asc;
        }
    }
}
