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

package org.efaps.update.schema.user;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.efaps.update.AbstractUpdate;

/**
 * @author The eFaps Team
 * @version $Id$
 * TODO:  description
 */
public class RoleUpdate
    extends AbstractUpdate
{

    /**
     * Global type or local.
     */
    private boolean global = true;

    /**
     *
     * @param _url        URL of the file
     */
    public RoleUpdate(final URL _url)
    {
        super(_url, "temp");
    }

    @Override
    public String getDataModelTypeName()
    {
        return this.global ? "Admin_User_RoleGlobal" : "Admin_User_RoleLocal";
    }

    /**
     * Creates new instance of class {@link RoleDefinition}.
     *
     * @return new definition instance
     * @see RoleDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new RoleDefinition();
    }

    /**
     *
     */
    public class RoleDefinition
        extends AbstractDefinition
    {
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("status".equals(value))  {
                addValue("Status", _text);
            } else if ("global".equals(value))  {
                RoleUpdate.this.global = Boolean.valueOf(_text);
            } else  {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}
