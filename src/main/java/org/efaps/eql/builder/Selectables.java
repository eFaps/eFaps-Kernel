/*
 * Copyright 2003 - 2018 The eFaps Team
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
 */
package org.efaps.eql.builder;

import org.efaps.ci.CIAttribute;
import org.efaps.eql2.bldr.AbstractSelectables;

/**
 * The Class Selectables.
 */
public class Selectables
    extends AbstractSelectables
{

    /**
     * Linkto.
     *
     * @param _linktoAttr the linkto attr
     * @return the linkto
     */
    public static Linkto linkto(final CIAttribute _linktoAttr)
    {
        return new Linkto(_linktoAttr);
    }

    /**
     * The Class Linkto.
     */
    public static class Linkto
        extends AbstractSelectables.Linkto
    {

        /**
         * Instantiates a new linkto.
         *
         * @param _attr the attr
         */
        public Linkto(final CIAttribute _attr)
        {
            this(_attr.name);
        }

        /**
         * Instantiates a new linkto.
         *
         * @param _attr the attr
         */
        public Linkto(final String _attr)
        {
            super(_attr);
        }

        /**
         * Attr.
         *
         * @param _attr the attr
         * @return the abstract linkto
         */
        public Linkto attr(final CIAttribute _attr)
        {
            return attribute(_attr);
        }

        /**
         * Attribute.
         *
         * @param _attr the attr
         * @return the abstract linkto
         */
        public Linkto attribute(final CIAttribute _attr)
        {
            super.attribute(_attr.name);
            return this;
        }
    }
}
