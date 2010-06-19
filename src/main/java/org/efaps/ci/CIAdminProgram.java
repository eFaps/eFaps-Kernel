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

    public static final _CSS2CSS CSS2CSS = new _CSS2CSS("9d69ef63-b248-4f50-9130-5f33d64d81f0");

    public static class _CSS2CSS
        extends _Program2Program
    {

        protected _CSS2CSS(final String _uuid)
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
}
