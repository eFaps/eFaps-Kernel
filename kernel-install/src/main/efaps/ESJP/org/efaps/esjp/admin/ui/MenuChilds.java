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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * ESJP used to move the children of a command on e position up or down and to
 * evaluate the values for the sorting.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("f03932cf-7227-46c9-bb92-546245b1f49b")
@EFapsRevision("$Rev$")
public class MenuChilds implements EventExecution
{

    /**
     * Method is used to retrieve the relationship id of the childs, so that the
     * table can be sorted in the same order as they are rendered from the
     * webapp. It is called as an UI_FIELD_VALUE event from within table
     * Admin_UI_Menu_Childs.
     *
     * @param _parameter Parameter as provided to the esjp from eFaps
     * @throws EFapsException on error
     * @return Return for eFaps
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
        final Instance callinstance = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);

        final SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_UI_Menu2Command");
        query.addWhereExprEqValue("FromMenu", callinstance.getId());
        query.addWhereExprEqValue("ToCommand", instance.getId());
        query.addSelect("ID");
        query.execute();

        final Return retVal = new Return();
        if (query.next()) {
            retVal.put(ReturnValues.VALUES, query.get("ID"));
        }
        return retVal;
    }

    /**
     * Method is called from a command to move the child of a menu on position
     * up.
     *
     * @param _parameter Parameter as provided to the esjp from eFaps
     * @return empty Return to follow interface
     * @throws EFapsException on error
     * @see #move(Parameter, boolean)
     */
    public Return moveChildUp(final Parameter _parameter) throws EFapsException
    {
        move(_parameter, true);
        return new Return();
    }

    /**
     * Method is called from a command to move the child of a menu on position
     * down.
     *
     * @param _parameter Parameter as provided to the esjp from eFaps
     * @return empty Return to follow interface
     * @throws EFapsException on error
     * @see #move(Parameter, boolean)
     */
    public Return moveChildDown(final Parameter _parameter) throws EFapsException
    {
        move(_parameter, false);
        return new Return();
    }

    /**
     * Method that moves a child up or down.
     *
     * @param _parameter Parameter as provided to the esjp from eFaps
     * @param _up move the child up
     * @throws EFapsException on error
     */
    private void move(final Parameter _parameter, final boolean _up) throws EFapsException
    {
        final String[] oidRows = (String[]) _parameter.get(ParameterValues.OTHERS);
        if (oidRows != null) {

            for (final String oidRow : oidRows) {
                final String[] oids = oidRow.split("\\|");

                final String selCmdId = oids[1].split("\\.")[1];
                final SearchQuery query = new SearchQuery();
                query.setObject(oids[0]);
                query.addSelect("FromMenu");
                query.execute();
                if (query.next()) {
                    final String menuid = query.get("FromMenu").toString();

                    final SearchQuery query2 = new SearchQuery();
                    query2.setQueryTypes("Admin_UI_Menu2Command");
                    query2.addWhereExprEqValue("FromMenu", menuid);
                    query2.addSelect("ID");
                    query2.addSelect("ToCommand");
                    query2.execute();
                    final List<Long> ids = new ArrayList<Long>();
                    final Map<Long, Long> id2cmd = new HashMap<Long, Long>();
                    while (query2.next()) {
                        final Long id = (Long) query2.get("ID");
                        final Long cmdId = (Long) query2.get("ToCommand");
                        ids.add(id);
                        id2cmd.put(id, cmdId);
                    }
                    Collections.sort(ids);
                    for (int i = 0; i < ids.size(); i++) {
                        final Long actId = ids.get(i);
                        final Long actCmd = id2cmd.get(actId);

                        if (actCmd.equals(Long.parseLong(selCmdId))) {
                            Long exId = null;
                            // if move upwards and the selected is not the first
                            if (_up && i > 0) {
                                exId = ids.get(i - 1);
                            } else if (!_up && i < ids.size() - 1) {
                                exId = ids.get(i + 1);
                            }
                            if (exId != null) {
                                // update the exChange
                                final Update update = new Update("Admin_UI_Menu2Command", exId.toString());
                                update.add("ToCommand", actCmd);
                                update.execute();
                                update.close();

                                // update the actual
                                final Update updateAct = new Update("Admin_UI_Menu2Command", actId.toString());
                                updateAct.add("ToCommand", id2cmd.get(exId));
                                updateAct.execute();
                                updateAct.close();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
