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

package org.efaps.update.schema.integration;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.update.AbstractUpdate;

/**
 * @author The eFaps Team
 * @version $Id$
 * TODO:  description
 */
public class WebDAVUpdate
    extends AbstractUpdate
{
    private static final Set<Link> ALLLINKS = new HashSet<Link>();
    /*{

         * ALLLINKS.add(LINK2ACCESSTYPE); ALLLINKS.add(LINK2DATAMODELTYPE);
         * ALLLINKS.add(LINK2PERSON); ALLLINKS.add(LINK2ROLE);
         * ALLLINKS.add(LINK2GROUP);
    }*/

    /**
     *
     * @param _url        URL of the file
     */
    public WebDAVUpdate(final URL _url)
    {
        super(_url, "Admin_Integration_WebDAV", WebDAVUpdate.ALLLINKS);
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

    public class Definition
        extends AbstractDefinition
    {
        @Override
        public void readXML(final List<String> _tags,
                            final Map<String, String> _attributes,
                            final String _text)
        {
            final String value = _tags.get(0);
            if ("path".equals(value))  {
                this.addValue("Path", value);
            } else  {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}
