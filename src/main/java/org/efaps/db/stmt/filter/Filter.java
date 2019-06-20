/*
 * Copyright 2003 - 2019 The eFaps Team
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

package org.efaps.db.stmt.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.LongType;
import org.efaps.admin.datamodel.attributetype.StatusType;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLWhere;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.eql2.Comparison;
import org.efaps.eql2.Connection;
import org.efaps.eql2.IAttributeSelectElement;
import org.efaps.eql2.IBaseSelectElement;
import org.efaps.eql2.ISelectElement;
import org.efaps.eql2.IWhere;
import org.efaps.eql2.IWhereElement;
import org.efaps.eql2.IWhereElementTerm;
import org.efaps.eql2.IWhereSelect;
import org.efaps.eql2.IWhereTerm;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Filter.
 */
public class Filter
{
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Filter.class);

    /** The i where. */
    private IWhere iWhere;

    /** The types. */
    private List<Type> types;

    /**
     * Analyze.
     *
     * @param _where the where
     * @param _types the types
     * @return the filter
     */
    private Filter analyze(final IWhere _where, final List<Type> _types)
    {
        iWhere = _where;
        types = _types;
        return this;
    }

    /**
     * Append two SQL select.
     *
     * @param _sqlSelect the sql select
     */
    public void append2SQLSelect(final SQLSelect _sqlSelect, final Set<TypeCriterion> _typeCriteria)
    {
        if (iWhere != null) {
            final SQLWhere sqlWhere = _sqlSelect.getWhere();
            for (final IWhereTerm<?> term : iWhere.getTerms()) {
                if (term instanceof IWhereElementTerm) {
                    final IWhereElement element = ((IWhereElementTerm) term).getElement();
                    if (element.getAttribute() != null)
                    {
                        final String attrName = element.getAttribute();
                        for (final Type type : types) {
                            final Attribute attr = type.getAttribute(attrName);
                            if (attr != null) {
                                addAttr(_sqlSelect, sqlWhere, attr, term, element);
                                break;
                            }
                        }
                    } else if (element.getSelect() != null) {
                        final IWhereSelect select = element.getSelect();
                        for (final ISelectElement ele : select.getElements()) {
                            if (ele instanceof IBaseSelectElement) {
                                switch (((IBaseSelectElement) ele).getElement()) {
                                    case STATUS:
                                        for (final Type type : types) {
                                            final Attribute attr = type.getStatusAttribute();
                                            if (attr != null) {
                                                addAttr(_sqlSelect, sqlWhere, attr, term, element);
                                                break;
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            } else if (ele instanceof IAttributeSelectElement) {
                                final String attrName = ((IAttributeSelectElement) ele).getName();
                                for (final Type type : types) {
                                    addAttr(_sqlSelect, sqlWhere, type.getAttribute(attrName), term, element);
                                }
                            }
                        }
                    }
                }
            }
        }
        addTypeCriteria(_sqlSelect, _typeCriteria);
    }

    protected void addAttr(final SQLSelect _sqlSelect, final SQLWhere _sqlWhere, final Attribute _attr,
                           final IWhereTerm<?> _term, final IWhereElement _element)
    {
        if (_attr != null) {
            final SQLTable table = _attr.getTable();
            final String tableName = table.getSqlTable();
            final TableIdx tableidx = _sqlSelect.getIndexer().getTableIdx(tableName);
            final IAttributeType attrType = _attr.getAttributeType().getDbAttrType();

            final boolean noEscape;
            final List<String> values;
            if (attrType instanceof StatusType) {
                values = _element.getValuesList().stream()
                                .map(val -> convertStatusValue(_attr, val))
                                .collect(Collectors.toList());
                noEscape = true;
            } else {
                noEscape = attrType instanceof LongType;
                values = Arrays.asList(_element.getValues());
            }
            _sqlWhere.addCriteria(tableidx.getIdx(), _attr.getSqlColNames(), _element.getComparison(),
                            new LinkedHashSet<>(values), !noEscape, _term.getConnection());
        }
    }

    protected String convertStatusValue(final Attribute _attr, final String _val)
    {
        String ret;
        if (StringUtils.isNumeric(_val)) {
            ret = _val;
        } else {
            Status status = null;
            try {
                status = Status.find(_attr.getLink().getUUID(), _val);
            } catch (final CacheReloadException e) {
                LOG.error("Cathed error:", e);
            } finally {
                if (status == null) {
                    LOG.warn("No Status could be found for the given key {} on {}", _val, _attr);
                }
                ret = status == null ? _val : String.valueOf(status.getId());
            }
        }
        return ret;
    }

    protected void addTypeCriteria(final SQLSelect _sqlSelect,
                                   final Set<TypeCriterion> _typeCriteria)
    {
        if (!_typeCriteria.isEmpty()) {

            final ComparatorChain<TypeCriterion> chain = new ComparatorChain<>();
            chain.addComparator((_criterion1,
                                 _criterion2) -> _criterion1.getTableIndex().compareTo(_criterion2.getTableIndex()));
            chain.addComparator((_criterion1,
                                _criterion2) -> Long.compare(_criterion1.getTypeId(), _criterion2.getTypeId()));

            final SQLWhere where = _sqlSelect.getWhere();
            _typeCriteria.stream()
                .sorted(chain)
                .collect(Collectors.groupingBy(TypeCriterion::getTableIndex))
                .forEach((index, criteria) -> {
                    final boolean nullable = criteria.stream()
                                    .filter(TypeCriterion::isNullable)
                                    .findAny()
                                    .isPresent();

                    if (nullable) {

                    } else {
                        final Set<String> values = new LinkedHashSet<>();
                        criteria.stream()
                            .map(citerion -> String.valueOf(citerion.getTypeId()))
                            .forEach(typeId -> values.add(typeId));

                        where.addCriteria(index.intValue(),Collections.singletonList(criteria.get(0).getSqlColType()),
                                        Comparison.EQUAL, values, false, Connection.AND);
                    }
                    /*
                    final Group group = new Group().setConnection(Connection.AND);
                    group.add(new Criteria()
                                    .tableIndex(tableidx.getIdx())
                                    .colName(((SQLTable) getTable()).getSqlColType())
                                    .comparison(Comparison.EQUAL)
                                    .value(String.valueOf(getAttributeSet().getId()))
                                    .connection(Connection.OR));
                    group.add(new Criteria()
                                    .tableIndex(tableidx.getIdx())
                                    .colName(((SQLTable) getTable()).getSqlColType())
                                    .comparison(Comparison.EQUAL)
                                    .connection(Connection.OR));
                    where.section(group);
                    */
            });
        }

    }


    /**
     * Gets the.
     *
     * @param _where the where
     * @param _baseTypes the base types
     * @return the selection
     * @throws CacheReloadException the cache reload exception
     */
    public static Filter get(final IWhere _where, final Type... _baseTypes)
        throws CacheReloadException
    {
        return new Filter().analyze(_where, Arrays.asList(_baseTypes));
    }
}
