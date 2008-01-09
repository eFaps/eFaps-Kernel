/*
 * Copyright 2003-2008 The eFaps Team
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

import java.io.IOException;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * This esjp is used from the UI_COMMAND_EXECUTE from the Form on Create.
 *
 * @author jmo
 * @version $Id$
 * @todo connection via middle object is missing
 * @todo throw Exception if checkin fails
 */
public class Create implements EventExecution {

  /**
   * @param _parameter
   * @throws  
   */
  public Return execute(final Parameter _parameter) throws EFapsException  {
    Return ret = new Return();
    final Instance parent = (Instance) _parameter.get(ParameterValues.INSTANCE);
    AbstractCommand command =
        (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);

    Context context = Context.getThreadContext();

    final Insert insert = new Insert(command.getTargetCreateType());
    for (final Field field : command.getTargetForm().getFields()) {
      if (field.getExpression() != null
          && (field.isCreatable() || field.isHidden())) {
        Attribute attr =
            command.getTargetCreateType().getAttribute(field.getExpression());
        if (attr != null
            && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                .getClassRepr())) {
          String value = context.getParameter(field.getName());
          insert.add(attr, value);
        }
      }
    }
    if (command.getTargetConnectAttribute() != null) {
      insert.add(command.getTargetConnectAttribute(), "" + parent.getId());
    }
    insert.execute();

    final Instance instance = insert.getInstance();

// "TargetConnectChildAttribute"
//        // "TargetConnectParentAttribute"
//        // "TargetConnectType"
//        if (getCommand().getProperty("TargetConnectType") != null) {
//          Instance parent = new Instance(getParameter("oid"));
    //
//          Insert connect = new Insert(getCommand().getProperty("TargetConnectType"));
//          connect.add(getCommand().getProperty("TargetConnectParentAttribute"), ""
//              + parent.getId());
//          connect.add(getCommand().getProperty("TargetConnectChildAttribute"), ""
//              + insert.getId());
//          connect.execute();
//        }

    for (final Field field : command.getTargetForm().getFields()) {
      if (field.getExpression() == null && field.isCreatable()) {
        final Context.FileParameter fileItem = context.getFileParameters().get(field.getName());

        if (fileItem != null) {
          final Checkin checkin = new Checkin(instance);
          try {
            checkin.execute(fileItem.getName(),
                            fileItem.getInputStream(),
                            (int) fileItem.getSize());
          } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }

    return ret;
  }
}
