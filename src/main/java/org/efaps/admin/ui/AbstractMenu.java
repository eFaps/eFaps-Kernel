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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractMenu
    extends AbstractCommand
{

    /**
     * All sub commands or menus are store in the tree map. The tree map is used
     * to sort the commands / menus belonging to their id.
     *
     * @see #getCommands
     * @add
     */
    private final Map<Long, AbstractCommand> commands = new TreeMap<Long, AbstractCommand>();

    /**
     * Constructor to set the id,uuid and name of the menu object.
     *
     * @param _id id of the command to set
     * @param _uuid UUID of the command to set
     * @param _name name of the command to set
     */
    protected AbstractMenu(final long _id,
                           final String _uuid,
                           final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Adds a command or menu to this menu instance. The method must be specific
     * implemented by all menu implementations.
     *
     * @param _sortId id used to sort
     * @param _id id of the sub command / menu to add
     * @throws CacheReloadException on error
     */
    protected abstract void add(final long _sortId,
                                final long _id)
        throws CacheReloadException;

    /**
     * Add a command to the menu structure.
     *
     * @param _sortId id used to sort
     * @param _command command to add
     */
    public void add(final long _sortId,
                    final AbstractCommand _command)
    {
        this.commands.put(_sortId, _command);
    }

    /**
     * Add all sub commands and menus of the given menu to this menu structure.
     *
     * @param _menu menu with sub structure
     */
    public void addAll(final AbstractMenu _menu)
    {
        this.commands.putAll(_menu.commands);
    }

    /**
     * Check, if the user of the context has access to this user interface
     * object. First, the instance method checks, if some access configuration
     * exists for this menu instance object. If the user has access for this
     * menu, it is test, if the context user has access to minimum one sub
     * command command / menu. If yes, the user is allowed to access this menu
     * instance, other the user is not allowed to access this menu.
     *
     * @param _targetMode TargetMode of the Command
     * @param _instance the field will represent, e.g. on edit mode
     * @return <i>true</i>if context user has access, otherwise <i>false</i> is
     *         returned
     * @throws EFapsException on error
     */
    @Override
    public boolean hasAccess(final TargetMode _targetMode,
                             final Instance _instance)
        throws EFapsException
    {
        boolean ret = super.hasAccess(_targetMode, _instance);

        if (ret && getCommands().size() > 0 && !AppAccessHandler.excludeMode()) {
            ret = false;
            for (final AbstractCommand cmd : getCommands()) {
                if (cmd.hasAccess(_targetMode, _instance)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Returns all information from the menu as string.
     *
     * @return String representation of this AbstractMenu
     */
    @Override
    public String toString()
    {
        final ToStringBuilder buf = new ToStringBuilder(this).appendSuper(super.toString());

        for (final AbstractCommand cmd : getCommands()) {
            buf.append(" ").append(cmd);
        }
        return buf.toString();
    }

    /**
     * The method takes values of the {@link #commands} and returnes them as
     * {@link java.util.ArrayList}.
     *
     * @return the values of the {@link #commands} map instance as array list
     * @see #commands
     * @see #add(Command)
     * @see #add(Menu)
     */
    public List<AbstractCommand> getCommands()
    {
        return new ArrayList<AbstractCommand>(this.commands.values());
    }

    /**
     * The instance method reads all needed information for this user interface
     * object. The method extends the original method, because the sub menus and
     * commands must be read.
     *
     * @see #readFromDB4Childs
     * @throws CacheReloadException on error during load
     */
    @Override
    protected void readFromDB()
        throws CacheReloadException
    {
        super.readFromDB();
        readFromDB4Childs();
    }

    /**
     * The instance method gets all sub menus and commands and adds them to this
     * menu instance via method {@link #add(long)}.
     *
     * @see #readFromDB
     * @see #add(long)
     * @throws CacheReloadException on error during load
     */
    private void readFromDB4Childs()
        throws CacheReloadException
    {
        try {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.Menu2Command);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.Menu2Command.FromMenu, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.Menu2Command.ToCommand);
            multi.executeWithoutAccessCheck();

            while (multi.next()) {
                final long commandId = multi.<Long> getAttribute(CIAdminUserInterface.Menu2Command.ToCommand);
                add(multi.getCurrentInstance().getId(), commandId);
            }
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read childs for menu '" + getName() + "'", e);
        }
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof AbstractMenu) {
            ret = ((AbstractMenu) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return  Long.valueOf(getId()).intValue();
    }
}
