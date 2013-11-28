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

package org.efaps.update.schema.ui;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.update.LinkInstance;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class MenuUpdate
    extends CommandUpdate
{
    /**
     * The links for the Menu to be updated.
     */
    protected static final Set<Link> ALLLINKS = new HashSet<Link>();

    /** Link from menu to child command / menu. */
    private static final Link LINK2CHILD = new OrderedLink("Admin_UI_Menu2Command",
                                                           "FromMenu",
                                                           "Admin_UI_Command", "ToCommand");

    /** Link from menu to type as type tree menu. */
    private static final Link LINK2TYPE = new UniqueLink("Admin_UI_LinkIsTypeTreeFor", "From",
                                                         "Admin_DataModel_Type", "To");

    static  {
        MenuUpdate.ALLLINKS.add(MenuUpdate.LINK2CHILD);
        MenuUpdate.ALLLINKS.add(MenuUpdate.LINK2TYPE);
        MenuUpdate.ALLLINKS.addAll(CommandUpdate.ALLLINKS);
    }

    /**
     * @param _url URL to the Configuration Item.
     */
    public MenuUpdate(final URL _url)
    {
        super(_url, "Admin_UI_Menu", MenuUpdate.ALLLINKS);
    }

    /**
     *
     * @param _url        URL to the Configuration Item.
     * @param _typeName     Name of the type
     * @param _allLinks     link definitions
     */
    protected MenuUpdate(final URL _url,
                         final String _typeName,
                         final Set<Link> _allLinks)
    {
        super(_url, _typeName, _allLinks);
    }

    /**
     * Creates new instance of class {@link MenuDefinition}.
     *
     * @return new definition instance
     * @see MenuDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new MenuDefinition();
    }

    /**
     *
     */
    protected class MenuDefinition
        extends CommandDefinition
    {
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            if ("childs".equals(value)) {
                if (_tags.size() > 1) {
                    final String subValue = _tags.get(1);
                    if ("child".equals(subValue)) {
                        final LinkInstance child = new LinkInstance(_text);
                        addLink(MenuUpdate.LINK2CHILD, child);
                    } else {
                        super.readXML(_tags, _attributes, _text);
                    }
                }
            } else if ("type".equals(value)) {
                // assigns a type the menu for which this menu instance is the type
                // tree menu
                addLink(MenuUpdate.LINK2TYPE, new LinkInstance(_text));
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}
