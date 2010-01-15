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

package org.efaps.update.schema.wiki;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class WikiMenuUpdate extends AbstractUpdate
{
    /** Link from menu to child command / menu. */
    private static final Link LINK2CHILD = new OrderedLink("Admin_Wiki_Menu2Menu",
                                                           "FromLink",
                                                           "Admin_Wiki_Menu", "ToLink");

    /** Link from menu to type as type tree menu. */
    private static final Link LINK2COMPILED = new Link("Admin_Wiki_Menu2Wiki",
                                                       "FromLink",
                                                       "Admin_Program_WikiCompiled", "ToLink");

    /**
     * Set of all links for wiki menus.
     */
    private static final Set<Link> ALLLINKS = new HashSet<Link>();
    static  {
        WikiMenuUpdate.ALLLINKS.add(WikiMenuUpdate.LINK2CHILD);
        WikiMenuUpdate.ALLLINKS.add(WikiMenuUpdate.LINK2COMPILED);
    }

    /**
     * Constructor calling the super constructor.
     * @param _url URL to the xml definition file.
     */
    public WikiMenuUpdate(final URL _url)
    {
        super(_url, "Admin_Wiki_Menu", WikiMenuUpdate.ALLLINKS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new WikiMenuDefinition();
    }

    /**
     * Definition for the wiki menu.
     */
    protected class WikiMenuDefinition
        extends AbstractDefinition
    {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("childs".equals(value)) {
                if (_tags.size() > 1) {
                    final String subValue = _tags.get(1);
                    if ("child".equals(subValue)) {
                        addLink(WikiMenuUpdate.LINK2CHILD, new LinkInstance(_text));
                    } else {
                        super.readXML(_tags, _attributes, _text);
                    }
                }
            } else if ("target".equals(value)) {
                if (_tags.size() == 2) {
                    final String subValue = _tags.get(1);
                    if ("wiki".equals(subValue)) {
                        addLink(WikiMenuUpdate.LINK2COMPILED, new LinkInstance(_text));
                    }
                }
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}
