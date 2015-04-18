/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.eql;

import java.util.Iterator;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.QueryBuilder;
import org.efaps.eql.stmt.parts.AbstractQueryPart;
import org.efaps.eql.stmt.parts.AbstractQueryPart.Where;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public class QueryBldrUtil
{

    /**
     * @param _printStmt
     */
    public static List<Instance> getInstances(final AbstractQueryPart _queryPart)
        throws EFapsException
    {
        final Iterator<String> typeIter = _queryPart.getTypes().iterator();
        final QueryBuilder queryBldr = new QueryBuilder(Type.get(typeIter.next()));
        while (typeIter.hasNext()) {
            queryBldr.addType(Type.get(typeIter.next()));
        }
        for (final Where where : _queryPart.getWheres()) {
            if (where.getAttribute() != null) {
                switch (where.getComparison()) {
                    case IN:
                    case EQUAL:
                        queryBldr.addWhereAttrEqValue(where.getAttribute(), where.getValues().toArray());
                        break;
                    case GREATER:
                        queryBldr.addWhereAttrGreaterValue(where.getAttribute(), where.getValues().get(0));
                        break;
                    case LESS:
                        queryBldr.addWhereAttrLessValue(where.getAttribute(), where.getValues().get(0));
                        break;
                    case LIKE:
                        queryBldr.addWhereAttrMatchValue(where.getAttribute(), where.getValues().get(0));
                        break;
                    case UNEQUAL:
                        queryBldr.addWhereAttrNotEqValue(where.getAttribute(), where.getValues().toArray());
                        break;
                    default:
                        queryBldr.addWhereAttrEqValue("ID", 0);
                        break;
                }
            } else {
                switch (where.getComparison()) {
                    case EQUAL:
                        queryBldr.addWhereSelectEqValue(where.getSelect(), where.getValues().toArray());
                        break;
                    case GREATER:
                        queryBldr.addWhereSelectGreaterValue(where.getSelect(), where.getValues().get(0));
                        break;
                    case LESS:
                        queryBldr.addWhereSelectLessValue(where.getSelect(), where.getValues().get(0));
                        break;
                    case LIKE:
                        queryBldr.addWhereSelectMatchValue(where.getSelect(), where.getValues().get(0));
                        break;
                    case UNEQUAL:
                    case IN:
                    default:
                        queryBldr.addWhereAttrEqValue("ID", 0);
                        break;
                }
            }
        }
        queryBldr.setLimit(_queryPart.getLimit());
        return queryBldr.getQuery().execute();
    }
}
