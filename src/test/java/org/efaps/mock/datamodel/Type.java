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

import org.efaps.test.EFapsQueryHandler;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class Type.
 */
public final class Type
    extends AbstractType
{

    /** The Constant SQL. */
    private static final String SQLID = "select ID,UUID,NAME,PURPOSE,PARENTDMTYPE,PARENTCLASSDMTYPE "
                    + "from V_ADMINTYPE T0 where T0.ID = ?";

    /** The Constant SQL. */
    private static final String SQLUUID = "select ID,UUID,NAME,PURPOSE,PARENTDMTYPE,PARENTCLASSDMTYPE "
                    + "from V_ADMINTYPE T0 where T0.UUID = ?";

    /** The Constant SQL. */
    private static final String SQLNAME = "select ID,UUID,NAME,PURPOSE,PARENTDMTYPE,PARENTCLASSDMTYPE "
                    + "from V_ADMINTYPE T0 where T0.NAME = ?";

    /** The Constant SQLCHILDREN. */
    private static final String SQLCHILDREN = "select ID,PURPOSE from V_ADMINTYPE T0 where T0.PARENTDMTYPE = ?";


    /** The purpose id. */
    private final Integer purposeId;

    /** The purpose type id. */
    private final Long parentTypeId;

    /** The children. */
    private boolean children = false;

    /**
     * Instantiates a new type.
     *
     * @param _builder the builder
     */
    private Type(final TypeBuilder _builder)
    {
        super(_builder);
        this.purposeId = _builder.purposeId;
        this.parentTypeId = _builder.parentTypeId;
    }

    @Override
    public QueryResult getResult()
    {
        final QueryResult ret;
        if (this.children) {
            ret = RowLists.rowList2(Long.class, Integer.class)
                            .append(getId(), this.purposeId)
                            .asResult();
        } else {
            ret =  RowLists.rowList6(Long.class, String.class, String.class, Integer.class, Long.class, Long.class)
                        .append(getId(), getUuid().toString(), getName(), this.purposeId, this.parentTypeId, null)
                        .asResult();
        }
        return ret;
    }

    @Override
    public String[] getSqls()
    {
        return new String[] { SQLID, SQLUUID, SQLNAME, SQLCHILDREN };
    }

    @Override
    public boolean applies(final String _sql, final List<Parameter> _parameters)
    {
        boolean ret = false;
        if (_parameters.size() == 1) {
            final Parameter parameter = _parameters.get(0);
            this.children = false;
            if (SQLID.equals(_sql)) {
                ret = getId().equals(parameter.right);
            } else if (SQLUUID.equals(_sql)) {
                ret = getUuid().toString().equals(parameter.right);
            } else if (SQLCHILDREN.equals(_sql)) {
                ret = this.parentTypeId != null && this.parentTypeId.equals(parameter.right);
                this.children = true;
            } else {
                ret = getName().toString().equals(parameter.right);
            }
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
        /** The purpose id. */
        private Integer purposeId = 0;

        /** The purpose type id. */
        private Long parentTypeId;

        /**
         * With sql table id.
         *
         * @param _purposeId the purpose id
         * @return the attribute builder
         */
        public TypeBuilder withPurposeId(final Integer _purposeId)
        {
            this.purposeId = _purposeId;
            return this;
        }

        /**
         * With sql table id.
         *
         * @param _parentTypeId the parent type id
         * @return the attribute builder
         */
        public TypeBuilder withParentTypeId(final Long _parentTypeId)
        {
            this.parentTypeId = _parentTypeId;
            return this;
        }

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
