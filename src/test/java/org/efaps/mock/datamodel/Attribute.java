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

import org.efaps.test.AbstractTest;
import org.efaps.test.EFapsQueryHandler;
import org.efaps.test.IResult;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class Attribute.
 */
public class Attribute
    extends AbstractType
{
    /** The Constant SQL. */
    private static final String SQL = "select ID,NAME,TYPEID,DMTABLE,DMATTRIBUTETYPE,DMTYPELINK,PARENTSET,SQLCOLUMN,"
                    + "DEFAULTVAL,DIMENSION,CLASSNAME from V_ADMINATTRIBUTE T0 where T0.DMTYPE = ?";

    private static final String SQL4ATTR2TYPE = "select DMTYPE from V_ADMINATTRIBUTE T0 where T0.ID = ?";

    /** The data model type id. */
    private final Long dataModelTypeId;

    /** The sql table id. */
    private final Long sqlTableId;

    /** The attribute type id. */
    private final Long attributeTypeId;

    /** The attribute type id. */
    private final Long typeId;
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
        this.typeId = _builder.typeId;
    }

    @Override
    public QueryResult getResult() {
        return RowLists.rowList11(Long.class, String.class, Long.class, Long.class, Long.class, Long.class, Long.class,
                        String.class, String.class, Long.class, String.class)
                        .append(getId(), getName(), this.typeId, this.sqlTableId, this.attributeTypeId,
                                        Long.valueOf(123), null, "column", null, null, null)
                        .asResult();
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
            ret = this.dataModelTypeId.equals(parameter.right);
        }
        return ret;
    }

    public Long getDataModelTypeId()
    {
        return this.dataModelTypeId;
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
    public static class AttributeBuilder
        extends AbstractBuilder<AttributeBuilder>
    {

        /** The data model type id. */
        private Long dataModelTypeId;

        /** The sql table id. */
        private Long sqlTableId;

        /** The attribute type id. */
        private Long attributeTypeId;

        /** The type id. */
        private Long typeId = AbstractTest.TYPE_Attribute.getId();

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
         * With attribute type id.
         *
         * @param _attributeTypeId the attribute type id
         * @return the attribute builder
         */
        public AttributeBuilder withTypeId(final Long _typeId)
        {
            this.typeId = _typeId;
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
            final Attribute2Type attr2Type = new Attribute2Type(this);
            EFapsQueryHandler.get().register(attr2Type);
            return ret;
        }
    }

    /**
     * The Class Attribute2Type.
     */
    private static class Attribute2Type
        implements IResult
    {

        /** The id. */
        private final Long id;

        /** The data model type id. */
        private final Long dataModelTypeId;

        /**
         * Instantiates a new attribute two type.
         *
         * @param _builder the builder
         */
        private Attribute2Type(final AttributeBuilder _builder) {
            this.id = _builder.id;
            this.dataModelTypeId = _builder.dataModelTypeId;
        }

        @Override
        public String[] getSqls()
        {
            return new String[] { SQL4ATTR2TYPE };
        }

        @Override
        public boolean applies(final String _sql, final List<Parameter> _parameters)
        {
            boolean ret = false;
            if (_parameters.size() == 1) {
                final Parameter parameter = _parameters.get(0);
                ret = this.id.equals(parameter.right);
            }
            return ret;
        }

        @Override
        public QueryResult getResult()
        {
            return RowLists.rowList1(Long.class)
                            .append(this.dataModelTypeId)
                            .asResult();
        }
    }
}
