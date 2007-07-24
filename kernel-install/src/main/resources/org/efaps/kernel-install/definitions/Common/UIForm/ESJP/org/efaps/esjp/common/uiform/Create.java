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

package org.efaps.esjp.common.uiform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id$
 * @todo description
 */
public class Create implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(Create.class);

  public Return execute(final Parameter _parameter) {
    Return ret = new Return();
    Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    CommandAbstract command =
        (CommandAbstract) _parameter.get(ParameterValues.UIOBJECT);
    try {
      Context context = Context.getThreadContext();

      Insert insert = new Insert(command.getTargetCreateType());
      for (Field field : command.getTargetForm().getFields()) {
        if (field.getExpression() != null
            && (field.isCreatable() || field.isHidden())) {
          Attribute attr =
              command.getTargetCreateType().getAttribute(field.getExpression());
          if (attr != null
              && !AbstractFileType.class.isAssignableFrom(attr
                  .getAttributeType().getClassRepr())) {
            String value = context.getParameter(field.getName());
            insert.add(attr, value);
          }
        }
      }
      if (command.getTargetConnectAttribute() != null) {

        insert.add(command.getTargetConnectAttribute(), "" + instance.getId());
      }
      insert.execute();
    } catch (EFapsException e) {
      LOG.error("execute(Parameter)", e);
    }
    return ret;
  }
}
