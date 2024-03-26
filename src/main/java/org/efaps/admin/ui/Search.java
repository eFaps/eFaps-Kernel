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
package org.efaps.admin.ui;

import java.util.UUID;

import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 *
 *
 */
public class Search
    extends AbstractMenu
{

    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Search.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the default search command used when the search is called.
     */
    private AbstractCommand defaultCommand = null;

    /**
     * This is the constructor to create a new instance of the class Search. The
     * parameter <i>_name</i> is a must value to identify clearly the search
     * instance.
     *
     * @param _id search id
     * @param _uuid uuid for this search
     * @param _name search name
     */
    public Search(final Long _id,
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
     * @throws CacheReloadException on error
     */
    @Override
    protected void add(final long _sortId,
                       final long _id)
        throws CacheReloadException
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
     * {@inheritDoc}
     */
    @Override
    protected void setLinkProperty(final UUID _linkTypeUUID,
                                   final long _toId,
                                   final UUID _toTypeUUID,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkTypeUUID.equals(CIAdminUserInterface.LinkDefaultSearchCommand.uuid)) {
            this.defaultCommand = Command.get(_toId);
            if (this.defaultCommand == null) {
                this.defaultCommand = Menu.get(_toId);
            }
        }
        super.setLinkProperty(_linkTypeUUID, _toId, _toTypeUUID, _toName);
    }

    /**
     * This is the getter method for the instance variable
     * {@link #defaultCommand}.
     *
     * @return value of instance variable {@link #defaultCommand}
     * @see #defaultCommand
     */
    public AbstractCommand getDefaultCommand()
    {
        return this.defaultCommand;
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class {@link Form}
     * .
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Form}
     * @throws CacheReloadException on error
     */
    public static Search get(final long _id)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Search>get(_id, Search.class, CIAdminUserInterface.Search.getType());
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Command}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Command}
     * @throws CacheReloadException on error
     */
    public static Search get(final String _name)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Search>get(_name, Search.class, CIAdminUserInterface.Search.getType());
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Command}.
     *
     * @param _uuid uuid to search in the cache
     * @return instance of class {@link Command}
     * @throws CacheReloadException on error
     */
    public static Search get(final UUID _uuid)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Search>get(_uuid, Search.class, CIAdminUserInterface.Search.getType());
    }
}
