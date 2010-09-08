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
public class CIAdminProgram
{

    public static final _Abstract Abstract = new _Abstract("da0d5338-118a-435a-ad5f-fc04d5c7c6d8");

    public static class _Abstract
        extends CIAdmin._Abstract
    {

        protected _Abstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute FileName = new CIAttribute(this, "FileName");
        public final CIAttribute FileLength = new CIAttribute(this, "FileLength");
    }

    public static final _Java Java = new _Java("11043a35-f73c-481c-8c77-00306dbce824");

    public static class _Java
        extends _Abstract
    {

        protected _Java(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Compiled Compiled = new _Compiled("f67dd041-e09f-4ff6-93f3-a2ac219b8684");

    public static class _Compiled
        extends _Abstract
    {

        protected _Compiled(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute FileName = new CIAttribute(this, "FileName");
        public final CIAttribute FileLength = new CIAttribute(this, "FileLength");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _StaticCompiled StaticCompiled = new _StaticCompiled("76fb464e-1d14-4437-ad23-092ab12669dd");

    public static class _StaticCompiled
        extends _Compiled
    {

        protected _StaticCompiled(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _JavaClass JavaClass = new _JavaClass("9118e1e3-ed4c-425d-8578-8d1f1d385110");

    public static class _JavaClass
        extends _Compiled
    {

        protected _JavaClass(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute ProgramLink = new CIAttribute(this, "ProgramLink");

    }
    public static final _CSS CSS = new _CSS("f5a5bcf6-3cc7-4530-a5a0-7808a392381b");

    public static class _CSS
        extends _Abstract
    {

        protected _CSS(final String _uuid)
        {
            super(_uuid);
        }
    }
    public static final _JavaScript JavaScript = new _JavaScript("1c9ce325-7e4f-401f-aeb8-74e2e0c9e224");

    public static class _JavaScript
        extends _Abstract
    {

        protected _JavaScript(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Wiki Wiki = new _Wiki("7a65b742-bfdf-45bd-bca6-e591273f567f");

    public static class _Wiki
        extends _Abstract
    {

        protected _Wiki(final String _uuid)
        {
            super(_uuid);
        }
    }
    public static final _JasperReport JasperReport = new _JasperReport("425eda62-5591-4799-828d-ced6687a138b");

    public static class _JasperReport
        extends _Abstract
    {

        protected _JasperReport(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _CSS2CSS CSS2CSS = new _CSS2CSS("9d69ef63-b248-4f50-9130-5f33d64d81f0");

    public static class _CSS2CSS
        extends _Program2Program
    {

        protected _CSS2CSS(final String _uuid)
        {
            super(_uuid);
        }
    }


    public static final _JasperReport2JasperReport JasperReport2JasperReport = new _JasperReport2JasperReport("c7e32d85-0d21-4891-b107-0700ac757633");

    public static class _JasperReport2JasperReport
        extends _Program2Program
    {

        protected _JasperReport2JasperReport(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _JavaScript2JavaScript JavaScript2JavaScript = new _JavaScript2JavaScript("2d24e861-580c-43ad-a59c-3266021ea190");

    public static class _JavaScript2JavaScript
        extends _Program2Program
    {

        protected _JavaScript2JavaScript(final String _uuid)
        {
            super(_uuid);
        }
    }
    public static final _Wiki2Wiki Wiki2Wiki = new _Wiki2Wiki("11cb5cee-654e-4cfd-8ac2-c5676cf0c33f");

    public static class _Wiki2Wiki
        extends _Program2Program
    {

        protected _Wiki2Wiki(final String _uuid)
        {
            super(_uuid);
        }
    }



    public static final _Program2Program Program2Program = new _Program2Program("86f6c8d6-b2c0-4d08-9ce7-7ce46d0dcdd0");

    public static class _Program2Program
        extends CIAdminCommon._Abstract2Abstract
    {

        protected _Program2Program(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute From = new CIAttribute(this, "From");
        public final CIAttribute To = new CIAttribute(this, "To");
    }



    public static final _CSSCompiled CSSCompiled = new _CSSCompiled("0607ea90-b48f-4b76-96f5-67cab19bd7b1");

    public static class _CSSCompiled
        extends _StaticCompiled
    {

        protected _CSSCompiled(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute ProgramLink = new CIAttribute(this, "ProgramLink");

    }

    public static final _JasperReportCompiled JasperReportCompiled = new _JasperReportCompiled("c2ed3807-efc1-497a-b4c4-c0f8ba27beb3");

    public static class _JasperReportCompiled
        extends _StaticCompiled
    {

        protected _JasperReportCompiled(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute ProgramLink = new CIAttribute(this, "ProgramLink");

    }


    public static final _JavaScriptCompiled JavaScriptCompiled = new _JavaScriptCompiled("5ed4d346-c82e-4f4e-b52e-a4d5afa0e284");

    public static class _JavaScriptCompiled
        extends _StaticCompiled
    {

        protected _JavaScriptCompiled(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute ProgramLink = new CIAttribute(this, "ProgramLink");

    }
    public static final _WikiCompiled WikiCompiled = new _WikiCompiled("f9d0aa00-5687-45d1-8206-36662f09fb24");

    public static class _WikiCompiled
        extends _StaticCompiled
    {

        protected _WikiCompiled(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute ProgramLink = new CIAttribute(this, "ProgramLink");

    }


    public static final _XSL XSL = new _XSL("2e40c566-a55c-4b3b-a79b-b786e20f8d1c");

    public static class _XSL
        extends _Abstract
    {

        protected _XSL(final String _uuid)
        {
            super(_uuid);
        }
    }
    public static final _WikiImage WikiImage = new _WikiImage("2afc7562-2d1d-4751-8ca6-7811cb60e783");

    public static class _WikiImage
        extends CIAdminUserInterface._File
    {

        protected _WikiImage(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _JasperImage JasperImage = new _JasperImage("7dbeb5cc-ceb4-4f90-bf3f-9aaa14fc8a22");

    public static class _JasperImage
        extends CIAdminUserInterface._File
    {

        protected _JasperImage(final String _uuid)
        {
            super(_uuid);
        }
    }

}
