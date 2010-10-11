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

package org.efaps.update.schema.ui;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.update.LinkInstance;

/**
 * @author The eFaps Team
 * @version $Id$
 * TODO:  description
 */
public class SearchUpdate
    extends MenuUpdate
{
    /** Link from search to default search command. */
    private static final Link LINK2DEFAULTCMD = new Link("Admin_UI_LinkDefaultSearchCommand",
                                                         "From",
                                                         "Admin_UI_Command", "To");

    /**
     * All known links for the search schema update.
     */
    private static final Set<Link> ALLLINKS = new HashSet<Link>();
    static  {
        SearchUpdate.ALLLINKS.add(SearchUpdate.LINK2DEFAULTCMD);
        SearchUpdate.ALLLINKS.addAll(MenuUpdate.ALLLINKS);
    }

    /**
     *
     * @param _url        URL of the file
     */
    public SearchUpdate(final URL _url)
    {
        super(_url, "Admin_UI_Search", SearchUpdate.ALLLINKS);
    }

    /**
     * Creates new instance of class {@link SearchDefinition}.
     *
     * @return new definition instance
     * @see SearchDefinition
     */
    @Override()
    protected AbstractDefinition newDefinition()
    {
        return new SearchDefinition();
    }

    public class SearchDefinition
        extends MenuDefinition
    {
        @Override()
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("default".equals(value))  {
                if (_tags.size() > 1)  {
                    final String subValue = _tags.get(1);
                    if ("command".equals(subValue))  {
                        // assigns a command as default for the search menu
                        this.addLink(SearchUpdate.LINK2DEFAULTCMD, new LinkInstance(_text));
                    } else  {
                        super.readXML(_tags, _attributes, _text);
                    }
                }
            } else  {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}
