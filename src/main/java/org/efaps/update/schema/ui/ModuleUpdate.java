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
package org.efaps.update.schema.ui;

import java.util.List;
import java.util.Map;

import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.util.EFapsException;

public class ModuleUpdate
    extends AbstractUpdate
{

    public ModuleUpdate(final InstallFile url)
    {
        super(url, "Admin_UI_Module");
    }

    @Override
    protected AbstractDefinition newDefinition()
    {
        return new ModuleDefinition();
    }

    /**
     * Definition for a Form.
     */
    public class ModuleDefinition
        extends AbstractDefinition
    {

        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            super.readXML(_tags, _attributes, _text);
        }
    }
}
