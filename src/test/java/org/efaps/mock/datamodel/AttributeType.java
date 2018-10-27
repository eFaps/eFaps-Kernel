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
 * The Class AttributeType.
 */
public final class AttributeType
    extends AbstractType
{

    /** The Constant SQL. */
    private static final String SQL = "select ID,NAME,UUID,CLASSNAME,CLASSNAMEUI,ALWAYSUPDATE,CREATEUPDATE "
                    + "from V_DMATTRIBUTETYPE T0 where T0.ID = ?";

    /** The class name. */
    private final String className;

    /** The class name UI. */
    private final String classNameUI;

    /** The always update. */
    private boolean alwaysUpdate = false;

    /** The create update. */
    private boolean createUpdate = false;

    /**
     * Instantiates a new attribute type.
     *
     * @param _builder the builder
     */
    private AttributeType(final AttributeTypeBuilder _builder)
    {
        super(_builder);
        this.className = _builder.className;
        this.classNameUI = _builder.classNameUI;
        this.alwaysUpdate = _builder.alwaysUpdate;
        this.createUpdate = _builder.createUpdate;
    }

    @Override
    public QueryResult getResult()
    {
        return RowLists.rowList7(Long.class, String.class, String.class, String.class, String.class, Integer.class,
                        Integer.class).append(getId(), getName(), getUuid().toString(), this.className,
                                        this.classNameUI,
                                        this.alwaysUpdate ? 1 : null, this.createUpdate ? 1 : null).asResult();
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
            ret = getId().equals(parameter.right);
        }
        return ret;
    }

    /**
     * Builder.
     *
     * @return the attribute type builder
     */
    public static AttributeTypeBuilder builder()
    {
        return new AttributeTypeBuilder();
    }

    /**
     * The Class AttributeTypeBuilder.
     */
    public static class AttributeTypeBuilder
        extends AbstractBuilder<AttributeTypeBuilder>
    {

        /** The class name. */
        private String className;

        /** The class name UI. */
        private String classNameUI;

        /** The always update. */
        private boolean alwaysUpdate = false;

        /** The create update. */
        private boolean createUpdate = false;

        /**
         * With class name.
         *
         * @param _className the class name
         * @return the attribute type builder
         */
        public AttributeTypeBuilder withClassName(final String _className)
        {
            this.className = _className;
            return this;
        }

        /**
         * With class name UI.
         *
         * @param _classNameUI the class name UI
         * @return the attribute type builder
         */
        public AttributeTypeBuilder withClassNameUI(final String _classNameUI)
        {
            this.classNameUI = _classNameUI;
            return this;
        }

        /**
         * With always Update.
         *
         * @param _alwaysUpdate the always update
         * @return the attribute type builder
         */
        public AttributeTypeBuilder withAlwaysUpdate(final boolean _alwaysUpdate)
        {
            this.alwaysUpdate = _alwaysUpdate;
            return this;
        }

        /**
         * With create Update.
         *
         * @param _createUpdate the create update
         * @return the attribute type builder
         */
        public AttributeTypeBuilder withCreateUpdate(final boolean _createUpdate)
        {
            this.createUpdate = _createUpdate;
            return this;
        }

        /**
         * Builds the.
         *
         * @return the attribute type
         */
        public AttributeType build()
        {
            final AttributeType ret = new AttributeType(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}
