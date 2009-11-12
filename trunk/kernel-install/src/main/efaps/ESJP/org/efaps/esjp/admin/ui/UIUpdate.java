/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.esjp.admin.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("2931694f-8a00-491e-8bee-d641145119f0")
@EFapsRevision("$Rev$")
public class UIUpdate
{
    /**
     * Method is used to add a Command or Menu to an existing Menu in a defined
     * position. This esjp is only used from the install scripts.
     *
     * @param _uuidAdd      UUID of the command or menu to add
     * @param _uuidMenu     UUID of the menu the command or menu will be added to
     * @param _pos          position in the menu, to ignore this "-1" can be used
     * @throws EFapsException on error
     */
    public void add2Menu(final String _uuidAdd,
                         final String _uuidMenu,
                         final Integer _pos)
        throws EFapsException
    {
        // get the Menu/Command to be connected
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_UI_Command");
        query.setExpandChildTypes(true);
        query.addWhereExprEqValue("UUID", _uuidAdd);
        query.addSelect("OID");
        query.execute();
        if (query.next()) {
            final Instance addInst = Instance.get((String) query.get("OID"));
            // get the Menu that the Menu must be connected to
            final SearchQuery queryMenu = new SearchQuery();
            queryMenu.setQueryTypes("Admin_UI_Menu");
            queryMenu.addWhereExprEqValue("UUID", _uuidMenu);
            queryMenu.addSelect("OID");
            queryMenu.execute();
            if (queryMenu.next()) {
                // get the relation and if it does not exist create one
                final Instance menuInst = Instance.get((String) queryMenu.get("OID"));
                final SearchQuery rel = new SearchQuery();
                rel.setQueryTypes("Admin_UI_Menu2Command");
                rel.addWhereExprEqValue("FromMenu", menuInst.getId());
                rel.addWhereExprEqValue("ToCommand", addInst.getId());
                rel.execute();
                if (!rel.next()) {
                    final Insert insert = new Insert("Admin_UI_Menu2Command");
                    insert.add("FromMenu", menuInst.getId());
                    insert.add("ToCommand", addInst.getId());
                    insert.execute();
                    if (_pos > -1) {
                        // sort the instances so that the new one is add the given position
                        final SearchQuery sortQuery = new SearchQuery();
                        sortQuery.setExpand(menuInst, "Admin_UI_Menu2Command\\FromMenu");
                        sortQuery.addSelect("ID");
                        sortQuery.addSelect("ToCommand");
                        sortQuery.execute();
                        final Map<Long, Long> pos2cmds = new TreeMap<Long, Long>();
                        while (sortQuery.next()) {
                            pos2cmds.put((Long) sortQuery.get("ID"), (Long) sortQuery.get("ToCommand"));
                        }
                        final List<Long> target = new ArrayList<Long>();
                        for (final Entry<Long, Long> entry : pos2cmds.entrySet()) {
                            if (addInst.getId() == entry.getValue()) {
                                target.add(_pos, entry.getValue());
                            } else {
                                target.add(entry.getValue());
                            }
                        }
                        final Iterator<Long> iter = target.iterator();
                        for (final Entry<Long, Long> entry : pos2cmds.entrySet()) {
                            final Update update = new Update("Admin_UI_Menu2Command", entry.getKey().toString());
                            update.add("ToCommand", iter.next());
                            update.execute();
                        }
                    }
                }
            } else {
                //TODO add Exception to properties
                throw new EFapsException(UIUpdate.class, "missingMenu2Add2", _uuidMenu);
            }
        } else {
            //TODO add Exception to properties
            throw new EFapsException(UIUpdate.class, "missingCmdMenu2Add", _uuidAdd);
        }
    }

    /**
     * Method is used to disconnect a command or menu from a menu.
     * This esjp is only used from the install scripts. The caches
     * are not used so that the kernel runlevel is enough to execute
     * this method.
     * @param _uuidRemove   uuid of the command,menu to be disconnected
     * @param _uuidMenu     uuid of the menu the <code>_uuidRemove</code> will
     *                      be removed from
     * @throws EFapsException on error
     */
    public void removeFromMenu(final String _uuidRemove,
                               final String _uuidMenu)
        throws EFapsException
    {
        // get the command to be removed
        final SearchQuery remQuery = new SearchQuery();
        remQuery.setQueryTypes("Admin_UI_Command");
        remQuery.setExpandChildTypes(true);
        remQuery.addWhereExprEqValue("UUID", _uuidRemove);
        remQuery.addSelect("ID");
        remQuery.execute();
        if (remQuery.next()) {
            final Long remId = (Long) remQuery.get("ID");
            final SearchQuery menuQuery = new SearchQuery();
            menuQuery.setQueryTypes("Admin_UI_Command");
            menuQuery.setExpandChildTypes(true);
            menuQuery.addWhereExprEqValue("UUID", _uuidMenu);
            menuQuery.addSelect("ID");
            menuQuery.execute();
            if (menuQuery.next()) {
                final Long menuId = (Long) menuQuery.get("ID");
                final SearchQuery query = new SearchQuery();
                query.setQueryTypes("Admin_UI_Menu2Command");
                query.addWhereExprEqValue("FromMenu", menuId);
                query.addWhereExprEqValue("ToCommand", remId);
                query.addSelect("OID");
                query.execute();
                if (query.next()) {
                    final Delete del = new Delete(Instance.get((String) query.get("OID")));
                    del.execute();
                }
            }
        }
    }
}
