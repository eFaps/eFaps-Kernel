/*
 * Copyright 2003 - 2011 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ci;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class CIDB
{

    public static final _Store Store = new _Store("0ca5fb5f-74b3-416e-b80d-80d4baf9abfd");

    public static class _Store
        extends CIAdmin._Abstract
    {

        protected _Store(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Store2Resource Store2Resource = new _Store2Resource("34be8b90-a753-45d5-98a7-78c1bcc34b72");

    public static class _Store2Resource
        extends CIAdminUserInterface._Abstract2Abstract
    {

        protected _Store2Resource(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute From = new CIAttribute(this, "From");
        public final CIAttribute To = new CIAttribute(this, "To");

    }

    public static final _Resource Resource = new _Resource("231082cc-1d04-4ea3-a618-da5e997c3a9c");

    public static class _Resource
        extends CIAdmin._Abstract
    {

        protected _Resource(final String _uuid)
        {
            super(_uuid);
        }
    }
}
