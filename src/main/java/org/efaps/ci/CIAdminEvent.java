/*
 * Copyright 2003 - 2013 The eFaps Team
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
//CHECKSTYLE:OFF
public class CIAdminEvent
{
    public static final _Definition Definition = new _Definition("9c1d52f4-94d6-4f95-ab81-bed23884cf03");

    public static class _Definition
        extends CIAdmin._Abstract
    {

        protected _Definition(final String _uuid)
        {
            super(_uuid);
        }
        public final CIAttribute IndexPosition = new CIAttribute(this, "IndexPosition");
        public final CIAttribute Abstract = new CIAttribute(this, "Abstract");
        public final CIAttribute Method = new CIAttribute(this, "Method");
        public final CIAttribute JavaProg = new CIAttribute(this, "JavaProg");
    }
}
