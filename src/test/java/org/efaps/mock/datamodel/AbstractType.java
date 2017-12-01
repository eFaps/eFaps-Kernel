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

import java.util.UUID;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.efaps.test.IResult;

/**
 * The Class AbstractType.
 */
public abstract class AbstractType
    implements IResult
{

    /** The id. */
    private final Long id;

    /** The uuid. */
    private final UUID uuid;

    /** The name. */
    private final String name;

    /**
     * Instantiates a new abstract type.
     *
     * @param _builder the builder
     */
    protected AbstractType(final AbstractBuilder<?> _builder)
    {
        this.id = _builder.id;
        this.uuid = _builder.uuid;
        this.name = _builder.name;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId()
    {
        return this.id;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public UUID getUuid()
    {
        return this.uuid;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * The Class AbstractBuilder.
     *
     * @param <T> the generic type
     */
    public static abstract class AbstractBuilder<T extends AbstractBuilder<T>>
    {

        /** The id. */
        protected long id = RandomUtils.nextLong();

        /** The name. */
        private String name;

        /** The uuid. */
        private UUID uuid = UUID.randomUUID();

        /**
         * With id.
         *
         * @param _id the id
         * @return the t
         */
        @SuppressWarnings("unchecked")
        public T withId(final Long _id)
        {
            this.id = _id;
            return (T) this;
        }

        /**
         * With name.
         *
         * @param _name the name
         * @return the t
         */
        @SuppressWarnings("unchecked")
        public T withName(final String _name)
        {
            this.name = _name;
            return (T) this;
        }

        /**
         * With uuid.
         *
         * @param _uuid the uuid
         * @return the t
         */
        @SuppressWarnings("unchecked")
        public T withUuid(final UUID _uuid)
        {
            this.uuid = _uuid;
            return (T) this;
        }
    }
}
