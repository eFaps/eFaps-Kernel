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

package org.efaps.esjp.common.uiform;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * This esjp is used from the UI_COMMAND_EXECUTE from the Form on Create.
 *
 * @author The eFaps TEam
 * @version $Id$
 */
@EFapsUUID("d74132b7-caf1-4f83-866d-8bc83bb26cdf")
@EFapsRevision("$Rev$")
public class Create implements EventExecution
{

    /**
     * Execute the esjp.
     *
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter Parameter as defined for an esjp
     * @return new empty Return
     * @throws EFapsException  on error
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Instance parent = _parameter.getInstance();
        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final Context context = Context.getThreadContext();

        final Insert insert = new Insert(command.getTargetCreateType());
        for (final Field field : command.getTargetForm().getFields()) {
            if (field.getExpression() != null && (field.isCreatable() || field.isHidden())) {
                final Attribute attr = command.getTargetCreateType().getAttribute(field.getExpression());
                if (attr != null && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                    if (context.getParameters().containsKey(field.getName())) {
                        final String value = context.getParameter(field.getName());
                        insert.add(attr, value);
                    }
                }
            }
        }
        if (command.getTargetConnectAttribute() != null) {
            insert.add(command.getTargetConnectAttribute(), "" + parent.getId());
        }
        insert.execute();

        final Instance instance = insert.getInstance();
        // connect new instance to parent via midle object
        if (properties.containsKey("ConnectType")) {
            final String type = (String) properties.get("ConnectType");
            final String childAttr = (String) properties.get("ConnectChildAttribute");
            final String parentAttr = (String) properties.get("ConnectParentAttribute");

            final Insert insert2 = new Insert(type);
            insert2.add(parentAttr, ((Long) parent.getId()).toString());
            insert2.add(childAttr, ((Long) instance.getId()).toString());
            insert2.execute();
        }

        // check if we have a fileupload field
        for (final Field field : command.getTargetForm().getFields()) {
            if (field.getExpression() == null && field.isCreatable()) {
                final Context.FileParameter fileItem = context.getFileParameters().get(field.getName());

                if (fileItem != null) {
                    final Checkin checkin = new Checkin(instance);
                    try {
                        checkin.execute(fileItem.getName(), fileItem.getInputStream(), (int) fileItem.getSize());
                    } catch (final IOException e) {
                        throw new EFapsException(this.getClass(), "execute", e, _parameter);
                    }
                }
            }
        }

        if (_parameter.get(ParameterValues.CLASSIFICATIONS) != null) {
            final List<?> classifications = (List<?>) _parameter.get(ParameterValues.CLASSIFICATIONS);

            for (final Object object : classifications) {
                final Classification classification = (Classification) object;

                final Insert relInsert = new Insert(classification.getClassifyRelationType());
                relInsert.add(classification.getRelLinkAttributeName(), ((Long) instance.getId()).toString());
                relInsert.add(classification.getRelTypeAttributeName(), ((Long) classification.getId()).toString());
                relInsert.execute();

                final Form form = Form.getTypeForm(classification);
                final Insert classInsert = new Insert(classification);
                classInsert.add(classification.getLinkAttributeName(), ((Long) instance.getId()).toString());
                for (final Field field : form.getFields()) {
                    if (field.getExpression() != null && (field.isCreatable() || field.isHidden())) {
                        final Attribute attr = classification.getAttribute(field.getExpression());
                        if (attr != null
                                        && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                                                        .getClassRepr())) {
                            if (context.getParameters().containsKey(field.getName())) {
                                final String value = context.getParameter(field.getName());
                                classInsert.add(attr, value);
                            }
                        }
                    }
                }
                classInsert.execute();
            }
        }
        return new Return();
    }
}
