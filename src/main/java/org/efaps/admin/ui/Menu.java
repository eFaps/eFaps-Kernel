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

package org.efaps.admin.ui;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class Menu
    extends AbstractMenu
{

    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Menu.class);

    /**
     * Constructor to set the id and name of the menu object.
     *
     * @param _id id of the command to set
     * @param _uuid uuid of this command
     * @param _name name of the command to set
     */
    public Menu(final Long _id,
                final String _uuid,
                final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * An sub command or menu with the given id is added to this menu.
     *
     * @param _sortId id used to sort
     * @param _id command / menu id
     */
    @Override
    protected void add(final long _sortId,
                       final long _id)
    {
        final Command command = Command.get(_id);
        if (command == null) {
            final Menu subMenu = Menu.get(_id);
            add(_sortId, subMenu);
        } else {
            add(_sortId, command);
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class {@link Menu}
     * .
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Menu}
     * @see #getCache
     */
    public static Menu get(final long _id)
    {
        return AbstractUserInterfaceObject.<Menu>get(_id, Menu.class, CIAdminUserInterface.Menu.getType());
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Menu}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Menu}
     * @see #getCache
     */
    public static Menu get(final String _name)
    {
        return AbstractUserInterfaceObject.<Menu>get(_name, Menu.class, CIAdminUserInterface.Menu.getType());
    }

    /**
     * Returns for given parameter <i>UUID</i> the instance of class
     * {@link Menu}.
     *
     * @param _uuid UUID to search in the cache
     * @return instance of class {@link Menu}
     * @see #getCache
     */
    public static Menu get(final UUID _uuid)
    {
        return AbstractUserInterfaceObject.<Menu>get(_uuid, Menu.class, CIAdminUserInterface.Menu.getType());
    }

    /**
     * Returns for given type the type tree menu. If no type tree menu is
     * defined for the type, it is searched if for parent type a menu is
     * defined.
     *
     * @param _type type for which the type tree menu is searched
     * @return type tree menu for given type if found; otherwise
     *         <code>null</code>.
     */
    public static Menu getTypeTreeMenu(final Type _type)
        throws EFapsException
    {
        return _type.getTypeMenu();
    }
}
