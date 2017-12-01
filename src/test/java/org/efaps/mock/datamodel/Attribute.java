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
 * The Class Attribute.
 */
public class Attribute extends AbstractType
{

    /** The Constant SQL. */
    private static final String SQL = "select ID,NAME,TYPEID,DMTABLE,DMATTRIBUTETYPE,DMTYPELINK,PARENTSET,SQLCOLUMN,"
                    + "DEFAULTVAL,DIMENSION,CLASSNAME from V_ADMINATTRIBUTE T0 where T0.DMTYPE = ?";

    /** The data model type id. */
    private final Long dataModelTypeId;

    /** The sql table id. */
    private final Long sqlTableId;

    /** The attribute type id. */
    private final Long attributeTypeId;

    /**
     * Instantiates a new attribute.
     *
     * @param _builder the builder
     */
    private Attribute(final AttributeBuilder _builder) {
        super(_builder);
        this.dataModelTypeId = _builder.dataModelTypeId;
        this.sqlTableId = _builder.sqlTableId;
        this.attributeTypeId = _builder.attributeTypeId;
    }

    @Override
    public QueryResult getResult() {
        return RowLists.rowList11(Long.class, String.class, Long.class, Long.class, Long.class, Long.class, Long.class,
                        String.class, String.class,Long.class, String.class)
                        .append(getId(), getName(), Long.valueOf(123), this.sqlTableId, this.attributeTypeId,
                                        Long.valueOf(123), null, "column", null, null, null)
                        .asResult();
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
            ret = this.dataModelTypeId.equals(parameter.right);
        }
        return ret;
    }

    /**
     * Builder.
     *
     * @return the attribute builder
     */
    public static AttributeBuilder builder() {
        return new AttributeBuilder();
    }

    /**
     * The Class AttributeBuilder.
     */
    public static class AttributeBuilder extends AbstractBuilder<AttributeBuilder> {

        /** The data model type id. */
        private Long dataModelTypeId;

        /** The sql table id. */
        private Long sqlTableId;

        /** The attribute type id. */
        private Long attributeTypeId;

        /**
         * With data model type id.
         *
         * @param _dataModelTypeId the data model type id
         * @return the attribute builder
         */
        public AttributeBuilder withDataModelTypeId(final Long _dataModelTypeId)
        {
            this.dataModelTypeId = _dataModelTypeId;
            return this;
        }

        /**
         * With sql table id.
         *
         * @param _sqlTableId the sql table id
         * @return the attribute builder
         */
        public AttributeBuilder withSqlTableId(final Long _sqlTableId)
        {
            this.sqlTableId = _sqlTableId;
            return this;
        }

        /**
         * With attribute type id.
         *
         * @param _attributeTypeId the attribute type id
         * @return the attribute builder
         */
        public AttributeBuilder withAttributeTypeId(final Long _attributeTypeId)
        {
            this.attributeTypeId = _attributeTypeId;
            return this;
        }

        /**
         * Builds the.
         *
         * @return the attribute
         */
        public Attribute build() {
            final Attribute ret = new Attribute(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}
