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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.efaps.test.EFapsQueryHandler;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.RowList2.Impl;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class EventDefinition.
 */
public final class EventDefinition
    extends AbstractType
{

    /** The object link. */
    private final Long objectLink;

    /** The inst ids. */
    private final List<Long> instIds;

    /** The type ids. */
    private final List<Long> typeIds;

    /** The inst sql. */
    private final String instSql;

    /** The value sql. */
    private final String valueSql;

    /** The esjp. */
    private final String esjp;

    /** The method. */
    private final String method;

    /** The inst. */
    private boolean inst;

    /**
     * Instantiates a new event definition.
     *
     * @param _builder the builder
     */
    private EventDefinition(final EventDefinitionBuilder _builder)
    {
        super(_builder);
        this.objectLink = _builder.objectLink;
        this.esjp = _builder.esjp;
        this.method = _builder.method;
        this.instIds = _builder.instIds;
        this.typeIds = _builder.typeIds;
        final List<Long> types = Arrays.asList(IDataModel.Admin_Event_Definition.getId(),
                        IDataModel.Admin_DataModel_TypeAccessCheckEvent.getId());
        final StringBuilder instSqlBldr = new StringBuilder()
                .append(String.format("select T0.ID,T0.TYPEID from %s T0 left join %s T1 on T0.ID=T1.ID "
                        + "where ( ( T1.Abstract_COL = %s ) "
                        + "and T0.Type_COL in ( ",
                        IDataModel.Admin_DataModel_SQLTable.getSqlTableName(),
                        IDataModel.Admin_Event_DefinitionSQLTable.getSqlTableName(),
                        this.objectLink));
        instSqlBldr.append(StringUtils.join(types, " , ")).append(" ) )");
        this.instSql = instSqlBldr.toString();

        final StringBuilder valueSqlBldr = new StringBuilder()
            .append(String.format("select T0.ID,T2.ID,T2.TYPEID,T0.TYPEID,T2.Name_COL,T0.Type_COL,T0.Name_COL,"
                            + "T1.IndexPosition_COL,T1.Method_COL "
                        + "from T_DMTABLE T0 left join T_EVENTDEF T1 on T0.ID=T1.ID "
                        + "left join T_DMTABLE T2 on T1.%s=T2.ID "
                        + "where T0.ID in ( ",
                        IDataModel.JavaProgAttr.getSQLColumnName()));
        valueSqlBldr.append(StringUtils.join(this.instIds, " , ")).append(" )");
        this.valueSql = valueSqlBldr.toString();
    }

    @Override
    public String[] getSqls()
    {
        return new String[] { this.instSql, this.valueSql };
    }

    @Override
    public boolean applies(final String _sql, final List<Parameter> _parameters)
    {
        boolean ret = false;
        if (_sql.equals(this.instSql)) {
            this.inst = true;
            ret = true;
        } else if (_sql.equals(this.valueSql)) {
            this.inst = false;
            ret = true;
        }
        return ret;
    }

    @Override
    public QueryResult getResult()
    {
        return this.inst ? getResult4Instance() : getResult4Value();
    }

    /**
     * Gets the result for instance.
     *
     * @return the result for instance
     */
    public QueryResult getResult4Value()
    {
        return RowLists.rowList9(Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                        Object.class, Object.class, Object.class)
             .append(1L, 1L, 1L, this.typeIds.iterator().next(), this.esjp, this.typeIds.iterator().next(),
                             "Name2", 0, this.method)
            .asResult();
    }

    /**
     * Gets the result for instance.
     *
     * @return the result for instance
     */
    public QueryResult getResult4Instance()
    {
        Impl<Object, Object> rr = RowLists.rowList2(Object.class, Object.class);
        final Iterator<Long> iter = this.typeIds.iterator();
        for (final Long instId : this.instIds) {
            rr = rr.append(instId, iter.next());
        }
        return rr.asResult();
    }

    /**
     * Builder.
     *
     * @return the event definition builder
     */
    public static EventDefinitionBuilder builder()
    {
        return new EventDefinitionBuilder();
    }

    /**
     * The Class TypeBuilder.
     */
    public static class EventDefinitionBuilder
        extends AbstractBuilder<EventDefinitionBuilder>
    {

        /** The object link. */
        private Long objectLink;

        /** The inst ids. */
        private final List<Long> instIds = new ArrayList<>();

        /** The inst ids. */
        private final List<Long> typeIds = new ArrayList<>();

        /** The esjp. */
        private String esjp;

        /** The method. */
        private String method;

        /**
         * With object link.
         *
         * @param _objectLink the object link
         * @return the event definition builder
         */
        public EventDefinitionBuilder withObjectLink(final Long _objectLink)
        {
            this.objectLink = _objectLink;
            return this;
        }

        /**
         * With inst id.
         *
         * @param _instIds the inst ids
         * @return the event definition builder
         */
        public EventDefinitionBuilder withInstId(final Long... _instIds)
        {
            for (final Long instId : _instIds) {
                this.instIds.add(instId);
            }
            return this;
        }

        /**
         * With inst id.
         *
         * @param _instIds the inst ids
         * @return the event definition builder
         */
        public EventDefinitionBuilder withTypeId(final Long... _instIds)
        {
            for (final Long instId : _instIds) {
                this.typeIds.add(instId);
            }
            return this;
        }

        /**
         * With object link.
         *
         * @param _esjp the esjp
         * @return the event definition builder
         */
        public EventDefinitionBuilder withESJP(final String _esjp)
        {
            this.esjp = _esjp;
            return this;
        }

        /**
         * With object link.
         *
         * @param _method the method
         * @return the event definition builder
         */
        public EventDefinitionBuilder withMethod(final String _method)
        {
            this.method = _method;
            return this;
        }

        /**
         * Builds the.
         *
         * @return the event definition
         */
        public EventDefinition build()
        {
            final EventDefinition ret = new EventDefinition(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}
