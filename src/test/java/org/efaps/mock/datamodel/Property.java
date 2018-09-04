/*
 * Copyright 2003 - 2018 The eFaps Team
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.efaps.test.EFapsQueryHandler;
import org.efaps.test.IMockResult;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class Property.
 */
public class Property
    implements IMockResult
{

    /** The Constant SQL. */
    private static final String SQL = "select NAME,VALUE from T_CMPROPERTY where ABSTRACT = ?";

    private final String name;
    private final String value;
    private final Long abstractId;

    public Property(final PropertyBuilder _builder)
    {
        this.name = _builder.name;
        this.value = _builder.value;
        this.abstractId = _builder.abstractId;
    }

    @Override
    public String[] getSqls()
    {
        return new String[] { SQL };
    }

    @Override
    public boolean applies(final String _sql, final List<Parameter> _parameters)
    {
        boolean ret = false;
        if (_parameters.size() == 1) {
            final Parameter parameter = _parameters.get(0);
            ret = this.abstractId.equals(parameter.right);
        }
        return ret;
    }

    @Override
    public QueryResult getResult()
    {
        return RowLists.rowList2(String.class, String.class)
                        .append(this.name, this.value)
                        .asResult();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * Builder.
     *
     * @return the type builder
     */
    public static PropertyBuilder builder()
    {
        return new PropertyBuilder();
    }

    /**
     * The Class TypeBuilder.
     */
    public static class PropertyBuilder
    {

        private String name;
        private String value;
        private Long abstractId;

        public PropertyBuilder withName(final String _name)
        {
            this.name = _name;
            return this;
        }

        public PropertyBuilder withValue(final String _value)
        {
            this.value = _value;
            return this;
        }

        public PropertyBuilder withAbstractId(final Long _abstractId)
        {
            this.abstractId = _abstractId;
            return this;
        }

        public Property build()
        {
            final Property ret = new Property(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}
