/*
 * Copyright 2003 - 2017 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.efaps.mock.datamodel;

import java.util.List;

import org.efaps.test.EFapsQueryHandler;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class Type.
 */
public class Type
    extends AbstractType
{

    /** The Constant SQL. */
    private static final String SQL = "select ID,UUID,NAME,PURPOSE,PARENTDMTYPE,PARENTCLASSDMTYPE "
                    + "from V_ADMINTYPE T0 where T0.ID = ?";

    /**
     * Instantiates a new type.
     *
     * @param _builder the builder
     */
    private Type(final TypeBuilder _builder)
    {
        super(_builder);
    }

    @Override
    public QueryResult getResult()
    {
        return RowLists.rowList6(Long.class, String.class, String.class, Integer.class, Long.class, Long.class).append(
                        getId(), getUuid().toString(), getName(), 0, null, null).asResult();
    }

    @Override
    public String getSql()
    {
        return SQL;
    }

    @Override
    public boolean applies(final List<Parameter> _parameters)
    {
        boolean ret = false;
        if (_parameters.size() == 1) {
            final Parameter parameter = _parameters.get(0);
            ret = getId().equals(parameter.right);
        }
        return ret;
    }

    /**
     * Builder.
     *
     * @return the type builder
     */
    public static TypeBuilder builder()
    {
        return new TypeBuilder();
    }

    /**
     * The Class TypeBuilder.
     */
    public static class TypeBuilder
        extends AbstractBuilder<TypeBuilder>
    {

        /**
         * Builds the.
         *
         * @return the type
         */
        public Type build()
        {
            final Type ret = new Type(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}
