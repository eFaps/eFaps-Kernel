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
 * The Class Person.
 */
public final class Person
    extends AbstractType
{

    /** The Constant SQL. */
    private static final String SQL = "select ID,UUID,NAME,STATUS from V_USERPERSON T0 where T0.NAME = ?";

    /**
     * Instantiates a new person.
     *
     * @param _builder the builder
     */
    private Person(final PersonBuilder _builder)
    {
        super(_builder);
    }

    @Override
    public String[] getSqls()
    {
        return new String[] { SQL };
    }

    @Override
    public QueryResult getResult()
    {
        return RowLists.rowList4(Long.class, String.class, String.class, Boolean.class)
                        .append(getId(), getUuid().toString(), getName(), true)
                        .asResult();
    }

    @Override
    public boolean applies(final String _sql, final List<Parameter> _parameters)
    {
        boolean ret = false;
        if (_parameters.size() == 1) {
            final Parameter parameter = _parameters.get(0);
            ret = getName().equals(parameter.right);
        }
        return ret;
    }

    /**
     * Builder.
     *
     * @return the person builder
     */
    public static PersonBuilder builder()
    {
        return new PersonBuilder();
    }

    /**
     * The Class PersonBuilder.
     */
    public static class PersonBuilder
        extends AbstractBuilder<PersonBuilder>
    {

        /**
         * Builds the.
         *
         * @return the person
         */
        public Person build()
        {
            final Person ret = new Person(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}
