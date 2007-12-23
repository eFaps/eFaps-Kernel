/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.ui.wicket.models;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id$
 */
public class FieldTableModel extends TableModel {

  private static final long serialVersionUID = 1L;

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TableModel.class);

  private final long id;

  private final String name;

  public FieldTableModel(final UUID _commanduuid, final String _oid,
                         final FieldTable _fieldTable) {
    super(_commanduuid, _oid);
    setTableUUID(_fieldTable.getTargetTable().getUUID());
    this.id = _fieldTable.getId();
    this.name = _fieldTable.getName();
    try {
      if (Context.getThreadContext().containsUserAttribute(
          getUserAttributeKey(UserAttributeKey.SORTKEY))) {
        super.setSortKey(Context.getThreadContext().getUserAttribute(
            getUserAttributeKey(UserAttributeKey.SORTKEY)));
      }
      if (Context.getThreadContext().containsUserAttribute(
          getUserAttributeKey(UserAttributeKey.SORTDIRECTION))) {
        super
            .setSortDirection(SortDirection
                .getEnum((Context.getThreadContext()
                    .getUserAttribute(getUserAttributeKey(UserAttributeKey.SORTDIRECTION)))));
      }
    } catch (EFapsException e) {
      // we don't throw an error because this are only Usersettings
      LOG.error("error during the retrieve of UserAttributes", e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected List<List<Instance>> getInstanceLists() throws EFapsException {
    final List<Return> ret =
        FieldTable.get(this.id).executeEvents(EventType.UI_TABLE_EVALUATE,
            ParameterValues.INSTANCE, new Instance(super.getOid()));
    final List<List<Instance>> lists =
        (List<List<Instance>>) ret.get(0).get(ReturnValues.VALUES);
    return lists;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.efaps.ui.wicket.models.TableModel#getUserAttributeKey(org.efaps.ui.wicket.models.TableModel.UserAttributeKey)
   */
  @Override
  public String getUserAttributeKey(UserAttributeKey _key) {
    return super.getCommandUUID() + "-" + this.name + "-" + _key.value;
  }

}
