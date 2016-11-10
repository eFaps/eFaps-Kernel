/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.ci;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
// CHECKSTYLE:OFF
public class CIAdminDataModel
{

    public static final _Abstract Abstract = new _Abstract("f4d7c43a-4773-49c6-a3a5-0c91317ada0f");

    public static class _Abstract
        extends CIAdmin._Abstract
    {

        protected _Abstract(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Type Type = new _Type("8770839d-60fd-4bb4-81fd-3903d4c916ec");

    public static class _Type
        extends _Abstract
    {

        protected _Type(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute ParentClassType = new CIAttribute(this, "ParentClassType");
        public final CIAttribute ParentType = new CIAttribute(this, "ParentType");
    }

    public static final _Attribute Attribute = new _Attribute("518a9802-cf0e-4359-9b3c-880f71e1387f");

    public static class _Attribute
        extends _Abstract
    {

        protected _Attribute(final String _uuid)
        {
            super(_uuid);
        }
        public final CIAttribute AttributeType = new CIAttribute(this, "AttributeType");
        public final CIAttribute SQLColumn = new CIAttribute(this, "SQLColumn");
        public final CIAttribute Table = new CIAttribute(this, "Table");
        public final CIAttribute ParentType = new CIAttribute(this, "ParentType");
        public final CIAttribute TypeLink = new CIAttribute(this, "TypeLink");
        public final CIAttribute DefaultValue = new CIAttribute(this, "DefaultValue");
        public final CIAttribute DimensionUUID = new CIAttribute(this, "DimensionUUID");
        public final CIAttribute ClassName = new CIAttribute(this, "ClassName");

    }

    public static final _AttributeSetAttribute AttributeSetAttribute = new _AttributeSetAttribute("f601ffc5-819c-41a0-8663-3e1b0fb35a9b");

    public static class _AttributeSetAttribute
        extends _Attribute
    {

        protected _AttributeSetAttribute(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute ParentAttributeSet = new CIAttribute(this, "ParentAttributeSet");
    }

    public static final _AttributeSet AttributeSet = new _AttributeSet("a23b6c9f-5220-438f-93d0-f4651c3ba455");

    public static class _AttributeSet
        extends _Attribute
    {

        protected _AttributeSet(final String _uuid)
        {
            super(_uuid);
        }

    }


    public static final _AttributeType AttributeType = new _AttributeType("c482e3d3-8387-4406-a1c2-b0e708af78f3");

    public static class _AttributeType
        extends CIAdminDataModel._Abstract
    {

        protected _AttributeType(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Classname = new CIAttribute(this, "Classname");
        public final CIAttribute ClassnameUI = new CIAttribute(this, "ClassnameUI");
        public final CIAttribute AlwaysUpdate = new CIAttribute(this, "AlwaysUpdate");
        public final CIAttribute CreateUpdate = new CIAttribute(this, "CreateUpdate");
    }

    public static final _SQLTable SQLTable = new _SQLTable("ebf29cc2-cf42-4cd0-9b6e-92d9b644062b");

    public static class _SQLTable
        extends CIAdminDataModel._Abstract
    {

        protected _SQLTable(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute SQLTable = new CIAttribute(this, "SQLTable");
        public final CIAttribute SQLColumnID = new CIAttribute(this, "SQLColumnID");
        public final CIAttribute SQLColumnType = new CIAttribute(this, "SQLColumnType");
        public final CIAttribute DMTableMain = new CIAttribute(this, "DMTableMain");
    }


    public static final _Type2Store Type2Store = new _Type2Store("433f8358-dd69-4d53-9161-5ae9b0b51c57");

    public static class _Type2Store
        extends CIAdminUserInterface._Abstract2Abstract
    {

        protected _Type2Store(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute From = new CIAttribute(this, "From");
        public final CIAttribute To = new CIAttribute(this, "To");
    }

    public static final _TypeEventIsAllowedFor TypeEventIsAllowedFor = new _TypeEventIsAllowedFor("bf3d70ce-206e-4328-aa35-761c4aeb9d1d");

    public static class _TypeEventIsAllowedFor
        extends CIAdminUserInterface._Abstract2Abstract
    {

        protected _TypeEventIsAllowedFor(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute From = new CIAttribute(this, "From");
        public final CIAttribute To = new CIAttribute(this, "To");
    }

    public static final _TypeClassifies TypeClassifies = new _TypeClassifies(
                    "276e9d68-08db-4ad2-91cb-26aa3947c690");

    public static class _TypeClassifies
        extends CIAdminUserInterface._Abstract2Abstract
    {

        protected _TypeClassifies(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute From = new CIAttribute(this, "From");
        public final CIAttribute To = new CIAttribute(this, "To");
    }

    public static final _TypeClassifyRelation TypeClassifyRelation = new _TypeClassifyRelation("fa6abe39-f23a-48bc-806d-0ee30d83449f");

    public static class _TypeClassifyRelation
        extends CIAdminUserInterface._Abstract2Abstract
    {

        protected _TypeClassifyRelation(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute From = new CIAttribute(this, "From");
        public final CIAttribute To = new CIAttribute(this, "To");
    }


    public static final _TypeClassifyCompany TypeClassifyCompany = new _TypeClassifyCompany("4bd5bd75-54b5-4be2-8874-d48cbd5f0b50");

    public static class _TypeClassifyCompany
        extends CIAdminUserInterface._Abstract2Abstract
    {

        protected _TypeClassifyCompany(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute From = new CIAttribute(this, "From");
        public final CIAttribute To = new CIAttribute(this, "To");
    }

    public static final _UoM UoM = new _UoM("af2a5069-6ee8-40c4-8f8e-80a11565869d");

    public static class _UoM
        extends CIType
    {

        protected _UoM(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Dimension = new CIAttribute(this, "Dimension");
        public final CIAttribute Symbol = new CIAttribute(this, "Symbol");
        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute CommonCode = new CIAttribute(this, "CommonCode");
        public final CIAttribute Numerator = new CIAttribute(this, "Numerator");
        public final CIAttribute Denominator = new CIAttribute(this, "Denominator");
    }

    public static final _Dimension Dimension = new _Dimension("e3a45e2e-bed9-4be0-a4a2-890acac3a669");

    public static class _Dimension
        extends CIType
    {

        protected _Dimension(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute Description = new CIAttribute(this, "Description");
        public final CIAttribute UUID = new CIAttribute(this, "UUID");
        public final CIAttribute BaseUoM = new CIAttribute(this, "BaseUoM");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }
}
