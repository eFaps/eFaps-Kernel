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
 * Revision:        $Rev: 1137 $
 * Last Changed:    $Date: 2007-07-22 06:53:17 -0500 (Sun, 22 Jul 2007) $
 * Last Changed By: $Author: tmo $
 */

package org.efaps.esjp.common.uiform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
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
 * @author jmo
 * @version $Id: Connect.java 1137 2007-07-22 11:53:17Z tmo $
 * @todo description
 */
public class Edit {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(Edit.class);

  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = new Return();
    Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    CommandAbstract command =
        (CommandAbstract) _parameter.get(ParameterValues.UIOBJECT);

    Context context = Context.getThreadContext();
    try {
      Update update = new Update(instance);

      for (Field field : command.getTargetForm().getFields()) {
        if (field.getExpression() != null && field.isEditable()) {
          Attribute attr =
              instance.getType().getAttribute(field.getExpression());
          if (attr != null
              && !AbstractFileType.class.isAssignableFrom(attr
                  .getAttributeType().getClassRepr())) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("execute(Parameter) - field.getName()="
                  + field.getName());
            }
            update.add(attr, context.getParameter(field.getName()).replace(',',
                '.'));
          }
        }
      }

      update.execute();
    } catch (Exception e) {
      LOG.error("execute(Parameter)", e);
    }

    return ret;
  }
}
