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

package org.efaps.update.schema.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * TODO:  description
 */
public class WebDAVUpdate
    extends AbstractUpdate
{
    /**
     *
     */
    private static final Set<Link> ALLLINKS = new HashSet<Link>();

    /**
     * Instantiates a new web dav update.
     *
     * @param _installFile the install file
     */
    public WebDAVUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_Integration_WebDAV", WebDAVUpdate.ALLLINKS);
    }

    /**
     * Creates new instance of class {@link Definition}.
     *
     * @return new definition instance
     * @see Definition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new Definition();
    }

    /**
     * TODO comment!
     *
     */
    public class Definition
        extends AbstractDefinition
    {
        @Override
        public void readXML(final List<String> _tags,
                            final Map<String, String> _attributes,
                            final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            if ("path".equals(value)) {
                addValue("Path", value);
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}
