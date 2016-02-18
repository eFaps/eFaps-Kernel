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

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.QueryBuilder;
import org.efaps.eql.stmt.parts.AbstractNestedQueryStmtPart;
import org.efaps.eql.stmt.parts.AbstractQueryPart;
import org.efaps.eql.stmt.parts.AbstractQueryStmtPart;
import org.efaps.eql.stmt.parts.IQueryPart;
import org.efaps.eql.stmt.parts.where.AbstractWhere;
import org.efaps.eql.stmt.parts.where.AttrQueryWhere;
import org.efaps.eql.stmt.parts.where.AttributeWhere;
import org.efaps.eql.stmt.parts.where.SelectQueryWhere;
import org.efaps.eql.stmt.parts.where.SelectWhere;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public final class QueryBldrUtil
{

    /**
     * Singelton instance.
     */
    private QueryBldrUtil()
    {
    }

    /**
     * Gets the instances.
     *
     * @param _queryPart the _query part
     * @return the instances
     * @throws EFapsException on error
     */
    public static List<Instance> getInstances(final AbstractQueryPart _queryPart)
        throws EFapsException
    {
        return getQueryBldr(_queryPart).getQuery().execute();
    }


    /**
     * Recursive method to get a QueryBuilder.
     *
     * @param _queryPart the _query part
     * @return the query bldr
     * @throws EFapsException on error
     */
    private static QueryBuilder getQueryBldr(final IQueryPart _queryPart)
        throws EFapsException
    {
        final Iterator<String> typeIter = ((AbstractQueryPart) _queryPart).getTypes().iterator();
        String typeStr = typeIter.next();
        final QueryBuilder ret = new QueryBuilder(UUIDUtil.isUUID(typeStr) ? Type.get(UUID.fromString(typeStr))
                        : Type.get(typeStr));
        while (typeIter.hasNext()) {
            typeStr = typeIter.next();
            ret.addType(UUIDUtil.isUUID(typeStr) ? Type.get(UUID.fromString(typeStr)) : Type.get(typeStr));
        }
        for (final AbstractWhere where : ((AbstractQueryPart) _queryPart).getWheres()) {
            if (where instanceof AttributeWhere) {
                final AttributeWhere attrWhere = (AttributeWhere) where;
                switch (where.getComparison()) {
                    case IN:
                    case EQUAL:
                        ret.addWhereAttrEqValue(attrWhere.getAttribute(), attrWhere.getValues().toArray());
                        break;
                    case GREATER:
                        ret.addWhereAttrGreaterValue(attrWhere.getAttribute(), attrWhere.getValues().get(0));
                        break;
                    case LESS:
                        ret.addWhereAttrLessValue(attrWhere.getAttribute(), attrWhere.getValues().get(0));
                        break;
                    case LIKE:
                        ret.addWhereAttrMatchValue(attrWhere.getAttribute(), attrWhere.getValues().get(0));
                        break;
                    case UNEQUAL:
                    case NOTIN:
                        ret.addWhereAttrNotEqValue(attrWhere.getAttribute(), attrWhere.getValues().toArray());
                        break;
                    default:
                        ret.addWhereAttrEqValue("ID", 0);
                        break;
                }
            } else if (where instanceof SelectWhere) {
                final SelectWhere selWhere = (SelectWhere) where;
                switch (where.getComparison()) {
                    case EQUAL:
                        ret.addWhereSelectEqValue(selWhere.getSelect(), selWhere.getValues().toArray());
                        break;
                    case GREATER:
                        ret.addWhereSelectGreaterValue(selWhere.getSelect(), selWhere.getValues().get(0));
                        break;
                    case LESS:
                        ret.addWhereSelectLessValue(selWhere.getSelect(), selWhere.getValues().get(0));
                        break;
                    case LIKE:
                        ret.addWhereSelectMatchValue(selWhere.getSelect(), selWhere.getValues().get(0));
                        break;
                    case UNEQUAL:
                    case IN:
                    case NOTIN:
                    default:
                        ret.addWhereAttrEqValue("ID", 0);
                        break;
                }
            } else if (where instanceof AttrQueryWhere) {
                final AttrQueryWhere attrQueryWhere = (AttrQueryWhere) where;
                switch (where.getComparison()) {
                    case IN:
                        ret.addWhereAttrInQuery(attrQueryWhere.getAttribute(),
                                        getQueryBldr(attrQueryWhere.getQuery()).getAttributeQuery(
                                                        ((AbstractNestedQueryStmtPart) attrQueryWhere.getQuery())
                                                                        .getSelect()));
                        break;
                    case NOTIN:
                        ret.addWhereAttrNotInQuery(attrQueryWhere.getAttribute(),
                                        getQueryBldr(attrQueryWhere.getQuery()).getAttributeQuery(
                                                        ((AbstractNestedQueryStmtPart) attrQueryWhere.getQuery())
                                                                        .getSelect()));
                        break;
                    case EQUAL:
                    case GREATER:
                    case LESS:
                    case LIKE:
                    case UNEQUAL:
                    default:
                        ret.addWhereAttrEqValue("ID", 0);
                        break;
                }
            } else if (where instanceof SelectQueryWhere) {
                final SelectQueryWhere selQueryWhere = (SelectQueryWhere) where;
                switch (where.getComparison()) {
                    case IN:
                        ret.addWhereAttrInQuery(selQueryWhere.getSelect(),
                                        getQueryBldr(selQueryWhere.getQuery()).getAttributeQuery(
                                                        ((AbstractNestedQueryStmtPart) selQueryWhere.getQuery())
                                                                        .getSelect()));
                        break;
                    case NOTIN:
                        ret.addWhereAttrNotInQuery(selQueryWhere.getSelect(),
                                        getQueryBldr(selQueryWhere.getQuery()).getAttributeQuery(
                                                        ((AbstractNestedQueryStmtPart) selQueryWhere.getQuery())
                                                                        .getSelect()));
                        break;
                    case EQUAL:
                    case GREATER:
                    case LESS:
                    case LIKE:
                    case UNEQUAL:
                    default:
                        ret.addWhereAttrEqValue("ID", 0);
                        break;
                }
            }
        }
        if (_queryPart instanceof AbstractQueryStmtPart) {
            ret.setLimit(((AbstractQueryStmtPart) _queryPart).getLimit());
        }
        return ret;
    }
}
