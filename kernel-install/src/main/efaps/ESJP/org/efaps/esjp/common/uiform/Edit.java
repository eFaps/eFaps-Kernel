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
 * Revision:        $Rev:1563 $
 * Last Changed:    $Date:2007-10-28 15:07:41 +0100 (So, 28 Okt 2007) $
 * Last Changed By: $Author:tmo $
 */

package org.efaps.esjp.common.uiform;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id:Edit.java 1563 2007-10-28 14:07:41Z tmo $
 * @todo description
 */
@EFapsUUID("d9ba2b85-8b9a-46b0-929e-8e938e7d5577")
public class Edit implements EventExecution
{
  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(Edit.class);

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException
  {
    final Return ret = new Return();
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final AbstractCommand command =
        (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
    final Map<?,?> others = (HashMap<?,?>) _parameter.get(ParameterValues.OTHERS);
    System.out.println(others);
    final Context context = Context.getThreadContext();

    final List<FieldSet>fieldsets = new ArrayList<FieldSet>();
    final Update update = new Update(instance);
    for (final Field field : command.getTargetForm().getFields()) {
      if (field.getExpression() != null && field.isEditable()) {
        final Attribute attr = instance.getType().getAttribute(field.getExpression());
        if (attr != null
            && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                .getClassRepr())) {
          if (LOG.isDebugEnabled()) {
            LOG
                .debug("execute(Parameter) - field.getName()="
                    + field.getName());
          }
          if (context.getParameters().containsKey(field.getName())){
            update.add(attr, context.getParameter(field.getName()).replace(',',
              '.'));
          }
        }
      }
      if (field instanceof FieldSet) {
        fieldsets.add((FieldSet) field);
      }
    }
    System.out.println(fieldsets);
    update.execute();
    final NumberFormat nf= NumberFormat.getInstance();
    nf.setMinimumIntegerDigits(2);
    nf.setMaximumIntegerDigits(2);

    for (final FieldSet fieldset : fieldsets) {

      final Attribute attr = instance.getType().getAttribute(fieldset.getExpression());

      final Type type = attr.getLink();


      boolean updateExisting = true;
      int y = 0;
      while (updateExisting) {
        final String idfield = "hiddenId" + fieldset.getName() + nf.format(y);
        if (context.getParameters().containsKey(idfield)) {
          final String id = context.getParameter(idfield);


          final Update setupdate = new Update(type, id);
           int x = 0;
          for (final String attrName : fieldset.getOrder()){
             final Attribute child =  attr.getChildAttribute(attrName);
             final String fieldName = fieldset.getName() + nf.format(y) + nf.format(x);
             System.out.println(fieldName);
             if (context.getParameters().containsKey(fieldName)){
               setupdate.add(child, context.getParameter(fieldName));
             }
             x++;
          }

          setupdate.execute();
        } else {
          updateExisting = false;
        }
        y++;
      }

      final String[] newOnes = (String[]) others.get(fieldset.getName());
      if(newOnes!=null) {
        for (final String newOne : newOnes){
          final Insert insert = new Insert(type);
          insert.add(type.getAttribute(fieldset.getExpression()),((Long)instance.getId()).toString());
          int x = 0;
          for (final String attrName : fieldset.getOrder()){
            final Attribute child =  attr.getChildAttribute(attrName);
            final String fieldName = fieldset.getName()+ "New" + nf.format(Integer.parseInt(newOne)) + nf.format(x);
            System.out.println(fieldName);
            if (context.getParameters().containsKey(fieldName)){
              System.out.println(context.getParameter(fieldName));
              insert.add(child, context.getParameter(fieldName));
            }
            x++;
          }
          insert.execute();
        }
        }
    }


    return ret;
  }
}
