/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.mock.datamodel;

import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;
import org.efaps.mock.Mocks;

public class CI
{


    public static final _SimpleType SimpleType = new _SimpleType(Mocks.SimpleType.getUuid().toString());

    public static class _SimpleType
        extends CIType
    {

        protected _SimpleType(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute TestAttr = new CIAttribute(this, Mocks.TestAttribute.getName());
    }


    public static final _TypedType TypedType = new _TypedType(Mocks.TypedType.getUuid().toString());

    public static class _TypedType
        extends CIType
    {

        protected _TypedType(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute TestAttr = new CIAttribute(this, Mocks.TypedTypeTestAttr.getName());
    }

    public static final _AllAttrType AllAttrType = new _AllAttrType(Mocks.AllAttrType.getUuid().toString());

    public static class _AllAttrType
        extends CIType
    {

        protected _AllAttrType(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute StringAttribute = new CIAttribute(this, Mocks.AllAttrStringAttribute.getName());
        public final CIAttribute LinkAttribute = new CIAttribute(this, Mocks.AllAttrLinkAttribute.getName());
        public final CIAttribute LongAttribute = new CIAttribute(this, Mocks.AllAttrLongAttribute.getName());
        public final CIAttribute IntegerAttribute = new CIAttribute(this, Mocks.AllAttrIntegerAttribute.getName());
        public final CIAttribute DecimalAttribute = new CIAttribute(this, Mocks.AllAttrDecimalAttribute.getName());
        public final CIAttribute DateAttribute = new CIAttribute(this, Mocks.AllAttrDateAttribute.getName());
        public final CIAttribute TimeAttribute = new CIAttribute(this, Mocks.AllAttrTimeAttribute.getName());
        public final CIAttribute DateTimeAttribute = new CIAttribute(this, Mocks.AllAttrDateTimeAttribute.getName());
    }

    public static final _CompanyType CompanyType = new _CompanyType(Mocks.CompanyType.getUuid().toString());

    public static class _CompanyType
        extends CIType
    {

        protected _CompanyType(final String _uuid)
        {
            super(_uuid);
        }
        public final CIAttribute CompanyAttribute = new CIAttribute(this, Mocks.CompanyCompanyAttribute.getName());
        public final CIAttribute StringAttribute = new CIAttribute(this, Mocks.CompanyStringAttribute.getName());
    }

}
