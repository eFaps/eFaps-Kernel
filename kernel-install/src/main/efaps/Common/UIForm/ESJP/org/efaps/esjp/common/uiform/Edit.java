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
 * Revision:        $Rev:1563 $
 * Last Changed:    $Date:2007-10-28 15:07:41 +0100 (So, 28 Okt 2007) $
 * Last Changed By: $Author:tmo $
 */

package org.efaps.esjp.common.uiform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id:Edit.java 1563 2007-10-28 14:07:41Z tmo $
 * @todo description
 */
public class Edit implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(Edit.class);

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = new Return();
    Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    CommandAbstract command =
        (CommandAbstract) _parameter.get(ParameterValues.UIOBJECT);

    Context context = Context.getThreadContext();

    Update update = new Update(instance);

    for (Field field : command.getTargetForm().getFields()) {
      if (field.getExpression() != null && field.isEditable()) {
        Attribute attr = instance.getType().getAttribute(field.getExpression());
        if (attr != null
            && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                .getClassRepr())) {
          if (LOG.isDebugEnabled()) {
            LOG
                .debug("execute(Parameter) - field.getName()="
                    + field.getName());
          }
          update.add(attr, context.getParameter(field.getName()).replace(',',
              '.'));
        }
      }
    }

    update.execute();

    return ret;
  }
}
