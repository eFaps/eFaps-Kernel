/*
 * Copyright 2003 - 2010 The eFaps Team
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
public class CIAdminUserInterface
{

    public static final _Abstract2Abstract Abstract2Abstract = new _Abstract2Abstract("8ef6037c-aabd-4265-ae60-c51d64d85789");

    public static class _Abstract2Abstract
        extends CIAdminCommon._Abstract2Abstract
    {

        protected _Abstract2Abstract(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Direct Direct = new _Direct("641d4913-63a0-4192-b594-b74e69c0aa25");

    public static class _Direct
        extends CIAdmin._Abstract
    {

        protected _Direct(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Collection Collection = new _Collection("0ad74515-5d2f-4579-bf67-4ed55c02ae9e");

    public static class _Collection
        extends _Direct
    {

        protected _Collection(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Command Command = new _Command("65e8da96-7bd3-4a9f-867d-b2188dc2d882");

    public static class _Command
        extends _Direct
    {

        protected _Command(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Field Field = new _Field("1b3e1892-74bb-4df8-8e56-1de9c47cb3b8");

    public static class _Field
        extends CIAdmin._Abstract
    {

        protected _Field(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Collection = new CIAttribute(this, "Collection");
    }

    public static final _FieldClassification FieldClassification = new _FieldClassification("f4ad729b-f96a-4057-865f-ad910114e695");

    public static class _FieldClassification
        extends _Field
    {

        protected _FieldClassification(final String _uuid)
        {
            super(_uuid);
        }

    }

    public static final _FieldCommand FieldCommand = new _FieldCommand("6e8fa8bf-5368-485d-a5ba-12dc600c54a5");

    public static class _FieldCommand
        extends _Field
    {

        protected _FieldCommand(final String _uuid)
        {
            super(_uuid);
        }

    }

    public static final _FieldGroup FieldGroup = new _FieldGroup("629cd86d-5103-4ee5-9eef-aef9de7862c3");

    public static class _FieldGroup
        extends _Field
    {

        protected _FieldGroup(final String _uuid)
        {
            super(_uuid);
        }

    }

    public static final _FieldHeading FieldHeading = new _FieldHeading("29e30382-63cd-44ac-91b6-ca1ca58ff434");

    public static class _FieldHeading
        extends _Field
    {

        protected _FieldHeading(final String _uuid)
        {
            super(_uuid);
        }

    }

    public static final _FieldTable FieldTable = new _FieldTable("d72728e9-2878-47fd-9250-21b5a04ebadb");

    public static class _FieldTable
        extends _Field
    {

        protected _FieldTable(final String _uuid)
        {
            super(_uuid);
        }

    }

    public static final _FieldSet FieldSet = new _FieldSet("415c196f-c1aa-4aa0-b96e-f8541332a921");

    public static class _FieldSet
        extends _Field
    {

        protected _FieldSet(final String _uuid)
        {
            super(_uuid);
        }

    }

    public static final _Menu Menu = new _Menu("209d2e8b-608b-4b09-bdbb-ef5b98d0e2ab");

    public static class _Menu
        extends _Command
    {

        protected _Menu(final String _uuid)
        {
            super(_uuid);
        }

    }

    public static final _Picker Picker = new _Picker("259e8dda-dc0e-492c-96dc-850a2fa13d98");

    public static class _Picker
        extends _Collection
    {

        protected _Picker(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Link Link = new _Link("23f3aa8c-30db-4aa5-8add-d2277ae8b3c3");

    public static class _Link
        extends _Abstract2Abstract
    {

        protected _Link(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute From = new CIAttribute(this, "From");
        public final CIAttribute To = new CIAttribute(this, "To");
    }

    public static final _LinkIcon LinkIcon = new _LinkIcon("c21150d9-f160-4eaf-b93f-66042697867e");

    public static class _LinkIcon
        extends _Link
    {

        protected _LinkIcon(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _LinkTargetTable LinkTargetTable = new _LinkTargetTable("27eae97f-c6f4-4c4e-9947-c1c9bc4ea297");

    public static class _LinkTargetTable
        extends _Link
    {

        protected _LinkTargetTable(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _LinkTargetCommand LinkTargetCommand = new _LinkTargetCommand("8909acf2-2f38-474d-ba7f-713b3bddbef7");

    public static class _LinkTargetCommand
        extends _Link
    {

        protected _LinkTargetCommand(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _LinkTargetForm LinkTargetForm = new _LinkTargetForm("3eb6f003-c04e-48f0-8fac-797438ed6501");

    public static class _LinkTargetForm
        extends _Link
    {

        protected _LinkTargetForm(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _LinkTargetHelp LinkTargetHelp = new _LinkTargetHelp("28d94899-9998-449d-9bd1-dc4255388cc8");

    public static class _LinkTargetHelp
        extends _Link
    {

        protected _LinkTargetHelp(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _LinkTargetMenu LinkTargetMenu = new _LinkTargetMenu("c646804e-29ad-4c7a-ac70-d024a77d131e");

    public static class _LinkTargetMenu
        extends _Link
    {

        protected _LinkTargetMenu(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _LinkTargetSearch LinkTargetSearch = new _LinkTargetSearch("c78c1f61-3f64-4f69-92fc-e01854bc7512");

    public static class _LinkTargetSearch
        extends _Link
    {

        protected _LinkTargetSearch(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Menu2Command Menu2Command = new _Menu2Command("1d101fd6-5b28-485b-b2d3-300b7729e2f1");

    public static class _Menu2Command
        extends _Abstract2Abstract
    {

        protected _Menu2Command(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute FromMenu = new CIAttribute(this, "FromMenu");
        public final CIAttribute ToCommand = new CIAttribute(this, "ToCommand");
    }

    public static final _LinkIsTypeTreeFor LinkIsTypeTreeFor = new _LinkIsTypeTreeFor("ce5087b5-ee5c-49c3-adfb-5da18f95a4d0");

    public static class _LinkIsTypeTreeFor
        extends _Link
    {

        protected _LinkIsTypeTreeFor(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Form Form = new _Form("e6ddf834-e4f4-481e-8afb-95bf3760b6ba");

    public static class _Form
        extends _Collection
    {

        protected _Form(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _LinkIsTypeIconFor LinkIsTypeIconFor = new _LinkIsTypeIconFor("74b91e57-e5a3-43df-b0e4-43815ad79fec");

    public static class _LinkIsTypeIconFor
        extends _Link
    {

        protected _LinkIsTypeIconFor(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _File File = new _File("74b91e57-e5a3-43df-b0e4-43815ad79fec");

    public static class _File
        extends _Direct
    {

        protected _File(final String _uuid)
        {
            super(_uuid);
        }
        public final CIAttribute FileName = new CIAttribute(this, "FileName");
        public final CIAttribute FileLength = new CIAttribute(this, "FileLength");
    }


    public static final _Image Image = new _Image("6e70fbed-fdfc-4ed3-a0f8-d0bc1858419d");

    public static class _Image
        extends _File
    {

        protected _Image(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Search Search = new _Search("2cb35fbd-d495-4680-b7ad-e236507a5e94");

    public static class _Search
        extends _Menu
    {

        protected _Search(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _LinkDefaultSearchCommand LinkDefaultSearchCommand = new _LinkDefaultSearchCommand("3f827900-eda2-409f-be92-497dcacb0eef");

    public static class _LinkDefaultSearchCommand
        extends _Link
    {

        protected _LinkDefaultSearchCommand(final String _uuid)
        {
            super(_uuid);
        }
    }
    public static final _Table Table = new _Table("6f3695cb-fab5-45e5-8d8e-eb1e6870dcd3");

    public static class _Table
        extends _Collection
    {

        protected _Table(final String _uuid)
        {
            super(_uuid);
        }
    }


}
